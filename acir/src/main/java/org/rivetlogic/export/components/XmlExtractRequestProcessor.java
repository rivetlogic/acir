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

import com.rivetlogic.core.cma.api.AuthenticationService;
import com.rivetlogic.core.cma.exception.AuthenticationFailure;
import com.rivetlogic.core.cma.exception.CmaRuntimeException;
import com.rivetlogic.core.cma.exception.InvalidTicketException;
import com.rivetlogic.core.cma.impl.AuthenticationServiceImpl;
import com.rivetlogic.core.cma.repo.Ticket;
import org.apache.log4j.Logger;
import org.dom4j.dom.DOMElement;
import org.mule.RequestContext;
import org.rivetlogic.export.components.data.FileExportData;
import org.rivetlogic.export.components.data.FileReportData;
import org.rivetlogic.export.deployers.Deployer;
import org.rivetlogic.export.deployers.Undeployer;
import org.rivetlogic.export.deployers.exceptions.DeployerException;
import org.rivetlogic.export.filters.Filter;
import org.rivetlogic.export.filters.exceptions.FilterException;
import org.rivetlogic.export.notifiers.CMSNotifier;
import org.rivetlogic.export.notifiers.exceptions.NotifierException;
import org.rivetlogic.export.transformers.content.ContentTransformer;
import org.rivetlogic.export.transformers.content.exceptions.ContentTransformationException;
import org.rivetlogic.export.transformers.filename.UrlTransformer;
import org.rivetlogic.export.transformers.filename.exceptions.UrlTransformationException;
import org.rivetlogic.utils.reflection.DeployerFactory;
import org.rivetlogic.utils.reflection.FilterFactory;
import org.rivetlogic.utils.reflection.NotifierFactory;
import org.rivetlogic.utils.reflection.TransformerFactory;
import org.rivetlogic.utils.reflection.UndeployerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Sumer Jabri
 * @author Vagif Jalilov 
 * @author Sandra O'Keeffe
 */
public class XmlExtractRequestProcessor {
	private final String CONTENT_TRANSFORMERS = "content-transformers";
	private final String CONTENT_TRANSFORMER_DEFINITONS = "content-transformer-definitions";
	private final String FILE_NAME_TRANSFORMERS = "file-name-transformers";
	private final String FILE_NAME_TRANSFORMER_DEFINITIONS = "file-name-transformer-definitions";
	private final String REF = "ref";

	private static FilterFactory filterFactory = new FilterFactory();
	private static TransformerFactory transformerFactory = new TransformerFactory();
	private static DeployerFactory deployerFactory = new DeployerFactory();
	private static UndeployerFactory undeployerFactory = new UndeployerFactory();
	private static NotifierFactory notifierFactory = new NotifierFactory();

	private Logger log = Logger.getLogger(this.getClass());
	
	/* the following variables are used for the CMS notifiers */
	/* CMA url, username, password */
	private String cmaUrl;
	private String cmaUsername;
	private String cmaPassword;
	
	private BeanFactory beanFactory;
	private Ticket ticket;
	private AuthenticationService authService;


	
	public void processFileExportData(FileExportData fed) throws Exception {

		File file = null;
		if (fed.getFileLocation() != null) {
			file = new File(fed.getFileLocation());
		}
		String finalPath = fed.getAlfrescoPath();
		String extractId = fed.getExtractId();
		
		FileReportData fileReportData = new FileReportData();
		fileReportData.setExtractsId(fed.getExtractsId());
		fileReportData.setExtractId(extractId);
		fileReportData.setPathAlfresco(finalPath);
		List<String> pathDiskLocation = new ArrayList<String>();
		pathDiskLocation.add(fed.getFileLocation());
		fileReportData.setPathDisk(pathDiskLocation);
		fileReportData.setQueryId(fed.getQueryId());
		fileReportData.setPathTransformed(finalPath);
		fileReportData.setProperties(fed.getProperties());
		fileReportData.setIncluded(true);
		
		try {
			/* filters */
			boolean ignore = doFilters(fed, fileReportData);			
			
			/* transformers */
			finalPath = doTransformers(fed, file, fileReportData, ignore);
			
			/* Deployers */
			if (file != null) {
				doDeployers(fed, file, finalPath, fileReportData, ignore);
			}
			
			/* Undeployers */
			doUndeployers(fed, finalPath, fileReportData, ignore);
			
			/* Notifiers */
			doNotifiers(fed, fileReportData, ignore);
			
		}	catch (Exception e) {
			
			/* this needs to be done before the exception is thrown or no email will be sent */
			try {
				log.debug("RP: sending " + fileReportData);
				RequestContext.getEventContext().dispatchEvent(fileReportData);
			} catch (Exception erp) {
				Logger.getLogger(this.getClass()).error("could not dispatch FileReportData "+ finalPath, e);
				throw erp;
			}
			
			log.error("could not process file "+ finalPath, e);   
			throw e;
		}
		
		try {
			log.debug("RP: sending " + fileReportData);
			RequestContext.getEventContext().dispatchEvent(fileReportData);
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error("could not dispatch FileReportData "+ finalPath, e);
			throw e;
		}
		
	}

	/* Execute the notifiers
	 */
	private void doNotifiers(FileExportData fed, FileReportData fileReportData, boolean ignore) 
		throws IOException, AuthenticationFailure, CmaRuntimeException, Exception {
		
		String extractId = fed.getExtractId();
		DOMElement notifiersElem = fed.getNotifiersElem();
		DOMElement notifiersDefinitionsElem = fed.getNotifierDefinitionsElem();
		if (!ignore && notifiersElem != null) {
			
			/* initialise CMS creds here */
			initializeCMSCreds(fed.getTicket());
			
			List<DOMElement> notifiersElems = notifiersElem.elements();
			for (DOMElement notifierElem : notifiersElems) {
				String id = extractId + notifierElem.attributeValue(REF);
				
				CMSNotifier notifier = notifierFactory.getNotifier(id);
				
				try {
					if (notifier == null) {
						notifierFactory.generateNotifiers(notifiersDefinitionsElem, extractId);
						notifier = notifierFactory.getNotifier(id);
					}
					
					if (notifier != null) {
						notifier.notify(fed.getAlfrescoPath(), fed.getNodeRef(), ticket.getTicket(), cmaUrl);
					} else {
						throw new NotifierException(id, "Notifier not defined");
					
					}
					
				} catch (Exception de) {
					log.error("Error sending notification for, id=" + id + ", Alfresco path=" + fed.getAlfrescoPath());
					fileReportData.setErrorObj(de);
					throw de;
				}
			}
		}
	}
	
	/* Execute the undeployers
	 */
	private void doUndeployers(FileExportData fed, String finalPath, FileReportData fileReportData, 
			boolean ignore) throws Exception {
		
		String extractId = fed.getExtractId();
		DOMElement undeployersElem = fed.getUndeployersElem();
		DOMElement undeployerDefinitionsElem = fed.getUndeployerDefinitionsElem();
		if (!ignore && undeployersElem != null) {
			List<DOMElement> undeployerElems = undeployersElem.elements();
			
			List<String> pathDisk = new ArrayList<String>();;
			for (DOMElement undeployerElem : undeployerElems) {
				String id = extractId + undeployerElem.attributeValue(REF);
				
				Undeployer undeployer = undeployerFactory.getUndeployer(id);
				
				try {
					String path = null;
					if (undeployer == null) {
						undeployerFactory.generateUndeployers(undeployerDefinitionsElem, extractId);
						undeployer = undeployerFactory.getUndeployer(id);
					}
					
					if (undeployer != null) {
						path = undeployer.undeploy(finalPath, fed.getProperties());
					} else {
						throw new DeployerException(id, "Deployer not defined");
					
					}
					pathDisk.add(path);
					
				} catch (Exception de) {
					log.error("Error deploying id=" + id + ", path=" + finalPath);
					fileReportData.setErrorObj(de);
					throw de;
				}
			}
			fileReportData.setPathDisk(pathDisk);
						
		}
	}

	/* Execute the deployers
	 */
	private void doDeployers(FileExportData fed, File file, String finalPath, FileReportData fileReportData,
			boolean ignore) throws Exception {
		
		String extractId = fed.getExtractId();
		DOMElement deployersElem = fed.getDeployersElem();
		DOMElement deployerDefinitionsElem = fed.getDeployerDefinitionsElem();
		if (!ignore && deployersElem != null) {
			
			List<DOMElement> deployerElems = deployersElem.elements();
			File tempFile = file;
			
			try {
				List<String> pathDisk = new ArrayList<String>();;
				for (DOMElement deployerElem : deployerElems) {
					String id = extractId + deployerElem.attributeValue(REF);
					log.debug(String.format("doDeployers: id '%s'", id));

					Deployer deployer = deployerFactory.getDeployer(id);
					String path  = null;
					try {
						if (deployer == null) {
							deployerFactory.generateDeployers(deployerDefinitionsElem, extractId);
							deployer = deployerFactory.getDeployer(id);
						}
						
						if (deployer != null) {
							log.debug(String.format("doDeployers: deployer.deploy file '%s' finalPath '%s' deployer '%s'",
									file.getAbsolutePath(), finalPath, deployer.getClass().getName()));
							path = deployer.deploy(file, finalPath);
						} else {
							throw new DeployerException(id, "Deployer not defined");
						
						}
						pathDisk.add(path);
						
					} catch (Exception de) {
						log.error("Error deploying id=" + id + ", path=" + finalPath);
						fileReportData.setErrorObj(de);
						throw de;
					}
				}
				fileReportData.setPathDisk(pathDisk);
			} finally {
				/* clean-up: delete tmp file here  - even if exception is thrown */
				if (!tempFile.delete()) {
					log.error("Failed to delete temporary file: " + tempFile.getAbsolutePath());
				}
			}
		}
	}

	/* Execute the transformers
	 */
	private String doTransformers(FileExportData fed, File file, FileReportData fileReportData,
			boolean ignore) throws InstantiationException,
			IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, ParseException, NoSuchMethodException,
			ContentTransformationException, UrlTransformationException {
	
		String finalPath = fed.getAlfrescoPath();
		String extractId = fed.getExtractId();
		DOMElement transformerDefinitionsElem = fed.getTransformerDefinitionsElem();
		if (!ignore && transformerDefinitionsElem != null && fed.getTransformersElem() != null) {
			
			/* */
			doContentTransformers(fed, file, finalPath, extractId, fileReportData, transformerDefinitionsElem);
			
			finalPath = doFilenameTransformers(fed, finalPath, extractId, fileReportData, transformerDefinitionsElem);
		}
		return finalPath;
	}

	private String doFilenameTransformers(FileExportData fed, String finalPath,
			String extractId, FileReportData fileReportData,
			DOMElement transformerDefinitionsElem)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException, ParseException,
			NoSuchMethodException, UrlTransformationException {
		DOMElement fileNameTransformersElem = (DOMElement) fed.getTransformersElem().element(FILE_NAME_TRANSFORMERS);
		if (fileNameTransformersElem != null) {
			List<DOMElement> transformerElems = fileNameTransformersElem.elements();
			for (DOMElement transformerElem : transformerElems) {
				String id = extractId + transformerElem.attributeValue(REF);

				UrlTransformer fileNameTransformer = (UrlTransformer) transformerFactory.getTransformer(id);

				if (fileNameTransformer == null) {
					DOMElement fileNameTransformerDefinitionsElem = (DOMElement) transformerDefinitionsElem
							.element(FILE_NAME_TRANSFORMER_DEFINITIONS);
					transformerFactory.generateFileNameTransformers(fileNameTransformerDefinitionsElem, extractId);
					fileNameTransformer = (UrlTransformer) transformerFactory.getTransformer(id);
				}

				if (fileNameTransformer != null) {
					try {
						finalPath = fileNameTransformer.transformUrl(finalPath);
						fileReportData.setPathTransformed(finalPath);
					} catch (UrlTransformationException ute) {
						fileReportData.setErrorObj(ute);
						log.error("url transformer '" + id + "' failed to transform '" + finalPath + "'", ute);							
						throw ute;
					}
				} else {
					fileReportData.setErrorObj(new UrlTransformationException(id, "Filename transformer not defined."));
					
					throw new UrlTransformationException("Filename transformer " + id + " not found.");
				}
			}
		}
		return finalPath;
	}

	private void doContentTransformers(FileExportData fed, File file,
			String finalPath, String extractId, FileReportData fileReportData,
			DOMElement transformerDefinitionsElem)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException, ParseException,
			NoSuchMethodException, ContentTransformationException {
		DOMElement contentTransformersElem = (DOMElement) fed.getTransformersElem().element(CONTENT_TRANSFORMERS);
		if (contentTransformersElem != null) {
			List<DOMElement> transformerElems = contentTransformersElem.elements();
			for (DOMElement transformerElem : transformerElems) {
				String id = extractId + transformerElem.attributeValue(REF);

				ContentTransformer ct = (ContentTransformer) transformerFactory.getTransformer(id);

				if (ct == null) {
					DOMElement contentTransformerDefinitionsElem = (DOMElement) transformerDefinitionsElem
							.element(CONTENT_TRANSFORMER_DEFINITONS);
					transformerFactory.generateContentTransformers(contentTransformerDefinitionsElem, extractId);
					ct = (ContentTransformer) transformerFactory.getTransformer(id);
				}

				if (ct != null) {
					try {
						ct.transformContent(file, finalPath, fed.getTransformerDependencies());
					} catch (ContentTransformationException cte) {
						fileReportData.setErrorObj(cte);
						log.error("content transformer '" + id + "' failed to transform '" + finalPath + "'", cte);
						throw cte;
					}
				} else {
					fileReportData.setErrorObj(new ContentTransformationException(id, "Content transformer not defined."));
					
					throw new ContentTransformationException("Content transformer " + id + " not found.");
				}
			}
		}
	}

	/* Execute the filters
	 */
	private boolean doFilters(FileExportData fed, FileReportData fileReportData)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException, ParseException,
			NoSuchMethodException, FilterException {
		
		boolean ignore = false;
		
		String extractId = fed.getExtractId();
		
		DOMElement filterDefinitionsElem = fed.getFilterDefinitionsElem();
		if (filterDefinitionsElem != null && fed.getFiltersElem() != null) {
			DOMElement filtersElem = fed.getFiltersElem();
			if (filtersElem != null) {
				List<DOMElement> filterElems = filtersElem.elements();
				for (DOMElement filterElem : filterElems) {
					String id = extractId + filterElem.attributeValue(REF);

					Filter filter = (Filter) filterFactory.getFilter(id);

					if (filter == null) {
						filterFactory.generateFilters(filterDefinitionsElem, extractId);
						filter = (Filter) filterFactory.getFilter(id);
					}

					if (filter != null) {
						try {
							if (!filter.includeFile(fed.getNodeRef(), fed.getProperties())) {
								fileReportData.setIncluded(false);
								ignore = true;
							}
						} catch (FilterException fe) {
							fileReportData.setErrorObj(fe);
							log.error("content transformer '" + id + "' failed to transform '" + fed.getAlfrescoPath() + "'", fe);
							throw fe;
						}
					} else {
						fileReportData.setErrorObj(new FilterException(id, "Filter not defined."));

						throw new FilterException(id, "Filter '" + id + "' not defined"); 
					}
				}
			}
		}
		return ignore;
	}
	
	/* Initialise creds for sending to any CMS notifier plugins
	 */
	private void initializeCMSCreds(Ticket ticketParam) throws IOException, AuthenticationFailure, CmaRuntimeException {
		
		// set up the bean factory
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
			authService = (AuthenticationService) beanFactory.getBean("authenticationService", AuthenticationServiceImpl.class);
		}
		
		// get a ticket
		if (this.ticket == null) {
			
			if (ticketParam != null) {
				this.ticket = ticketParam;
			
			} else {
				this.ticket = authService.authenticate(cmaUrl, cmaUsername, cmaPassword.toCharArray());
			}
		}

		try {
			authService.validate(ticket);
		} catch (InvalidTicketException e) {
			log.debug("ticket invalid, getting a new one.");
			ticket = authService.authenticate(cmaUrl, cmaUsername, cmaPassword.toCharArray());
		}
	}
	
	public void processFileExportData() {
		/* The purpose of this method is to stop false exception from confusing the logs.  
		 * The overloaded method with the argument FileExportData is what is called using a dispatch.
		 * The return of null (output) from ExportXMLProcessor results in this being called.
		 */
	}
	
	/**
	 * Log  an errors that might get thrown from the previous process (makes troubleshooting easier)
	 * 
	 * @param e
	 * @throws Exception
	 */
	public void logException(Exception e) throws Exception {
		log.error(e);
		throw e;
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
}
