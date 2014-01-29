package org.rivetlogic.export.components;

import org.rivetlogic.export.components.data.FileExportData;

import com.rivetlogic.core.cma.repo.Node;

public class XMLProcessor extends AbstractXMLProcessor {

	@Override
	public void dispatchNode(ExtractRequestDispatcher dispatcher, Node node,
			XMLExportTO xmlExportTO) throws Exception {
		
		FileExportData fed = dispatcher.buildExportObject(node, null, xmlExportTO);
		dispatcher.dispatch(fed, xmlExportTO.getQueryElem());

	}

}
