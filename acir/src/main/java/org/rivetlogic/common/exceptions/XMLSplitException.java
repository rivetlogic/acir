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
package org.rivetlogic.common.exceptions;

import org.dom4j.dom.DOMElement;

public class XMLSplitException extends Exception {
	private DOMElement element;

	public XMLSplitException(String msg, Throwable cause, DOMElement document) {
		super(msg, cause);
		this.element = document;
	}

	public DOMElement getElement() {
		return element;
	}

	public void setElement(DOMElement document) {
		this.element = document;
	}
	
}
