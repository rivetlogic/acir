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
package org.rivetlogic.export.components.data;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.xml.sax.SAXException;

public interface Reporter {
	public enum ReporterState {NOT_STARTED, STARTED, COMPLETED};

	public abstract void startReport() throws SAXException, IOException;

	public abstract void endReport() throws SAXException, IOException;
	
	public abstract File getReportXmlFile();

	public abstract Date getLastModified();

	public abstract int getNumResults();
	
	public abstract void setNumResults(int numResults) throws SAXException, IOException;

	public abstract int getNumResultsProcessed();

	public abstract String getId();
	
	public abstract long getMaxIdleTime();

	public abstract ReporterState getState();
	
	public abstract void tick();
}