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
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.dom.DOMElement;
import org.rivetlogic.utils.IntegrationConstants;

public class ObjectFactory {
	private static final String PUT_METHOD = "put";
	private static final String MAP_KEY = "key";
	private static final String ADD_METHOD = "add";
	private static final String ENTRIES = "entries";
	private final String CLASS = "class";
	private final String PROPERTIES = "properties";
	private final String NAME = "name";

	private Logger log = Logger.getLogger(this.getClass());

	public Object getObject(DOMElement objectDefinitionElem) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ParseException, SecurityException, NoSuchMethodException {
		String objectClass = objectDefinitionElem.attributeValue(CLASS);

		Class cls = Class.forName(objectClass);
		Method[] methods = cls.getMethods();
		HashMap<String, Method> settersMap = new HashMap<String, Method>(methods.length);
		for (Method method : methods) {
			if (method.getName().startsWith("set")) {
				char[] propNameChars = method.getName().substring(3).toCharArray();
				propNameChars[0] = Character.toLowerCase(propNameChars[0]);
				String propName = new String(propNameChars);
				method.setAccessible(true);
				settersMap.put(propName, method);
			}
		}

		Object object = cls.newInstance();

		DOMElement propertiesElem = (DOMElement) objectDefinitionElem.element(PROPERTIES);
		
		if (propertiesElem != null) {
			List<DOMElement> propertyElems = propertiesElem.elements();
	
			for (DOMElement propertyElem : propertyElems) {
				String name = propertyElem.getAttribute(NAME);
				
	
				if (name != null && name.length() > 0 && settersMap.containsKey(name)) {
					Method method = settersMap.get(name);
					Class[] paramTypes = method.getParameterTypes();
					if (paramTypes.length == 1) {
						String paramTypeName = paramTypes[0].getName();
						String paramTypeNameLower = paramTypeName.toLowerCase();
						Object param = null;
	
						/* */
						String value = null;
						if (propertyElem.isTextOnly()){
							value = propertyElem.getTextTrim();
						}
							
						if (paramTypeNameLower.startsWith("int")) {
							param = Integer.valueOf(value);
						} else if (paramTypeNameLower.startsWith("short")) {
							param = Short.valueOf(value);
						} else if (paramTypeNameLower.startsWith("double")) {
							param = Double.valueOf(value);
						} else if (paramTypeNameLower.startsWith("float")) {
							param = Float.valueOf(value);
						} else if (paramTypeNameLower.startsWith("byte")) {
							param = Byte.valueOf(value);
						} else if (paramTypeNameLower.startsWith("char")) {
							param = Character.valueOf(value.charAt(0));
						} else if (paramTypeName.equals(Date.class.getName())) {
							param = IntegrationConstants.xmlDateTimeFormat.parse(value);
						} else if (List.class.isAssignableFrom(Class.forName(paramTypeName))) {
							String propertyClassName = propertyElem.getAttribute(CLASS);
							Class propertyClass = Class.forName(propertyClassName);
							param = getList(propertyClass, propertyElem);
						} else if (Map.class.isAssignableFrom(Class.forName(paramTypeName))) {
							String propertyClassName = propertyElem.getAttribute(CLASS);
							Class propertyClass = Class.forName(propertyClassName);
							param = getMap(propertyClass, propertyElem);
						} else {
							param = value;
						}
						
	
						log.debug("Setting property '" + name + "' to '" + param + "'");
						method.invoke(object, param);
					} else {
						throw new IllegalArgumentException("Method should take only one parameter. Could not set property " + name + " of class "+ objectClass);
					}
				}
			}
		}

		return object;
	}

	private Object getMap(Class<Map> mapClass, DOMElement mapElem) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, SecurityException, InvocationTargetException, ParseException, NoSuchMethodException {
		
		Object mapObj = mapClass.newInstance();
		DOMElement entriesElem = (DOMElement) mapElem.element(ENTRIES);
		
		if (entriesElem != null) {
			Method method = mapClass.getMethod(PUT_METHOD, Object.class, Object.class);
			
			List<DOMElement> entryElems = entriesElem.elements();
			for (DOMElement entryElem : entryElems) {
				String mapKey = entryElem.attributeValue(MAP_KEY);
				
				String value;
				if (entryElem.isTextOnly()){
					value = entryElem.getTextTrim();
					
					method.invoke(mapObj, mapKey, value);
					
				} else {
				
					Object mapEntry = null;
					String className = entryElem.attributeValue(CLASS);
					Class propertyClass = Class.forName(className);
					if (List.class.isAssignableFrom(propertyClass)) {
						mapEntry = getList(propertyClass, entryElem);
					} else {
						mapEntry = getObject(entryElem);
					}
					
					method.invoke(mapObj, mapKey, mapEntry);
				}
				
			}
			
		}
		
		return mapObj;
	}

	private Object getList(Class<List> listClass, DOMElement listElem) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, ParseException, SecurityException, NoSuchMethodException {

		Object listObj = listClass.newInstance();
		DOMElement entriesElem = (DOMElement) listElem.element(ENTRIES);
		
		if (entriesElem != null) {
			List<DOMElement> entryElems = entriesElem.elements();
			Method method = listClass.getMethod(ADD_METHOD, Object.class);
			
			for (DOMElement entryElem : entryElems) {
				
				String value;
				if (entryElem.isTextOnly()){
					value = entryElem.getTextTrim();
					
					method.invoke(listObj, value);
				
				} else {
				
					Object listEntry = getObject(entryElem);
					method.invoke(listObj, listEntry);
				}
			}
		}
		
		return listObj;
		
	}
}
