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

import org.apache.log4j.Logger;
import org.dom4j.dom.DOMElement;

import org.rivetlogic.export.filters.Filter;
import org.rivetlogic.utils.ObjectLRUCache;

public class FilterFactory {
	private final String ID = "id";

	private Logger log = Logger.getLogger(this.getClass());

	private static Map<String, Filter> filterLRUCache = Collections.synchronizedMap(new ObjectLRUCache<String, Filter>());

	private ObjectFactory objectFactory = new ObjectFactory();

	public Filter generateFilter(DOMElement filterDefinitionElem, String idPrefix) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {

		String id = idPrefix + filterDefinitionElem.attributeValue(ID);
		
		Filter filter = getFilter(id);
		
		if (filter == null) {
		
			filter = (Filter)objectFactory.getObject(filterDefinitionElem);
			filter.setId(id);
				
			synchronized (this) {
				Filter fil = getFilter(id);
				if (fil == null) {
					filterLRUCache.put(id, filter);
				}
			}
		}
		
		return (Filter) filter;
	}

	public List<Filter> generateFilters(DOMElement filterDefinitionsElem, String idPrefix) throws IllegalArgumentException,
			InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {
		List<Filter> filtersList = null;

		if (filterDefinitionsElem != null) {
			List<DOMElement> filterDefinitionElems = filterDefinitionsElem.elements();
			if (filterDefinitionElems != null && filterDefinitionElems.size() > 0) {
				filtersList = new ArrayList<Filter>();

				for (DOMElement filterDefinitionElem : filterDefinitionElems) {
					Filter filter = (Filter) generateFilter(filterDefinitionElem, idPrefix);

					if (filter != null) {
						filtersList.add(filter);
					}
				}
			}
		}

		return filtersList;
	}
	
	public Filter getFilter(String id) {
		if (filterLRUCache.containsKey(id)) {
			return filterLRUCache.get(id);
		}
		
		return null;
	}

}
