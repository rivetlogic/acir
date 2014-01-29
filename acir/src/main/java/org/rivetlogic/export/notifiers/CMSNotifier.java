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
package org.rivetlogic.export.notifiers;

import org.alfresco.service.cmr.repository.NodeRef;
import org.rivetlogic.export.notifiers.exceptions.NotifierException;

/**
 * Interface that should be implemented when you want to send any notifications 
 * back to the CMS once the publish is successfully completed 
 * 
 * @author Sandra O'Keeffe
 */
public interface CMSNotifier {

	public String getId();
	public void setId(String id);
	
	/**
	 * Method to implement in order to send a notification to the CMS once the
	 * publish is complete.  The ticket to user and the service url of the configured
	 * CMS is passed in. 
	 * 
	 * @param path
	 * @param nodeRef
	 * @param ticket
	 * @param cmsServiceUrl
	 * @throws NotifierException
	 */
	public void notify(String path, NodeRef nodeRef, String ticket, String cmsServiceUrl) throws NotifierException;
}
