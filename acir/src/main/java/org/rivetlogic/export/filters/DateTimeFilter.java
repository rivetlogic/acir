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
package org.rivetlogic.export.filters;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import org.rivetlogic.export.filters.exceptions.FilterException;

public class DateTimeFilter implements Filter {
	
	public boolean includeFile(NodeRef nodeRef, Map<QName, Serializable> properties) throws FilterException {
		Date dateModified = (Date)properties.get(ContentModel.PROP_MODIFIED);
		boolean include = true;
		
		if (dateModified != null) {
			if (olderThan != null) {
				include = dateModified.before(olderThan) && include;
			}

			if (newerThan != null) {
				include = dateModified.after(newerThan) && include;
			}
		}
		
		return include;
	}
	
	private Date olderThan; 
	private Date newerThan; 
	
	private String id;
	
	public Date getOlderThan() {
		return this.olderThan;
	}
	public void setOlderThan(Date olderThan) {
		this.olderThan = olderThan;
	}
	
	public Date getNewerThan() {
		return newerThan;
	}
	public void setNewerThan(Date newerThan) {
		this.newerThan = newerThan;
	}

	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
