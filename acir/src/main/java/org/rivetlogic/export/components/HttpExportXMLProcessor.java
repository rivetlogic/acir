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
package org.rivetlogic.export.components;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.transformer.TransformerException;

/**
 * 
 * @author Sandra O'Keeffe
 */
public class HttpExportXMLProcessor extends ExportXMLProcessor {

	protected InputStream getInputStream(MuleEventContext eventContext) throws TransformerException {
		Map parameters  = (Map) eventContext.transformMessage();
	
		logger.debug("Extract post query:" + (String)parameters.get("query"));
		return new StringBufferInputStream((String)parameters.get("query"));
	}
}
