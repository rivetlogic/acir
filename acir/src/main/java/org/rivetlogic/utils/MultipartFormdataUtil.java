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

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

/**
 * Class containing utility methods to create formdata type post requests.
 * 
 * Note the format is very important - number of new lines etc. 
 * 
 * @author Sandra O'Keeffe
 * 
 */
public class MultipartFormdataUtil {

	private static Random random = new Random();

	protected static String randomString() {
		return Long.toString(random.nextLong(), 36);
	}

	/**
	 * Returns a randomly correctly formatted boundary.
	 * 
	 * @return
	 */
	public static String createBoundary() {
		return "---------------------------" + randomString() + randomString() + randomString();
	}

	/**
	 * Writes the file data parameter to the form data - represented by the
	 * StringBuilder
	 * 
	 * @param builder
	 * @param boundary
	 * @param name
	 * @param fileName
	 * @param contentType
	 * @param data
	 */
	public static void writeFormDataFileParameter(StringBuilder builder, String boundary, String name, String fileName,
			String contentType, String data) {

		writeBoundary(builder, boundary);

		builder.append("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"");

		writeNewLine(builder);

		builder.append("Content-Type: "
				+ (StringUtils.isNotEmpty(contentType) ? contentType : "application/octet-stream"));

		writeNewLine(builder);
		writeNewLine(builder);

		builder.append(data);

		writeNewLine(builder);
	}

	/**
	 * Writes a regular form data string/value pair to the formdata
	 * StringBuilder object passed in.
	 * 
	 * @param builder
	 * @param boundary
	 * @param name
	 * @param value
	 */
	public static void writeFormDataParameter(StringBuilder builder, String boundary, String name, String value) {
		writeBoundary(builder, boundary);

		builder.append("Content-Disposition: form-data; name=\"" + name + "\"");

		writeNewLine(builder);
		writeNewLine(builder);

		builder.append(value);

		writeNewLine(builder);
	}

	/**
	 * Writes all parameters in the maps for the formdata StringBuilder object.
	 * passed in.
	 * 
	 * @param builder
	 * @param boundary
	 * @param parameters
	 */
	public static void writeFormDataParameters(StringBuilder builder, String boundary, Map<String, String> parameters) {

		Iterator<Entry<String, String>> paramIter = parameters.entrySet().iterator();
		Entry<String, String> currentEntry;
		while (paramIter.hasNext()) {
			currentEntry = paramIter.next();

			writeFormDataParameter(builder, boundary, currentEntry.getKey(), currentEntry.getValue());
		}
	}

	private static void writeNewLine(StringBuilder builder) {
		builder.append("\r\n");
	}

	/*
	 * Writes boundary
	 * 
	 * @param builder
	 * 
	 * @param boundary
	 */
	private static void writeBoundary(StringBuilder builder, String boundary) {
		builder.append("--" + boundary);
		writeNewLine(builder);
	}

	/**
	 * Writes the end of the format data.
	 * 
	 * @param builder
	 * @param boundary
	 */
	public static void writeEndBody(StringBuilder builder, String boundary) {

		builder.append("--" + boundary + "--");
		writeNewLine(builder);
		writeNewLine(builder);

	}

	/**
	 * Writes a correctly formatted formdata file to the StringBuilder passed in.
	 * 
	 * @param builder
	 * @param boundary
	 * @param fileFieldName
	 * @param fileName
	 * @param contentType
	 * @param data
	 * @param otherParams
	 */
	public static void writeFile(StringBuilder builder, String boundary, String fileFieldName, String fileName,
			String contentType, String data, Map<String, String> otherParams) {

		writeFormDataFileParameter(builder, boundary, fileFieldName, fileName, contentType, data);

		writeFormDataParameters(builder, boundary, otherParams);

		writeEndBody(builder, boundary);
	}
}
