package to.etc.domui.component.form;

import java.util.logging.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * This is a helper class to generate tabular forms. It is more capable than FormBuilder
 * in that it allows for multiple layouts within the table, by spanning cells where
 * needed. In addition it allows for multiple table sections using multiple TBody's in
 * the generated table. This can be used to create collapsable forms.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 19, 2008
 */
public class TabularFormBuilder {
	static private final Logger LOG = Logger.getLogger(TabularFormBuilder.class.getName());

	/** If a concrete input class is known this contains it's type. */
	private Class< ? > m_currentInputClass;

	/** The current source model for the object containing the properties. */
	private IReadOnlyModel< ? > m_model;

	/** The concrete MetaModel to use for properties within this object. */
	private ClassMetaModel m_classMeta;

	private ModelBindings m_bindings = new ModelBindings();

	private Table m_parentTable;

	/** The current body we're filling in */
	private TBody m_tbody;

	/** Thingy to help calculating access rights (delegate) */
	private final AccessCalculator m_calc = new AccessCalculator();

	/** For columnar mode this is the "next row" where we add a column */
	private int m_colRow;

	/** For columnar mode this is the "current column" we're filling in */
	private int m_colCol;

	/** For append-into, this is the separator to use. When null it defaults to a nbsp. */
	private String m_appendIntoSeparator;

	private String m_appendIntoDefaultSeparator = "\u00a0";

	private enum Mode {
		/** Add each label/input pair in their own row, two cells. */
		NORM,

		/** Add the label/input pair by appending two cells to the last row */
		APPEND,

		/** Add the label/input pair by appending both to the last cell in the last-used(!) row */
		APPEND_INTO,

		/** Set the label/input pair in the specified column. */
		COL
	}

	private Mode m_mode = Mode.NORM;

	private Mode m_nextNodeMode = Mode.NORM;

	private Mode m_nextMode;

	private TR m_lastUsedRow;

	private IControlLabelFactory m_controlLabelFactory;

	/*--------------------------------------------------------------*/
	/*	CODING:	Construction, initialization.						*/
	/*--------------------------------------------------------------*/
	public TabularFormBuilder() {}

	public <T> TabularFormBuilder(final Class<T> clz, final IReadOnlyModel<T> mdl) {
		setClassModel(clz, mdl);
	}

	public IControlLabelFactory getControlLabelFactory() {
		return m_controlLabelFactory;
	}

	public void setControlLabelFactory(final IControlLabelFactory controlLabelFactory) {
		m_controlLabelFactory = controlLabelFactory;
	}

	/**
	 * Set or change the current base class and base model. This can be changed whenever needed.
	 *
	 * @param <T>
	 * @param clz
	 * @param mdl
	 */
	public <T> void setClassModel(final Class<T> clz, final IReadOnlyModel<T> mdl) {
		m_classMeta = MetaManager.findClassMeta(clz);
		m_currentInputClass = clz;
		m_model = mdl;
	}

	/**
	 * Sets the base metamodel and value source to use for obtaining properties.
	 *
	 * @param cmm
	 * @param source
	 */
	public void setMetaModel(final ClassMetaModel cmm, final IReadOnlyModel< ? > source) {
		m_classMeta = cmm;
		m_model = source;
		m_currentInputClass = null;
	}

	/**
	 * Clears the current generated layout and starts a new table.
	 */
	public void reset() {
		m_tbody = null;
		m_parentTable = null;
		m_colRow = 0;
		m_colCol = 0;
	}

	/**
	 * Sets a new table. This resets the current body and stuff.
	 * @param b
	 */
	public void setTable(final Table b) {
		finish(); // Make sure old dude is finished
		m_parentTable = b;
		m_lastUsedRow = null;
		m_tbody = null;
		m_colCol = m_colRow = 0;
	}

	/**
	 * Sets the TBody to use. This resets all layout state.
	 * @param b
	 */
	public void setTBody(final TBody b) {
		finish(); // Make sure old dude is finished
		m_tbody = b;
		m_parentTable = b.getParent(Table.class);
	}

	/**
	 * Creates a new TBody and adds it to the table. This can be used to create multiple re-generatable
	 * layouts within a single layout table. The body inherits the table's core layout.
	 *
	 * @return
	 */
	public TBody newBody() {
		TBody b = new TBody();
		m_parentTable.add(b);
		m_tbody = b;
		m_lastUsedRow = null;
		m_colCol = m_colRow = 0;
		return b;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Core public interface.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Add an input for the specified property. The property is based at the current input
	 * class. The input model is default (using metadata) and the property is labeled using
	 * the metadata-provided label.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param name
	 */
	public void addProp(final String name) {
		addProp(name, (String) null);
	}

	/**
	 * Add an input for the specified property just as <code>addProp(String)</code>,
	 * only this input won't be editable.
	 *
	 * @param name
	 */
	public void addReadOnlyProp(final String name) {
		addReadOnlyProp(name, null);
	}

	/**
	 * Add an input for the specified property. The property is based at the current input
	 * class. The input model is default (using metadata) and the property is labeled.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param name
	 * @param label		The label text to use. Use the empty string to prevent a label from being generated. This still adds an empty cell for the label though.
	 */
	public void addProp(final String name, String label) {
		PropertyMetaModel pmm = resolveProperty(name);
		if(label == null)
			label = pmm.getDefaultLabel();
		addPropertyControl(name, label, pmm, true);
	}

	/**
	/**
	 * Add an input for the specified property just as <code>addProp(String, String)</code>,
	 * only this input won't be editable.
	 *
	 * @param name
	 * @param label
	 */
	public void addReadOnlyProp(final String name, String label) {
		PropertyMetaModel pmm = resolveProperty(name);
		if(label == null)
			label = pmm.getDefaultLabel();
		addPropertyControl(name, label, pmm, false);
	}

	/**
	 * Add the specified properties to the form, in the current mode. Watch out: if a
	 * MODIFIER is in place the modifier is obeyed for <b>all properties</b>, not for
	 * the first one only!! This means that when this gets called like:
	 * <pre>
	 * 	f.append().addProps("a", "b","c");
	 * </pre>
	 * all three fields are appended to the current row.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param names
	 */
	public TabularFormBuilder addProps(final String... names) {
		//-- Store the current mode override && restore after each property (keep mode-override active for each property)
		Mode m = m_nextNodeMode;
		Mode nextm = m_nextMode;

		for(String name : names) {
			m_nextNodeMode = m;
			addProp(name);
		}
		if(nextm != null)
			m_nextNodeMode = nextm; // Cancel mode override
		return this;
	}

	/**
	 * Add the specified properties to the form, just as <code>addProps(String...)</code>,
	 * only these properties won't be editable.
	 * @param names
	 * @return
	 */
	public TabularFormBuilder addReadOnlyProps(final String... names) {
		//-- Store the current mode override && restore after each property (keep mode-override active for each property)
		Mode m = m_nextNodeMode;
		Mode nextm = m_nextMode;

		for(String name : names) {
			m_nextNodeMode = m;
			addReadOnlyProp(name);
		}
		if(nextm != null)
			m_nextNodeMode = nextm; // Cancel mode override
		return this;
	}

	/**
	 * Add a user-specified control for a given property. This adds the control, using
	 * the property-specified label and creates a binding for the property on the
	 * control.
	 *
	 * FORMAL-INTERFACE.
	 *
	 * @param propertyname
	 * @param ctl
	 */
	public <T extends NodeBase & IInputNode< ? >> void addProp(final String propertyname, final T ctl) {
		PropertyMetaModel pmm = resolveProperty(propertyname);
		String label = pmm.getDefaultLabel();
		addControl(label, ctl, new NodeBase[]{ctl}, ctl.isMandatory());
		getBindings().add(new SimpleComponentPropertyBinding(getModel(), pmm, ctl));
	}

	/**
	 *
	 * @param name
	 * @param label
	 * @param pmm
	 * @param editPossible, when false, the rendered control will be display-only and cannot be changed back to EDITABLE.
	 */
	private void addPropertyControl(final String name, final String label, final PropertyMetaModel pmm, final boolean editPossible) {
		//-- Check control permissions: does it have view permissions?
		if(!m_calc.calculate(pmm))
			return;
		final ControlFactory.Result r = createControlFor(m_model, pmm, editPossible && m_calc.isEditable()); // Add the proper input control for that type
		addControl(label, r.getLabelNode(), r.getNodeList(), pmm.isRequired());
		if(r.getBinding() != null)
			m_bindings.add(r.getBinding());
		else
			throw new IllegalStateException("No binding for a " + r);
	}

	/**
	 * Adds a control plus a label at the current location.
	 * @param label
	 * @param labelnode			The node to connect the Label to (for=)
	 * @param mandatory
	 */
	private void addControl(final String label, final NodeBase labelnode, final NodeBase[] list, final boolean mandatory) {
		IControlLabelFactory clf = getControlLabelFactory();
		if(clf == null) {
			clf = DomApplication.get().getControlLabelFactory();
			if(clf == null)
				throw new IllegalStateException("Programmer error: the DomApplication instance returned a null IControlLabelFactory!?!?!?!?");
		}
		Label l = clf.createControlLabel(labelnode, label, true, mandatory);
		//
		//		if(mandatory)
		//			label = "*"+label;
		//		Label	l = new Label(ctl, label);
		modalAdd(l, list);
	}

	public void addLabelAndControl(final String label, final NodeBase control, final boolean mandatory) {
		addControl(label, control, new NodeBase[]{control}, mandatory);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Placement manipulators (public interface)			*/
	/*--------------------------------------------------------------*/

	/**
	 * Add the next field in "normal" mode, then return to the current
	 * mode. All fields added after this will be added on their own
	 * row, with two cells per field (for label and control). This is the default mode.
	 * @return self (chained)
	 */
	public TabularFormBuilder norm() {
		m_nextNodeMode = Mode.NORM;
		m_nextMode = m_mode;
		return this;
	}

	/**
	 * Append the next field, then return to the current mode. This adds
	 * the next pair as two cells added to the current row.
	 *
	 * @return self (chained)
	 */
	public TabularFormBuilder append() {
		m_nextNodeMode = Mode.APPEND;
		m_nextMode = m_mode;
		return this;
	}

	/**
	 * Add the next pair into the last cell of the last row added, then return to the
	 * current mode. This merges >1 control in a single cell, causing them to be close
	 * together.
	 *
	 * @return self (chained)
	 */
	public TabularFormBuilder into() {
		m_nextNodeMode = Mode.APPEND_INTO;
		m_nextMode = m_mode;
		return this;
	}

	/**
	 * Add the next pair into the last cell of the last row added using the specified
	 * string to separate them, then return to the current mode. This merges >1 control
	 * in a single cell, causing them to be close together.
	 *
	 * @return self (chained)
	 */
	public TabularFormBuilder into(final String separator) {
		m_appendIntoSeparator = separator;
		return into();
	}

	/**
	 * Move to column (x) and add this to the first free thingy there. This calculates
	 * a new row position by finding the first row where the column is not yet set. After
	 * the call this returns to the current mode.
	 *
	 * FIXME This changes the (col, row) position of the "COL" mode without resetting it
	 * to it's previous position after the call. This violates the interface. I do not
	 * fix this now since I expect this not to be a problem.
	 *
	 * @param x
	 * @return
	 */
	public TabularFormBuilder col(final int x) {
		m_nextNodeMode = Mode.COL;
		m_nextMode = m_mode;
		m_colCol = x;

		//-- Find the 1st free "column" in the rowset
		int rindex = x * 2;
		m_colRow = 0;
		for(NodeBase b : tbody()) {
			TR tr = (TR) b; // Must be a row
			if(tr.getChildCount() <= rindex)
				return this;
			m_colRow++; // This row has cells in this column -> advance to next
		}
		return this;
	}

	/**
	 * Sets the default mode to NORMAL, causing each field to occupy it's own row containing 2 cells
	 * for label and input control.
	 */
	public void setModeNorm() {
		m_mode = Mode.NORM;
	}

	public void setModeAppend() {
		m_mode = Mode.APPEND;
	}

	public void setModeAppendInto() {
		m_mode = Mode.APPEND_INTO;
	}

	public void setModeAppendInto(final String sepa) {
		m_mode = Mode.APPEND_INTO;
		m_appendIntoDefaultSeparator = sepa;
	}

	public void setModeColumnar(final int col, final int row) {
		m_mode = Mode.COL;
		m_colRow = row;
		m_colCol = col;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple getters and internal stuff.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Get the current ClassMetaModel in effect.
	 * @return
	 */
	protected ClassMetaModel getClassMeta() {
		if(m_classMeta == null)
			throw new IllegalStateException("No ClassMetaModel is known!");
		return m_classMeta;
	}

	/**
	 * Create the optimal control for the specified thingy, and return the binding for it.
	 *
	 * @param container		This will receive all nodes forming the control.
	 * @param model 		The content model used to obtain the Object instance whose property is being edited, for binding purposes.
	 * @param pmm			The property meta for the property to find an editor for.
	 * @param editable		When false this must make a displayonly control.
	 * @return				The binding to bind the control to it's valueset
	 */
	private ControlFactory.Result createControlFor(final IReadOnlyModel< ? > model, final PropertyMetaModel pmm, final boolean editable) {
		ControlFactory cf = DomApplication.get().getControlFactory(pmm, editable);
		return cf.createControl(model, pmm, editable);
	}

	/**
	 * Find a property relative to the current input class.
	 *
	 * @param name
	 * @return
	 */
	protected PropertyMetaModel resolveProperty(final String name) {
		PropertyMetaModel pmm = getClassMeta().findProperty(name);
		if(pmm == null)
			throw new IllegalStateException("Unknown property " + name);
		return pmm;
	}

	public ModelBindings getBindings() {
		return m_bindings;
	}

	public void setBindings(final ModelBindings bindings) {
		if(m_bindings != null && m_bindings.size() > 0)
			LOG.warning("Setting new bindings but current binding list has bindings!! Make sure you use the old list to bind too!!");
		m_bindings = bindings;
	}

	public Class< ? > getCurrentInputClass() {
		if(m_currentInputClass == null)
			throw new IllegalStateException("Usage error: you need to provide a 'current input class' type!!");
		return m_currentInputClass;
	}

	public IReadOnlyModel< ? > getModel() {
		if(m_model == null)
			throw new IllegalStateException("Usage error: you need to provide a 'model accessor'");
		return m_model;
	}

	public Table getTable() {
		return m_parentTable;
	}

	protected TBody tbody() {
		if(m_tbody == null) {
			if(m_parentTable == null)
				m_parentTable = new Table();
			m_tbody = m_parentTable.getBody(); // Force a new body.
		}
		return m_tbody;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Table add modes.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Adds the node to the table, using the current mode. This decides the form placement.
	 */
	private void modalAdd(final Label l, final NodeBase[] ctlcontainer) {
		switch(m_nextNodeMode){
			default:
				throw new IllegalStateException("Invalid table insert mode: " + m_mode);
			case NORM:
				modeAddNormal(l, ctlcontainer);
				break;
			case APPEND:
				modeAddAppend(l, ctlcontainer);
				break;
			case COL:
				modeAddColumnar(l, ctlcontainer);
				break;
			case APPEND_INTO:
				modeAppendInto(l, ctlcontainer);
				break;
		}

		//-- Return to any "next" mode, if applicable
		if(m_nextMode != null) {
			m_nextNodeMode = m_nextMode;
			m_nextMode = null;
		}
		m_appendIntoSeparator = m_appendIntoDefaultSeparator; // Make sure this thingy is reset,
	}

	private void addCells(final TR tr, final NodeBase l, final NodeBase[] c) {
		TD lcell = new TD();
		tr.add(lcell);
		lcell.setCssClass("ui-f-lbl");
		if(l != null)
			lcell.add(l);

		TD ccell = new TD();
		tr.add(ccell);
		ccell.setCssClass("ui-f-in");
		for(NodeBase ch : c)
			ccell.add(ch);
	}

	/**
	 * Adds a node normally, by adding the label and the control in a single row formed by two cells. This
	 * takes the current tbody and adds a row containing 2 cells to it.
	 * @param l
	 * @param c
	 */
	protected void modeAddNormal(final Label l, final NodeBase[] c) {
		m_lastUsedRow = new TR();
		tbody().add(m_lastUsedRow);
		addCells(m_lastUsedRow, l, c);
	}

	/**
	 * This "appends" the new set to the "current" row. It adds another two cells to the row. If
	 * there is no "current row" it gets created.
	 * @param l
	 * @param c
	 */
	protected void modeAddAppend(final Label l, final NodeBase[] c) {
		//-- Find the last used TR in the body.
		if(tbody().getChildCount() == 0 || m_lastUsedRow == null) {
			m_lastUsedRow = new TR();
			tbody().add(m_lastUsedRow);
		}
		addCells(m_lastUsedRow, l, c);
	}

	/**
	 * Sets the control and label into the nodes for a given "column", then advances to the next "row".
	 * @param l
	 * @param c
	 */
	protected void modeAddColumnar(final Label l, final NodeBase[] c) {
		//-- 1. Find the appropriate "row" or make sure it exists.
		while(tbody().getChildCount() <= m_colRow)
			tbody().add(new TR());
		m_lastUsedRow = (TR) tbody().getChild(m_colRow);

		//-- 2. Move to the proper cellpair, or cause them to exist.
		int cindex = 2 * m_colCol;
		TD lcell, ccell;
		while(m_lastUsedRow.getChildCount() <= cindex + 1) {
			TD td = new TD();
			td.setCssClass((m_lastUsedRow.getChildCount() & 1) == 0 ? "ui-f-lbl" : "ui-f-in");
			m_lastUsedRow.add(td);
		}
		lcell = (TD) m_lastUsedRow.getChild(cindex);
		ccell = (TD) m_lastUsedRow.getChild(cindex + 1);

		//-- Set the data into the cells but make sure they're empty
		lcell.removeAllChildren();
		ccell.removeAllChildren();
		if(l != null)
			lcell.add(l);
		for(NodeBase nb : c)
			ccell.add(nb);
		m_colRow++;
	}

	/**
	 * Appends the control to the last cell of the last row used.
	 * @param l
	 * @param c
	 */
	protected void modeAppendInto(final Label l, final NodeBase[] c) {
		if(m_lastUsedRow == null) { // If there's no row-> add one,
			m_lastUsedRow = new TR();
			tbody().add(m_lastUsedRow);
		}

		if(m_lastUsedRow.getChildCount() == 0) { // No cells yet?
			modeAddNormal(l, c); // Then add as normal
			return;
		}

		TD td = (TD) m_lastUsedRow.getChild(m_lastUsedRow.getChildCount() - 1); // Find last td
		if(m_appendIntoSeparator != null && m_appendIntoSeparator.length() > 0)
			td.addLiteral(m_appendIntoSeparator); // Append any string separator

		if(l != null) {
			l.setCssClass("ui-f-lbl");
			td.add(l);
			if(m_appendIntoSeparator != null && m_appendIntoSeparator.length() > 0)
				td.addLiteral(m_appendIntoSeparator); // Append any string separator
		}
		for(NodeBase nb : c)
			td.add(nb);
	}

	/**
	 * This finishes off the current table by calculating colspans for all skewed rows. This discards the
	 * current table!
	 *
	 * @return
	 */
	public Table finish() {
		if(m_parentTable == null)
			return null;

		//-- jal 20090508 MUST clear the table, because when the builder is used for the NEXT tab it must return a new table!
		Table tbl = m_parentTable;
		DomUtil.adjustTableColspans(tbl);
		reset();
		return tbl;
	}
}
