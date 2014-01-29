package org.rivetlogic.export.components;

import java.io.File;

import org.alfresco.service.cmr.repository.StoreRef;
import org.dom4j.dom.DOMElement;

/**
 * Transfer object for passing query data around 
 * 
 * @author Sandra O'Keeffe
 */
public class XMLExportTO {

	private DOMElement filtersElem = null; 
	private DOMElement transformersElem = null;
	private DOMElement deployersElem = null;
	private DOMElement undeployersElem = null;
	private DOMElement notifiersElem = null;
	private DOMElement dependencyResolversElem = null;
	private DOMElement filterDefinitionsElem = null;
	private DOMElement transformerDefinitionsElem = null;
	private DOMElement deployerDefinitionsElem = null;
	private DOMElement undeployerDefinitionsElem = null;
	private DOMElement dependencyResolverDefinitionsElem = null;
	private DOMElement notifierDefinitionsElem = null;

	private File tmpLocation = null; 
	private String extractsId = null;
	private String extractId = null;
	private DOMElement queryElem = null;
	private String queryId = null;
	private StoreRef storeRef = null;
	
	private int numResults = 0;

	
	public String getExtractsId() {
		return extractsId;
	}

	public void setExtractsId(String extractsId) {
		this.extractsId = extractsId;
	}

	public String getExtractId() {
		return extractId;
	}

	public void setExtractId(String extractId) {
		this.extractId = extractId;
	}


	public DOMElement getFiltersElem() {
		return filtersElem;
	}

	public void setFiltersElem(DOMElement filtersElem) {
		this.filtersElem = filtersElem;
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

	public DOMElement getDependencyResolversElem() {
		return dependencyResolversElem;
	}

	public void setDependencyResolversElem(DOMElement dependencyResolversElem) {
		this.dependencyResolversElem = dependencyResolversElem;
	}

	public DOMElement getFilterDefinitionsElem() {
		return filterDefinitionsElem;
	}

	public void setFilterDefinitionsElem(DOMElement filterDefinitionsElem) {
		this.filterDefinitionsElem = filterDefinitionsElem;
	}

	public DOMElement getTransformerDefinitionsElem() {
		return transformerDefinitionsElem;
	}

	public void setTransformerDefinitionsElem(DOMElement transformerDefinitionsElem) {
		this.transformerDefinitionsElem = transformerDefinitionsElem;
	}

	public DOMElement getDeployerDefinitionsElem() {
		return deployerDefinitionsElem;
	}

	public void setDeployerDefinitionsElem(DOMElement deployerDefinitionsElem) {
		this.deployerDefinitionsElem = deployerDefinitionsElem;
	}

	public DOMElement getDependencyResolverDefinitionsElem() {
		return dependencyResolverDefinitionsElem;
	}

	public void setDependencyResolverDefinitionsElem(
			DOMElement dependencyResolverDefinitionsElem) {
		this.dependencyResolverDefinitionsElem = dependencyResolverDefinitionsElem;
	}

	public File getTmpLocation() {
		return tmpLocation;
	}

	public void setTmpLocation(File tmpLocation) {
		this.tmpLocation = tmpLocation;
	}

	public DOMElement getQueryElem() {
		return queryElem;
	}

	public void setQueryElem(DOMElement queryElem) {
		this.queryElem = queryElem;
	}

	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	public StoreRef getStoreRef() {
		return storeRef;
	}

	public void setStoreRef(StoreRef storeRef) {
		this.storeRef = storeRef;
	}

	public int getNumResults() {
		return numResults;
	}

	public void setNumResults(int numResults) {
		this.numResults = numResults;
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

	public DOMElement getUndeployersElem() {
		return undeployersElem;
	}

	public void setUndeployersElem(DOMElement undeployersElem) {
		this.undeployersElem = undeployersElem;
	}

	public DOMElement getUndeployerDefinitionsElem() {
		return undeployerDefinitionsElem;
	}

	public void setUndeployerDefinitionsElem(DOMElement undeployerDefinitionsElem) {
		this.undeployerDefinitionsElem = undeployerDefinitionsElem;
	}
	
	/**
	 * Add results to numResults
	 * @param results
	 */
	public void addToNumResults(int results) {
		this.numResults += results;
		
	}
	
	
}
