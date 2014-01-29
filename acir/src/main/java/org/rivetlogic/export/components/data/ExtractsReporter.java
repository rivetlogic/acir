/*
 * Copyright (C) 2007-2011 Rivet Logic Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.rivetlogic.export.components.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

import org.rivetlogic.utils.IntegrationConstants;

public class ExtractsReporter implements Serializable, Reporter {
	private static final long serialVersionUID = 6958793070692814326L;
	private long MAX_IDLE_TIME = 1 * 60 * 60 * 1000; // 1 hours

	private File reportXmlFile;
	private String ackPath;
	private XMLWriter xmlWriter;
	private OutputStream outputStream;
	private DOMElement extractElem;
	private Date lastModified;
	private int numResults = -1;
	private int numResultsProcessed = 0;
	/* this two attributes are used to tell if an acknowledgement file should be created or not
	 * this is useful in the scenario where a single query is continuously polled */
	private boolean fileForNoResults = true;
	private int totalResultSet = 0;  

	private String id;
	private ReporterState state = ReporterState.NOT_STARTED;
	private Logger logger = Logger.getLogger(this.getClass());
	
	private HashMap<String, ExtractReporter> extractReporters = new HashMap<String, ExtractReporter>();
	
	public ExtractsReporter(String id, String ackPath) throws IOException, SAXException {
		this.id = id;
		this.ackPath = ackPath;
		
		lastModified = new Date();
		numResults = -1;
		numResultsProcessed = 0;
		
		startReport();
	}
	
	public void startReport() throws SAXException, IOException {
		if (state.compareTo(ReporterState.STARTED) < 0) {
			synchronized(this) {
				if (state.compareTo(ReporterState.STARTED) < 0) {
					state = ReporterState.STARTED;
				}
			}
			lastModified = new Date();
		}
	}
	
	public void endReport() throws SAXException, IOException {
		synchronized (extractReporters) {
			
			if (state.compareTo(ReporterState.COMPLETED) < 0) {
				boolean extractReportersCompleted = true;
				
				for (ExtractReporter extractReporter:extractReporters.values()) {
					if (!extractReporter.getState().equals(ReporterState.COMPLETED)) {
						extractReportersCompleted = false;
					}
				}
				
				if (extractReportersCompleted) {
					
					synchronized (this) {
						if (state.compareTo(ReporterState.COMPLETED) < 0) {
							File tmpLocation = new File(IntegrationConstants.TMP_FOLDER);
							tmpLocation.mkdirs();
							
							reportXmlFile = File.createTempFile(id + '_' + System.currentTimeMillis(), ".extracts", tmpLocation);
							outputStream = new FileOutputStream(reportXmlFile);
							xmlWriter = new XMLWriter(outputStream, OutputFormat.createPrettyPrint());
	
							extractElem = new DOMElement("extracts-request");
							extractElem.addAttribute("id", id);
	
							xmlWriter.startDocument();
							xmlWriter.writeOpen(extractElem);
							xmlWriter.flush();
							
							String[] keys = extractReporters.keySet().toArray(new String[extractReporters.size()]);
							
							for (String key:keys) {
								ExtractReporter extractReporter = extractReporters.get(key);
								// access to this object should be synchronized
								
								File extractReportFile = extractReporter.getReportXmlFile();
								FileInputStream inputStream = null;
								inputStream = new FileInputStream(extractReportFile);
								
								StreamUtils.copy(inputStream, outputStream);
								inputStream.close();
								extractReportFile.delete();
								
								outputStream.flush();
								xmlWriter.println();
								xmlWriter.flush();
								extractReporters.remove(key);
							}
							
							
							xmlWriter.writeClose(extractElem);
							xmlWriter.endDocument();
							xmlWriter.close();
							outputStream.close();
							
							if (fileForNoResults || totalResultSet != 0) {
								
								File finalFile = new File(ackPath + "/" + id + "_"+ System.currentTimeMillis());
								if (!finalFile.getParentFile().exists()) {
									finalFile.getParentFile().mkdirs();
								}
								reportXmlFile.renameTo(finalFile);
								reportXmlFile.delete();
								reportXmlFile = finalFile;
							} else {
								// just delete if an acknowledgement file is not needed when no results are returned
								reportXmlFile.delete();
							}
							
							
							state = ReporterState.COMPLETED;
						}
					}
				}
			}
		}
	}
	
	public void processReportData(FileReportData reportData) throws SAXException, IOException {
		if (state.compareTo(ReporterState.STARTED) != 0) {
			throw new IOException("Invalid Reporter state");
		}
		
		lastModified = new Date();
		generateExtractReporter(reportData.getExtractId()).processReportData(reportData);
	}
	
	public void processReportData(QueryReportData reportData) throws SAXException, IOException {
		if (state.compareTo(ReporterState.STARTED) != 0) {
			throw new IOException("Invalid Reporter state");
		}

		lastModified = new Date();
		generateExtractReporter(reportData.getExtractId()).processReportData(reportData);
	}
	
	public void processReportData(ExtractReportData reportData) throws SAXException, IOException {
		if (state.compareTo(ReporterState.STARTED) != 0) {
			throw new IOException("Invalid Reporter state");
		}

		lastModified = new Date();
		numResultsProcessed++;

		ExtractReporter extractReporter = generateExtractReporter(reportData.getExtractId());
		extractReporter.setNumResults(reportData.getNumQueries());
		
		if (numResults > -1 && numResultsProcessed >= numResults) {
			endReport();
		}
	}

	public void tick() {
		String[] keys = extractReporters.keySet().toArray(new String[extractReporters.size()]);
		for (String key:keys) {
			ExtractReporter reporter = extractReporters.get(key);
			reporter.tick();
		}

		if (((new Date().getTime() - lastModified.getTime()) >= MAX_IDLE_TIME) || (numResults > -1 && numResultsProcessed >= numResults)) {
			try {
				endReport();
			} catch (Exception e) {
				logger.error("could not endReport() for " + id, e);
			}
		}
	}
	
	private ExtractReporter generateExtractReporter(String extractId) throws IOException, SAXException {
		ExtractReporter extractReporter = extractReporters.get(extractId);
		if (extractReporter == null || extractReporter.getState().equals(ReporterState.COMPLETED)) {
			synchronized (extractReporters) {
				extractReporter = extractReporters.get(extractId);
				if (extractReporter == null || extractReporter.getState().equals(ReporterState.COMPLETED)) {
					extractReporter = new ExtractReporter(extractId);
					extractReporters.put(extractId, extractReporter);
				}
			}
		}
		
		return extractReporter;
	}
	
	public File getReportXmlFile() {
		return reportXmlFile;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public int getNumResults() {
		return numResults;
	}
	public void setNumResults(int numResults) throws SAXException, IOException {
		this.numResults = numResults;
		lastModified = new Date();
		
		if (this.numResults <= this.numResultsProcessed) {
			endReport();
		}
	}
	
	public int getNumResultsProcessed() {
		return numResultsProcessed;
	}

	public String getId() {
		return id;
	}

	public long getMaxIdleTime() {
		return MAX_IDLE_TIME;
	}

	public ReporterState getState() {
		return state;
	}

	public String getAckPath() {
		return ackPath;
	}
	public void setAckPath(String ackPath) {
		this.ackPath = ackPath;
	}
	public void setTotalResultSet(int totalResultSet) {
		this.totalResultSet = totalResultSet;
	}
	public void setFileForNoResults(boolean fileForNoResults) {
		this.fileForNoResults = fileForNoResults;
	}
}
