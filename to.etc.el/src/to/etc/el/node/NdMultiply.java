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

public class NdMultiply extends NdBinOp {
	/**
	 * @param a
	 * @param b
	 */
	public NdMultiply(NdBase a, NdBase b) {
		super(a, b);
	}

	@Override
	protected Object apply(BigDecimal a, BigDecimal b) throws ELException {
		return a.multiply(b);
	}

	@Override
	protected Object apply(BigInteger a, BigInteger b) throws ELException {
		return a.multiply(b);
	}

	@Override
	protected double apply(double a, double b) throws ELException {
		return a * b;
	}

	@Override
	protected long apply(long a, long b) throws ELException {
		return a * b;
	}

	@Override
	protected String getOperator() {
		return "*";
	}

}
