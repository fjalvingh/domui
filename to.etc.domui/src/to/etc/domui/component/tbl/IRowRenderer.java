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


/**
 * Delegate for a table which must render a row of items from a single row object.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public interface IRowRenderer<T> {
	void beforeQuery(TableModelTableBase<T> tbl) throws Exception;

	void renderRow(TableModelTableBase<T> tbl, ColumnContainer<T> cc, int index, T instance) throws Exception;

	/**
	 * Render table header.
	 * @param tbl
	 * @param cc
	 * @throws Exception
	 */
	void renderHeader(TableModelTableBase<T> tbl, HeaderContainer<T> cc) throws Exception;

	ICellClicked< ? > getRowClicked();

	//
	//	/**
	//	 * If a table has a selectable model, this gets called to change the visual appearance
	//	 * of a <i>rendered</i> row when it is selected/deselected. The row is present on screen;
	//	 * the renderer should know how to modify the row in such a way that it is visually
	//	 * consistent. Because this can also imply changes to the header it is passed in too.
	//	 *
	//	 * @param tbl
	//	 * @param row
	//	 * @param instance
	//	 * @param on 			T if the selection should be rendered as ON.
	//	 */
	//	void renderSelectionChanged(TableModelTableBase<T> tbl, TR headerrow, TR row, T instance, boolean on) throws Exception;
}
