package org.rivetlogic.export.dependencyresolvers;

import java.util.List;

import org.rivetlogic.export.components.data.RetrievedNode;
import org.rivetlogic.export.dependencyresolvers.exceptions.DependencyResolverException;

import com.rivetlogic.core.cma.exception.CmaRuntimeException;
import com.rivetlogic.core.cma.exception.InvalidTicketException;

/**
 * Implementations of interface can be used to resolve dependencies for a 
 * particular list of nodes.
 * 
 * Implementers will also download the resolved dependencies
 * The reason for this is if the content retrieval mechanism is different from the default (RAAr implementation)
 * 
 * next() is called to retrieve resultSetSize of dependencies
 * 
 * @author Sandra O'Keeffe
 */
public interface DependencyResolverAndRetriever extends DependencyResolver {

		/**
		 * Resolves and retrieves the dependencies for nodes set in the object.  Return up to resultSetSize results.
		 * This method is overridden when dependencies are resolved not using RAAr
		 * 
	     * @return
		 * 	list of dependencies
		 * 
		 * @throws InvalidTicketException
		 * @throws CmaRuntimeException
		 */
		public List<RetrievedNode> next() throws DependencyResolverException;
		
}
