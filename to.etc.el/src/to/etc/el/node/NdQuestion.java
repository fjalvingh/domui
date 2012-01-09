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

import to.etc.util.*;

public class NdQuestion extends NdBase {
	private NdBase m_cond;

	private NdBase m_ifpart;

	private NdBase m_elsepart;

	/**
	 * @param a
	 * @param b
	 */
	public NdQuestion(NdBase a, NdBase b) {
		m_cond = a;
		m_ifpart = b;
	}

	public void setElse(NdBase e) {
		m_elsepart = e;
	}

	/**
	 * See jsp 2.0, JSP2.3.5.2
	 * @see to.etc.el.node.NdBase#evaluate(javax.servlet.jsp.el.VariableResolver, javax.servlet.jsp.el.FunctionMapper)
	 */
	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object cond = m_cond.evaluate(vr);
		boolean va = RuntimeConversions.convertToBool(cond);
		if(va)
			return m_ifpart.evaluate(vr);
		else
			return m_elsepart.evaluate(vr);
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		m_cond.getExpression(a);
		a.append('?');
		m_ifpart.getExpression(a);
		a.append(':');
		m_elsepart.getExpression(a);
	}
}
