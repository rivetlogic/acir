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
package org.rivetlogic.utils.reflection;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dom4j.dom.DOMElement;
import org.rivetlogic.export.dependencyresolvers.DependencyResolver;
import org.rivetlogic.utils.ObjectLRUCache;

public class DependencyResolverFactory {

	private final String ID = "id";
	
	private static Map<String, DependencyResolver> depResolverLRUCache = Collections.synchronizedMap(new ObjectLRUCache<String, DependencyResolver>());

	private ObjectFactory objectFactory = new ObjectFactory();

	public DependencyResolver generateDependencyResolver(DOMElement depResolverDefinitionElem, String idPrefix) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {

		String id = idPrefix + depResolverDefinitionElem.attributeValue(ID);

		DependencyResolver dependencyResolver = getDependencyResolver(id);

		if (dependencyResolver == null) {
			dependencyResolver = (DependencyResolver) objectFactory.getObject(depResolverDefinitionElem);
			dependencyResolver.setId(id);
			
			synchronized (this) {
				DependencyResolver tr = getDependencyResolver(id);
				if (tr == null) {
					depResolverLRUCache.put(id, dependencyResolver);
				}
			}
		}

		return (DependencyResolver) dependencyResolver;
	}

	public List<DependencyResolver> generateDependencyResolvers(DOMElement depResolverDefinitionsElem, String idPrefix) throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {
		List<DependencyResolver> dependencyResolversList = null;

		if (depResolverDefinitionsElem != null) {
			List<DOMElement> depResolverDefinitionElems = depResolverDefinitionsElem.elements();
			if (depResolverDefinitionElems != null && depResolverDefinitionElems.size() > 0) {
				dependencyResolversList = new ArrayList<DependencyResolver>();

				for (DOMElement depResolverDefinitionElem : depResolverDefinitionElems) {
					DependencyResolver cTrans = (DependencyResolver) generateDependencyResolver(depResolverDefinitionElem, idPrefix);

					if (cTrans != null) {
						dependencyResolversList.add(cTrans);
					}
				}
			}
		}

		return dependencyResolversList;
	}

	public DependencyResolver getDependencyResolver(String id) {
		if (depResolverLRUCache.containsKey(id)) {
			return depResolverLRUCache.get(id);
		}

		return null;
	}
}
