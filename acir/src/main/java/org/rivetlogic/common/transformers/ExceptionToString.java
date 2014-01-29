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
package org.rivetlogic.common.transformers;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;

public class ExceptionToString extends AbstractTransformer {
	@Override
	protected Object doTransform(Object src, String encoding) throws TransformerException {
		if (src instanceof Exception) {
			StringWriter w = new StringWriter();
			PrintWriter pw = new PrintWriter(w, true);
			((Exception) src).printStackTrace(pw);
			
			return w.toString();
		} else {
			return src;
		}
	}
}
