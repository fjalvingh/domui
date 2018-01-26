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

import to.etc.domui.server.RequestContextImpl;

import javax.annotation.*;


/**
 * Delegate for a table which must render a row of items from a single row object.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public interface IRowRenderer<T> {
	void beforeQuery(@Nonnull TableModelTableBase<T> tbl) throws Exception;

	void renderRow(@Nonnull TableModelTableBase<T> tbl, @Nonnull ColumnContainer<T> cc, int index, @Nonnull T instance) throws Exception;

	/**
	 * Render table header.
	 * @param tbl
	 * @param cc
	 * @throws Exception
	 */
	void renderHeader(@Nonnull TableModelTableBase<T> tbl, @Nonnull HeaderContainer<T> cc) throws Exception;

	/**
	 * Return the row clicked handler to use.
	 * @return
	 */
	@Nullable
	ICellClicked<T> getRowClicked();

	default void updateWidths(@Nonnull TableModelTableBase<T> tbl, @Nonnull RequestContextImpl context) throws Exception {}
}
