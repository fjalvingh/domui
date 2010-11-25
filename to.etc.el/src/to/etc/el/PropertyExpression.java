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
import to.etc.util.*;

/**
 * This encapsulates a "property" expression. This is a possibly
 * complex property reference based off some unknown object. To
 * resolve the reference one needs to pass in the object where
 * the root of the property can be resolved on.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 16, 2006
 */
public class PropertyExpression {
	static private class TmpResolver implements VariableResolver {
		private Object m_root;

		TmpResolver(Object root) {
			m_root = root;
		}

		public Object resolveVariable(String name) throws ELException {
			if(name == null)
				return m_root;
			throw new ELException("Variables not allowed in a property expression.");
		}
	};

	private NdLookup m_expr;

	PropertyExpression(NdLookup bo) {
		m_expr = bo;
	}

	public String getProperty() {
		return m_expr.getExpression();
	}

	/**
	 * Must return true if the value is read-only.
	 * @return
	 */
	public boolean isReadOnly(Object root) throws ELException {
		return m_expr.isReadOnly(new TmpResolver(root));
	}

	/**
	 * The type of the expression.
	 * @param vr		The resolver to use when evaluating variables
	 * @return
	 */
	public Class getType(Object root) throws ELException {
		return m_expr.getType(new TmpResolver(root));
	}

	/**
	 * Must return the value of the bound expression.
	 * @param vr		The resolver to use when evaluating variables
	 * @return
	 */
	public Object getValue(Object root, Class< ? > target) throws Exception {
		Object o = m_expr.evaluate(new TmpResolver(root));
		if(o == null || target == null)
			return o;
		return RuntimeConversions.convertTo(o, target);
	}

	/**
	 * Must set the bound expression's target to a value. If the thing is
	 * not writeable this throws an exception.
	 * @param vr		The resolver to use when evaluating variables
	 * @param value
	 * @throws Exception
	 */
	public void setValue(Object root, Object value) throws Exception {
		m_expr.setValue(new TmpResolver(root), value);
	}
}
