/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.el.node;

import java.io.*;

import javax.servlet.jsp.el.*;

/**
 *
 *
 *
 * @author jal
 * Created on May 17, 2005
 */
abstract public class NdBase {
	public String getNodeName() {
		String s = getClass().getName();
		return s.substring(s.lastIndexOf('.') + 3);
	}

	public void dump(IndentWriter w) throws IOException {
		w.println(getNodeName());
	}

	final public void dump() {
		OutputStreamWriter w = new OutputStreamWriter(System.out);
		IndentWriter iw = new IndentWriter(w, true);
		try {
			dump(iw);
		} catch(IOException x) {
			x.printStackTrace();
		} finally {
			try {
				w.flush();
			} catch(Exception x) {}
		}
	}

	abstract public Object evaluate(VariableResolver vr) throws ELException;

	//	{
	//		throw new IllegalStateException("No expression evaluator for "+getClass().getName());
	//	}

	abstract public void getExpression(Appendable a) throws IOException;

	final public String getExpression() {
		try {
			StringBuilder sb = new StringBuilder();
			getExpression(sb);
			return sb.toString();
		} catch(IOException x) {
			return x.toString();
		}
	}
}
