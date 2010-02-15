package to.etc.domui.component.form;

import java.util.*;

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

	static private enum TableMode {
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
	/*	CODING:	Adding components.									*/
	/*--------------------------------------------------------------*/
	//	/**
	//	 * Enable adding of field into table cell with possibility to customize colspan.
	//	 * Add an input for the specified property. The property is based at the current input
	//	 * class. The input model is default (using metadata) and the property is labeled using
	//	 * the metadata-provided label.
	//	 *
	//	 * FORMAL-INTERFACE.
	//	 *
	//	 * @param name
	//	 * @param readOnly In case of readOnly set to true behaves same as addReadOnlyProp.
	//	 * @param mandatory Specify if field is mandatory. This <b>always</b> overrides the mandatoryness of the metadata which is questionable.
	//	 * @param span Specify cell span.
	//	 * @return
	//	 */
	//	public IFormControl addPropWithSpan(final String name, final boolean readOnly, final boolean mandatory, int colSpan) {
	//		PropertyMetaModel pmm = resolveProperty(name);
	//		String label = pmm.getDefaultLabel();
	//
	//		//-- Check control permissions: does it have view permissions?
	//		if(!rights().calculate(pmm))
	//			return null;
	//		final ControlFactory.Result r = createControlFor(getModel(), pmm, !readOnly && rights().isEditable()); // Add the proper input control for that type
	//		addControl(label, colSpan, r.getLabelNode(), r.getNodeList(), mandatory, pmm);
	//
	//		//-- jal 20090924 Bug 624 Assign the control label to all it's node so it can specify it in error messages
	//		if(label != null) {
	//			for(NodeBase b : r.getNodeList())
	//				b.setErrorLocation(label);
	//		}
	//
	//		if(r.getBinding() != null)
	//			getBindings().add(r.getBinding());
	//		else
	//			throw new IllegalStateException("No binding for a " + r);
	//		return r.getFormControl();
	//	}
	//
	//	/**
	//	 * Enable adding of field into table cell with possibility to customize colspan.
	//	 * This adds a fully user-specified control for a given property with it's default label,
	//	 * without creating <i>any<i> binding. The only reason the property is passed is to use
	//	 * it's metadata to define it's access rights and default label.
	//	 *
	//	 * @param propertyName
	//	 * @param nb
	//	 * @param mandatory
	//	 * @param colSpan
	//	 */
	//	public void addPropertyAndControlWithSpan(final String propertyName, final NodeBase nb, final boolean mandatory, int colSpan) {
	//		PropertyMetaModel pmm = resolveProperty(propertyName);
	//		String label = pmm.getDefaultLabel();
	//		addControl(label, colSpan, nb, new NodeBase[]{nb}, mandatory, pmm);
	//		if(label != null)
	//			nb.setErrorLocation(label);
	//	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple helpers										*/
	/*--------------------------------------------------------------*/
	/**
	 * @see to.etc.domui.component.form.GenericTableFormBuilder#addCell()
	 */
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
	protected void addControl(String label, NodeBase labelnode, NodeBase[] list, boolean mandatory, PropertyMetaModel pmm) {
		IControlLabelFactory clf = getControlLabelFactory();
		if(clf == null) {
			clf = getBuilder().getControlLabelFactory();
			if(clf == null)
				throw new IllegalStateException("Programmer error: the DomApplication instance returned a null IControlLabelFactory!?!?!?!?");
		}
		Label l = clf.createControlLabel(labelnode, label, true, mandatory, pmm);
		modalAdd(l, list);
		clearRun();
	}

	@Override
	protected IFormControl[] addListOfProperties(boolean editable, String... names) {
		IFormControl[] res = new IFormControl[names.length];
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
	private void modalAdd(Label l, NodeBase[] list) {
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
		td.setCssClass(m_controlClass == null ? m_defaultControlClass : m_controlClass);
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
