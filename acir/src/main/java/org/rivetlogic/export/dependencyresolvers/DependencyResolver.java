package org.rivetlogic.export.dependencyresolvers;

import java.io.File;
import java.util.List;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

import com.rivetlogic.core.cma.api.ContentService;
import com.rivetlogic.core.cma.api.NodeService;
import com.rivetlogic.core.cma.api.SearchService;
import com.rivetlogic.core.cma.repo.Node;
import com.rivetlogic.core.cma.repo.SortDefinition;
import com.rivetlogic.core.cma.repo.Ticket;


/**
 *  The initialize() method is called to set the list of nodes to resolve dependencies for
 * 	hasNext() indicates whether or not there are more dependencies
 * 	setResultSetSize() sets the number of results to return on each invocation next()
 * 
 * @author Sandra O'Keeffe
 */
public interface DependencyResolver {
	
	public String getId();
	public void setId(String id);
	
	
	/**
	 * Initialises the dependency resolver with the nodes to resolve dependencies for 
	 * 
	 * @param nodes
	 */
	public void initialize(List<Node> nodes, SearchService searchService, ContentService contentService, 
			StoreRef storeRef, Ticket ticket, List<SortDefinition> sortDefinitions, List<QName> properties,
			File tmpLocation, NodeService nodeService);
	
	/**
	 * Returns true if there are more nodes to resolve dependencies for, otherwise false
	 * @return
	 */
	public boolean hasNext();
	
	/**
	 * Sets the max. number of results to return with each invocation of next.
	 * 
	 * @param size
	 */
	public void setResultSetSize(int size);
}
