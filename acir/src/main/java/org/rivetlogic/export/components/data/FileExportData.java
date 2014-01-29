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
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.dom4j.dom.DOMElement;

import com.rivetlogic.core.cma.repo.Ticket;

public class FileExportData implements Serializable {
	private static final long serialVersionUID = -4247674630401984313L;

	private String fileLocation;
	private String alfrescoPath;
	private String alfrescoPathWithNamespaces;
	private DOMElement deployerDefinitionsElem;
	private DOMElement undeployerDefinitionsElem;
	private DOMElement notifierDefinitionsElem;
	private DOMElement transformerDefinitionsElem;
	private Map<String, String> transformerDependencies;
	private DOMElement transformersElem;
	private DOMElement filterDefinitionsElem;
	private DOMElement filtersElem;
	private DOMElement deployersElem;
	private DOMElement undeployersElem;
	private DOMElement notifiersElem;

	private String queryId;
	private String extractsId;
	private String extractId;
	private NodeRef nodeRef;
	private Map<QName, Serializable> properties;
	
	private Ticket ticket;
	
	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	public String getQueryId() {
		return queryId;
	}

	public String getExtractsId() {
		return extractsId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	public void setExtractsId(String extractsId) {
		this.extractsId = extractsId;
	}

	public DOMElement getDeployerDefinitionsElem() {
		return deployerDefinitionsElem;
	}
	public void setDeployerDefinitionsElem(DOMElement deployerDefinitionsElem) {
		this.deployerDefinitionsElem = deployerDefinitionsElem;
	}
	
	public DOMElement getTransformerDefinitionsElem() {
		return transformerDefinitionsElem;
	}
	public void setTransformerDefinitionsElem(DOMElement transformerDefinitionsElem) {
		this.transformerDefinitionsElem = transformerDefinitionsElem;
	}
	
	public DOMElement getTransformersElem() {
		return transformersElem;
	}
	public void setTransformersElem(DOMElement transformersElem) {
		this.transformersElem = transformersElem;
	}
	
	public DOMElement getDeployersElem() {
		return deployersElem;
	}
	public void setDeployersElem(DOMElement deployersElem) {
		this.deployersElem = deployersElem;
	}
	
	public String getFileLocation() {
		return fileLocation;
	}
	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}
	
	public String getAlfrescoPath() {
		return alfrescoPath;
	}
	public void setAlfrescoPath(String alfrescoPath) {
		this.alfrescoPath = alfrescoPath;
	}

	public Map<QName, Serializable> getProperties() {
		return properties;
	}

	public void setProperties(Map<QName, Serializable> properties) {
		this.properties = properties;
	}

	public String getExtractId() {
		return extractId;
	}

	public void setExtractId(String extractId) {
		this.extractId = extractId;
	}

	public DOMElement getFilterDefinitionsElem() {
		return filterDefinitionsElem;
	}

	public void setFilterDefinitionsElem(DOMElement filterDefinitionsElem) {
		this.filterDefinitionsElem = filterDefinitionsElem;
	}

	public DOMElement getFiltersElem() {
		return filtersElem;
	}

	public void setFiltersElem(DOMElement filtersElem) {
		this.filtersElem = filtersElem;
	}

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	public Map<String, String> getTransformerDependencies() {
		return transformerDependencies;
	}

	public void setTransformerDependencies(
			Map<String, String> transformerDependencies) {
		this.transformerDependencies = transformerDependencies;
	}	
	public String getAlfrescoPathWithNamespaces() {
		return alfrescoPathWithNamespaces;
	}

	public void setAlfrescoPathWithNamespaces(String alfrescoPathWithNamespaces) {
		this.alfrescoPathWithNamespaces = alfrescoPathWithNamespaces;
	}
	
	public DOMElement getNotifiersElem() {
		return notifiersElem;
	}

	public void setNotifiersElem(DOMElement notifiersElem) {
		this.notifiersElem = notifiersElem;
	}
	
	public DOMElement getNotifierDefinitionsElem() {
		return notifierDefinitionsElem;
	}

	public void setNotifierDefinitionsElem(DOMElement notifierDefinitionsElem) {
		this.notifierDefinitionsElem = notifierDefinitionsElem;
	}
	
	public DOMElement getUndeployerDefinitionsElem() {
		return undeployerDefinitionsElem;
	}

	public void setUndeployerDefinitionsElem(DOMElement undeployerDefinitionsElem) {
		this.undeployerDefinitionsElem = undeployerDefinitionsElem;
	}

	public DOMElement getUndeployersElem() {
		return undeployersElem;
	}

	public void setUndeployersElem(DOMElement undeployersElem) {
		this.undeployersElem = undeployersElem;
	}
}
