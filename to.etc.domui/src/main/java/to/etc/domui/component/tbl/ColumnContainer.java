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
package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.TextNode;

/**
 * Helper class which maintains data for a set of columns (i.e. a row).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class ColumnContainer<T> {
	@NonNull
	final private TableModelTableBase<T> m_table;

	@Nullable
	private RowButtonContainer m_rowButtonContainer;

	@Nullable
	private TR m_tr;

	public ColumnContainer(@NonNull TableModelTableBase<T> table) {
		m_table = table;
	}

	public void setParent(@NonNull TR p) {
		m_tr = p;
	}

	@NonNull
	public TableModelTableBase<T> getTable() {
		return m_table;
	}

	/**
	 * Adds a column to the table.
	 * @param columnContent
	 */
	@NonNull
	public TD add(NodeBase columnContent) {
		TD td = new TD();
		getTR().add(td);
		if(columnContent != null)
			td.add(columnContent);
		return td;
	}

	@NonNull
	public TD add(@Nullable String txt) {
		return add(new TextNode(txt));
	}

	@NonNull
	public TR getTR() {
		if(null != m_tr)
			return m_tr;
		throw new IllegalStateException("Row not set.");
	}

	@NonNull
	public RowButtonContainer getRowButtonContainer() {
		RowButtonContainer c = m_rowButtonContainer;
		if(c == null)
			c = m_rowButtonContainer = new RowButtonContainer();
		return c;
	}
}
