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

public class ExtractsReportData implements Serializable, ReportData {
	private static final long serialVersionUID = -4099865326683150163L;

	private String extractsId;
	private int numExtracts;
	private boolean fileForNoResults = true;
	private int totalNumResults;
	
	
	public boolean isFileForNoResults() {
		return fileForNoResults;
	}
	public void setFileForNoResults(boolean fileForNoResults) {
		this.fileForNoResults = fileForNoResults;
	}
	public String getExtractsId() {
		return extractsId;
	}
	public void setExtractsId(String extractsId) {
		this.extractsId = extractsId;
	}
	public int getNumExtracts() {
		return numExtracts;
	}
	public void setNumExtracts(int numExtracts) {
		this.numExtracts = numExtracts;
	}
	public void setTotalNumResults(int totalNumResults) {
		this.totalNumResults = totalNumResults;		
	}
	public int getTotalNumResults() {
		return totalNumResults;
	}
}
