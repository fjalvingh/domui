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

/**
 * The model for a table. This is the abstract type.
 * A table model contains a list of objects accessible by index and by
 * key. Access by index is used to handle paging.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public interface ITableModel<T> {
	/**
	 * Default size used to truncate results in case of large data sets as query results, if no other limit has been set.
	 */
	int DEFAULT_MAX_SIZE = 1000;

	/**
	 * Size used to truncate results in case of in memory filtering or sorting of large data sets as query results.
	 */
	int IN_MEMORY_FILTER_OR_SORT_MAX_SIZE = 8000;

	@Nonnull
	List<T> getItems(int start, int end) throws Exception;

	/**
	 * This must return the total #of rows in this table.
	 * @return
	 */
	int getRows() throws Exception;

	void addChangeListener(@Nonnull ITableModelListener<T> l);

	void removeChangeListener(@Nonnull ITableModelListener<T> l);

	void refresh();
}
