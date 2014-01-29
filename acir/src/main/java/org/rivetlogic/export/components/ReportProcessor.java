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
package org.rivetlogic.export.components;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.transport.NullPayload;
import org.xml.sax.SAXException;

import org.rivetlogic.export.components.data.ExtractReportData;
import org.rivetlogic.export.components.data.ExtractsReportData;
import org.rivetlogic.export.components.data.ExtractsReporter;
import org.rivetlogic.export.components.data.FileReportData;
import org.rivetlogic.export.components.data.QueryReportData;
import org.rivetlogic.export.components.data.Reporter.ReporterState;

/**
 * @author Sumer Jabri
 * @author Sandra O'Keeffe
 */
public class ReportProcessor implements Callable {
	private static HashMap<String, ExtractsReporter> reporters = new HashMap<String, ExtractsReporter>();
	private static Logger logger = Logger.getLogger(ReportProcessor.class);
	
	private String ackPath;
	
	public Object onCall(MuleEventContext eventContext) throws Exception {
		Object payload = eventContext.getMessage().getPayload();

		/* don't want to log null payload */
		if (!(payload instanceof NullPayload)) {
			logger.debug("RP: received " + payload);
		}
		
		if (payload instanceof FileReportData) {
			processReport((FileReportData)payload);
		} else if (payload instanceof QueryReportData) {
			processReport((QueryReportData)payload);
		} else if (payload instanceof ExtractReportData) {
			processReport((ExtractReportData)payload);
		} else if (payload instanceof ExtractsReportData) {
			processReport((ExtractsReportData)payload);
		} else {
			tick();
		}
		
		if (!(payload instanceof NullPayload)) {
			logger.debug("RP: processed " + payload);
		}

		return null;
	}
	
	private void processReport(FileReportData reportData) throws IOException, SAXException {
		generateExtractsReporter(reportData.getExtractsId()).processReportData(reportData);
	}

	private void processReport(QueryReportData reportData) throws IOException, SAXException {
		generateExtractsReporter(reportData.getExtractsId()).processReportData(reportData);
	}

	private void processReport(ExtractReportData reportData) throws IOException, SAXException {
		generateExtractsReporter(reportData.getExtractsId()).processReportData(reportData);
	}

	private void processReport(ExtractsReportData reportData) throws IOException, SAXException {
		ExtractsReporter extractsReporter = generateExtractsReporter(reportData.getExtractsId());
		extractsReporter.setFileForNoResults(reportData.isFileForNoResults());
		extractsReporter.setTotalResultSet(reportData.getTotalNumResults());
		extractsReporter.setNumResults(reportData.getNumExtracts());
	}
	
	public void tick() {
		String[] keys = reporters.keySet().toArray(new String[reporters.size()]);
		for (String key:keys) {
			ExtractsReporter reporter = reporters.get(key);
			try {
				reporter.tick();
				if (reporter.getState().equals(ReporterState.COMPLETED)) {
					synchronized(reporters) {
						reporters.remove(key);
					}
				}
			} catch (Exception e) {
				logger.error("could not tick()", e);
			}
		}
	}
	
	private ExtractsReporter generateExtractsReporter(String extractsId) throws IOException, SAXException {
		ExtractsReporter extractsReporter = reporters.get(extractsId);
		if (extractsReporter == null || extractsReporter.getState().equals(ReporterState.COMPLETED)) {
			synchronized(reporters) {
				extractsReporter = reporters.get(extractsId);
				if (extractsReporter == null|| extractsReporter.getState().equals(ReporterState.COMPLETED)) {
					extractsReporter = new ExtractsReporter(extractsId, ackPath);
					reporters.put(extractsId, extractsReporter);
				}
			}
		}
		
		return extractsReporter;
	}
	
	/**
	 * @return the ackPath
	 */
	public String getAckPath() {
		return ackPath;
	}

	/**
	 * @param ackPath the ackPath to set
	 */
	public void setAckPath(String ackPath) {
		this.ackPath = ackPath;
	}
}
