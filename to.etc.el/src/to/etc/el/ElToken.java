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

public enum ElToken {
	EOF, LogicalAnd(20), LogicalOr(20), Plus(50), Minus(50), Multiply(60), Divide(60), Modulus(60), GreaterThan(40), LessThan(40), GreaterThanOrEqual(40), LessThanOrEqual(40), Equal(30), NotEqual(30), LogicalNot(
		70), QuestionMark(10), Colon(10), BinaryOr, BinaryAnd, Empty, // The "empty" function keyword
	ParentheseOpen, // (
	ParentheseClose, // )
	Dot, BraceOpen, // [
	BraceClose, // ]
	Comma, True, False, Null, // The 'null' keyword

	//-- Complex, constructed types
	Id, // Identifier
	StringLiteral, // String literal
	IntLiteral, // An integer literal
	FloatLiteral;

	/** If an operator: the precedence of the op */
	private int m_prec;

	ElToken() {
		m_prec = -1;
	}

	ElToken(int prec) {
		m_prec = prec;
	}

	final public int getPrec() {
		return m_prec;
	}
}
