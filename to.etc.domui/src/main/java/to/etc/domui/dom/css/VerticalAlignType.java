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
package to.etc.domui.dom.css;

/**
 * Mapping for <I>vertical-align</I> CSS Style attribute.	 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Apr 7, 2010
 */
public enum VerticalAlignType {
	/** Align the baseline of the element with the baseline of the parent element. This is default. */
	BASELINE("baseline"),
	/** Aligns the element as it was subscript. */
	SUB("sub"),
	/** Aligns the element as it was superscript. */
	SUPER("super"),
	/** The top of the element is aligned with the top of the tallest element on the line. */
	TOP("top"),
	/** The top of the element is aligned with the top of the parent element's font. */
	TEXT_TOP("text-top"),
	/** The element is placed in the middle of the parent element. */
	MIDDLE("middle"),
	/** The bottom of the element is aligned with the lowest element on the line. */
	BOTTOM("bottom"),
	/** The bottom of the element is aligned with the bottom of the parent element's font. */
	TEXT_BOTTOM("text-bottom"),
	/** Specifies that the value of the vertical-align property should be inherited from the parent element. */
	INHERIT("inherit");

	private String m_txt;

	VerticalAlignType(String s) {
		m_txt = s;
	}

	@Override
	public String toString() {
		return m_txt;
	}
}
