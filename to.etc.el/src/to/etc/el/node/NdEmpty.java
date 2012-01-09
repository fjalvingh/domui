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
import java.lang.reflect.*;
import java.util.*;

import javax.servlet.jsp.el.*;

public class NdEmpty extends NdUnary {
	public NdEmpty(NdBase expr) {
		super(expr);
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		a.append("empty (");
		m_expr.getExpression(a);
		a.append(")");
	}

	/**
	 * As per JSP 2.0 std, JSP2.3.7
	 * @see to.etc.el.node.NdBase#evaluate(javax.servlet.jsp.el.VariableResolver)
	 */
	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object a = m_expr.evaluate(vr);
		if(a == null)
			return Boolean.TRUE;
		if((a instanceof String) && ((String) a).length() == 0)
			return Boolean.TRUE;
		Class cla = a.getClass();
		if(cla.isArray()) {
			if(Array.getLength(a) == 0)
				return Boolean.TRUE;
		}
		if(a instanceof Map && ((Map) a).size() == 0)
			return Boolean.TRUE;
		if(a instanceof Collection && ((Collection) a).size() == 0)
			return Boolean.TRUE;
		return Boolean.FALSE;
	}
}
