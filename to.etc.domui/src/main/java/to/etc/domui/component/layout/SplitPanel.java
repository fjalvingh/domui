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
package to.etc.domui.component.layout;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;

/**
 * A simple two-column divider, where the widths of both columns can be set using table
 * width specifications. The internal representation is a table with two columns, where
 * each column contains a side node.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 21, 2008
 */
public class SplitPanel extends Table {
	private NodeBase m_a, m_b;

	private TD m_acell, m_bcell;

	public SplitPanel() {
		setTableBorder(0);
		setCellPadding("0");
		setCellSpacing("0");
		setCssClass("ui-sp");
		m_acell = new TD();
		m_acell.setWidth("10%");
		m_acell.setCssClass("ui-sp-a");
		m_acell.setValign(TableVAlign.TOP);
		m_acell.setVerticalAlign(VerticalAlignType.TOP);

		m_bcell = new TD();
		m_bcell.setWidth("90%");
		m_bcell.setCssClass("ui-sp-b");
		m_bcell.setValign(TableVAlign.TOP);
		m_bcell.setVerticalAlign(VerticalAlignType.TOP);
	}

	public SplitPanel(NodeBase a, String awidth, NodeBase b, String bwidth) {
		this();
		m_a = a;
		m_b = b;
		m_acell.add(a);
		m_bcell.add(b);
		m_acell.setWidth(awidth);
		m_bcell.setWidth(bwidth);
	}

	@Override
	public void createContent() throws Exception {
		TBody b = new TBody();
		add(b);
		TR row = b.addRow();
		row.add(m_acell);
		row.add(m_bcell);
	}

	public void setA(String width, NodeBase a) {
		setA(a);
		setAWidth(width);
	}

	public void setB(String width, NodeBase b) {
		setB(b);
		setBWidth(width);
	}

	public void setA(NodeBase a) {
		m_a = a;
		if(m_acell != null) {
			m_acell.forceRebuild();
			m_acell.add(m_a);
		}
	}

	public void setB(NodeBase a) {
		m_b = a;
		if(m_bcell != null) {
			m_bcell.forceRebuild();
			m_bcell.add(m_b);
		}
	}

	public NodeBase getA() {
		return m_a;
	}

	public NodeBase getB() {
		return m_b;
	}

	public TD getACell() {
		return m_acell;
	}

	public TD getBCell() {
		return m_bcell;
	}

	public void setAWidth(String s) {
		m_acell.setWidth(s);
	}

	public void setBWidth(String s) {
		m_bcell.setWidth(s);
	}
}
