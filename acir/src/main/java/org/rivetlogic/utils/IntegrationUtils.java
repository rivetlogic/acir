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

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.util.ISO9075;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class IntegrationUtils {
	public static String MD5(String str) throws NoSuchAlgorithmException {
	    MessageDigest digest = MessageDigest.getInstance("MD5");
		
		byte[] dg = digest.digest(str.getBytes());
		
		BigInteger number = new BigInteger(1, dg);
	    
	    return number.toString(16);
	}
	
	public static BigInteger MD5BigInteger(String str) throws NoSuchAlgorithmException {
	    MessageDigest digest = MessageDigest.getInstance("MD5");
		
		byte[] dg = digest.digest(str.getBytes());
		
		BigInteger number = new BigInteger(1, dg);
	    
	    return number;
	}
	
	public static Boolean parseBoolean(String pBoolStr) {
		String[] vTrueStrs = new String[]{"1", "t", "y", "true", "yes"};
		
		Boolean rResult = false;
		
		for (String vTStr:vTrueStrs) {
			if (vTStr.equalsIgnoreCase(pBoolStr)) {
				rResult = true;
				break;
			}
		}
		
		return rResult;
	}
	
	public static BeanFactory getBeanFactory() throws IOException {
		if (beanFactory == null) {
			synchronized(beanFactoryLockObj) {
				if (beanFactory == null) {
					PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
					AbstractApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");
		
					Resource resource = new FileSystemResource("cma-cfg.properties");
					Properties properties = new Properties();
					properties.load(resource.getInputStream());
		
					configurer.setProperties(properties);
		
					context.addBeanFactoryPostProcessor(configurer);
					context.refresh();
					
					beanFactory = context.getBeanFactory();
				}
			}
		}
		
		return beanFactory;
	}
	
	public static String convertPath(Path alfPath) {
		StringBuilder pathBuilder = new StringBuilder(100);
		if (alfPath != null) {
			for (Element pElem : alfPath) {
				String pStr = pElem.getPrefixedString(IntegrationConstants.NAMESPACE_PREFIX_RESOLVER);
				if (!pStr.startsWith("/")) {
					for (String prefix : IntegrationConstants.NAMESPACE_PREFIX_RESOLVER.getPrefixes()) {
						if (pStr.startsWith(prefix)) {
							pStr = ISO9075.decode(pStr.substring(prefix.length() + 1));
							break;
						}
					}
					pathBuilder.append(File.separatorChar);
					pathBuilder.append(pStr);
				}
			}
		}
		
		return pathBuilder.toString();
	}
	
	public static String decodePath(Path alfPath) {
		StringBuilder pathBuilder = new StringBuilder(100);
		if (alfPath != null) {
			for (Element pElem : alfPath) {
				String pStr = pElem.getPrefixedString(IntegrationConstants.NAMESPACE_PREFIX_RESOLVER);
				if (!pStr.startsWith("/")) {
					for (String prefix : IntegrationConstants.NAMESPACE_PREFIX_RESOLVER.getPrefixes()) {
						if (pStr.startsWith(prefix)) {
							pStr = pStr.substring(0, prefix.length() + 1) + ISO9075.decode(pStr.substring(prefix.length() + 1));
							break;
						}
					}
					pathBuilder.append(File.separatorChar);
					pathBuilder.append(pStr);
				}
			}
		}
		
		return pathBuilder.toString();
	}
	
	public static String encodePath(String alfPath) {
		
		StringBuilder pathBuilder = new StringBuilder(100);
		StringTokenizer tokenizer = new StringTokenizer(alfPath, Character.toString(File.separatorChar));
		
		while (tokenizer.hasMoreElements())
		{
			String pStr = tokenizer.nextElement().toString();
			if (!pStr.startsWith("/")) {
				for (String prefix : IntegrationConstants.NAMESPACE_PREFIX_RESOLVER.getPrefixes()) {
					if (pStr.startsWith(prefix)) {
						pStr = pStr.substring(0, prefix.length() + 1) + ISO9075.encode(pStr.substring(prefix.length() + 1));
					}
				}
				pathBuilder.append(File.separatorChar);
				pathBuilder.append(pStr);
			}
		}
		
		return pathBuilder.toString();
	}
	
	private static BeanFactory beanFactory;
	private static Object beanFactoryLockObj = new Object();
	
	
//	public static void main(String args[]) {
//		String[] vStrs = new String[]{"a","2","1","4","0","y","f","t","false","true","yes","YeS","No","Maybe","ho","hu","nice","good","True","","T"};
//		
//		for (String vStr:vStrs) {
//			System.out.print(vStr);
//			System.out.print("\t");
//			System.out.println(parseBoolean(vStr));
//		}
//	}
}
