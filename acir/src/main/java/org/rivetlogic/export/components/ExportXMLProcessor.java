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

import com.rivetlogic.core.cma.repo.Node;

/**
 * @author Sumer Jabri
 * @author Sandra O'Keeffe
 */
public class ExportXMLProcessor extends AbstractXMLProcessor {

	@Override
	public void dispatchNode(ExtractRequestDispatcher dispatcher, Node node, XMLExportTO xmlExportTO) throws Exception {		
		dispatcher.buildAndDispatchNode(node, xmlExportTO);			
	}
	

}
