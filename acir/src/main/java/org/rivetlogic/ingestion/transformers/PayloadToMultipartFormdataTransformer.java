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
package org.rivetlogic.ingestion.transformers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageAwareTransformer;
import org.rivetlogic.utils.MultipartFormdataUtil;

/**
 * This class is responsible for converting a message payload to a multipart/form-data type for a
 * a HTTP post.
 * 
 * The filename, contentType and fileFieldname are required parameters.  Additional attributes may be 
 * set in the optionalAttributes field, and these will be added as additional form data fields. 
 *  
 * @author Sandra O'Keeffe
 */
public class PayloadToMultipartFormdataTransformer extends AbstractMessageAwareTransformer implements MuleContextAware {

	private String filename = null;
	private String contentType = null;
	private String fileFieldname = null;
	private Map<String, String> optionalAttributes = null;
	private MuleContext context = null;

	public static String newline = System.getProperty("line.separator");
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	public Object transform(MuleMessage message, String encoding)
			throws TransformerException {
		
		StringBuilder multipartPayload = new StringBuilder();
		
		String boundary = MultipartFormdataUtil.createBoundary();
		
		logger.debug("Building multipart formdata for filename=" + filename + ", contentType=" + contentType + ".");
		
		MultipartFormdataUtil.writeFile(multipartPayload, boundary, evaluateExpression(fileFieldname, message), evaluateExpression(filename, message), evaluateExpression(contentType, message), (String) message.getPayload(), evaluateExpression(optionalAttributes, message));
		
		logger.debug("Post data for filename=" + filename + ":" + newline +  multipartPayload.toString());
		
		message.setProperty("formdata-boundary", boundary);
		
		message.setPayload(multipartPayload.toString());
		
		return multipartPayload.toString();
	}
	
	public Map<String, String> evaluateExpression(Map<String, String> map, MuleMessage message) {
		Map<String, String> evaluatedMap = new HashMap<String, String>();
		if (map != null) {
			Iterator<Entry<String, String>> iter = map.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<String, String> currentEntry = iter.next();
				evaluatedMap.put(currentEntry.getKey(), evaluateExpression(currentEntry.getValue(), message));
			}
		}
		return evaluatedMap;
	}
 	
	public String evaluateExpression(String property, MuleMessage message) {
		return (String) context.getExpressionManager().evaluate(property, message);
	}
	
	public String getContentType() {
		return contentType;
	}


	public void setContentType(String contentType) {
		this.contentType = contentType
;
	}


	public String getFilename() {
		return filename;
	}


	public void setFilename(String filename) {
		this.filename = filename;
	}


	public String getFileFieldname() {
		return fileFieldname;
	}


	public void setFileFieldname(String fileFieldname) {
		this.fileFieldname = fileFieldname;
	}
	

	public Map<String, String> getOptionalAttributes() {
		return optionalAttributes;
	}

	public void setOptionalAttributes(Map<String, String> optionalAttributes) {
		this.optionalAttributes = optionalAttributes;
	}

	public void setMuleContext(MuleContext context) {
		this.context = context;
		
	}

}
