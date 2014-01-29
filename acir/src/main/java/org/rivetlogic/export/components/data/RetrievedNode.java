package org.rivetlogic.export.components.data;

import java.io.File;

import com.rivetlogic.core.cma.repo.Node;

/**
 * Container class for Node and the tmp output location for its content
 * 
 * @author Sandra O'Keeffe
 */
public class RetrievedNode {

	public RetrievedNode(File file) {
		this.outputFile = file;
	}
	/* composition becuase inheritance won't work in this case */
	private Node node;
	
	private File outputFile;

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
	
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}	
	
}
