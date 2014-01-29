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
package org.rivetlogic.export.components;

import static org.rivetlogic.utils.QueryXMLContstants.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMDocumentFactory;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.Callable;
import org.rivetlogic.export.components.data.ExtractReportData;
import org.rivetlogic.export.components.data.ExtractsReportData;
import org.rivetlogic.export.components.data.QueryReportData;
import org.rivetlogic.export.dependencyresolvers.DependencyResolverProcessor;
import org.rivetlogic.export.dependencyresolvers.TransformerDependencyResolver;
import org.rivetlogic.utils.IntegrationConstants;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import com.rivetlogic.core.cma.api.AuthenticationService;
import com.rivetlogic.core.cma.api.ContentService;
import com.rivetlogic.core.cma.api.NodeService;
import com.rivetlogic.core.cma.api.SearchService;
import com.rivetlogic.core.cma.api.SearchService.QueryLanguage;
import com.rivetlogic.core.cma.exception.AuthenticationFailure;
import com.rivetlogic.core.cma.exception.CmaRuntimeException;
import com.rivetlogic.core.cma.exception.InvalidTicketException;
import com.rivetlogic.core.cma.impl.AuthenticationServiceImpl;
import com.rivetlogic.core.cma.impl.ContentServiceImpl;
import com.rivetlogic.core.cma.impl.NodeServiceImpl;
import com.rivetlogic.core.cma.impl.SearchServiceImpl;
import com.rivetlogic.core.cma.repo.Node;
import com.rivetlogic.core.cma.repo.SortDefinition;
import com.rivetlogic.core.cma.repo.SortDefinition.SortType;
import com.rivetlogic.core.cma.repo.Ticket;

/**
 * @author Sumer Jabri
 * @author Sandra O'Keeffe
 */
public abstract class AbstractXMLProcessor implements Callable {
	private String xsdPath;
	private String xPath;
	private Validator validator = null;
	private MuleEventContext eventContext = null;

	/* CMA url, username, password */
	private String cmaUrl;
	private String cmaUsername;
	private String cmaPassword;
	
	private BeanFactory beanFactory;
	private Ticket ticket;
	private AuthenticationService authService;

	private NodeService nodeService;
	private SearchService searchService;
	private ContentService contentService;
	protected Logger logger;
	

	private String ackPath;
	
	private volatile int numExports = 0;
	
	private List<QName> properties = new ArrayList<QName>(3);
	{
		properties.add(ContentModel.PROP_NAME);
	}

	private List<SortDefinition> sortDefinitions = new ArrayList<SortDefinition>(2);
	{
		sortDefinitions.add(new SortDefinition(SortType.FIELD, ContentModel.PROP_NAME.toPrefixString(), true));
	}

	public AbstractXMLProcessor() {
		
		PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
		logger = Logger.getLogger(this.getClass());
		
	}

	private class XMLElementHandler implements ElementHandler {
		private int numExtractsInRequest = 0;
		private int totalNumResults = 0;
		
		public void onStart(ElementPath path) {
		// do nothing here...
		}

		public void onEnd(ElementPath path) {
			// process an element
			DOMElement product = (DOMElement) path.getCurrent();
			// DOMDocument doc = new DOMDocument((DOMElement)product);

			if (product != null && validator != null) {
				try {
					validator.validate(new DOMSource(product));
				} catch (Exception e) {
					// exception handling - document not valid!
					if (e instanceof SAXException && ((SAXException) e).getException() instanceof TransformerException) {
						// continue
					} else {
						//logger.error(e);
						try {
							eventContext.dispatchEvent(e);
						} catch (MuleException e1) {
							logger.error(e1);
						}
					}
				}
			}
			
			numExtractsInRequest++;
			totalNumResults += processExportRequest(product);

			// prune the tree
			product.detach();
		}
	}

	public Object onCall(MuleEventContext eventContext) throws Exception {
		SAXReader saxReader = new SAXReader();
		saxReader.setDocumentFactory(DOMDocumentFactory.getInstance());
		XMLElementHandler xmlElementHandler = new XMLElementHandler();
		saxReader.addHandler(xPath, xmlElementHandler);
		this.eventContext = eventContext;
		
		//this.eventContext.getMessage().setStringProperty(EXTRACT_ID, String.valueOf(System.currentTimeMillis()));

		InputStream input = getInputStream(eventContext);
		DOMDocument document = (DOMDocument) saxReader.read(input);

		String extractsId = document.getRootElement().elementText(EXTRACTS_ID);

		ExtractsReportData extractsReportData = new ExtractsReportData();
		extractsReportData.setExtractsId(extractsId);
		extractsReportData.setNumExtracts(xmlElementHandler.numExtractsInRequest); 
		extractsReportData.setFileForNoResults(document.getRootElement().elementText(FILE_FOR_NO_RESULTS) != null ? 
				Boolean.valueOf(document.getRootElement().elementText(FILE_FOR_NO_RESULTS)) : 
					true);
		extractsReportData.setTotalNumResults(xmlElementHandler.totalNumResults);

		document.clearContent();
		document = null;
		input.close();
		
		return extractsReportData;
	}

	protected InputStream getInputStream(MuleEventContext eventContext) throws org.mule.api.transformer.TransformerException{
		return (InputStream) eventContext.getMessage().getPayload();
	}

	/**
	 * 
	 * @param extractRequestElem
	 * @return
	 */
	public int processExportRequest(DOMElement extractRequestElem) {
		
		// monitors the total number of results
		int totalNumResults = 0;
		
		List<DOMElement> queryElems = (List<DOMElement>) extractRequestElem.element(QUERIES).elements();
		DOMElement storeRefElem = (DOMElement) extractRequestElem.element(STORE_REF);
		DOMElement extractRequestsElem = (DOMElement) extractRequestElem.getParent();
		DOMElement filtersElem = (DOMElement) extractRequestElem.element(FILTERS);
		DOMElement transformersElem = (DOMElement) extractRequestElem.element(TRANSFORMERS);
		DOMElement deployersElem = (DOMElement) extractRequestElem.element(DEPLOYERS);
		DOMElement undeployersElem = (DOMElement) extractRequestElem.element(UNDEPLOYERS);
		DOMElement dependencyResolversElem = (DOMElement) extractRequestElem.element(DEPENDENCY_RESOLVER);
		DOMElement notifiersElem = (DOMElement) extractRequestElem.element(NOTIFIER);
		DOMElement filterDefinitionsElem = null;
		DOMElement transformerDefinitionsElem = null;
 		DOMElement deployerDefinitionsElem = null;
 		DOMElement undeployerDefinitionsElem = null;
 		DOMElement notifierDefinitionsElem = null;
 		DOMElement dependencyResolverDefinitionsElem = null;
		DOMElement definitionsElem = (DOMElement)extractRequestsElem.element(DEFINITIONS);
		if (definitionsElem != null) {
			filterDefinitionsElem = (DOMElement) definitionsElem.element(FILTER_DEFINITIONS);
			transformerDefinitionsElem = (DOMElement) definitionsElem.element(TRANSFORMER_DEFINITIONS);
	 		deployerDefinitionsElem = (DOMElement) definitionsElem.element(DEPLOYER_DEFINITIONS);
	 		undeployerDefinitionsElem = (DOMElement) definitionsElem.element(UNDEPLOYER_DEFINITIONS);
	 		notifierDefinitionsElem = (DOMElement) definitionsElem.element(NOTIFIER_DEFINITIONS);
	 		dependencyResolverDefinitionsElem = (DOMElement) definitionsElem.element(DEPENDENCY_RESOLVER_DEFINITIONS);
		}
		File tmpLocation = new File(IntegrationConstants.TMP_FOLDER + (numExports++) + '/');
		tmpLocation.mkdirs();
		
		String extractsId = extractRequestsElem.elementText(EXTRACTS_ID);
		/* append ID to milliseconds (fix for Jira ACIR-2) */
		String extractId = String.valueOf(System.currentTimeMillis())+extractRequestsElem.elementText(EXTRACTS_ID);	
		
		/* build the XMLExportTO object */
		XMLExportTO xmlExportTO = buildBaseXMLExportTO(filtersElem,
				deployersElem, undeployersElem, dependencyResolversElem, filterDefinitionsElem,
				deployerDefinitionsElem, undeployerDefinitionsElem, dependencyResolverDefinitionsElem, 
				transformerDefinitionsElem, transformersElem, 
				notifierDefinitionsElem, notifiersElem,
				tmpLocation, extractsId, extractId);
		
		try {
			initialize();

			for (int j=1; j<= queryElems.size(); j++) {
				DOMElement queryElem = queryElems.get(j-1);
				
				xmlExportTO.setQueryElem(queryElem);
				
				int i = 1;
				boolean done = false;
				String queryId = queryElem.attributeValue(ID, extractId + String.valueOf(j));
				
				QueryReportData queryReportData = new QueryReportData();
				queryReportData.setExtractsId(extractsId);
				queryReportData.setExtractId(extractId);
				queryReportData.setQueryId(queryId);
				queryReportData.setNumResults(0);
				
				xmlExportTO.setQueryId(queryId);
				
				StoreRef storeRef = new StoreRef(storeRefElem.getTextTrim());
				xmlExportTO.setStoreRef(storeRef);
				
				try {
					
					/* initialize transform dependency resolver processor - to be used by dispatcher */
					TransformerDependencyResolver tdrp = new TransformerDependencyResolver(searchService, contentService, 
							properties, sortDefinitions, ticket);
					
					/* initialize request dispatcher */
					ExtractRequestDispatcher dispatcher = new ExtractRequestDispatcher(contentService, 
							nodeService, ticket, eventContext, tdrp);
					
					DependencyResolverProcessor drp = null;
					
					/* processing 100 at a time */
					while (!done) {
						List<Node> nodes = searchService.query(ticket, storeRef, QueryLanguage.lucene, queryElem.getTextTrim(), properties, false,
								false, false, false, null, (i - 1) * 100, i * 100, sortDefinitions);
						
						if (nodes != null) {
							
							int parentNodeSize = nodes.size();
							
							if (parentNodeSize < 100) done = true;
	
							logger.info("Processing nodes...");
							
							for (Node node : nodes) {	
									
								/* Process any dependencies for each node (the Processor also handles the dispatch) */
								if (dependencyResolversElem != null) {
									
									/* initialise the dependency resolver */
									if (drp == null) {
										drp = new DependencyResolverProcessor(searchService, contentService, 
											properties, sortDefinitions, ticket, dispatcher, nodeService);
									}
									
									drp.processDependencies(node, xmlExportTO);
									
									/* if transaction, would assume do here */
								}
								
								/* dispatch the root nodes */
								dispatchNode(dispatcher, node, xmlExportTO);
								
							}
	
							i++;
						} else {
							done = true;
						}
						
						xmlExportTO.addToNumResults(nodes.size());
					}
					
					queryReportData.setNumResults(xmlExportTO.getNumResults());
					totalNumResults += xmlExportTO.getNumResults();
				} catch (Exception e) {
					/* ensure error notifier is sent */
					logger.error("Error thrown reading content", e);
					try {
						eventContext.dispatchEvent(e);
					} catch (MuleException e1) {
						logger.error("Could not dispatch error message.", e);
						throw e1;
					}
					
					/* create query response */
					queryReportData.setErrorMsg(e.getLocalizedMessage());
					logger.error(queryElem.getTextTrim(), e);
				}
				
				try {
					eventContext.dispatchEvent(queryReportData);
				} catch (Exception e) {
					logger.error("could not dispatch QueryReportData", e);
					throw e;
				}
			}
			
			ExtractReportData extractReportData = new ExtractReportData();
			extractReportData.setExtractsId(extractsId);
			extractReportData.setExtractId(extractId);
			extractReportData.setNumQueries(queryElems.size());
			
			try {
				eventContext.dispatchEvent(extractReportData);
			} catch (MuleException e1) {
				logger.error("Could not dispatch ExtractReportData message.", e1);
				throw e1;
			}
			
		} catch (Exception e) {
			logger.error("Error in initialize()", e);
			try {
				eventContext.dispatchEvent(e);
			} catch (MuleException e1) {
				logger.error("Could not dispatch error message.", e);
			}
		}
		
		return totalNumResults;
	}
	
	/**
	 * Handles dispatching of the node
	 * @param dispatcher
	 * @param node
	 * @param xmlExportTO
	 * @throws Exception
	 */
	public abstract void dispatchNode(ExtractRequestDispatcher dispatcher, Node node, XMLExportTO xmlExportTO) throws Exception;

	/* Creates & returns an XMLExportTO object with the data passed in
	 * 
	 * @param filtersElem
	 * @param deployersElem
	 * @param dependencyResolversElem
	 * @param filterDefinitionsElem
	 * @param deployerDefinitionsElem
	 * @param dependencyResolverDefinitionsElem
	 * @param transformerDefinitionsElem
	 * @param transformersElem
	 * @param tmpLocation
	 * @param extractsId
	 * @param extractId
	 * @return
	 */
	private XMLExportTO buildBaseXMLExportTO(DOMElement filtersElem,
			DOMElement deployersElem, DOMElement undeployersElem, DOMElement dependencyResolversElem,
			DOMElement filterDefinitionsElem, DOMElement deployerDefinitionsElem, DOMElement undeployerDefinitionsElem,
			DOMElement dependencyResolverDefinitionsElem, DOMElement transformerDefinitionsElem, DOMElement transformersElem, 
			DOMElement notifierDefinitionsElem, DOMElement notifiersElem,
			File tmpLocation, String extractsId, String extractId) {
		
		XMLExportTO xmlExportTO = new XMLExportTO();
		xmlExportTO.setDependencyResolverDefinitionsElem(dependencyResolverDefinitionsElem);
		xmlExportTO.setDependencyResolversElem(dependencyResolversElem);
		xmlExportTO.setDeployerDefinitionsElem(deployerDefinitionsElem);
		xmlExportTO.setUndeployerDefinitionsElem(undeployerDefinitionsElem);
		xmlExportTO.setDeployersElem(deployersElem);
		xmlExportTO.setUndeployersElem(undeployersElem);
		xmlExportTO.setNotifierDefinitionsElem(notifierDefinitionsElem);
		xmlExportTO.setNotifiersElem(notifiersElem);
		xmlExportTO.setExtractId(extractId);
		xmlExportTO.setExtractsId(extractsId);
		xmlExportTO.setFilterDefinitionsElem(filterDefinitionsElem);
		xmlExportTO.setFiltersElem(filtersElem);
		xmlExportTO.setTmpLocation(tmpLocation);
		xmlExportTO.setTransformerDefinitionsElem(transformerDefinitionsElem);
		xmlExportTO.setTransformersElem(transformersElem);
		
		return xmlExportTO;
	}

	
	

	private void initialize() throws AuthenticationFailure, CmaRuntimeException, IOException {

		if (ticket == null) {
			if (beanFactory == null) {
				PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
				AbstractApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");
				
				Resource resource = context.getResource("classpath:core/cma-cfg.properties");
				Properties properties = new Properties();
				properties.load(resource.getInputStream());

				configurer.setProperties(properties);

				context.addBeanFactoryPostProcessor(configurer);
				context.refresh();
				
				beanFactory = context.getBeanFactory();
			}

			authService = (AuthenticationService) beanFactory.getBean("authenticationService", AuthenticationServiceImpl.class);

			ticket = authService.authenticate(cmaUrl, cmaUsername, cmaPassword.toCharArray());
			nodeService = (NodeService) beanFactory.getBean("nodeService", NodeServiceImpl.class);
			searchService = (SearchService) beanFactory.getBean("searchService", SearchServiceImpl.class);

			contentService = (ContentServiceImpl) beanFactory.getBean("contentService", ContentServiceImpl.class);
		}

		try {
			authService.validate(ticket);
		} catch (InvalidTicketException e) {
			logger.debug("ticket invalid, getting a new one.");
			ticket = authService.authenticate(cmaUrl, cmaUsername, cmaPassword.toCharArray());
		}
	}

	/**
	 * @return the ackPath
	 */
	public String getAckPath() {
		return ackPath;
	}

	/**
	 * @param ackPath the ackPath to set
	 */
	public void setAckPath(String ackPath) {
		this.ackPath = ackPath;
	}
	
	/**
	 * @param cmaUrl
	 */
	public void setCmaUrl(String cmaUrl) {
		this.cmaUrl = cmaUrl;
	}

	/**
	 * @param cmaUsername
	 */
	public void setCmaUsername(String cmaUsername) {
		this.cmaUsername = cmaUsername;
	}

	/**
	 * @param cmaPassword
	 */
	public void setCmaPassword(String cmaPassword) {
		this.cmaPassword = cmaPassword;
	}
	
	public String getXPath() {
		return xPath;
	}

	public void setXPath(String path) {
		xPath = path;
	}

	public String getXsdPath() {
		return xsdPath;
	}

	public void setXsdPath(String xsdPath) throws SAXException {
		this.xsdPath = xsdPath;
		if (xsdPath != null) {
		
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = null;
			try {
				schema = factory.newSchema(this.getClass().getClassLoader().getResource(xsdPath));
			} catch (SAXException e) {
				logger.error(e);
				throw e;
			}	
			validator = schema.newValidator();
		}
	}

	public boolean getDebug() {
		return IntegrationConstants.DEBUG;
	}

	public void setDebug(boolean debug) {
		IntegrationConstants.DEBUG = debug;
	}

	public String getTmpFolder() {
		return IntegrationConstants.TMP_FOLDER;
	}

	public void setTmpFolder(String tmpFolder) {
		IntegrationConstants.TMP_FOLDER = tmpFolder;
		File tmpLocation = new File(IntegrationConstants.TMP_FOLDER);
		tmpLocation.mkdirs();
		tmpLocation.deleteOnExit();
	}

}
