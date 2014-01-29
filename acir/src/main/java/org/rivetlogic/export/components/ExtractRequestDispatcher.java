package org.rivetlogic.export.components;

import java.io.File;
import java.io.FileOutputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.Path;
import org.apache.log4j.Logger;
import org.dom4j.dom.DOMElement;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleException;
import org.rivetlogic.export.components.data.FileExportData;
import org.rivetlogic.export.dependencyresolvers.DependencyResolverProcessor;
import org.rivetlogic.export.dependencyresolvers.TransformerDependencyResolver;
import org.rivetlogic.utils.IntegrationUtils;

import com.rivetlogic.core.cma.api.ContentService;
import com.rivetlogic.core.cma.api.NodeService;
import com.rivetlogic.core.cma.repo.Node;
import com.rivetlogic.core.cma.repo.Ticket;

/**
 * Responsible for dispatching extract request
 * 
 * @author Sandra O'Keeffe
 *
 */
public class ExtractRequestDispatcher {

	protected Logger logger = Logger.getLogger(DependencyResolverProcessor.class);
	
	private ContentService contentService;
	private NodeService nodeService;
	private Ticket ticket;
	private MuleEventContext eventContext = null;
	private TransformerDependencyResolver tdrp = null;
	
	public ExtractRequestDispatcher(ContentService contentService, NodeService nodeService, Ticket ticket, 
			MuleEventContext eventContext, TransformerDependencyResolver tdrp) {
		
		this.contentService = contentService;
		this.nodeService = nodeService;
		this.ticket = ticket;
		this.eventContext = eventContext;
		this.tdrp = tdrp;
		

	}
	
	/**
	 * 
	 * @param filtersElem
	 * @param transformersElem
	 * @param deployersElem
	 * @param filterDefinitionsElem
	 * @param transformerDefinitionsElem
	 * @param deployerDefinitionsElem
	 * @param tmpLocation
	 * @param extractsId
	 * @param extractId
	 * @param queryId
	 * @param storeRef
	 * @param node
	 * @return
	 * @throws Exception
	 */
	private FileExportData retrieveContentAndBuildExportObject(Node node, XMLExportTO xmlExportTO) throws Exception {
		
		FileExportData fed = null;
		
		if(!ContentModel.TYPE_FOLDER.equals(node.getType())) {
			
			File outputFile = File.createTempFile("integration_", ".tmp", xmlExportTO.getTmpLocation());

			FileOutputStream out = new FileOutputStream(outputFile);
			contentService.readContentIntoStream(ticket, node.getNodeRef(), ContentModel.PROP_CONTENT, out);
			out.close();
			
			fed = buildExportObject(node, outputFile, xmlExportTO);
		}
		
		return fed;
	}

	/** 
	 * Builds a FileExportData object for dispatch
	 * 
	 * @param filtersElem
	 * @param transformersElem
	 * @param deployersElem
	 * @param filterDefinitionsElem
	 * @param transformerDefinitionsElem
	 * @param deployerDefinitionsElem
	 * @param tmpLocation
	 * @param extractsId
	 * @param extractId
	 * @param queryId
	 * @param storeRef
	 * @param node
	 * @param alfPath
	 * @param path
	 * @param outputFile
	 * @return
	 * @throws Exception
	 */
	public FileExportData buildExportObject(Node node, File outputFile, XMLExportTO xmlExportTO) throws Exception {
		
		/* get the properties */
		node.setProperties(nodeService.getProperties(ticket, node.getNodeRef()));
		
		Path alfPath = nodeService.getPath(ticket, node.getNodeRef());
		logger.info("Processing node at path: " + alfPath);
		String path = IntegrationUtils.convertPath(alfPath);
		
		// Generate a message and pass it on.
		FileExportData fed = new FileExportData();
		fed.setAlfrescoPath(path);
		fed.setAlfrescoPathWithNamespaces(IntegrationUtils.decodePath(alfPath));
		fed.setFileLocation((outputFile == null) ? null : outputFile.getAbsolutePath());
		fed.setFilterDefinitionsElem(xmlExportTO.getFilterDefinitionsElem());
		fed.setTransformerDefinitionsElem(xmlExportTO.getTransformerDefinitionsElem());
		fed.setNotifierDefinitionsElem(xmlExportTO.getNotifierDefinitionsElem());
		/* set dependents for transformers */
		fed.setTransformerDependencies(tdrp.getTransformerDependencies(node, outputFile, path, 
				xmlExportTO.getTransformerDefinitionsElem(), 
				xmlExportTO.getTransformersElem(), 
				xmlExportTO.getExtractId(), 
				xmlExportTO.getStoreRef(), 
				xmlExportTO.getTmpLocation()));
		fed.setDeployerDefinitionsElem(xmlExportTO.getDeployerDefinitionsElem());
		fed.setUndeployerDefinitionsElem(xmlExportTO.getUndeployerDefinitionsElem());
		fed.setFiltersElem(xmlExportTO.getFiltersElem());
		fed.setTransformersElem(xmlExportTO.getTransformersElem());
		fed.setDeployersElem(xmlExportTO.getDeployersElem());
		fed.setUndeployersElem(xmlExportTO.getUndeployersElem());
		fed.setNotifiersElem(xmlExportTO.getNotifiersElem());
		fed.setQueryId(xmlExportTO.getQueryId());
		fed.setExtractsId(xmlExportTO.getExtractsId());
		fed.setExtractId(xmlExportTO.getExtractId());
		fed.setProperties(node.getProperties());
		fed.setNodeRef(node.getNodeRef());
		
		fed.setTicket(ticket);
		
		return fed;
	}
	
	/**
	 * Dispatches a FileExportData object
	 * @param fed
	 * @param queryElem
	 * @throws MuleException 
	 * @throws Exception 
	 */
	public void dispatch(FileExportData fed, DOMElement queryElem) throws MuleException {

		if (fed != null) {
			try {
				eventContext.dispatchEvent(fed);
			} catch (MuleException e) {
				logger.error(queryElem.getTextTrim(), e);
				throw e;
			}
		}

	}
	
	/**
	 * Builds a FileExportData object and dispatches it
	 * 
	 * @param node
	 * @param xmlExportTO
	 * @throws Exception
	 */
	public void buildAndDispatchNode(Node node, XMLExportTO xmlExportTO) throws Exception {
				
		/* build object */
		FileExportData fed = retrieveContentAndBuildExportObject(node, xmlExportTO);
		
		/* dispatch */
		dispatch(fed, xmlExportTO.getQueryElem());
		
	}

}

