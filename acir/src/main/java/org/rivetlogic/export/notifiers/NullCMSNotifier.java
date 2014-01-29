/*
 * Copyright (C) 2007-2012 Rivet Logic Corporation.
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
import org.apache.log4j.Logger;
import org.rivetlogic.export.notifiers.exceptions.NotifierException;

public class NullCMSNotifier implements CMSNotifier {

	private String id = null;
	private Logger logger = Logger.getLogger(this.getClass());
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void notify(String path, NodeRef nodeRef, String ticket,
			String cmsServiceUrl) throws NotifierException {
		logger.info("Executing NullCMSNotifier...");
		/* sample */
	}

}
