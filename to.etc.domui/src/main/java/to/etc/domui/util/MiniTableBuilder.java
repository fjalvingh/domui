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
package to.etc.domui.util;

import to.etc.domui.component.input.ComboFixed;
import to.etc.domui.component.input.ComboLookup;
import to.etc.domui.component.input.Text;
import to.etc.domui.component.input.TextStr;
import to.etc.domui.component.input.ValueLabelPair;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.layout.IButtonBar;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TH;
import to.etc.domui.dom.html.THead;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;

import java.util.List;

/**
 * Helper thingy to create something based on a table.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 21, 2008
 */
public class MiniTableBuilder {
	private Table m_table;

	private TBody m_body;

	private THead m_head;

	private ButtonBar m_bb;

	public Table getTable() {
		if(m_table == null)
			m_table = new Table();
		return m_table;
	}

	public TBody getBody() {
		if(m_body == null) {
			m_body = new TBody();
			getTable().add(m_body);
		}
		return m_body;
	}

	public THead getHead() {
		if(m_head == null) {
			m_head = new THead();
			getTable().add(m_head);
		}
		return m_head;
	}

	public void appendTo(NodeContainer c) {
		if(m_table != null) {
			c.add(m_table);
			m_table = null;
			m_body = null;
			m_head = null;
			m_bb = null;
		}
	}

	public void adjustColspans() {
		DomUtil.adjustTableColspans(m_table);
	}

	public void clear() {
		m_table = null;
		m_body = null;
		m_head = null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Proxies to body's cell builder code.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a new TR and a TD within that row. If needed this creates the entire table structure.
	 * @return
	 */
	public TD addRowAndCell() {
		return getBody().addRowAndCell();
	}

	public TR addRow() {
		return getBody().addRow();
	}

	public TD addCell() {
		if(row() == null)
			addRow();
		return getBody().addCell();
	}

	public TR row() {
		return getBody().row();
	}

	public TD cell() {
		return getBody().cell();
	}

	public <T extends NodeBase> T add(T comp) {
		cell().add(comp);
		return comp;
	}

	public void setHeaders(String... hdrs) {
		getHead().setHeaders(hdrs);
	}

	public TH addHeader(String text) {
		return getHead().addHeader(text);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	QD Component add thingies.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a TEXT component to the current layout thingy.
	 * @param <T>
	 * @param clz
	 * @return
	 */
	public <T> Text<T> addText(Class<T> clz) {
		return add(new Text<T>(clz));
	}

	private void doLabelCtl(String label) {
		addCell().add(DomUtil.nlsLabel(label));
		cell().setCssClass("ui-f-lbl");
		addCell().setCssClass("ui-f-in");
	}

	public void addLabel(String lbl) {
		addCell().add(DomUtil.nlsLabel(lbl));
		cell().setCssClass("ui-f-lbl");
	}

	public <T> Text<T> addText(String label, Class<T> clz) {
		doLabelCtl(label);
		return addText(clz);
	}

	public TextStr addStr() {
		return add(new TextStr());
	}

	public TextStr addStr(String label) {
		doLabelCtl(label);
		return add(new TextStr());
	}

	//public FileUpload addUpload(int maxfiles, String exts) {
	//	return add(new FileUpload(maxfiles, exts));
	//}

	//public FileUpload addUpload(String label, int maxfiles, String exts) {
	//	doLabelCtl(label);
	//	return addUpload(maxfiles, exts);
	//}

	public <T> ComboLookup<T> addComboLookup(IListMaker<T> maker) {
		ComboLookup<T> cb = new ComboLookup<T>(maker);
		return add(cb);
	}

	public <T> ComboLookup<T> addComboLookup(String label, IListMaker<T> maker) {
		doLabelCtl(label);
		return addComboLookup(maker);
	}

	public <T> ComboLookup<T> addComboLookup(List<T> input) {
		ComboLookup<T> cb = new ComboLookup<T>(input);
		return add(cb);
	}

	public <T> ComboLookup<T> addComboLookup(String label, List<T> input) {
		doLabelCtl(label);
		return addComboLookup(input);
	}

	public <T> ComboFixed<T> addComboFixed(List<ValueLabelPair<T>> list) {
		ComboFixed<T> cf = new ComboFixed<T>(list);
		return add(cf);
	}

	public <T> ComboFixed<T> addComboFixed(String label, List<ValueLabelPair<T>> list) {
		doLabelCtl(label);
		return addComboFixed(list);
	}

	/**
	 * Adds a ButtonBar().
	 * @return
	 */
	public IButtonBar bb() {
		if(m_bb == null) {
			m_bb = new ButtonBar();
			if(row() == null || row().getChildCount() > 0)
				addRowAndCell();
			cell().add(m_bb);
		}
		return m_bb;
	}
}
