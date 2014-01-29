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
package org.rivetlogic.common.components;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.Callable;
import org.mule.message.ExceptionMessage;

public class ErrorFilter implements Callable {
	
	private Logger logger = Logger.getLogger(ErrorFilter.class);;
	
	private static int maxMapSize = 100;
	private static Long minExeptionInterval = 1 * 60 * 60 * 1000L; // 1 hour.

	private static Hashtable<String, Long> exceptionTimestampMap = null;

	public Object onCall(MuleEventContext eventContext) throws Exception {
		initialize();

		Object o = eventContext.getMessage().getPayload();
		String key = null;
		Throwable e = null;
		
		if (o instanceof ExceptionMessage) {
			ExceptionMessage em = (ExceptionMessage) o;
			e = em.getException().getCause();
			
			key = e.getMessage();
		} else {
			key = ((Exception)o).getMessage();
			e = (Exception)o;
		}
		
		Long lastTimestamp = null;
		Long now = System.currentTimeMillis();

		if (key != null) {
			lastTimestamp = exceptionTimestampMap.get(key);
			exceptionTimestampMap.put(key, now);
		}
		
		if (lastTimestamp == null || ((lastTimestamp - now) > minExeptionInterval)) {
			try {
				eventContext.dispatchEvent(e);
			} catch (MuleException ex) {
				logger.error(ex);
				throw ex;
			}
		}

		cleanUp();
		return null;
	}
	
	private void initialize() {
		if (exceptionTimestampMap == null) {
			synchronized(minExeptionInterval) {
				if (exceptionTimestampMap == null) {
					exceptionTimestampMap = new Hashtable<String, Long>(maxMapSize + (int) (maxMapSize * 0.1));
				}
			}
		}
	}

	private void cleanUp() {
		if (exceptionTimestampMap.size() > maxMapSize) {
			Enumeration<String> keys = exceptionTimestampMap.keys();
			long now = System.currentTimeMillis();

			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				Long lastOccurTimestamp = exceptionTimestampMap.get(key);

				if (lastOccurTimestamp != null && ((lastOccurTimestamp - now) > minExeptionInterval)) {
					exceptionTimestampMap.remove(key);
				}
			}
		}
	}

	public int getMaxMapSize() {
		return maxMapSize;
	}

	public void setMaxMapSize(int _maxMapSize) {
		maxMapSize = _maxMapSize;
	}

	public long getMinExeptionInterval() {
		return minExeptionInterval;
	}

	public void setMinExeptionInterval(long _minExeptionInterval) {
		minExeptionInterval = _minExeptionInterval;
	}

}
