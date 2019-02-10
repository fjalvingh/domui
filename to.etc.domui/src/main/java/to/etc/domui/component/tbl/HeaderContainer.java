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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.Col;
import to.etc.domui.dom.html.ColGroup;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TH;
import to.etc.domui.dom.html.THead;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.TextNode;

/**
 * Temp thingy to create the header for a table.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 2, 2008
 */
@NonNullByDefault
final public class HeaderContainer<T> {
	@Nullable
	private final String m_headerRowCSS;

	private TableModelTableBase<T> m_table;

	final private THead m_head;

	final private ColGroup m_colGroup;

	@Nullable
	private TR m_tr;

	private boolean m_finished;

	public HeaderContainer(TableModelTableBase<T> table, ColGroup colGroup, THead head, @Nullable String headerRowCSS) {
		m_table = table;
		m_head = head;
		m_colGroup = colGroup;
		m_headerRowCSS = headerRowCSS;
	}

	public TableModelTableBase<T> getTable() {
		return m_table;
	}

	public void addHeader(boolean after, @NonNull TableHeader header) {
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
	 */
	final public TR row() {
		m_finished = true;
		TR tr = m_tr;
		if(null == tr) {
			m_tr = tr = new TR();
			tr.setCssClass(m_headerRowCSS);
			tr.setKeepNode(true);
			m_head.add(tr);
		}
		return tr;
	}

	/**
	 * Adds a column header to the table.
	 */
	public HeaderContainerCell add(@Nullable NodeBase columnContent) {
		TH th = new TH();
		th.setKeepNode(true);
		row().add(th);

		Col col = new Col();
		col.setKeepNode(true);
		m_colGroup.add(col);

		th.addCssClass("ui-dt-th");
		if(columnContent != null)
			th.add(columnContent);
		return new HeaderContainerCell(th, col);
	}

	public HeaderContainerCell add(@Nullable String txt) {
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
	 */
	public boolean hasContent() {
		TR tr = m_tr;
		return (tr != null && tr.getChildCount() > 0);
	}

	public static final class HeaderContainerCell {
		private final TH m_th;

		private final Col m_col;

		public HeaderContainerCell(TH th, Col col) {
			m_th = th;
			m_col = col;
		}

		public TH getTh() {
			return m_th;
		}

		public Col getCol() {
			return m_col;
		}
	}
}
