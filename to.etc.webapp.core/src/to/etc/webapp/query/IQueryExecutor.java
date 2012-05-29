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
package to.etc.webapp.query;

import java.util.*;

import javax.annotation.*;

public interface IQueryExecutor<C extends QDataContext> {
	/**
	 * Execute the query specified by q and return a list of results. Before and after the query execution all
	 * registered listeners will be called.
	 *
	 * @param <T>	The return type for this query, a persistent class type
	 * @param q		The selection criteria
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	<T> List<T> query(@Nonnull C root, @Nonnull QCriteria<T> q) throws Exception;

	/**
	 * Issue a selection query, where multiple fields or projections on fields are selected from a base class.
	 * @param sel
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	List<Object[]> query(@Nonnull C root, @Nonnull QSelection< ? > sel) throws Exception;

	/**
	 * Load the persistent object with the specified type and primary key from the database. This will
	 * execute an actual select in the database if the object is not yet cached, ensuring that the
	 * object actually exists. If the object does not exist this will return null.
	 *
	 * @param <T>	The object type.
	 * @param clz	The persistent class for which an instance is being sought.
	 * @param pk	The PK for the instance required.
	 * @return		Null if the instance does not exist, the actual and fully initialized instance (or proxy) otherwise.
	 * @throws Exception
	 */
	@Nullable
	<T> T find(@Nonnull C root, @Nonnull Class<T> clz, @Nonnull Object pk) throws Exception;

	@Nullable
	<T> T find(@Nonnull C root, @Nonnull ICriteriaTableDef<T> metatable, @Nonnull Object pk) throws Exception;

	/**
	 * Load the persistent object with the specified type and primary key from the database. This will
	 * return an object <i>always</i> even if the object does not exist in the database! This should
	 * only be used when you need an instance representing a given primary key that you know exists.
	 * This usually returns a proxy, meaning that <i>nonexistent</i> objects will throw exceptions
	 * as soon as properties other than it's primary key are accessed. To get an object and be
	 * <i>sure</i> it is present in the database use {@link QDataContext#find(Class, Object)}.
	 *
	 * @param <T>	The object type
	 * @param clz	The persistent class for which an instance is being sought.
	 * @param pk	The PK for the instance required.
	 * @return		Always returns an instance; it can be invalid when it does not really exist on the backing store.
	 * @throws Exception
	 */
	@Nonnull
	<T> T getInstance(@Nonnull C root, @Nonnull Class<T> clz, @Nonnull Object pk) throws Exception;

	@Nonnull
	<T> T getInstance(@Nonnull C root, @Nonnull ICriteriaTableDef<T> clz, @Nonnull Object pk) throws Exception;

	/**
	 * Cause the object to be inserted in the database.
	 * @param o
	 * @throws Exception
	 */
	void save(@Nonnull C root, @Nonnull Object o) throws Exception;

	/**
	 * Refresh with latest content in the database.
	 * @param o
	 * @throws Exception
	 */
	void refresh(@Nonnull C root, @Nonnull Object o) throws Exception;

	/**
	 * Cause the object to be deleted from the database.
	 * @param o
	 * @throws Exception
	 */
	void delete(@Nonnull C root, @Nonnull Object o) throws Exception;

	void attach(@Nonnull C root, @Nonnull Object o) throws Exception;
}
