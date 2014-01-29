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

public class ExtractReportData implements Serializable, ReportData {
	private static final long serialVersionUID = 1575946651977344300L;

	String extractId;
	String extractsId;
	int numQueries;
	
	public String getExtractId() {
		return extractId;
	}
	public void setExtractId(String id) {
		this.extractId = id;
	}
	public int getNumQueries() {
		return numQueries;
	}
	public void setNumQueries(int numQueries) {
		this.numQueries = numQueries;
	}
	/* (non-Javadoc)
	 * @see com.hbsp.export.components.data.ReportData#getExtractsId()
	 */
	public String getExtractsId() {
		return extractsId;
	}
	/* (non-Javadoc)
	 * @see com.hbsp.export.components.data.ReportData#setExtractsId(java.lang.String)
	 */
	public void setExtractsId(String extractsId) {
		this.extractsId = extractsId;
	}
}
