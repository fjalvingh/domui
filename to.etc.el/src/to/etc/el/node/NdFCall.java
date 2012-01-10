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

import to.etc.util.*;

/**
 * Implement calling an EL function.
 *
 * <p>Created on Aug 5, 2005
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class NdFCall extends NdBase {
	private List<NdBase> m_actuals;

	private Method m_method;

	private NdQualifiedName m_qn;

	public NdFCall(NdQualifiedName qn, Method m, List<NdBase> act) {
		m_actuals = act;
		m_method = m;
		m_qn = qn;
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		m_qn.getExpression(a);
		a.append('(');
		boolean comma = false;
		for(NdBase b : m_actuals) {
			if(comma)
				a.append(',');
			comma = true;
			b.getExpression(a);
		}
		a.append('.');
	}

	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		/*
		 * Evaluate all arguments and convert them to the appropriate
		 * type. Report an exception on converter errors.
		 */
		Object[] ar = new Object[m_actuals.size()];
		Class[] par = m_method.getParameterTypes();
		for(int i = 0; i < ar.length; i++) {
			Object res = m_actuals.get(i).evaluate(vr);
			try {
				ar[i] = RuntimeConversions.convertTo(res, par[i]);
			} catch(Exception x) {
				throw new ELException("Argument " + (i + 1) + " of EL function " + m_qn.getExpression() + " cannot be converted from '" + res.getClass().getName() + "' to '" + par[i].getName() + "'");
			}
		}

		Exception x;
		try {
			return m_method.invoke(null, ar);
		} catch(InvocationTargetException itc) {
			itc.getTargetException().printStackTrace();
			if(itc.getTargetException() instanceof Exception)
				x = (Exception) itc.getTargetException();
			else if(itc.getTargetException() instanceof Error)
				throw (Error) itc.getTargetException();
			else
				x = itc;
		} catch(Exception ex) {
			x = ex;
		}
		if(x instanceof ELException)
			throw (ELException) x;
		throw new ELException("Evaluating function '" + m_method.getName() + "' caused exception " + x, x);
	}

}
