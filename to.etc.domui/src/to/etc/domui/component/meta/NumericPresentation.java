/*
 * DomUI Java User Interface library
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
package to.etc.domui.component.meta;

/**
 * Default numeric representations that can be used.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 30, 2009
 */
public enum NumericPresentation {
	/** No explicit presentation set. */
	UNKNOWN,

	/** Just a run-of-the-mill number, containing only a fraction separator when needed */
	NUMBER,

	/** Presented in full precision, moving to scientific (x.xxEyy) mode when needed */
	NUMBER_SCIENTIFIC,

	/** Amount, including fraction and specified number of decimals places (by scale), without any thousand separators */
	NUMBER_SCALED,

	/** Fully embellished number, with thousand separators and such */
	NUMBER_FULL,

	/** Default monetary representation. */
	MONEY,

	/** Display as amount with currency symbol, thousands separators, decimal point and complete fraction (always) */
	MONEY_FULL,

	/** Display as full, but if the fraction (cents) is all zeroes remove them */
	MONEY_FULL_TRUNC,

	/** Display as a number having thousands separator and fraction but without currency symbol */
	MONEY_NO_SYMBOL,

	/** Just the amount, including faction, without any unnecessary adornments (similar to NUMBER_SCALED) */
	MONEY_NUMERIC;

	static public boolean isMonetary(NumericPresentation np) {
		switch(np){
			default:
				return false;
			case MONEY:
			case MONEY_FULL:
			case MONEY_FULL_TRUNC:
			case MONEY_NO_SYMBOL:
			case MONEY_NUMERIC:
				return true;
		}
	}

	/**
	 * Returns if specified numeric presentation should be rendered without trailing zeros at the end of decimal fraction part.
	 * @param np
	 * @return
	 */
	static public boolean isStripTrailingZeros(NumericPresentation np) {
		switch(np){
			default:
				return true;
			case NUMBER_SCALED:
			case NUMBER_FULL:
			case MONEY_FULL:
			case MONEY_FULL_TRUNC:
			case MONEY_NO_SYMBOL:
			case MONEY_NUMERIC:
				return false;
		}
	}

}
