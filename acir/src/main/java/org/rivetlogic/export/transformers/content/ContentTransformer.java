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
package org.rivetlogic.export.transformers.content;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.rivetlogic.export.transformers.Transformer;
import org.rivetlogic.export.transformers.content.exceptions.ContentTransformationException;

/**
 * @author Sumer Jabri
 * @author Sandra O'Keeffe
 */
public interface ContentTransformer extends Transformer {
	
	/**
	 * Transforms the content, and updates the temporary file with the transformation
	 * 
	 * @param file
	 * @param finalPath
	 * @param dependencies
	 * @throws ContentTransformationException
	 */
	public void transformContent(File file, String finalPath, Map<String, String> dependencies) throws ContentTransformationException;
	
	/* Returns a list of Alfresco PATHs to the dependencies in Alfresco */
	
	/**
	 * Determines a list of paths to dependencies
	 * 
	 * @param name
	 * @return
	 */
	@Deprecated
	public List<String> determineDependenciesByPath(String path)  throws TransformerException;
	
	/**
	 * Determines a list of paths to dependencies
	 * 
	 * @param path
	 * @param fileLocation
	 * @return
	 */
	public List<String> determineDependencies(String path, File fileLocation)  throws TransformerException;
}
