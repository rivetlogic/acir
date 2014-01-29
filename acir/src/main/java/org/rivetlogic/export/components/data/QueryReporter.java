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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

import org.rivetlogic.export.deployers.exceptions.DeployerException;
import org.rivetlogic.export.transformers.content.exceptions.ContentTransformationException;
import org.rivetlogic.export.transformers.filename.exceptions.UrlTransformationException;
import org.rivetlogic.utils.IntegrationConstants;

/**
 * @author Sumer Jabri 
 * @author Sandra O'Keeffe 
 */
public class QueryReporter implements Serializable, Reporter {
	private static final long serialVersionUID = 8958625700423195046L;
	private long MAX_IDLE_TIME = 1 * 60 * 60 * 1000; // 1 hour

	private File reportXmlFile;
	private OutputStream outputStream;
	private XMLWriter xmlWriter;
	private DOMElement queryElem;
	private Date lastModified;
	private int numResults = -1;
	private int numResultsProcessed = 0;
	private int numResultsFiltered = 0;
	private String id;
	private ReporterState state = ReporterState.NOT_STARTED;
	private Logger logger = Logger.getLogger(this.getClass());
	
	public QueryReporter(String id) throws IOException, SAXException {
		this.id = id;

		File tmpLocation = new File(IntegrationConstants.TMP_FOLDER);
		tmpLocation.mkdirs();

		reportXmlFile = File.createTempFile(id + '_' + System.currentTimeMillis(), ".query", tmpLocation);
		outputStream = new FileOutputStream(reportXmlFile);
		xmlWriter = new XMLWriter(outputStream, OutputFormat.createPrettyPrint());

		lastModified = new Date();
		numResults = -1;
		numResultsProcessed = 0;
		
		startReport();
	}
	
	/* (non-Javadoc)
	 * @see com.hbsp.export.components.data.Reporter#startReport()
	 */
	public void startReport() throws SAXException, IOException {
		if (state.compareTo(ReporterState.STARTED) < 0) {
			synchronized (reportXmlFile) {
				if (state.compareTo(ReporterState.STARTED) < 0) {
					queryElem = new DOMElement("query");
					queryElem.addAttribute("id", id);
	
					//xmlWriter.startDocument();
					xmlWriter.writeOpen(queryElem);
					xmlWriter.flush();
					
					state = ReporterState.STARTED;
				}
			}

			lastModified = new Date();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.hbsp.export.components.data.Reporter#endReport()
	 */
	public void endReport() throws SAXException, IOException {
		if (state.compareTo(ReporterState.COMPLETED) < 0) {
			synchronized (reportXmlFile) {
				if (state.compareTo(ReporterState.COMPLETED) < 0) {
					DOMElement resultsElem = new DOMElement("results");
					resultsElem.setText(String.valueOf(numResultsProcessed - numResultsFiltered));
					xmlWriter.write(resultsElem);
					
					DOMElement filteredResultsElem = new DOMElement("results-filtered");
					filteredResultsElem.setText(String.valueOf(numResultsFiltered));
					xmlWriter.write(filteredResultsElem);
					
					xmlWriter.writeClose(queryElem);
					xmlWriter.close();
					outputStream.close();

					state = ReporterState.COMPLETED;
					lastModified = new Date();
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.hbsp.export.components.data.Reporter#processFileReportData(com.hbsp.export.components.data.FileReportData)
	 */
	public void processReportData(FileReportData reportData) throws SAXException, IOException {
		if (state.compareTo(ReporterState.STARTED) != 0) {
			throw new IOException("Invalid QueryReporter state");
		}
		
		if (reportData.isIncluded()) {
			DOMElement fileReportElem = new DOMElement("file");
			
			fileReportElem.addAttribute("path-transformed", reportData.getPathTransformed());
			fileReportElem.addAttribute("path-alfresco", reportData.getPathAlfresco());
			StringBuilder builder = new StringBuilder();
			/* build a string of each path deployed to */
			Iterator<String> pathDiskIter = reportData.getPathDisk().iterator();
			while(pathDiskIter.hasNext()) {
				builder.append(pathDiskIter.next());
				
				if (pathDiskIter.hasNext()) builder.append('|');
			}
			fileReportElem.addAttribute("path-disk", builder.toString());
			
			if (reportData.getErrorObj() != null) {
				fileReportElem.add(generateErrorElem(reportData.getErrorObj(), null));
			}
				
			if (reportData.getProperties() != null && !reportData.getProperties().isEmpty()) {
				Map<QName,Serializable> propMap = reportData.getProperties();
				
				for (QName key:propMap.keySet()) {
					DOMElement propElem = new DOMElement(key.getLocalName());
					Object value = propMap.get(key);
					propElem.setText((value != null)?value.toString():"");
					
					fileReportElem.add(propElem);
				}
			}
			
			synchronized (reportXmlFile) {
				xmlWriter.write(fileReportElem);
			}
		} else {
			numResultsFiltered++;
		}
		
		lastModified = new Date();
		numResultsProcessed++;
		
		attemptEndReport();
	}
	
	public void processReportData(QueryReportData reportData) throws SAXException, IOException {
		if (state.compareTo(ReporterState.STARTED) != 0) {
			throw new IOException("Invalid Reporter state");
		}

		lastModified = new Date();

		if (reportData.getErrorMsg() != null) {
			DOMElement errorElem = generateErrorElem(null, reportData.getErrorMsg());
			synchronized(reportXmlFile) {
				xmlWriter.write(errorElem);
			}
		}

		setNumResults(reportData.getNumResults());
		attemptEndReport();
	}
	
	public void attemptEndReport() throws SAXException, IOException {
		if (numResults > -1 && numResultsProcessed >= numResults) {
			endReport();
		}
	}
	
	public void tick() {
		if (((new Date().getTime() - lastModified.getTime()) >= MAX_IDLE_TIME) || (numResults > -1 && numResultsProcessed >= numResults)) {
			try {
				endReport();
			} catch (Exception e) {
				logger.error("could not endReport() for " + id, e);
			}
		}
	}
	
	private DOMElement generateErrorElem(Throwable cte, String errorMsg) {
		DOMElement errorReportElem = new DOMElement("error");
		
		if (cte != null) {
			if (cte instanceof ContentTransformationException) {
				errorReportElem.setAttribute("source", ((ContentTransformationException)cte).getId());
			} else if (cte instanceof UrlTransformationException) {
				errorReportElem.setAttribute("source", ((UrlTransformationException)cte).getId());
			} else if (cte instanceof DeployerException) {
				errorReportElem.setAttribute("source", ((DeployerException)cte).getId());
			} else {
				errorReportElem.setAttribute("source", "unknown");
			}

			ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream(); 
			PrintStream errorPrintStream = new PrintStream(errorOutputStream, true);
			cte.printStackTrace(errorPrintStream);
			errorReportElem.setText(errorOutputStream.toString());
		} else if (errorMsg != null) {
			errorReportElem.setText(errorMsg);
		} else {
			// no msg
		}
		
		return errorReportElem;
	}
	
	/* (non-Javadoc)
	 * @see com.hbsp.export.components.data.Reporter#getReportXmlFile()
	 */
	public File getReportXmlFile() {
		return reportXmlFile;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	/* (non-Javadoc)
	 * @see com.hbsp.export.components.data.Reporter#getNumResults()
	 */
	public int getNumResults() {
		return numResults;
	}
	public void setNumResults(int numResults) throws SAXException, IOException {
		this.numResults = numResults;
		
		attemptEndReport();
	}
	
	/* (non-Javadoc)
	 * @see com.hbsp.export.components.data.Reporter#getNumResultsProcessed()
	 */
	public int getNumResultsProcessed() {
		return numResultsProcessed;
	}

	/* (non-Javadoc)
	 * @see com.hbsp.export.components.data.Reporter#getId()
	 */
	public String getId() {
		return id;
	}

	public long getMaxIdleTime() {
		return MAX_IDLE_TIME;
	}

	public ReporterState getState() {
		return state;
	}

	public int getNumResultsFiltered() {
		return numResultsFiltered;
	}
}
