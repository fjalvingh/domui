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

import java.sql.*;
import java.util.*;

import to.etc.webapp.core.*;

/**
 * Generalized thingy representing a database connection, and state associated with
 * objects. Equals the "DataContext" or "Hibernate Session". This is a controlled
 * resource.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 25, 2008
 */
public interface QDataContext {
	/**
	 * Returns the context source which created this DataContext. This context source is used to get
	 * query listeners to execute when a query is done.
	 * @return
	 */
	QDataContextFactory getFactory();

	/**
	 * When ignoreClose is set to T the close call must be silently ignored. Ugly, but for a lot of reasons (all having
	 * to do with the very strained object model around the ViewPoint database code) this is the least invasive method
	 * to allow for per-conversation shared contexts. Please do not replace this with any kind of wrapper/proxy based
	 * solution; it will not work.
	 *
	 * @param on
	 */
	void setIgnoreClose(boolean on);

	/**
	 * This will close and fully discard all resources belonging to this context, provided ignoreClose is
	 * not true. A closed context cannot be reused anymore and should be discarded.
	 */
	void close();

	/**
	 * Execute the query specified by q and return a list of results. Before and after the query execution all
	 * registered listeners will be called.
	 *
	 * @param <T>	The return type for this query, a persistent class type
	 * @param q		The selection criteria
	 * @return
	 * @throws Exception
	 */
	<T> List<T> query(QCriteria<T> q) throws Exception;

	/**
	 * Execute the query specified by q, and expect and return at most 1 result. If the query has no
	 * result this will return null. If more than one result is obtained this will throw an IllegalStateException.
	 * @param <T>
	 * @param q
	 * @return
	 * @throws Exception
	 */
	<T> T queryOne(QCriteria<T> q) throws Exception;

	/**
	 * Issue a selection query, where multiple fields or projections on fields are selected from a base class.
	 * @param sel
	 * @return
	 * @throws Exception
	 */
	List<Object[]> query(QSelection< ? > sel) throws Exception;

	/**
	 * Execute the selection query specified by q, and expect and return at most 1 result. If the query has no
	 * result this will return null. If more than one result is obtained this will throw an IllegalStateException.
	 * @param <T>
	 * @param q
	 * @return
	 * @throws Exception
	 */
	Object[] queryOne(QSelection< ? > q) throws Exception;

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
	<T> T find(Class<T> clz, Object pk) throws Exception;

	<T> T find(ICriteriaTableDef<T> metatable, Object pk) throws Exception;

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
	<T> T getInstance(Class<T> clz, Object pk) throws Exception;

	<T> T getInstance(ICriteriaTableDef<T> clz, Object pk) throws Exception;

	/**
	 * If the object was from an earlier database session reattach it to another, live session.
	 * @param o
	 * @throws Exception
	 */
	void attach(Object o) throws Exception;

	/**
	 * Cause the object to be inserted in the database.
	 * @param o
	 * @throws Exception
	 */
	void save(Object o) throws Exception;

	/**
	 * EXPERIMENTAL/NOT FINAL Refresh with latest content in the database.
	 * @param o
	 * @throws Exception
	 */
	void refresh(Object o) throws Exception;

	/**
	 * Cause the object to be deleted from the database.
	 * @param o
	 * @throws Exception
	 */
	void delete(Object o) throws Exception;


	void startTransaction() throws Exception;

	void commit() throws Exception;

	void rollback() throws Exception;

	boolean inTransaction() throws Exception;

	Connection getConnection() throws Exception;

	/**
	 * Add an action which should be executed after a succesful commit on this context.
	 * @param cx
	 */
	void addCommitAction(IRunnable cx);
}
