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
package org.rivetlogic.export.dependencyresolvers;

import java.io.File;
import java.util.ArrayList;
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
 * Sample implementation of dependency resolver
 * 
 * @author Sandra O'Keeffe
 * 
 */
public class EmptyDependencyResolver implements SimplePublishDependencyResolver {

	private String id = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rivetlogic.export.dependencyresolvers.DependencyResolver#initialize
	 * (java.util.List)
	 */
	public void initialize(List<Node> nodes, SearchService searchService,
			ContentService contentService, StoreRef storeRef, Ticket ticket,
			List<SortDefinition> sortDefinitions, List<QName> properties,
			File tmpLocation, NodeService nodeService) {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rivetlogic.export.dependencyresolvers.DependencyResolver#hasNext()
	 */
	public boolean hasNext() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rivetlogic.export.dependencyresolvers.DependencyResolver#next(com
	 * .rivetlogic.core.cma.api.SearchService,
	 * org.alfresco.service.cmr.repository.StoreRef,
	 * com.rivetlogic.core.cma.repo.Ticket, java.util.List, java.util.List)
	 */
	public List<Node> next() {
		return new ArrayList<Node>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rivetlogic.export.dependencyresolvers.DependencyResolver#setResultSetSize
	 * (int)
	 */
	public void setResultSetSize(int size) {
		/* Not yet implemented */
	}

	public void setId(String id) {
		this.id = id;

	}

	public String getId() {
		return id;
	}

	

}
