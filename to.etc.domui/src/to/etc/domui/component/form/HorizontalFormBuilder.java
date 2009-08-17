package to.etc.domui.component.form;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * EXPERIMENTAL UNSTABLE INTERFACE This builder helps with constructing horizontally-oriented
 * forms, where input fields are put next to each other with their labels on top of each other.
 * This is a replacement for TabularFormBuilder doing much of the same work but generating a
 * different layout.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 13, 2009
 */
public class HorizontalFormBuilder extends GenericTableFormBuilder {
	private TR m_labelRow;

	private TR m_editRow;

	public HorizontalFormBuilder() {}

	public <T> HorizontalFormBuilder(Class<T> clz, IReadOnlyModel<T> mdl) {
		super(clz, mdl);
	}

	/**
	 *
	 * @see to.etc.domui.component.form.GenericFormBuilder#addControl(java.lang.String, to.etc.domui.dom.html.NodeBase, to.etc.domui.dom.html.NodeBase[], boolean, to.etc.domui.component.meta.PropertyMetaModel)
	 */
	@Override
	protected void addControl(String label, NodeBase labelnode, NodeBase[] list, boolean mandatory, PropertyMetaModel pmm) {
		IControlLabelFactory clf = getControlLabelFactory();
		if(clf == null) {
			clf = DomApplication.get().getControlLabelFactory();
			if(clf == null)
				throw new IllegalStateException("Programmer error: the DomApplication instance returned a null IControlLabelFactory!?!?!?!?");
		}
		Label l = clf.createControlLabel(labelnode, label, true, mandatory, pmm);
		modalAdd(l, list);
	}

	@Override
	protected void addListOfProperties(boolean editable, String... names) {
		for(String name : names) {
			if(editable)
				addProp(name);
			else
				addReadOnlyProp(name);
		}
	}

	/**
	 * Adds the presentation for a label AND a control to the form.
	 * @param l
	 * @param list
	 */
	private void modalAdd(Label l, NodeBase[] list) {
		TR tr = getLabelRow(); // Row containing zhe labelz.
		TD td = tr.addCell(); // Create cell for label;
		td.setCssClass("ui-fvs-lbl");
		td.add(l);

		tr = getEditRow();
		td = tr.addCell();
		td.setCssClass("ui-fvs-in");
		for(NodeBase nb : list)
			td.add(nb);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Layouter code.										*/
	/*--------------------------------------------------------------*/
	@Override
	protected void internalClearLocation() {
		m_labelRow = null;
		m_editRow = null;
	}

	/**
	 *
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

	private void checkRows() {
		if(m_labelRow != null && m_editRow != null)
			return;

		m_labelRow = addRow();
		m_editRow = addRow();
	}

	/**
	 * Start a new row of input fields.
	 */
	public void nl() {
		m_labelRow = null;
		m_editRow = null;
	}

}
