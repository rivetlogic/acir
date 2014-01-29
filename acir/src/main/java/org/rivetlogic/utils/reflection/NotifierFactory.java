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
import org.rivetlogic.export.notifiers.CMSNotifier;
import org.rivetlogic.utils.ObjectLRUCache;

/**
 * Responsible for creatign notifier instances
 * 
 * @author Sandra O'Keeffe
 */
public class NotifierFactory {
	private final String ID = "id";

	private Logger log = Logger.getLogger(this.getClass());
	private static Map<String, CMSNotifier> deployerLRUCache = Collections.synchronizedMap(new ObjectLRUCache<String, CMSNotifier>());

	private ObjectFactory objectFactory = new ObjectFactory();

	public CMSNotifier generateDeployer(DOMElement notifierDefinitionElem, String idPrefix) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {

		String id = idPrefix + notifierDefinitionElem.attributeValue(ID);

		CMSNotifier notifier = getNotifier(id);

		if (notifier == null) {
			notifier = (CMSNotifier) objectFactory.getObject(notifierDefinitionElem);
			notifier.setId(id);
			
			synchronized (this) {
				CMSNotifier tr = getNotifier(id);
				if (tr == null) {
					deployerLRUCache.put(id, notifier);
				}
			}
		}

		return (CMSNotifier) notifier;
	}

	public List<CMSNotifier> generateNotifiers(DOMElement notifierDefinitionsElem, String idPrefix) throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {
		List<CMSNotifier> notifiersList = null;

		if (notifierDefinitionsElem != null) {
			List<DOMElement> notifierDefinitionElems = notifierDefinitionsElem.elements();
			if (notifierDefinitionElems != null && notifierDefinitionElems.size() > 0) {
				notifiersList = new ArrayList<CMSNotifier>();

				for (DOMElement contentNotifierDefinitionElem : notifierDefinitionElems) {
					CMSNotifier cTrans = (CMSNotifier) generateDeployer(contentNotifierDefinitionElem, idPrefix);

					if (cTrans != null) {
						notifiersList.add(cTrans);
					}
				}
			}
		}

		return notifiersList;
	}

	public CMSNotifier getNotifier(String id) {
		if (deployerLRUCache.containsKey(id)) {
			return deployerLRUCache.get(id);
		}

		return null;
	}

}
