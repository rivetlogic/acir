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
package org.rivetlogic.export.transformers.filename.exceptions;

/**
 * @author Sumer Jabri
 *
 */
public class UrlTransformationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1987858998547665933L;
	private String id = null;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id; 
	}

	public UrlTransformationException() {
		super();
	}

	public UrlTransformationException(String id) {
		super();
		this.id = id;
	}

	public UrlTransformationException(String id, String message, Throwable cause) {
		super(message, cause);
		this.id = id;
	}

	public UrlTransformationException(String id, String message) {
		super(message);
		this.id = id;
	}

	public UrlTransformationException(String id, Throwable cause) {
		super(cause);
		this.id = id;
	}
}
