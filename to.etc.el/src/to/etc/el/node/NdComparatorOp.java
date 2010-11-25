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

abstract public class NdComparatorOp extends NdBinary {
	public NdComparatorOp(NdBase a, NdBase b) {
		super(a, b);
	}

	abstract protected boolean apply(BigDecimal a, BigDecimal b) throws ELException;

	abstract protected boolean apply(BigInteger a, BigInteger b) throws ELException;

	abstract protected boolean apply(double a, double b) throws ELException;

	abstract protected boolean apply(long a, long b) throws ELException;


	/**
	 * Handles all coerces for comparison operations as described in
	 * JSP 2.0 standard JSP2.3.5.1
	 * @see to.etc.el.node.NdBase#evaluate(javax.servlet.jsp.el.VariableResolver, javax.servlet.jsp.el.FunctionMapper)
	 */
	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object a = m_a.evaluate(vr);
		Object b = m_b.evaluate(vr);

		if(a == null && b == null) // If a and b both null return Long(0)
			return Boolean.FALSE;

		Class cla = a == null ? null : a.getClass();
		Class clb = b == null ? null : b.getClass();

		//-- If either class is BigDecimal coerce the other to BigDecimal and operate
		if(cla == BigDecimal.class)
			return Boolean.valueOf(apply((BigDecimal) a, ElUtil.coerceToBigDecimal(b)));
		if(clb == BigDecimal.class)
			return Boolean.valueOf(apply(ElUtil.coerceToBigDecimal(a), (BigDecimal) b));

		/*
		 * If either is a float, double or "String containing '.', e or E then
		 * 	if the other is BigInteger coerce both to BigInteger
		 * 	else coerce to double
		 */
		if(cla == Float.class || cla == Double.class || ElUtil.isFloatingPointString(a)) {
			if(clb == BigInteger.class) // If the other is BigInteger
				return Boolean.valueOf(apply(ElUtil.coerceToBigInteger(a), (BigInteger) b));
			return Boolean.valueOf(apply(ElUtil.coerceToDouble(a), ElUtil.coerceToDouble(b)));
		}
		if(clb == Float.class || clb == Double.class || ElUtil.isFloatingPointString(b)) {
			if(cla == BigInteger.class) // If the other is BigInteger
				return Boolean.valueOf(apply((BigInteger) a, ElUtil.coerceToBigInteger(b)));
			return Boolean.valueOf(apply(ElUtil.coerceToDouble(a), ElUtil.coerceToDouble(b)));
		}

		/*
		 * If a or b is BigInteger coerce both to BigInteger
		 */
		if(cla == BigInteger.class)
			return Boolean.valueOf(apply((BigInteger) a, ElUtil.coerceToBigInteger(b)));
		if(clb == BigInteger.class)
			return Boolean.valueOf(apply(ElUtil.coerceToBigInteger(a), (BigInteger) b));

		/*
		 * coerce both to long and handle
		 */
		return Boolean.valueOf(apply(ElUtil.coerceToLong(a), ElUtil.coerceToLong(b)));
	}
}
