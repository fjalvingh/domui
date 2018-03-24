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
package to.etc.domui.component.input;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * This is simplified row renderer that is used ad default render for popup results in keyword search.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 27 Jan 2010
 */
final class KeyWordPopupRowRenderer<T> implements IRowRenderer<T>, IClickableRowRenderer<T> {
	@Nullable
	private ICellClicked<T> m_rowClicked;

	/** When the definition has completed (the object is used) this is TRUE; it disables all calls that change the definition */
	private boolean m_completed;

	@Nonnull
	private final ColumnDefList<T> m_columnList;

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple renderer initialization && parameterization	*/
	/*--------------------------------------------------------------*/
	/**
	 * Create a renderer by handling the specified class and a list of properties off it.
	 */
	KeyWordPopupRowRenderer(@Nonnull final ClassMetaModel cmm) {
		m_columnList = new ColumnDefList<T>((Class<T>) cmm.getActualClass(), cmm);
	}

	/**
	 * Throws an exception if this renderer has been completed and is unmutable.
	 */
	private void check() {
		if(m_completed)
			throw new IllegalStateException("Programmer error: This instance has been USED and cannot be changed anymore");
	}

	/**
	 * When set each row will be selectable (will react when the mouse hovers over it), and when clicked will call this handler.
	 * @return
	 */
	@Override
	@Nullable
	public ICellClicked<T> getRowClicked() {
		return m_rowClicked;
	}

	/**
	 * When set each row will be selectable (will react when the mouse hovers over it), and when clicked will call this handler.
	 */
	@Override
	public void setRowClicked(@Nonnull ICellClicked<T> rowClicked) {
		m_rowClicked = rowClicked;
	}

	@Override public void setCellClicked(int col, @Nullable ICellClicked<T> cellClicked) {
		throw new IllegalStateException("Not supported");
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
	public void beforeQuery(final @Nonnull TableModelTableBase<T> tbl) throws Exception {
		if(m_columnList.size() == 0)
			addDefaultColumns();
		m_completed = true;
	}

	@Override
	public void renderHeader(@Nonnull TableModelTableBase<T> tbl, @Nonnull HeaderContainer<T> cc) throws Exception {
		//-- Do not render a header.
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Actual rendering: a row.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.component.tbl.IRowRenderer#renderRow(to.etc.domui.component.tbl.ColumnContainer, int, java.lang.Object)
	 */
	@Override
	public void renderRow(final @Nonnull TableModelTableBase<T> tbl, final @Nonnull ColumnContainer<T> cc, final int index, final @Nonnull T instance) throws Exception {
		final ICellClicked< ? > rowClicked = m_rowClicked;
		if(rowClicked != null) {
			cc.getTR().setClicked(new IClicked<TR>() {
				@Override
				public void clicked(final @Nonnull TR b) throws Exception {
					ICellClicked< ? > rowClicked = getRowClicked();
					if(null != rowClicked)
						((ICellClicked<T>) rowClicked).cellClicked(instance);
				}
			});
			cc.getTR().addCssClass("ui-keyword-popup-row");
		}

		//must be set as bug fix for IE table rendering
		Object tblBase = cc.getTR().findParent(Table.class);
		if(tblBase instanceof Table) {
			((Table) tblBase).setWidth("100%");
			((Table) tblBase).setOverflow(Overflow.HIDDEN);
		}

		for(final SimpleColumnDef< ? > cd : m_columnList) {
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
	private <X> void renderColumn(final TableModelTableBase<T> tbl, final ColumnContainer<T> cc, final int index, final T instance, final SimpleColumnDef<X> cd) throws Exception {
		//-- If a value transformer is known get the column value, else just use the instance itself (case when Renderer is used)
		X colval;
		IValueTransformer< ? > vtr = cd.getValueTransformer();
		if(vtr == null)
			colval = (X) instance;
		else
			colval = (X) vtr.getValue(instance);

		//-- Is a node renderer used?
		TD cell;
		Div wrapDiv = new Div();
		wrapDiv.setCssClass("no-wrap");
		cell = cc.add((NodeBase) null); // Add the new row
		cell.add(wrapDiv); // Add no-wrap div

		IRenderInto< ? > contentRenderer = cd.getContentRenderer();
		if(null != contentRenderer) {
			((IRenderInto<Object>) contentRenderer).renderOpt(wrapDiv, colval);
			//((IRenderInto<Object>) contentRenderer).render(tbl, wrapDiv, colval, instance);
		} else {
			String s;
			if(colval == null)
				s = null;
			else {
				IObjectToStringConverter<X> presentationConverter = cd.getPresentationConverter();
				if(presentationConverter != null)
					s = presentationConverter.convertObjectToString(NlsContext.getLocale(), colval);
				else
					s = String.valueOf(colval);
			}
			if(s != null) {
				wrapDiv.add(s);
			}
		}

		if(cd.getAlign() != null)
			cell.setTextAlign(cd.getAlign());
		else {
			String cssc = cd.getCssClass();
			if(cssc != null) {
				cell.addCssClass(cssc);
			}
		}
	}

	public void add(SimpleColumnDef< ? > cd) {
		check();
		m_columnList.add(cd);
	}

	public void addColumn(String name) {
		m_columnList.addColumns(name);
	}

	public <R> void addColumns(Object... cols) {
		check();
		if(cols.length != 0)
			m_columnList.addColumns(cols);
	}

	public void addDefaultColumns() {
		check();
		m_columnList.addDefaultColumns();
	}
}
