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

import java.util.LinkedHashMap;

public class ObjectLRUCache<T,O> extends LinkedHashMap<T,O> {
	private static final long serialVersionUID = -5915089566299608040L;
	
	private int maxSize = 100;

	public ObjectLRUCache() {
		super( 10, 0.75f, true);
	}

	public ObjectLRUCache(int maxSize, float loadFactor) {
		super((int)(maxSize*((float)1-loadFactor)), loadFactor, true);
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<T,O> eldest) {
		return size() > maxSize;
	}
}
