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
package to.etc.el;

import javax.servlet.jsp.el.*;

import to.etc.el.node.*;

public class ElExpressionImpl extends Expression {
	/** The compiled form of the expression. */
	private NdBase m_node;

	private String m_expr;

	ElExpressionImpl(NdBase root, String expr) {
		m_node = root;
		m_expr = expr;
	}

	@Override
	public Object evaluate(VariableResolver arg0) throws ELException {
		try {
			return m_node.evaluate(arg0);
		} catch(EtcELException x) {
			x.setExpression(m_expr);
			throw x;
		} catch(ELException x) {
			throw x;
		} catch(RuntimeException x) {
			x.printStackTrace();
			throw new ELException("The EL expression '" + m_expr + "' caused " + x, x);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	ValueBinding interface.								*/
	/*--------------------------------------------------------------*/
	public String getExpression() {
		return m_expr;
	}

	@Override
	public String toString() {
		return m_expr;
	}

	public NdBase getNode() {
		return m_node;
	}

	public MethodInvocator getInvocator(VariableResolver vr) throws ELException {
		if(!(m_node instanceof NdLookup))
			throw new ELException("The expression " + m_expr + " does not resolve as a method call.");

		//-- 2. We need to evaluate the base expression to find out.
		NdLookup l = (NdLookup) m_node;
		return l.getMethodInvocator(vr);
	}
}
