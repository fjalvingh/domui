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

import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TH;
import to.etc.domui.dom.html.THead;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.TextNode;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Temp thingy to create the header for a table.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 2, 2008
 */
@DefaultNonNull
final public class HeaderContainer<T> {
	@Nullable
	private final String m_headerRowCSS;

	private TableModelTableBase<T> m_table;

	final private THead m_head;

	@Nullable
	private TR m_tr;

	private boolean m_finished;

	public HeaderContainer(TableModelTableBase<T> table, THead head, @Nullable String headerRowCSS) {
		m_table = table;
		m_head = head;
		m_headerRowCSS = headerRowCSS;
	}

	public TableModelTableBase<T> getTable() {
		return m_table;
	}

	public void addHeader(boolean after, @Nonnull TableHeader header) {
		if(after) {
			row();							// Make sure the label row is there.
			m_head.add(header);
		} else {
			TR tr = m_tr;					// Already have the label row?
			if(null == tr) {
				m_head.add(header);			// No: just add
			} else {
				tr.appendBeforeMe(header);
			}
		}
		header.addCssClass("ui-dt-hdr-extra");
	}

	/**
	 * Return the main header row. Create it if it does not yet exist.
	 * @return
	 */
	final public TR row() {
		m_finished = true;
		TR tr = m_tr;
		if(null == tr) {
			m_tr = tr = new TR();
			tr.setCssClass(m_headerRowCSS);
			m_head.add(tr);
		}
		return tr;
	}

	/**
	 * Adds a column header to the table.
	 * @param columnContent
	 */
	public TH add(@Nullable NodeBase columnContent) {
		TH td = new TH();
		row().add(td);
		td.addCssClass("ui-dt-th");
		if(columnContent != null)
			td.add(columnContent);
		return td;
	}

	public TH add(@Nullable String txt) {
		if(txt != null) {
			txt = txt.trim();
			if(txt != null && txt.length() > 0) {
				return add(new TextNode(txt));
			}
		}

		//-- Just add an empty (for now) header and return it.
		return add((NodeBase) null);
	}

	/**
	 * Use to check whether there is some content rendered to it or not.
	 * @return
	 */
	public boolean hasContent() {
		TR tr = m_tr;
		return (tr != null && tr.getChildCount() > 0);
	}
}
