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
 * CSS2 display property values
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
public enum DisplayType {
	NONE("none"), INLINE("inline"), BLOCK("block"), INLINE_BLOCK("inline-block"), LIST_ITEM("list-item"), RUN_IN("run-in"), COMPACT("compact"), MARKER("marker"), TABLE("table"), INLINE_TABLE("inline-table"), TABLE_ROW_GROUP(
		"table-row-group"), TABLE_HEADER_GROUP("table-header-group"), TABLE_FOOTER_GROUP("table-footer-group"), TABLE_ROW("table-row"), TABLE_COLUMN_GROUP("table-column-group"), TABLE_COLUMN(
		"table-column"), TABLE_CELL("table-cell"), TABLE_CAPTION("table-caption");

	private String m_txt;

	DisplayType(String txt) {
		m_txt = txt;
	}

	@Override
	public String toString() {
		return m_txt;
	}

}
