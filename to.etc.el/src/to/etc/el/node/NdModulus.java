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

import java.math.*;

import javax.servlet.jsp.el.*;

import to.etc.el.*;
import to.etc.util.*;

public class NdModulus extends NdBinary {
	/**
	 * @param a
	 * @param b
	 */
	public NdModulus(NdBase a, NdBase b) {
		super(a, b);
	}

	/**
	 * See jsp 2.0, JSP2.3.5.3
	 * @see to.etc.el.node.NdBase#evaluate(javax.servlet.jsp.el.VariableResolver, javax.servlet.jsp.el.FunctionMapper)
	 */
	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object a = m_a.evaluate(vr);
		Object b = m_b.evaluate(vr);

		if(a == null && b == null) // If a and b both null return Long(0)
			return WrapperCache.getLong(0);
		Class cla = a == null ? null : a.getClass();
		Class clb = b == null ? null : b.getClass();

		/**
		 * if a or b is bigdecimal, float, double or string looking like # convert
		 * to double and apply.
		 */
		if(isFloaty(cla) || isFloaty(clb) || ElUtil.isFloatingPointString(a) || ElUtil.isFloatingPointString(b))
			WrapperCache.getDouble(ElUtil.coerceToDouble(a) % ElUtil.coerceToDouble(b));

		/*
		 * If either is BigInteger coerce both to that
		 */
		if(cla == BigInteger.class)
			return ((BigInteger) a).remainder(ElUtil.coerceToBigInteger(b));
		if(clb == BigInteger.class)
			return ElUtil.coerceToBigInteger(a).remainder((BigInteger) b);

		return WrapperCache.getLong(ElUtil.coerceToLong(a) % ElUtil.coerceToLong(b));
	}

	static private boolean isFloaty(Class cl) {
		return cl == BigDecimal.class || cl == Double.class || cl == Float.class;
	}

	@Override
	protected String getOperator() {
		return "%";
	}

}
