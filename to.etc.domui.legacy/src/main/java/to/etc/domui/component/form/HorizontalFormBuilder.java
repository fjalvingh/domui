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
package to.etc.domui.component.form;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * Deprecated: use {@link to.etc.domui.component2.form4.FormBuilder}.
 * EXPERIMENTAL UNSTABLE INTERFACE This builder helps with constructing horizontally-oriented
 * forms, where input fields are put next to each other with their labels on top of each other.
 * This is a replacement for TabularFormBuilder doing much of the same work but generating a
 * different layout.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 13, 2009
 */
@Deprecated
public class HorizontalFormBuilder extends GenericTableFormBuilder {
	private TR m_labelRow;

	private TR m_editRow;

	private enum TableMode {
		perRow, perForm
	}

	private TableMode m_tableMode = TableMode.perForm;

	/** In perRow table mode this generates multiple tables; this contains the list of them. */
	private List<Table> m_generatedTableList = new ArrayList<Table>();

	/**
	 * Uninitialized form builder.
	 */
	public HorizontalFormBuilder() {}

	/**
	 * Create a form builder using a lazy model and a current type.
	 * @param <T>
	 * @param clz
	 * @param mdl
	 */
	public <T> HorizontalFormBuilder(Class<T> clz, IReadOnlyModel<T> mdl) {
		super(clz, mdl);
	}

	/**
	 * Create a form builder to edit the specified, immutable instance (meaning the instance is immutable, not it's properties).
	 * @param <T>
	 * @param instance
	 */
	public <T> HorizontalFormBuilder(T instance) {
		setInstance(instance);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple helpers										*/
	/*--------------------------------------------------------------*/
	/**
	 * @see to.etc.domui.component.form.GenericTableFormBuilder#addCell()
	 */
	@Override
	public @Nonnull TD addCell() {
		return addCell(null, 1, 2);
	}

	public TD addCell(int colSpan, int rowSpan) {
		return addCell(null, colSpan, rowSpan);
	}

	public TD addCell(String css, int colSpan, int rowSpan) {
		TR tr = getLabelRow();
		TD td = tr.addCell(css);
		if(colSpan > 1) {
			td.setColspan(colSpan);
		}
		if(rowSpan > 1) {
			td.setRowspan(rowSpan);
		}
		return td;
	}

	/**
	 * @return
	 */
	public TR getLabelRow() {
		checkRows();
		return m_labelRow;
	}

	public TR getEditRow() {
		checkRows();
		return m_editRow;
	}

	/**
	 * Ensure that a label row and an edit row are both available, create 'm if needed.
	 */
	private void checkRows() {
		if(m_labelRow != null && m_editRow != null) // If either is null recreate both of 'm
			return;

		//-- If we're in multitable mode we might need to have to create a new table...
		if(m_tableMode == TableMode.perRow) {
			if(getTable() == null)
				reset(); // Make sure everything is clear
			else {
				if(getTBody() == null)
					tbody();
				else {
					//-- We have an existing table and body... The body must be empty or we need to create new table and body.
					if(getTBody().getChildCount() > 0) {
						reset();
					}
				}
			}
		}

		m_labelRow = addRow();
		m_editRow = addRow();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Main workhorses for genericly adding controls.		*/
	/*--------------------------------------------------------------*/
	/**
	 * @see to.etc.domui.component.form.GenericFormBuilder#addControl(java.lang.String, to.etc.domui.dom.html.NodeBase, to.etc.domui.dom.html.NodeBase[], boolean, to.etc.domui.component.meta.PropertyMetaModel)
	 */
	@Override
	protected void addControl(String label, NodeBase labelnode, NodeBase[] list, boolean mandatory, boolean editable, PropertyMetaModel< ? > pmm) {
		IControlLabelFactory clf = getControlLabelFactory();
		if(clf == null) {
			clf = getBuilder().getControlLabelFactory();
			if(clf == null)
				throw new IllegalStateException("Programmer error: the DomApplication instance returned a null IControlLabelFactory!?!?!?!?");
		}
		Label l = clf.createControlLabel(labelnode, label, editable, mandatory, pmm);
		modalAdd(l, list, editable);
		clearRun();
	}

	@Override
	protected void addControl(NodeBase label, NodeBase labelnode, NodeBase[] list, boolean mandatory, boolean editable, PropertyMetaModel< ? > pmm) {
		modalAdd(label, list, editable);
		clearRun();
	}

	@Override
	public void addContent(NodeBase label, NodeBase[] control, boolean editable) {
		modalAdd(label, control, editable);
		clearRun();
	}

	@Override
	protected IControl< ? >[] addListOfProperties(boolean editable, String... names) {
		IControl< ? >[] res = new IControl< ? >[names.length];
		int ix = 0;
		for(String name : names) {
			if(editable)
				res[ix] = addProp(name);
			else
				res[ix] = addDisplayProp(name);
			ix++;
		}
		clearRun();
		return res;
	}

	/**
	 * Adds the presentation for a label AND a control to the form.
	 * @param l
	 * @param list
	 */
	private void modalAdd(NodeBase l, NodeBase[] list, boolean editable) {
		TR tr = getLabelRow(); // Row containing zhe labelz.
		TD td = tr.addCell(); // Create cell for label;
		td.setCssClass(m_labelClass == null ? m_defaultLabelClass : m_labelClass);
		td.add(l);
		if(m_labelColSpan > 1)
			td.setColspan(m_labelColSpan);
		if(m_labelRowSpan > 1)
			td.setRowspan(m_labelRowSpan);
		if(m_labelNowrap != null)
			td.setNowrap(m_labelNowrap.booleanValue());
		if(m_labelWidth != null)
			td.setWidth(m_labelWidth);

		tr = getEditRow();
		td = tr.addCell();
		String css = editable ? m_defaultControlClass : "ui-fvs-do";
		if(m_controlClass != null)
			css = m_controlClass;

		td.setCssClass(css);
		if(m_controlColSpan > 1)
			td.setColspan(m_controlColSpan);
		if(m_controlRowSpan > 1)
			td.setRowspan(m_controlRowSpan);
		if(m_controlNoWrap != null)
			td.setNowrap(m_controlNoWrap.booleanValue());
		if(m_controlWidth != null)
			td.setWidth(m_controlWidth);

		for(NodeBase nb : list)
			td.add(nb);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Layouter code.										*/
	/*--------------------------------------------------------------*/

	@Override
	protected void onTableAdded(Table t) {
		m_generatedTableList.add(t);
	}

	@Override
	protected void internalClearLocation() {
		m_labelRow = null;
		m_editRow = null;
	}

	/**
	 * Start a new row of input fields.
	 */
	public void nl() {
		m_labelRow = null;
		m_editRow = null;
		if(m_tableMode == TableMode.perRow)
			reset();
	}

	private void basicFinish(boolean balance) {
		for(Table t : m_generatedTableList) {
			if(balance)
				DomUtil.balanceTable(t);
		}
	}

	/**
	 * This finishes off the current table by calculating colspans for all skewed rows. This discards the
	 * current table!
	 *
	 * @return
	 */
	@Override
	public NodeContainer finish() {
		basicFinish(false); // Handle generic "finish" processing for all finish methods.
		NodeContainer result = null;
		if(m_generatedTableList.size() == 0)
			result = null;
		else if(m_generatedTableList.size() == 1) // Only one table generated?
			result = m_generatedTableList.get(0); // Return that one
		else {
			//-- Multiple tables but only one container... Wrap'm in a sizeless div
			Div d = new Div();
			d.setCssClass("ui-szless");
			for(Table t : m_generatedTableList)
				d.add(t);
			result = d;
		}
		m_generatedTableList.clear();
		reset();
		return result;
		//
		//		if(m_parentTable == null)
		//			return null;
		//
		//		//-- jal 20090508 MUST clear the table, because when the builder is used for the NEXT tab it must return a new table!
		//		Table tbl = m_parentTable;
		//		reset();
		//
		//		//-- vmijic 20091106 Do not automatically change colspans, no need for this and also cause rowspans to not working properly
		//		//DomUtil.adjustTableColspans(tbl);
		//		return tbl;
	}

	public void finish(NodeContainer target) {
		finish(target, true);
	}

	/**
	 * Adds the generated tables/table to the target specified.
	 * @param target
	 */
	public void finish(NodeContainer target, boolean balance) {
		basicFinish(balance);
		for(Table t : m_generatedTableList)
			target.add(t);
		m_generatedTableList.clear();
		reset();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Layouter configuration.								*/
	/*--------------------------------------------------------------*/
	/** Next row span to use. */
	//	private int m_nextRowSpan = 1;
	//
	//	private int m_nextColSpan = 1;

	private int m_labelRowSpan = 1;

	private int m_controlRowSpan = 1;

	private int m_labelColSpan = 1;

	private int m_controlColSpan = 1;

	private String m_defaultLabelClass = "ui-fvs-lbl";

	private String m_defaultControlClass = "ui-fvs-in";

	private String m_labelClass, m_controlClass;

	private String m_labelWidth, m_controlWidth;

	private Boolean m_labelNowrap, m_controlNoWrap;

	private void clearRun() {
		m_labelColSpan = 1;
		m_labelRowSpan = 1;
		m_controlColSpan = 1;
		m_controlRowSpan = 1;
		m_labelClass = null;
		m_controlClass = null;
		m_labelWidth = null;
		m_controlWidth = null;
		m_labelNowrap = null;
		m_controlNoWrap = null;
	}

	/**
	 * Set the colspan for both label and control for all controls added after this call.
	 * @param x
	 * @return
	 */
	public HorizontalFormBuilder colSpan(int x) {
		if(x < 0)
			throw new IllegalArgumentException("colspan " + x + " must be > 0");
		m_labelColSpan = x;
		m_controlColSpan = x;
		return this;
	}

	/**
	 * Set the rowspan for both label and control for all controls added after this call.
	 * @param x
	 * @return
	 */
	public HorizontalFormBuilder rowSpan(int x) {
		if(x < 0)
			throw new IllegalArgumentException("rowspan " + x + " must be > 0");
		m_labelRowSpan = x;
		m_controlRowSpan = x;
		return this;
	}

	/**
	 * Set the colspan for only the label for all controls added after this call.
	 * @param x
	 * @return
	 */
	public HorizontalFormBuilder labelColSpan(int x) {
		if(x < 0)
			throw new IllegalArgumentException("colspan " + x + " must be > 0");
		m_labelColSpan = x;
		return this;
	}

	/**
	 * Set the rowspan for only the label for all controls added after this call.
	 * @param x
	 * @return
	 */
	public HorizontalFormBuilder labelRowSpan(int x) {
		if(x < 0)
			throw new IllegalArgumentException("rowspan " + x + " must be > 0");
		m_labelRowSpan = x;
		return this;
	}

	/**
	 * Set the colspan for only the control for all controls added after this call.
	 * @param x
	 * @return
	 */
	public HorizontalFormBuilder controlColSpan(int x) {
		if(x < 0)
			throw new IllegalArgumentException("colspan " + x + " must be > 0");
		m_controlColSpan = x;
		return this;
	}

	/**
	 * Set the rowspan for only the control for all controls added after this call.
	 * @param x
	 * @return
	 */
	public HorizontalFormBuilder controlRowSpan(int x) {
		if(x < 0)
			throw new IllegalArgumentException("rowspan " + x + " must be > 0");
		m_controlRowSpan = x;
		return this;
	}

	public HorizontalFormBuilder defaultLabelClass(String defaultLabelClass) {
		m_defaultLabelClass = defaultLabelClass;
		return this;
	}

	public HorizontalFormBuilder defaultControlClass(String defaultControlClass) {
		m_defaultControlClass = defaultControlClass;
		return this;
	}

	public HorizontalFormBuilder labelClass(String labelClass) {
		m_labelClass = labelClass;
		return this;
	}

	public HorizontalFormBuilder controlClass(String controlClass) {
		m_controlClass = controlClass;
		return this;
	}

	public HorizontalFormBuilder tablePerRow() {
		m_tableMode = TableMode.perRow;
		return this;
	}

	public HorizontalFormBuilder tablePerForm() {
		m_tableMode = TableMode.perForm;
		return this;
	}

	public HorizontalFormBuilder labelWidth(String s) {
		m_labelWidth = s;
		return this;
	}

	public HorizontalFormBuilder controlWidth(String s) {
		m_controlWidth = s;
		return this;
	}

	public HorizontalFormBuilder labelNowrap() {
		m_labelNowrap = Boolean.TRUE;
		return this;
	}

	public HorizontalFormBuilder controlNowrap() {
		m_controlNoWrap = Boolean.TRUE;
		return this;
	}

	public HorizontalFormBuilder nowrap() {
		m_labelNowrap = Boolean.TRUE;
		m_controlNoWrap = Boolean.TRUE;
		return this;
	}

	public HorizontalFormBuilder width(String s) {
		m_labelWidth = s;
		m_controlWidth = s;
		return this;
	}
}
