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
package org.rivetlogic.utils;

import java.text.SimpleDateFormat;

import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;

public class IntegrationConstants {
    public static boolean DEBUG = true;
    public static String TMP_FOLDER = "/tmp/integration/";
	
	public static final String XML_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final SimpleDateFormat xmlDateTimeFormat = new SimpleDateFormat(XML_DATETIME_FORMAT);
	
	public static final DynamicNamespacePrefixResolver NAMESPACE_PREFIX_RESOLVER = new DynamicNamespacePrefixResolver();
	static {
		initNameSpacePrefixResolver();
	}	
	
	private static void initNameSpacePrefixResolver () {
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.WCMWF_MODEL, NamespaceService.WCMWF_MODEL_1_0_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.APP_MODEL_PREFIX, NamespaceService.APP_MODEL_1_0_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.BPM_MODEL_PREFIX, NamespaceService.BPM_MODEL_1_0_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.WCM_MODEL_PREFIX, NamespaceService.WCM_MODEL_1_0_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.WCMAPP_MODEL_PREFIX, NamespaceService.WCMAPP_MODEL_1_0_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.SYSTEM_MODEL_PREFIX, NamespaceService.SYSTEM_MODEL_1_0_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.FORUMS_MODEL_PREFIX, NamespaceService.FORUMS_MODEL_1_0_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace("sites", "http://www.alfresco.org/model/site/1.0");
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.SECURITY_MODEL_PREFIX, NamespaceService.SECURITY_MODEL_1_0_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.WORKFLOW_MODEL_PREFIX, NamespaceService.WORKFLOW_MODEL_1_0_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.REPOSITORY_VIEW_PREFIX, NamespaceService.REPOSITORY_VIEW_1_0_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.DICTIONARY_MODEL_PREFIX, NamespaceService.DICTIONARY_MODEL_1_0_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService.EMAILSERVER_MODEL_PREFIX, NamespaceService.EMAILSERVER_MODEL_URI);
		NAMESPACE_PREFIX_RESOLVER.registerNamespace("usr", "http://www.alfresco.org/model/user/1.0");
		NAMESPACE_PREFIX_RESOLVER.registerNamespace("rule", "http://www.alfresco.org/model/rule/1.0");
		NAMESPACE_PREFIX_RESOLVER.registerNamespace("act", "http://www.alfresco.org/model/action/1.0");
		NAMESPACE_PREFIX_RESOLVER.registerNamespace("ver", "http://www.alfresco.org/model/versionstore/1.0");
		
		
		
//		NAMESPACE_PREFIX_RESOLVER.registerNamespace(NamespaceService., NamespaceService.);
		
	}
}
