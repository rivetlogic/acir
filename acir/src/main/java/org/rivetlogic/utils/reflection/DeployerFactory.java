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

import org.rivetlogic.export.deployers.Deployer;
import org.rivetlogic.utils.ObjectLRUCache;

public class DeployerFactory {
	private final String ID = "id";

	private Logger log = Logger.getLogger(this.getClass());
	private static Map<String, Deployer> deployerLRUCache = Collections.synchronizedMap(new ObjectLRUCache<String, Deployer>());

	private ObjectFactory objectFactory = new ObjectFactory();

	public Deployer generateDeployer(DOMElement deployerDefinitionElem, String idPrefix) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {

		String id = idPrefix + deployerDefinitionElem.attributeValue(ID);

		Deployer deployer = getDeployer(id);

		if (deployer == null) {
			deployer = (Deployer) objectFactory.getObject(deployerDefinitionElem);
			deployer.setId(id);
			
			synchronized (this) {
				Deployer tr = getDeployer(id);
				if (tr == null) {
					deployerLRUCache.put(id, deployer);
				}
			}
		}

		return (Deployer) deployer;
	}

	public List<Deployer> generateDeployers(DOMElement deployerDefinitionsElem, String idPrefix) throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {
		List<Deployer> contentDeployersList = null;

		if (deployerDefinitionsElem != null) {
			List<DOMElement> contentDeployerDefinitionElems = deployerDefinitionsElem.elements();
			if (contentDeployerDefinitionElems != null && contentDeployerDefinitionElems.size() > 0) {
				contentDeployersList = new ArrayList<Deployer>();

				for (DOMElement contentDeployerDefinitionElem : contentDeployerDefinitionElems) {
					Deployer cTrans = (Deployer) generateDeployer(contentDeployerDefinitionElem, idPrefix);

					if (cTrans != null) {
						contentDeployersList.add(cTrans);
					}
				}
			}
		}

		return contentDeployersList;
	}

	public Deployer getDeployer(String id) {
		if (deployerLRUCache.containsKey(id)) {
			return deployerLRUCache.get(id);
		}

		return null;
	}

}
