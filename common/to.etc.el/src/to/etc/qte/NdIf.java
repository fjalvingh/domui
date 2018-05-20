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
package to.etc.qte;

import javax.servlet.jsp.el.*;

public class NdIf extends NdBase {
	private Expression m_expr;

	private NdList m_if;

	private NdList m_else;

	public NdIf(Expression x, NdList theif, NdList theelse) {
		m_expr = x;
		m_if = theif;
		m_else = theelse;
	}

	public NdIf(Expression x) {
		m_expr = x;
	}

	public final void setElse(NdList else1) {
		m_else = else1;
	}

	public final void setIf(NdList if1) {
		m_if = if1;
	}

	public void generate(Appendable a, VariableResolver vr) throws Exception {
		Boolean b = (Boolean) m_expr.evaluate(vr);
		if(b.booleanValue())
			m_if.generate(a, vr);
		else
			m_else.generate(a, vr);
	}
}
