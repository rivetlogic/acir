package org.rivetlogic.export.dependencyresolvers;

import static org.rivetlogic.utils.QueryXMLContstants.CONTENT_TRANSFORMERS;
import static org.rivetlogic.utils.QueryXMLContstants.CONTENT_TRANSFORMER_DEFINITONS;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.dom4j.dom.DOMElement;
import org.rivetlogic.export.dependencyresolvers.exceptions.DependencyResolverException;
import org.rivetlogic.export.transformers.content.ContentTransformer;
import org.rivetlogic.export.transformers.content.exceptions.ContentTransformationException;
import org.rivetlogic.utils.reflection.TransformerFactory;

import com.rivetlogic.core.cma.api.ContentService;
import com.rivetlogic.core.cma.api.SearchService;
import com.rivetlogic.core.cma.api.SearchService.QueryLanguage;
import com.rivetlogic.core.cma.repo.Node;
import com.rivetlogic.core.cma.repo.SortDefinition;
import com.rivetlogic.core.cma.repo.Ticket;

/**
 * Responsible for determining dependencies used by the content transformers 
 * 
 * @author Sandra O'Keeffe
 */
public class TransformerDependencyResolver {

	private final String REF = "ref";
	
	private static TransformerFactory transformerFactory = new TransformerFactory();
	
	protected Logger logger = Logger.getLogger(DependencyResolverProcessor.class);
	
	private SearchService searchService;
	private ContentService contentService;
	private List<QName> properties;
	private List<SortDefinition> sortDefinitions;
	private Ticket ticket;
	
	public TransformerDependencyResolver(SearchService searchService, ContentService contentService, 
			List<QName> properties, List<SortDefinition> sortDefinitions, Ticket ticket) {
		
		this.searchService = searchService;
		this.contentService = contentService;
		this.properties = properties;
		this.sortDefinitions = sortDefinitions;
		this.ticket = ticket;
		
	}
	
	/* This method is responsible for determining dependencies the content transformers may have
	 * and creating temporary files for these and returning a map of the file name and temp location.
	 */
	public Map<String, String> getTransformerDependencies(Node node, File coreFileContent, String path,
			DOMElement transformerDefinitionsElem, DOMElement transformersElem, String extractId, 
			StoreRef storeRef, File tmpLocation) throws Exception {
		
		Map<String, String> transformationDependents = new HashMap<String, String>();
		
		if (transformersElem != null && transformersElem != null) {
			DOMElement contentTransformersElem = (DOMElement) transformersElem.element(CONTENT_TRANSFORMERS);
			
			if (contentTransformersElem != null) {
				List<DOMElement> transformerElems = contentTransformersElem.elements();
				for (DOMElement transformerElem : transformerElems) {
					String id = extractId + transformerElem.attributeValue(REF);
					
					ContentTransformer transformer = (ContentTransformer) transformerFactory.getTransformer(id);
					
					try {
						if (transformer == null) {
							DOMElement contentTransformerDefinitionsElem = (DOMElement) transformerDefinitionsElem
								.element(CONTENT_TRANSFORMER_DEFINITONS);
							transformerFactory.generateContentTransformers(contentTransformerDefinitionsElem, extractId);
							transformer = (ContentTransformer) transformerFactory.getTransformer(id);
						} 
						
						if (transformer != null) {
							logger.debug("Calculating dependencies for node " + node.getNodeRef() );
							
							List<String> dependencies = new ArrayList<String>();
							
							List<String> pathDependencies = transformer.determineDependenciesByPath(path);
							if (pathDependencies != null) dependencies.addAll(pathDependencies);

							List<String> otherDependencies = transformer.determineDependencies(path, coreFileContent);
							if (otherDependencies != null) dependencies.addAll(otherDependencies);
							
							if (dependencies != null) {
								Iterator<String> dependentIter = dependencies.iterator();
								
								while(dependentIter.hasNext()) {
									String dependentQuery = dependentIter.next();
									
									/* only one should be return*/
									// String query = "PATH:\"" + IntegrationUtils.encodePath(dependent) + "\"";
									String query = dependentQuery;
									
									List<Node> nodes = searchService.query(ticket, storeRef, QueryLanguage.lucene, query, properties, false,
											false, false, false, null, 0, 100, sortDefinitions);
									
									logger.debug(nodes.size() + " dependents returned for node: " + node.getNodeRef() + ". Query executed: " + query);
									Node dependentNode = null;
									if (nodes.size() == 1 || nodes.size() > 1) {
										dependentNode = nodes.get(0);
										
										/* get from Alfresco and write to file sys and set location in file sys in transformer */
										File outputFile = File.createTempFile("integration_", ".tmp", tmpLocation);
			
										FileOutputStream out = new FileOutputStream(outputFile);
										contentService.readContentIntoStream(ticket, dependentNode.getNodeRef(), ContentModel.PROP_CONTENT, out);
										out.close();
										
										transformationDependents.put(dependentNode.getNodeRef().toString(), outputFile.getAbsolutePath());
										
									} else if (nodes.size() == 0) {
										logger.error("Could not find dependent with query: " + dependentQuery + ".");
									} else {
										logger.error("Invalid number of dependents returned: " + nodes.size());
										throw new ContentTransformationException(extractId, "Invalid number of dependents (" + nodes.size() + ") returned " + " for query " + dependentQuery);
									}
								}
							}
		 				} else {
		 					logger.error("Transformer not defined: " + id);
							throw new DependencyResolverException(id, "Transformer not defined");
						
						}
					} catch (Exception te) {
						throw te;
					}
				}
			}
		}
		return transformationDependents;
	}
}
