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

import to.etc.domui.component.controlfactory.ModelBindings;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.server.IControlLabelFactory;
import to.etc.domui.util.IReadOnlyModel;

import java.util.Arrays;

/**
 * Deprecated: use {@link to.etc.domui.component2.form4.FormBuilder}.
 * This is a helper class to generate tabular forms. It lays out the form by creating
 * a table and adding labels and input controls in separate table cells in a row. If
 * nothing else is done this creates a form in the layout of:
 * <table>
 * 	<tr><td>First name</td><td><input type="text"></td></tr>
 * 	<tr><td>Last name</td><td><input type="text"></td></tr>
 * 	<tr><td>Address</td><td><input type="text"></td></tr>
 * </table>
 *
 * A form builder is not a component by itself; it is a helper class which <i>creates</i>
 * DomUI nodes AND a {@link ModelBindings} instance containing the bindings of the
 * created components to their class instance and properties. After use you can discard the FormBuilder,
 * provided you retain the bindings if you need them.
 * A formbuilder is highly stateful during use.
 *
 * <p>There are ways to play around with the layout by using <i>format directives</i> <b>before</b>
 * fields are added. Usually layout directives take effect for the <i>next control</i> that is added.
 * There are two kinds of layout directives. Permanent ones change the settting, well, permanently
 * until it is explicitly changed again. Permanent layout directives start with "set" in the call. There
 * are also temporary layout directives, these have no "set" in their name. These take effect for the next
 * the control or the next <i>run of controls</i> if they are added with a single call to the form builder,
 * for instance using addProps(String... names). When the control or controls are added the layout directive
 * returns to it's default setting.</p>
 *
 * <h2>Layout directives</h2>
 * The following layout directives are available:
 * <ul>
 *	<li>append(): this adds the next field(s) as new cells to the last row. Every added component always
 *		adds two cells: a label cell and an input cell containing the component. Components added after
 *		this will appear after the previous added component; because the table itself contains more columns
 *		it means that the first "visual column" will become less wide.</li>
 *	<li>into(): this adds the component in the <i>same cell</i> as the previous one, optionally separated
 *		with some characters as a separator. Because everything remains in a cell the "other" cells do not
 *		become "smaller"</li>
 *	<li>col(x): this defaults to col(0), by using other column numbers you create a form with two
 *		"visible columns" where each "visible column" contains (label, input) pairs. By using the colspan(x)
 *		directive you can spread a single input field over multiple visible columns.</li>
 * </ul>
 * See <a href="http://www.domui.org/wiki/bin/view/Documentation/FormBuilders">the wiki for more details</a>.
 *
 * FIXME Names for adding either property-based or control-based new additions are very unclear.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 19, 2008
 */
@Deprecated
public class TabularFormBuilder extends GenericTableFormBuilder {
	//	/** For columnar mode this is the "next row" where we add a column */
	//	private int m_colRow;

	/** For columnar mode this is the "current column" we're filling in */
	private int m_colCol;

	/** For append-into, this is the separator to use. */
	private String m_appendIntoSeparator;

	private String m_appendIntoDefaultSeparator;

	private enum Mode {
		/** Add each label/input pair in their own row, two cells. */
		NORM,

		/** Add the label/input pair by appending two cells to the last row */
		APPEND,

		/** Add the label/input pair by appending both to the last cell in the last-used(!) row */
		APPEND_INTO,

		//		/** Set the label/input pair in the specified column. */
		//		COL
	}

	private Mode m_mode = Mode.NORM;

	private Mode m_nextNodeMode = Mode.NORM;

	private Mode m_nextMode;

	/** Current max column count. */
	private int m_maxColumns;

	/** The "current row number" where items are added for the specified column. */
	private int[] m_columnRowCount = new int[20];

	/** The next column span to apply to a row. */
	private int m_nextColSpan = 1;

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

	/**
	 * Reset variables after finish. Cannot be done when table is created!
	 */
	@Override
	public NodeContainer finish() {
		m_colCol = 0;
		m_nextColSpan = 1;
		m_maxColumns = 1;
		Arrays.fill(m_columnRowCount, 0);
		return super.finish();
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
	public void addControl(final String label, final NodeBase labelnode, final NodeBase[] list, final boolean mandatory, boolean editable, PropertyMetaModel< ? > pmm) {
		Label l = createLabel(label, labelnode, mandatory, editable, pmm);
		modalAdd(l, list, editable);
	}

	/**
	 * Control label factory method. Method can be used to manually generate labels in same manner as it is done by tabular form builder internally.
	 * @param label
	 * @param labelnode
	 * @param mandatory
	 * @param editable
	 * @param pmm
	 * @return
	 */
	public Label createLabel(final String label, final NodeBase labelnode, final boolean mandatory, boolean editable, PropertyMetaModel< ? > pmm) {
		IControlLabelFactory clf = getControlLabelFactory();
		if(clf == null) {
			clf = getBuilder().getControlLabelFactory();
			if(clf == null)
				throw new IllegalStateException("Programmer error: the DomApplication instance returned a null IControlLabelFactory!?!?!?!?");
		}
		Label l = clf.createControlLabel(labelnode, label, editable, mandatory, pmm);
		return l;
	}

	@Override
	public void addControl(final NodeBase label, final NodeBase labelnode, final NodeBase[] list, final boolean mandatory, boolean editable, PropertyMetaModel< ? > pmm) {
		modalAdd(label, list, editable);
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
	 * Add the next fields in a set to the specified column. After the fields return to
	 * the "previous" current column.
	 *
	 * FIXME This changes the (col, row) position of the "COL" mode without resetting it
	 * to it's previous position after the call. This violates the interface. I do not
	 * fix this now since I expect this not to be a problem.
	 *
	 * @param x
	 * @return
	 */
	public TabularFormBuilder col(final int x) {
		return setCol(x);
	}


	/**
	 * Make column(x) the "current" column where things are added to, until another call
	 * is done.
	 *
	 * Move to column (x) and add this to the first free thingy there. This calculates
	 * a new row position by finding the first row where the column is not yet set. After
	 * the call this returns to the current mode.
	 * @param x
	 * @return
	 */
	public TabularFormBuilder setCol(final int x) {
		if(x < 0 || x >= m_columnRowCount.length)
			throw new IllegalArgumentException("Column number " + x + " invalid.");

		//		m_nextNodeMode = Mode.COL;
		//		m_nextMode = m_mode;
		m_colCol = x;
		if(m_maxColumns <= m_colCol)
			m_maxColumns = m_colCol + 1;

		//		//-- Find the 1st free "column" in the rowset
		//		int rindex = x * 2;
		//		m_colRow = 0;
		//		for(NodeBase b : tbody()) {
		//			TR tr = (TR) b; // Must be a row
		//			if(tr.getChildCount() <= rindex)
		//				return this;
		//			m_colRow++; // This row has cells in this column -> advance to next
		//		}
		return this;
	}

	/**
	 * Sets the colspan for the next component set. When the set has been added the
	 * value returns to 1.
	 * @param cells
	 * @return
	 */
	public TabularFormBuilder colspan(final int cells) {
		if(cells <= 0 || cells >= m_columnRowCount.length)
			throw new IllegalArgumentException("Cell count of " + cells + " is invalid.");
		m_nextColSpan = cells;
		return this;
	}

	/**
	 * Sets the default mode to NORMAL, causing each field to occupy it's own row containing 2 cells
	 * for label and input control.
	 */
	public void setModeNorm() {
		m_mode = Mode.NORM;
		m_nextNodeMode = Mode.NORM;
	}

	public void setModeAppend() {
		m_nextNodeMode = m_mode = Mode.APPEND;
	}

	public void setModeAppendInto() {
		m_nextNodeMode = m_mode = Mode.APPEND_INTO;
	}

	public void setModeAppendInto(final String sepa) {
		m_nextNodeMode = m_mode = Mode.APPEND_INTO;
		m_appendIntoDefaultSeparator = sepa;
	}

	//	public void setModeColumnar(final int col, final int row) {
	//		m_nextNodeMode = m_mode = Mode.COL;
	//		m_colRow = row;
	//		m_colCol = col;
	//	}

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
			//			case COL:
			//				modeAddColumnar(l, ctlcontainer, editable);
			//				break;
			case APPEND_INTO:
				modeAppendInto(l, ctlcontainer, editable);
				break;
		}

		//-- Return to any "next" mode, if applicable
		if(m_nextMode != null) {
			m_nextNodeMode = m_nextMode;
			m_nextMode = null;
		}
		m_nextColSpan = 1;
		m_appendIntoSeparator = m_appendIntoDefaultSeparator; // Make sure this thingy is reset,
	}

	@Deprecated
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
	 * For the specified row, this selects the specified cell. All cells before it
	 * are created if this cell does not yet exist.
	 * @param row
	 * @param cellindex
	 * @return
	 */
	private TD selectCell(TR row, int cellindex) {
		int curx = 0;
		int actix = 0;
		for(;;) {
			//-- Get or add the cell.
			TD td;
			if(actix >= row.getChildCount()) {
				//-- No cell here. Add a cell, and exit if this cell is the one we need
				td = new TD();
				td.setCssClass("ui-f-empty");
				row.add(td);
				actix++;
				if(curx >= cellindex)
					return td;
				curx++;
			} else {
				/*
				 * Cell here. If it has a colspan...
				 */
				td = (TD) row.getChild(actix++);
				int csp = td.getColspan();
				if(csp <= 0)
					csp = 1;

				//-- Have cell, will travel. If we now reached the exact cell # return it regardless of colspan
				if(curx == cellindex)
					return td;
				else if(curx > cellindex)
					throw new IllegalStateException("Internal: cellindex=" + cellindex + ", curx=" + curx + ", actual=" + actix);

				//-- This is not the one... Add in the colspan to see what the next col# would be
				curx += csp;
				if(curx > cellindex) {
					/*
					 * The current cell "overflows" into the selected one: this means
					 * the cells before the one we need overflow the current one. In
					 * that case we return null, indicating that the row is "full".
					 */
					return null;
				}
			}
		}
	}

	/**
	 * This ensures that the specified cell can be extended to the specified
	 * columnar width, specified in table cells. If colspan = 1 this always works,
	 * for others this works if all cells after is are either missing or empty for
	 * the required width.
	 *
	 * @param corecell
	 * @param numcols
	 * @return
	 */
	private boolean mergeCells(TD corecell, int numcols) {
		if(numcols <= 0)
			throw new IllegalArgumentException();
		if(numcols == 1)
			return true;

		//-- Walk all cell inputs after this one and make sure they're empty;
		TR	row = (TR) corecell.getParent();
		int ix = row.findChildIndex(corecell);
		if(ix < 0)
			throw new IllegalStateException();	// Cannot happen
		ix++;
		TD[] cand = new TD[numcols]; // Will collect all TD's to remove because this TD will use their alloted col location
		int remove = 0; // The #of tds in the above array that need to be removed
		int nleft = numcols - 1; // The #of columns we need to merge, in total, will be >= 1.
		while(ix < row.getChildCount() && nleft > 0) {
			TD td = (TD) row.getChild(ix++); // Get the cell following this.
			if(td.getChildCount() != 0) {
				//-- This cell is filled- the row is full.
				return false;
			}

			//-- Empty cell. How wide?
			int csp = td.getColspan();
			if(csp <= 0)
				csp = 1;
			nleft -= csp;
			if(nleft < 0)
				return false; // Unbalanced.

			//-- Ok, this cell is acceptable but needs to be removed.
			cand[remove++] = td;
		}

		//-- Ok: we can manage ;-) Remove all cells to remove
		while(remove > 0)
			row.removeChild(cand[--remove]);
		corecell.setColspan(numcols);
		return true;
	}

	/**
	 * Adds a node normally. If the current column is 0 this just adds a row and two cells, else
	 * it will locate the "current" row in the specified column and add cells there.
	 *
	 * @param l
	 * @param c
	 */
	protected void modeAddNormal(final NodeBase l, final NodeBase[] c, boolean editable) {
		/*
		 * Now we need to add the cells at the correct column index.
		 */
		tbody(); // Trigger body/table creation
		int tcellix = m_colCol * 2; // Thje formal table cell index.
		for(;;) {
			int rowix = m_columnRowCount[m_colCol]; // Get && increment current row# in this column.
			TR row = selectRow(rowix);

			TD lbltd = selectCell(row, tcellix);
			if(lbltd != null) {
				TD inptd = selectCell(row, tcellix + 1);
				if(inptd != null) {
					//-- We have cell candidates... Try to add here. Does the required cellspan fit here?
					if(mergeCells(inptd, m_nextColSpan * 2 - 1)) { // each "fb col" takes 2 table cols, minus the label column
						//-- Acceptable.
						addNodes(lbltd, inptd, l, c, editable);
						setLastUsedRow(row);
						setLastUsedCell(inptd);
						return;
					}
				}
			}

			//-- This row is full. Move to the next one and try it there.
		}
	}

	private void addNodes(TD lbltd, TD inptd, final NodeBase l, final NodeBase[] c, boolean editable) {
		lbltd.setCssClass("ui-f-lbl");
		if(l != null)
			lbltd.add(l);

		inptd.setCssClass(editable ? "ui-f-in" : "ui-f-do");
		for(NodeBase ch : c)
			inptd.add(ch);
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

	@Override
	public void onRowAdded(TR row) {
		m_columnRowCount[m_colCol]++; // increment current row# in this column.
	}
}
