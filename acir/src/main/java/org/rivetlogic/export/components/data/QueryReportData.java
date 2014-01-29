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

import java.io.Serializable;

public class QueryReportData implements Serializable, ReportData {
	private static final long serialVersionUID = -132974774716724875L;
	
	private int numResults = 0;
	private String queryId = null;
	private String extractsId = null;
	private String extractId = null;
	private String errorMsg = null;
	
	public int getNumResults() {
		return numResults;
	}
	public String getQueryId() {
		return queryId;
	}
	public String getExtractsId() {
		return extractsId;
	}
	public void setNumResults(int numResults) {
		this.numResults = numResults;
	}
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	public void setExtractsId(String extractsId) {
		this.extractsId = extractsId;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public String getExtractId() {
		return extractId;
	}
	public void setExtractId(String extractId) {
		this.extractId = extractId;
	}
}
