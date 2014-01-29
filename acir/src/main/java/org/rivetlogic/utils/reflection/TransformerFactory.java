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

import org.rivetlogic.export.transformers.Transformer;
import org.rivetlogic.export.transformers.content.ContentTransformer;
import org.rivetlogic.export.transformers.filename.UrlTransformer;
import org.rivetlogic.utils.ObjectLRUCache;

public class TransformerFactory {
	private final String ID = "id";

	private Logger log = Logger.getLogger(this.getClass());
	private static Map<String,Transformer> transformerLRUCache = Collections.synchronizedMap(new ObjectLRUCache<String, Transformer>());
	
	private ObjectFactory objectFactory = new ObjectFactory();

	public Transformer generateTransformer(DOMElement transformerDefinitionElem, String idPrefix) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {

		String id = idPrefix + transformerDefinitionElem.attributeValue(ID);
		
		Transformer transformer = getTransformer(id);
		
		if (transformer == null) {
		
			transformer = (Transformer)objectFactory.getObject(transformerDefinitionElem);
			transformer.setId(id);
				
			synchronized (this) {
				Transformer tr = getTransformer(id);
				if (tr == null) {
					transformerLRUCache.put(id, transformer);
				}
			}
		}
		
		return (Transformer) transformer;
	}

	public List<ContentTransformer> generateContentTransformers(DOMElement contentTransformerDefinitionsElem, String idPrefix) throws IllegalArgumentException,
			InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {
		List<ContentTransformer> contentTransformersList = null;

		if (contentTransformerDefinitionsElem != null) {
			List<DOMElement> contentTransformerDefinitionElems = contentTransformerDefinitionsElem.elements();
			if (contentTransformerDefinitionElems != null && contentTransformerDefinitionElems.size() > 0) {
				contentTransformersList = new ArrayList<ContentTransformer>();

				for (DOMElement contentTransformerDefinitionElem : contentTransformerDefinitionElems) {
					ContentTransformer cTrans = (ContentTransformer) generateTransformer(contentTransformerDefinitionElem, idPrefix);

					if (cTrans != null) {
						contentTransformersList.add(cTrans);
					}
				}
			}
		}

		return contentTransformersList;
	}
	

	public List<UrlTransformer> generateFileNameTransformers(DOMElement fileNameTransformerDefinitionsElem, String idPrefix) throws IllegalArgumentException,
			InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, ParseException, SecurityException, NoSuchMethodException {
		List<UrlTransformer> fileNameTransformersList = null;

		if (fileNameTransformerDefinitionsElem != null) {
			List<DOMElement> fileNameTransformerDefinitionElems = fileNameTransformerDefinitionsElem.elements();
			if (fileNameTransformerDefinitionElems != null && fileNameTransformerDefinitionElems.size() > 0) {
				fileNameTransformersList = new ArrayList<UrlTransformer>();

				for (DOMElement fileNameTransformerDefinitionElem : fileNameTransformerDefinitionElems) {
					UrlTransformer cTrans = (UrlTransformer) generateTransformer(fileNameTransformerDefinitionElem, idPrefix);

					if (cTrans != null) {
						fileNameTransformersList.add(cTrans);
					}
				}
			}
		}

		return fileNameTransformersList;
	}

	
	public Transformer getTransformer(String id) {
		if (transformerLRUCache.containsKey(id)) {
			return transformerLRUCache.get(id);
		}
		
		return null;
	}
}
