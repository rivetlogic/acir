/*
 * Copyright (C) 2007-2008 Rivet Logic Corporation.
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
package org.rivetlogic.export.transformers.filename;

import org.apache.log4j.Logger;
import org.rivetlogic.export.transformers.filename.exceptions.UrlTransformationException;


public class ReplaceAllTransformer implements UrlTransformer {
	private static Logger logger = Logger.getLogger(ReplaceAllTransformer.class);
	private String targetRegex;
	private String replacement;
	private String id = null;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id; 
	}
	
	public void setTargetRegex(String targetRegex) {
		this.targetRegex = targetRegex;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	public String transformUrl(String url) throws UrlTransformationException {
		try {
			String result = url.replaceAll(targetRegex, replacement);
	
			if (logger.isDebugEnabled())
				logger.debug("Transformation in:  " + url + "\nTransformation out: " + result);
			
			return result;
		
		} catch (Exception e) {
			logger.error("Transformer: " + id + " failed.", e);
			throw new UrlTransformationException(id, e);
		}
	}

}
