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
package org.rivetlogic.export.dependencyresolvers;

import java.util.List;

import org.rivetlogic.export.dependencyresolvers.exceptions.DependencyResolverException;

import com.rivetlogic.core.cma.exception.CmaRuntimeException;
import com.rivetlogic.core.cma.exception.InvalidTicketException;
import com.rivetlogic.core.cma.repo.Node;

/**
 * Implementations of interface can be used to resolve dependencies for a 
 * particular list of nodes.
 * 	
 *  next() is called to retrieve resultSetSize of dependencies
 * 
 * @author Sandra O'Keeffe
 */
public interface SimpleUnpublishDependencyResolver extends UnpublishDependencyResolver {
	

	
	/**
	 * Resolves the dependencies for nodes set in the object.  
	 * 
     * @return
	 * 	list of dependencies
	 * 
	 * @throws InvalidTicketException
	 * @throws CmaRuntimeException
	 */
	public List<Node> next() throws DependencyResolverException;
	
}
