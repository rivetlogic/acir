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
import org.rivetlogic.export.deployers.Undeployer;
import org.rivetlogic.utils.ObjectLRUCache;

public class UndeployerFactory {
	private final String ID = "id";

	private Logger log = Logger.getLogger(this.getClass());
	private static Map<String, Undeployer> undeployerLRUCache = Collections.synchronizedMap(new ObjectLRUCache<String, Undeployer>());

	private ObjectFactory objectFactory = new ObjectFactory();

	public Undeployer generateUndeployer(DOMElement undeployerDefinitionElem, String idPrefix) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {

		String id = idPrefix + undeployerDefinitionElem.attributeValue(ID);

		Undeployer undeployer = getUndeployer(id);

		if (undeployer == null) {
			undeployer = (Undeployer) objectFactory.getObject(undeployerDefinitionElem);
			undeployer.setId(id);
			
			synchronized (this) {
				Undeployer tr = getUndeployer(id);
				if (tr == null) {
					undeployerLRUCache.put(id, undeployer);
				}
			}
		}

		return (Undeployer) undeployer;
	}

	public List<Undeployer> generateUndeployers(DOMElement undeployerDefinitionsElem, String idPrefix) throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {
		List<Undeployer> contentUndeployersList = null;

		if (undeployerDefinitionsElem != null) {
			List<DOMElement> contentUndeployerDefinitionElems = undeployerDefinitionsElem.elements();
			if (contentUndeployerDefinitionElems != null && contentUndeployerDefinitionElems.size() > 0) {
				contentUndeployersList = new ArrayList<Undeployer>();

				for (DOMElement contentUndeployerDefinitionElem : contentUndeployerDefinitionElems) {
					Undeployer cTrans = (Undeployer) generateUndeployer(contentUndeployerDefinitionElem, idPrefix);

					if (cTrans != null) {
						contentUndeployersList.add(cTrans);
					}
				}
			}
		}

		return contentUndeployersList;
	}

	public Undeployer getUndeployer(String id) {
		if (undeployerLRUCache.containsKey(id)) {
			return undeployerLRUCache.get(id);
		}

		return null;
	}

}
