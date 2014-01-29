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
package org.rivetlogic.export.dependencyresolvers.exceptions;

/**
 * @author Sandra O'Keeffe
 */
public class DependencyResolverException extends Exception {
	private static final long serialVersionUID = 1987858998547665935L;
	private String id = null;
	
	public DependencyResolverException() {
		super();
	}

	public DependencyResolverException(String id) {
		super();
		this.id = id;
	}

	public DependencyResolverException(String id, String msg, Throwable throwable) {
		super(msg, throwable);
		this.id = id;
	}

	public DependencyResolverException(String id, String msg) {
		super(msg);
		this.id = id;
	}

	public DependencyResolverException(String id, Throwable throwable) {
		super(throwable);
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
