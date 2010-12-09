package to.etc.domui.component.form;

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
 * FIXME Names for adding either property-based or control-based new additions are very unclear.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 19, 2008
 */
public class TabularFormBuilder extends GenericTableFormBuilder {
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

	/*--------------------------------------------------------------*/
	/*	CODING:	Construction, initialization.						*/
	/*--------------------------------------------------------------*/
	public TabularFormBuilder() {}

	public <T> TabularFormBuilder(final Class<T> clz, final IReadOnlyModel<T> mdl) {
		setClassModel(clz, mdl);
	}

	public <T> TabularFormBuilder(T instance) {
		super(instance);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	GenericTableFormBuilder extensions.					*/
	/*--------------------------------------------------------------*/
	@Override
	protected void internalClearLocation() {
		m_colRow = 0;
		m_colCol = 0;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	GenericFormBuilder implementation.					*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 * This one handles mode maintenance while placing the individual controls so the entire list added here
	 * obeys the "current" mode setting, not just the 1st control added.
	 */
	@Override
	protected IControl< ? >[] addListOfProperties(boolean editable, final String... names) {
		//-- Store the current mode override && restore after each property (keep mode-override active for each property)
		Mode m = m_nextNodeMode;
		Mode nextm = m_nextMode;

		IControl< ? >[] res = new IControl< ? >[names.length];
		int ix = 0;
		for(String name : names) {
			m_nextNodeMode = m;
			if(editable)
				res[ix] = addProp(name);
			else
				res[ix] = addDisplayProp(name);
			ix++;
		}
		if(nextm != null)
			m_nextNodeMode = nextm; // Cancel mode override
		return res;
	}

	/**
	 * Adds a control plus a label at the current location.
	 * @param label
	 * @param labelnode			The node to connect the Label to (for=)
	 * @param mandatory
	 */
	@Override
	public void addControl(final String label, final NodeBase labelnode, final NodeBase[] list, final boolean mandatory, boolean editable, PropertyMetaModel pmm) {
		IControlLabelFactory clf = getControlLabelFactory();
		if(clf == null) {
			clf = getBuilder().getControlLabelFactory();
			if(clf == null)
				throw new IllegalStateException("Programmer error: the DomApplication instance returned a null IControlLabelFactory!?!?!?!?");
		}
		Label l = clf.createControlLabel(labelnode, label, editable, mandatory, pmm);
		modalAdd(l, list, editable);
	}

	@Override
	public void addContent(NodeBase label, NodeBase[] control, boolean editable) {
		modalAdd(label, control, editable);
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
	/*	CODING:	Table add modes.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Adds the node to the table, using the current mode. This decides the form placement.
	 */
	private void modalAdd(final NodeBase l, final NodeBase[] ctlcontainer, boolean editable) {
		switch(m_nextNodeMode){
			default:
				throw new IllegalStateException("Invalid table insert mode: " + m_mode);
			case NORM:
				modeAddNormal(l, ctlcontainer, editable);
				break;
			case APPEND:
				modeAddAppend(l, ctlcontainer, editable);
				break;
			case COL:
				modeAddColumnar(l, ctlcontainer, editable);
				break;
			case APPEND_INTO:
				modeAppendInto(l, ctlcontainer, editable);
				break;
		}

		//-- Return to any "next" mode, if applicable
		if(m_nextMode != null) {
			m_nextNodeMode = m_nextMode;
			m_nextMode = null;
		}
		m_appendIntoSeparator = m_appendIntoDefaultSeparator; // Make sure this thingy is reset,
	}

	private void addCells(final TR tr, final NodeBase l, final NodeBase[] c, boolean editable) {
		TD lcell = new TD();
		tr.add(lcell);
		lcell.setCssClass("ui-f-lbl");
		if(l != null)
			lcell.add(l);

		TD ccell = new TD();
		tr.add(ccell);
		ccell.setCssClass(editable ? "ui-f-in" : "ui-f-do");
		for(NodeBase ch : c)
			ccell.add(ch);
	}

	/**
	 * Adds a node normally, by adding the label and the control in a single row formed by two cells. This
	 * takes the current tbody and adds a row containing 2 cells to it.
	 * @param l
	 * @param c
	 */
	protected void modeAddNormal(final NodeBase l, final NodeBase[] c, boolean editable) {
		addRow();
		addCells(row(), l, c, editable);
	}

	/**
	 * This "appends" the new set to the "current" row. It adds another two cells to the row. If
	 * there is no "current row" it gets created.
	 * @param l
	 * @param c
	 */
	protected void modeAddAppend(final NodeBase l, final NodeBase[] c, boolean editable) {
		//-- Find the last used TR in the body.
		if(tbody().getChildCount() == 0 || getLastUsedRow() == null) { // FIXME Why this exhaustive test? Null lastrow should be enough?
			addRow();
		}
		addCells(row(), l, c, editable);
	}

	/**
	 * Sets the control and label into the nodes for a given "column", then advances to the next "row".
	 * @param l
	 * @param c
	 */
	protected void modeAddColumnar(final NodeBase l, final NodeBase[] c, boolean editable) {
		//-- 1. Find the appropriate "row" or make sure it exists.
		TR tr = selectRow(m_colRow);

		//-- 2. Move to the proper cellpair, or cause them to exist.
		int cindex = 2 * m_colCol;
		TD lcell, ccell;
		while(tr.getChildCount() <= cindex + 1) {
			TD td = new TD();
			td.setCssClass((tr.getChildCount() & 1) == 0 ? "ui-f-lbl" : "ui-f-in");
			tr.add(td);
		}
		lcell = (TD) tr.getChild(cindex);
		ccell = (TD) tr.getChild(cindex + 1);

		//-- Set the data into the cells but make sure they're empty
		lcell.removeAllChildren();
		ccell.removeAllChildren();
		if(l != null)
			lcell.add(l);
		for(NodeBase nb : c)
			ccell.add(nb);
		ccell.setCssClass(editable ? "ui-f-in" : "ui-f-do");
		m_colRow++;
	}

	/**
	 * Appends the control to the last cell of the last row used.
	 * @param l
	 * @param c
	 */
	protected void modeAppendInto(final NodeBase l, final NodeBase[] c, boolean editable) {
		TR tr = row(); // If there's no row-> add one,

		if(tr.getChildCount() == 0) { // No cells yet?
			modeAddNormal(l, c, editable); // Then add as normal
			return;
		}

		TD td = (TD) tr.getChild(tr.getChildCount() - 1); // Find last td
		if(m_appendIntoSeparator != null && m_appendIntoSeparator.length() > 0)
			td.add(m_appendIntoSeparator); // Append any string separator

		if(l != null) {
			l.setCssClass("ui-f-lbl");
			td.add(l);
			if(m_appendIntoSeparator != null && m_appendIntoSeparator.length() > 0)
				td.add(m_appendIntoSeparator); // Append any string separator
		}
		for(NodeBase nb : c)
			td.add(nb);
	}
}
