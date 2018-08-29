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
import java.math.*;

import javax.servlet.jsp.el.*;

import to.etc.el.*;
import to.etc.util.*;

public class NdUnaryMinus extends NdUnary {
	public NdUnaryMinus(NdBase b) {
		super(b);
	}

	/**
	 * See JSP 2.0, JSP 2.3.5.4
	 * @see to.etc.el.node.NdBase#evaluate(javax.servlet.jsp.el.VariableResolver, javax.servlet.jsp.el.FunctionMapper)
	 */
	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object a = m_expr.evaluate(vr);
		if(a == null)
			return WrapperCache.getLong(0);
		//		Class cl	= a.getClass();
		if(a instanceof BigDecimal)
			return ((BigDecimal) a).negate();
		if(a instanceof BigInteger)
			return ((BigInteger) a).negate();

		if(a instanceof String) {
			if(ElUtil.isFloatingPointString(a))
				return Double.valueOf(-ElUtil.coerceToDouble(a));
			return Long.valueOf(-ElUtil.coerceToLong(a));
		}
		if(a instanceof Double)
			return WrapperCache.getDouble(-((Double) a).doubleValue());
		if(a instanceof Float)
			return WrapperCache.getFloat(-((Float) a).floatValue());
		if(a instanceof Long)
			return WrapperCache.getLong(-((Long) a).longValue());
		if(a instanceof Integer)
			return WrapperCache.getInteger(-((Integer) a).intValue());
		if(a instanceof Short)
			return WrapperCache.getShort((short) -((Short) a).shortValue());
		if(a instanceof Byte)
			return WrapperCache.getByte((byte) -((Byte) a).byteValue());
		throw new ELException("Unary minus operator not applicable to operand " + a);
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		a.append('-');
		a.append('(');
		m_expr.getExpression(a);
		a.append(')');
	}

}
