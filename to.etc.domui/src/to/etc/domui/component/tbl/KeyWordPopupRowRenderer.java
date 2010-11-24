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
package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;

/**
 * This is simplified row renderer that is used ad default render for popup results in keyword search.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 27 Jan 2010
 */
public class KeyWordPopupRowRenderer<T> implements IRowRenderer<T> {
	/** The class whose instances we'll render in this table. */
	private final Class<T> m_dataClass;

	final private ClassMetaModel m_metaModel;

	/** When the definition has completed (the object is used) this is TRUE; it disables all calls that change the definition */
	private boolean m_completed;

	protected final List<SimpleColumnDef> m_columnList = new ArrayList<SimpleColumnDef>();

	private ICellClicked< ? > m_rowClicked;

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple renderer initialization && parameterisation	*/
	/*--------------------------------------------------------------*/
	/**
	 * Create a renderer by handling the specified class and a list of properties off it.
	 * @param dataClass
	 * @param cols
	 */
	public KeyWordPopupRowRenderer(@Nonnull final Class<T> dataClass, @Nonnull final ClassMetaModel cmm, final String... cols) {
		this(dataClass, cmm);
		List<ExpandedDisplayProperty> xdpl;
		if(cols.length != 0)
			xdpl = ExpandedDisplayProperty.expandProperties(cmm, cols);
		else {
			final List<DisplayPropertyMetaModel> dpl = cmm.getTableDisplayProperties();
			if(dpl.size() == 0)
				throw new IllegalStateException("The list-of-columns to show is empty, and the class has no metadata (@MetaObject) defining a set of columns as default table columns, so there.");
			xdpl = ExpandedDisplayProperty.expandDisplayProperties(dpl, cmm, null);
		}
		addColumns(xdpl);
	}

	protected KeyWordPopupRowRenderer(@Nonnull Class<T> data, @Nonnull ClassMetaModel cmm) {
		m_dataClass = data;
		m_metaModel = cmm;
	}

	/**
	 * Returns the metamodel used.
	 * @return
	 */
	protected ClassMetaModel model() {
		return m_metaModel;
	}

	/**
	 * Returns the record type being rendered.
	 * @return
	 */
	protected Class< ? > getActualClass() {
		return m_dataClass;
	}

	/**
	 * Throws an exception if this renderer has been completed and is unmutable.
	 */
	protected void check() {
		if(m_completed)
			throw new IllegalStateException("Programmer error: This object has been USED and cannot be changed anymore");
	}

	/**
	 * Complete this object if it is not already complete (internal).
	 */
	protected void complete(final TableModelTableBase<T> tbl) {
		m_completed = true;
	}

	/**
	 * Check if this object is used (completed) and thereby unmodifyable (internal).
	 * @return
	 */
	protected boolean isComplete() {
		return m_completed;
	}

	/**
	 * Return the definition for the nth column. You can change the column's definition there.
	 * @param ix
	 * @return
	 */
	public SimpleColumnDef getColumn(final int ix) {
		if(ix < 0 || ix >= m_columnList.size())
			throw new IndexOutOfBoundsException("Column " + ix + " does not exist (yet?)");
		return m_columnList.get(ix);
	}

	/**
	 * Return the #of columns in this renderer.
	 * @return
	 */
	public int getColumnCount() {
		return m_columnList.size();
	}

	/**
	 * Find a column by the property it is displaying. This only works for that kind of columns, and will
	 * not work for any joined columns defined from metadata. If no column exists for the specified property
	 * this will throw an exception.
	 * @param propertyName
	 * @return
	 */
	public SimpleColumnDef getColumnByName(String propertyName) {
		for(SimpleColumnDef scd : m_columnList) {
			if(propertyName.equals(scd.getPropertyName()))
				return scd;
		}
		throw new ProgrammerErrorException("The property with the name '" + propertyName + "' is undefined in this RowRenderer - perhaps metadata has changed?");
	}

	/**
	 * Convenience method to set the column's cell renderer; replacement for getColumn(index).setRenderer().
	 * @param index
	 * @param renderer
	 */
	public void setNodeRenderer(final int index, final INodeContentRenderer< ? > renderer) {
		check();
		getColumn(index).setContentRenderer(renderer);
	}

	/**
	 * Convenience method to get the column's cell renderer; replacement for getColumn(index).getRenderer().
	 * @param index
	 * @return
	 */
	public INodeContentRenderer< ? > getNodeRenderer(final int index) {
		return getColumn(index).getContentRenderer();
	}


	/**
	 * When set each row will be selectable (will react when the mouse hovers over it), and when clicked will call this handler.
	 * @return
	 */
	public ICellClicked< ? > getRowClicked() {
		return m_rowClicked;
	}

	/**
	 * When set each row will be selectable (will react when the mouse hovers over it), and when clicked will call this handler.
	 * @param rowClicked
	 */
	public void setRowClicked(final ICellClicked< ? > rowClicked) {
		m_rowClicked = rowClicked;
	}

	/**
	 * This gets called by the data table component just before it starts to render
	 * a new page. When called the query has not yet been done and nothing is rendered
	 * for this object. This exposes the actual model that will be used during the rendering
	 * process and allows this component to define sorting, if needed.
	 *
	 * @see to.etc.domui.component.tbl.IRowRenderer#beforeQuery(to.etc.domui.component.tbl.DataTable)
	 */
	@Override
	public void beforeQuery(final TableModelTableBase<T> tbl) throws Exception {
		complete(tbl);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Actual rendering: a row.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.component.tbl.IRowRenderer#renderRow(to.etc.domui.component.tbl.ColumnContainer, int, java.lang.Object)
	 */
	@Override
	public void renderRow(final TableModelTableBase<T> tbl, final ColumnContainer<T> cc, final int index, final T instance) throws Exception {
		if(m_rowClicked != null) {
			cc.getTR().setClicked(new IClicked<TR>() {
				@Override
				@SuppressWarnings("unchecked")
				public void clicked(final TR b) throws Exception {
					((ICellClicked<T>) getRowClicked()).cellClicked(tbl.getPage(), b, instance);
				}
			});
			cc.getTR().addCssClass("ui-keyword-popup-row");
		}

		//must be set as bug fix for IE table rendering
		Object tblBase = cc.getTR().getParent(Table.class);
		if(tblBase instanceof Table) {
			((Table) tblBase).setWidth("100%");
			((Table) tblBase).setOverflow(Overflow.HIDDEN);
		}

		for(final SimpleColumnDef cd : m_columnList) {
			renderColumn(tbl, cc, index, instance, cd);
		}
	}

	/**
	 * Render a single column fully.
	 * @param tbl
	 * @param cc
	 * @param index
	 * @param instance
	 * @param cd
	 * @throws Exception
	 */
	protected <X> void renderColumn(final TableModelTableBase<T> tbl, final ColumnContainer<T> cc, final int index, final T instance, final SimpleColumnDef cd) throws Exception {
		//-- If a value transformer is known get the column value, else just use the instance itself (case when Renderer is used)
		X colval;
		if(cd.getValueTransformer() == null)
			colval = (X) instance;
		else
			colval = (X) cd.getValueTransformer().getValue(instance);

		//-- Is a node renderer used?
		TD cell;
		Div wrapDiv = new Div();
		wrapDiv.setCssClass("no-wrap");
		cell = cc.add((NodeBase) null); // Add the new row
		cell.add(wrapDiv); // Add no-wrap div

		if(null != cd.getContentRenderer()) {
			((INodeContentRenderer<Object>) cd.getContentRenderer()).renderNodeContent(tbl, wrapDiv, colval, instance); // %&*(%&^%*&%&( generics require casting here
		} else {
			String s;
			if(colval == null)
				s = null;
			else {
				if(cd.getPresentationConverter() != null)
					s = ((IConverter<X>) cd.getPresentationConverter()).convertObjectToString(NlsContext.getLocale(), colval);
				else
					s = String.valueOf(colval);
			}
			if(s != null) {
				wrapDiv.add(s);
			}
		}

		if(cd.getAlign() != null)
			cell.setTextAlign(cd.getAlign());
		else if(cd.getCssClass() != null) {
			cell.addCssClass(cd.getCssClass());
		}
	}

	@Override
	public void renderHeader(TableModelTableBase<T> tbl, HeaderContainer<T> cc) throws Exception {
	//empty since header is not rendered.
	}

	protected void addColumns(final List<ExpandedDisplayProperty> xdpl) {
		for(final ExpandedDisplayProperty xdp : xdpl) {
			if(xdp instanceof ExpandedDisplayPropertyList) {
				//-- Flatten: call for subs recursively.
				final ExpandedDisplayPropertyList xdl = (ExpandedDisplayPropertyList) xdp;
				addColumns(xdl.getChildren());
				continue;
			}

			//-- Create a column def from the metadata
			final SimpleColumnDef scd = new SimpleColumnDef(xdp);
			m_columnList.add(scd); // ORDER!

			if(scd.getNumericPresentation() != null && scd.getNumericPresentation() != NumericPresentation.UNKNOWN) {
				scd.setCssClass("ui-numeric");
				scd.setHeaderCssClass("ui-numeric");
			}
		}
	}
}
