package to.etc.domui.util;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.input.ComboFixed.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.upload.*;
import to.etc.domui.dom.html.*;

/**
 * Helper thingy to create something based on a table.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 21, 2008
 */
public class MiniTableBuilder {
	private Table			m_table;
	private TBody			m_body;
	private THead			m_head;
	private ButtonBar		m_bb;

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

	public void	appendTo(NodeContainer c) {
		if(m_table != null) {
			c.add(m_table);
			m_table = null;
			m_body = null;
			m_head = null;
			m_bb = null;
		}
	}
	public void	adjustColspans() {
		DomUtil.adjustTableColspans(m_table);
	}

	public void	clear() {
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
	public TD	addRowAndCell() {
		return getBody().addRowAndCell();
	}
	public TR	addRow() {
		return getBody().addRow();
	}
	public TD	addCell() {
		if(row() == null)
			addRow();
		return getBody().addCell();
	}
	public TR	row() {
		return getBody().row();
	}
	public TD	cell() {
		return getBody().cell();
	}

	public <T extends NodeBase>	T	add(T comp) {
		cell().add(comp);
		return comp;
	}
	public void	setHeaders(String... hdrs) {
		getHead().setHeaders(hdrs);
	}
	public TH	addHeader(String text) {
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
	public <T> Text<T>	addText(Class<T> clz) {
		return add(new Text<T>(clz));
	}
	private void	doLabelCtl(String label) {
		addCell().addLiteral(DomUtil.nlsLabel(label));
		cell().setCssClass("ui-f-lbl");
		addCell().setCssClass("ui-f-in");
	}
	public void		addLabel(String lbl) {
		addCell().addLiteral(DomUtil.nlsLabel(lbl));
		cell().setCssClass("ui-f-lbl");
	}

	public <T> Text<T>	addText(String label, Class<T> clz) {
		doLabelCtl(label);
		return addText(clz);
	}

	public TextStr	addStr() {
		return add(new TextStr());
	}
	public TextStr	addStr(String label) {
		doLabelCtl(label);
		return add(new TextStr());
	}
	public FileUpload	addUpload(int maxfiles, String exts) {
		return add(new FileUpload(maxfiles, exts));
	}

	public FileUpload	addUpload(String label, int maxfiles, String exts) {
		doLabelCtl(label);
		return addUpload(maxfiles, exts);
	}
	public <T> ComboLookup2<T>	addComboLookup(IListMaker<T> maker) {
		ComboLookup2<T>	cb = new ComboLookup2<T>(maker);
		return add(cb);
	}
	public <T> ComboLookup2<T>	addComboLookup(String label, IListMaker<T> maker) {
		doLabelCtl(label);
		return addComboLookup(maker);
	}
	public <T> ComboLookup2<T>	addComboLookup(List<T> input) {
		ComboLookup2<T>	cb = new ComboLookup2<T>(input);
		return add(cb);
	}
	public <T> ComboLookup2<T>	addComboLookup(String label, List<T> input) {
		doLabelCtl(label);
		return addComboLookup(input);
	}

	public <T> ComboFixed<T>	addComboFixed(List<Pair<T>> list) {
		ComboFixed<T>	cf = new ComboFixed<T>(list);
		return add(cf);
	}
	public <T> ComboFixed<T>	addComboFixed(String label, List<Pair<T>> list) {
		doLabelCtl(label);
		return addComboFixed(list);
	}

	/**
	 * Adds a ButtonBar().
	 * @return
	 */
	public ButtonBar	bb() {
		if(m_bb == null) {
			m_bb = new ButtonBar();
			if(row() == null || row().getChildCount() > 0)
				addRowAndCell();
			cell().add(m_bb);
		}
		return m_bb;
	}
}
