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
public class OldHorizontalFormBuilder extends GenericTableFormBuilder {
	private TR m_labelRow;

	private TR m_editRow;

	public OldHorizontalFormBuilder() {}

	public <T> OldHorizontalFormBuilder(Class<T> clz, IReadOnlyModel<T> mdl) {
		super(clz, mdl);
	}

	public <T> OldHorizontalFormBuilder(T instance) {
		setInstance(instance);
	}

	/**
	 *
	 * @see to.etc.domui.component.form.GenericFormBuilder#addControl(java.lang.String, to.etc.domui.dom.html.NodeBase, to.etc.domui.dom.html.NodeBase[], boolean, to.etc.domui.component.meta.PropertyMetaModel)
	 */
	@Override
	protected void addControl(String label, NodeBase labelnode, NodeBase[] list, boolean mandatory, boolean editable, PropertyMetaModel pmm) {
		addControl(label, 1, labelnode, list, mandatory, editable, pmm);
	}

	/**
	 * @see to.etc.domui.component.form.GenericFormBuilder#addControl(java.lang.String, to.etc.domui.dom.html.NodeBase, to.etc.domui.dom.html.NodeBase[], boolean, to.etc.domui.component.meta.PropertyMetaModel)
	 * In addition, enables customization of colSpan for rendered cell.
	 */
	protected void addControl(String label, int colSpan, NodeBase labelnode, NodeBase[] list, boolean mandatory, boolean editable, PropertyMetaModel pmm) {
		IControlLabelFactory clf = getControlLabelFactory();
		if(clf == null) {
			clf = getBuilder().getControlLabelFactory();
			if(clf == null)
				throw new IllegalStateException("Programmer error: the DomApplication instance returned a null IControlLabelFactory!?!?!?!?");
		}
		Label l = clf.createControlLabel(labelnode, label, editable, mandatory, pmm);
		modalAdd(l, colSpan, list);
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
		return res;
	}

	/**
	 * Adds the presentation for a label AND a control to the form.
	 * @param l
	 * @param list
	 */
	private void modalAdd(Label l, int colSpan, NodeBase[] list) {
		TR tr = getLabelRow(); // Row containing zhe labelz.
		TD td = tr.addCell(); // Create cell for label;
		td.setCssClass("ui-fvs-lbl");
		td.add(l);
		if(colSpan > 1) {
			td.setColspan(colSpan);
		}

		tr = getEditRow();
		td = tr.addCell();
		td.setCssClass("ui-fvs-in");
		if(colSpan > 1) {
			td.setColspan(colSpan);
		}
		for(NodeBase nb : list)
			td.add(nb);
	}

	@Override
	public TD addCell() {
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

	/*--------------------------------------------------------------*/
	/*	CODING:	Layouter code.										*/
	/*--------------------------------------------------------------*/
	@Override
	protected void internalClearLocation() {
		m_labelRow = null;
		m_editRow = null;
	}

	/**
	 * This finishes off the current table by calculating colspans for all skewed rows. This discards the
	 * current table!
	 *
	 * @return
	 */
	@Override
	public Table finish() {
		if(getTable() == null)
			return null;

		//-- jal 20090508 MUST clear the table, because when the builder is used for the NEXT tab it must return a new table!
		Table tbl = getTable();
		reset();

		//-- vmijic 20091106 Do not automatically change colspans, no need for this and also cause rowspans to not working properly
		//DomUtil.adjustTableColspans(tbl);
		return tbl;
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

	/**
	 * Enable adding of field into table cell with possibility to customize colspan.
	 * Add an input for the specified property. The property is based at the current input
	 * class. The input model is default (using metadata) and the property is labeled using
	 * the metadata-provided label.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param name
	 * @param readOnly In case of readOnly set to true behaves same as addReadOnlyProp.
	 * @param mandatory Specify if field is mandatory. This <b>always</b> overrides the mandatoryness of the metadata which is questionable.
	 * @param span Specify cell span.
	 * @return
	 */
	public IControl< ? > addPropWithSpan(final String name, final boolean readOnly, final boolean mandatory, int colSpan) {
		PropertyMetaModel pmm = resolveProperty(name);
		String label = pmm.getDefaultLabel();
		return addPropWithSpan(name, label, readOnly, mandatory, colSpan);
	}

	/**
	 * Enable adding of field into table cell with possibility to customize colspan.
	 * Add an input for the specified property. The property is based at the current input
	 * class. The input model is default (using metadata) and the property is labeled using
	 * the label provided by method parameter.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param name
	 * @param label User defined label.
	 * @param readOnly In case of readOnly set to true behaves same as addReadOnlyProp.
	 * @param mandatory Specify if field is mandatory. This <b>always</b> overrides the mandatoryness of the metadata which is questionable.
	 * @param span Specify cell span.
	 * @return
	 */
	public IControl< ? > addPropWithSpan(final String name, final String label, final boolean readOnly, final boolean mandatory, int colSpan) {
		PropertyMetaModel pmm = resolveProperty(name);

		//-- Check control permissions: does it have view permissions?
		if(!rights().calculate(pmm))
			return null;
		final ControlFactory.Result r = createControlFor(getModel(), pmm, !readOnly && rights().isEditable()); // Add the proper input control for that type
		addControl(label, colSpan, r.getLabelNode(), r.getNodeList(), mandatory, !readOnly, pmm);

		//-- jal 20090924 Bug 624 Assign the control label to all it's node so it can specify it in error messages
		if(label != null) {
			for(NodeBase b : r.getNodeList())
				b.setErrorLocation(label);
		}

		if(r.getBinding() != null)
			getBindings().add(r.getBinding());
		else
			throw new IllegalStateException("No binding for a " + r);
		return r.getFormControl();
	}

	/**
	 * Enable adding of field into table cell with possibility to customize colspan.
	 * This adds a fully user-specified control for a given property with it's default label,
	 * without creating <i>any<i> binding. The only reason the property is passed is to use
	 * it's metadata to define it's access rights and default label.
	 *
	 * @param propertyName
	 * @param nb
	 * @param mandatory
	 * @param colSpan
	 */
	public void addPropertyAndControlWithSpan(final String propertyName, final NodeBase nb, final boolean mandatory, int colSpan) {
		PropertyMetaModel pmm = resolveProperty(propertyName);
		String label = pmm.getDefaultLabel();

		// FIXME Kludge to determine if the control is meant to be editable!
		boolean editable = nb instanceof IControl< ? >;

		addControl(label, colSpan, nb, new NodeBase[]{nb}, mandatory, editable, pmm);
		if(label != null)
			nb.setErrorLocation(label);
	}

}
