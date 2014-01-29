package org.rivetlogic.export.dependencyresolvers;

import com.rivetlogic.core.cma.api.ContentService;
import com.rivetlogic.core.cma.api.NodeService;
import com.rivetlogic.core.cma.api.SearchService;
import com.rivetlogic.core.cma.exception.CmaRuntimeException;
import com.rivetlogic.core.cma.exception.InvalidTicketException;
import com.rivetlogic.core.cma.repo.Node;
import com.rivetlogic.core.cma.repo.SortDefinition;
import com.rivetlogic.core.cma.repo.Ticket;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.dom4j.dom.DOMElement;
import org.rivetlogic.export.components.ExtractRequestDispatcher;
import org.rivetlogic.export.components.XMLExportTO;
import org.rivetlogic.export.components.data.FileExportData;
import org.rivetlogic.export.components.data.RetrievedNode;
import org.rivetlogic.export.dependencyresolvers.exceptions.DependencyResolverException;
import org.rivetlogic.utils.reflection.DependencyResolverFactory;

import java.util.ArrayList;
import java.util.List;

import static org.rivetlogic.utils.QueryXMLContstants.REF;

/**
 * Responsible for processing resolved dependencies
 * 
 * @author Sandra O'Keeffe
 */
public class DependencyResolverProcessor {

	protected Logger logger = Logger.getLogger(DependencyResolverProcessor.class);
	
	private static DependencyResolverFactory dependencyResolverFactory = new DependencyResolverFactory();
	private SearchService searchService;
	private ContentService contentService;
	private List<QName> properties;
	private List<SortDefinition> sortDefinitions;
	private Ticket ticket;
	private ExtractRequestDispatcher dispatcher;
	private NodeService nodeService;
	
	public DependencyResolverProcessor(SearchService searchService, ContentService contentService, 
			List<QName> properties, List<SortDefinition> sortDefinitions, Ticket ticket, 
			ExtractRequestDispatcher dispatcher, NodeService nodeService) {
		
		this.searchService = searchService;
		this.contentService = contentService;
		this.properties = properties;
		this.sortDefinitions = sortDefinitions;
		this.ticket = ticket;
		this.dispatcher = dispatcher;
		this.nodeService = nodeService;
		
	}
	
	/**
	 * Executes the DependencyResolvers configured for this request
	 * and decides to read the content or not depending on the type of dependency resolver it is. 
	 * 
	 * @param node
	 * @param xmlExportTO
	 * @throws Exception
	 */
	public void processDependencies(Node node, XMLExportTO xmlExportTO)
			throws Exception {
		
		List<DOMElement> dependencyResolverElems = xmlExportTO.getDependencyResolversElem().elements();
		for (DOMElement dependencyResolverElem : dependencyResolverElems) {
			
			/* want a singleton each time - maybe change this */
			String id = xmlExportTO.getExtractId() + dependencyResolverElem.attributeValue(REF);
			DependencyResolver dependencyResolver = dependencyResolverFactory.getDependencyResolver(id);			
			logger.debug(String.format("processDependencies: id '%s' dependencyResolver '%s'",
					id, (dependencyResolver != null ? dependencyResolver.getClass().getName() : null)));

			try {
				if (dependencyResolver == null) {
					dependencyResolverFactory.generateDependencyResolvers(xmlExportTO.getDependencyResolverDefinitionsElem(), xmlExportTO.getExtractId());
					dependencyResolver = dependencyResolverFactory.getDependencyResolver(id);
				}
				
				if (dependencyResolver != null) {
					
					/* use the dependency resolver as an iterator */
					List<Node> depNodes = new ArrayList<Node>();
					depNodes.add(node);
					
					dependencyResolver.initialize(depNodes, searchService, contentService, 
								xmlExportTO.getStoreRef(), ticket, sortDefinitions, properties, xmlExportTO.getTmpLocation(), nodeService);
					
					while(dependencyResolver.hasNext()) {
						
						if (dependencyResolver instanceof DependencyResolverAndRetriever) {
							// call one method
							processNextDependencies((DependencyResolverAndRetriever)dependencyResolver, xmlExportTO);
						} else if (dependencyResolver instanceof UnpublishDependencyResolver) {
							processNextDependencies((SimpleUnpublishDependencyResolver)dependencyResolver, xmlExportTO);
						} else {
							processNextDependencies((SimplePublishDependencyResolver)dependencyResolver, xmlExportTO);
						}
					}
					
					
				} else {
					logger.error("Dependency Resolver not defined: " + id);
					throw new DependencyResolverException(id, "Dependency Resolver not defined");
				
				}
			} catch (Exception dre) {
				throw dre;
			}
		}
	}

	private void processNextDependencies(SimpleUnpublishDependencyResolver dependencyResolver, XMLExportTO xmlExportTO) throws Exception {
		List<Node> dependencies = dependencyResolver.next();

		logger.debug(String.format("processNextDependencies: dependencies count %d", dependencies.size()));

		xmlExportTO.addToNumResults(dependencies.size());
		
		/* dispatch each node here  - need to change this to just build the node */
		/* dispatch each node here */
		for (Node dep : dependencies) {
	
			/* Build the dispatch object */
			FileExportData fed = dispatcher.buildExportObject(dep, null, xmlExportTO);
			
			// dispatch
			dispatcher.dispatch(fed, xmlExportTO.getQueryElem());
			
		}
		
	}

	/*
	 * 
	 * @param dependencyResolver
	 * @param xmlExportTO
	 * @throws InvalidTicketException
	 * @throws CmaRuntimeException
	 * @throws Exception
	 */
	private void processNextDependencies(DependencyResolverAndRetriever dependencyResolver, XMLExportTO xmlExportTO)
		throws InvalidTicketException, CmaRuntimeException, Exception {
	
		List<RetrievedNode> retrievedNodes = dependencyResolver.next();
		
		xmlExportTO.addToNumResults(retrievedNodes.size());
		
		/* dispatch each node here  (just build the node, it has already been retrieved) */
		for (RetrievedNode retrievedNode : retrievedNodes) {
	
			/* Build the dispatch object */
			FileExportData fed = dispatcher.buildExportObject(retrievedNode.getNode(), retrievedNode.getOutputFile(), xmlExportTO);
			
			// dispatch
			dispatcher.dispatch(fed, xmlExportTO.getQueryElem());
			
		}
	}
	
	private void processNextDependencies(SimplePublishDependencyResolver dependencyResolver, XMLExportTO xmlExportTO)
			throws InvalidTicketException, CmaRuntimeException, Exception {
		
		List<Node> dependencies = dependencyResolver.next();

		xmlExportTO.addToNumResults(dependencies.size());
		
		/* dispatch each node here */
		for (Node dep : dependencies) {
			
			dispatcher.buildAndDispatchNode(dep, xmlExportTO);
		}
	}
	
	

}
