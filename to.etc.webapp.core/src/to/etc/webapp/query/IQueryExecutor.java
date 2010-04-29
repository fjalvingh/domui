package to.etc.webapp.query;

import java.util.*;

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
	<T> List<T> query(C root, QCriteria<T> q) throws Exception;

	/**
	 * Issue a selection query, where multiple fields or projections on fields are selected from a base class.
	 * @param sel
	 * @return
	 * @throws Exception
	 */
	List<Object[]> query(C root, QSelection< ? > sel) throws Exception;

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
	<T> T find(C root, Class<T> clz, Object pk) throws Exception;

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
	<T> T getInstance(C root, Class<T> clz, Object pk) throws Exception;

	/**
	 * EXPERIMENTAL/NOT FINAL Cause the object to be inserted in the database.
	 * @param o
	 * @throws Exception
	 */
	//	@Deprecated
	void save(C root, Object o) throws Exception;

	/**
	 * EXPERIMENTAL/NOT FINAL Refresh with latest content in the database.
	 * @param o
	 * @throws Exception
	 */
	void refresh(C root, Object o) throws Exception;

	/**
	 * EXPERIMENTAL/NOT FINAL Cause the object to be deleted from the database.
	 * @param o
	 * @throws Exception
	 */
	void delete(C root, Object o) throws Exception;

	void attach(C root, Object o) throws Exception;
}
