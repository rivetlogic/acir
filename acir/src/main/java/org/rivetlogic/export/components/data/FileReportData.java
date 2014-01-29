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
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * @author Sumer Jabri
 * @author Sandra O'Keeffe
 */
public class FileReportData implements Serializable, ReportData {
	private static final long serialVersionUID = -1464599363845628635L;

	private String queryId = null;
	private String extractsId = null;
	private String extractId = null;
	private Throwable errorObj = null;
	private List<String> pathDisk = null;
	private String pathAlfresco = null;
	private String pathTransformed = null;
	private Map<QName, Serializable> properties;
	private boolean included;
	
	public String getExtractsId() {
		return extractsId;
	}
	public void setExtractsId(String extractsId) {
		this.extractsId = extractsId;
	}

	public String getQueryId() {
		return queryId;
	}
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	public List<String> getPathDisk() {
		return pathDisk;
	}
	public void setPathDisk(List<String> pathDisk) {
		this.pathDisk = pathDisk;
	}
	public String getPathAlfresco() {
		return pathAlfresco;
	}
	public void setPathAlfresco(String pathAlfresco) {
		this.pathAlfresco = pathAlfresco;
	}
	public String getPathTransformed() {
		return pathTransformed;
	}
	public void setPathTransformed(String pathTransformed) {
		this.pathTransformed = pathTransformed;
	}
	public Throwable getErrorObj() {
		return errorObj;
	}
	public void setErrorObj(Throwable errorObj) {
		this.errorObj = errorObj;
	}
	public String getExtractId() {
		return extractId;
	}
	public void setExtractId(String extractId) {
		this.extractId = extractId;
	}
	public Map<QName, Serializable> getProperties() {
		return properties;
	}
	public void setProperties(Map<QName, Serializable> properties) {
		this.properties = properties;
	}
	public boolean isIncluded() {
		return included;
	}
	public void setIncluded(boolean include) {
		this.included = include;
	}
}
