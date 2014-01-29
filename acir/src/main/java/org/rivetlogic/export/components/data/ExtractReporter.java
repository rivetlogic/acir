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

public class ExtractReporter implements Serializable, Reporter {
	private static final long serialVersionUID = 6958793070692814326L;
	private long MAX_IDLE_TIME = 1 * 60 * 60 * 1000; // 1 hours

	private File reportXmlFile;
	private XMLWriter xmlWriter;
	private OutputStream outputStream;
	private DOMElement extractElem;
	private Date lastModified;
	private int numResults = -1;
	private int numResultsProcessed = 0;
	
	private String id;
	private ReporterState state = ReporterState.NOT_STARTED;
	private Logger logger = Logger.getLogger(this.getClass());
	
	private HashMap<String, QueryReporter> queryReporters = new HashMap<String, QueryReporter>();

	public ExtractReporter(String id) throws IOException, SAXException {
		this.id = id;
		
		lastModified = new Date();
		numResults = -1;
		numResultsProcessed = 0;
		
		startReport();
	}
	
	public void startReport() throws SAXException, IOException {
		if (state.compareTo(ReporterState.STARTED) < 0) {
			extractElem = new DOMElement("extract-request");
			extractElem.addAttribute("id", id);
	
			synchronized (this) {
				if (state.compareTo(ReporterState.STARTED) < 0) {
					state = ReporterState.STARTED;
					lastModified = new Date();
				}
			}
		}
	}
	
	public void endReport() throws SAXException, IOException {
		if (state.compareTo(ReporterState.COMPLETED) < 0) {
			boolean queryReportersCompleted = true;
			
			for (QueryReporter queryReporter:queryReporters.values()) {
				if (!queryReporter.getState().equals(ReporterState.COMPLETED)) {
					queryReportersCompleted = false;
				}
			}
			
			if (queryReportersCompleted) {
				synchronized (this) {	// values changing for this file 
					if (state.compareTo(ReporterState.COMPLETED) < 0) {
						File tmpLocation = new File(IntegrationConstants.TMP_FOLDER);
						tmpLocation.mkdirs();

						reportXmlFile = File.createTempFile(id + '_' + System.currentTimeMillis(), ".extract", tmpLocation);
						outputStream = new FileOutputStream(reportXmlFile);
						xmlWriter = new XMLWriter(outputStream, OutputFormat.createPrettyPrint());

						xmlWriter.writeOpen(extractElem);
						xmlWriter.flush();

						synchronized (queryReporters) {
							String[] keys = queryReporters.keySet().toArray(new String[queryReporters.size()]);
							for (String key:keys) {
								QueryReporter queryReporter = queryReporters.get(key);
								File queryReportFile = queryReporter.getReportXmlFile();
								FileInputStream inputStream = new FileInputStream(queryReportFile);
								StreamUtils.copy(inputStream, outputStream);
								inputStream.close();
								queryReportFile.delete();
								outputStream.flush();
								xmlWriter.println();
								xmlWriter.flush();
								queryReporters.remove(key);
							}
						}
						
						xmlWriter.writeClose(extractElem);
						//xmlWriter.endDocument();
						xmlWriter.close();
	
						state = ReporterState.COMPLETED;
						lastModified = new Date();
					}
				}
				
				// dispatch even when report is complete
			}
		}
	}
	
	public void processReportData(FileReportData reportData) throws SAXException, IOException {
		if (state.compareTo(ReporterState.STARTED) != 0) {
			throw new IOException("Invalid Reporter state");
		}
		
		lastModified = new Date();
		generateQueryReporter(reportData.getQueryId()).processReportData(reportData);
	}
	
	public void processReportData(QueryReportData reportData) throws SAXException, IOException {
		if (state.compareTo(ReporterState.STARTED) != 0) {
			throw new IOException("Invalid Reporter state");
		}

		lastModified = new Date();
		numResultsProcessed++;

		QueryReporter queryReporter = generateQueryReporter(reportData.getQueryId());
		queryReporter.processReportData(reportData);
		
		attemptEndReport();
	}

	public void attemptEndReport() throws SAXException, IOException {
		if (numResults > -1 && numResultsProcessed >= numResults) {
			endReport();
		}
	}
	
	public void tick() {
		String[] keys = queryReporters.keySet().toArray(new String[queryReporters.size()]);
		for (String key:keys) {
			QueryReporter reporter = queryReporters.get(key);
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
	
	private QueryReporter generateQueryReporter(String queryId) throws IOException, SAXException {
		QueryReporter queryReporter = null;
		
		synchronized (queryReporters) {
			queryReporter = queryReporters.get(queryId);
			if (queryReporter == null || queryReporter.getState().equals(ReporterState.COMPLETED)) {
				queryReporter = new QueryReporter(queryId);
				queryReporters.put(queryId, queryReporter);
			}
		}
		
		
		return queryReporter;
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
		
		attemptEndReport();
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

}
