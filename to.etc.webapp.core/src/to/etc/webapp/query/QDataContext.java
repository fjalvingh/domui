package to.etc.webapp.query;

import java.sql.*;
import java.util.*;

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
	QDataContextSource	getSource();

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
	List<Object[]>	query(QSelection<?> sel) throws Exception;

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

	/**
	 * Load the persistent object with the specified type and primary key from the database. This will
	 * return a proxy usually, meaning that nonexistent objects will return a more or less valid object
	 * which will throw exceptions as soon as properties other than it's primary key are accessed. This
	 * is useful for code where foreign keys are filled in; for these you do not usually need an actual
	 * filled-in object instance. To get an object and be <i>sure</i> it is present in the database use
	 * the load() method.
	 *
	 * @param <T>	The object type
	 * @param clz	The persistent class for which an instance is being sought.
	 * @param pk	The PK for the instance required.
	 * @return		Always returns an instance; it can be invalid when it does not really exist on the backing store.
	 * @throws Exception
	 */
	<T> T getInstance(Class<T> clz, Object pk) throws Exception;

	/**
	 * If the object was from an earlier database session reattach it to another, live session.
	 * @param o
	 * @throws Exception
	 */
	void attach(Object o) throws Exception;

	/**
	 * EXPERIMENTAL/NOT FINAL Cause the object to be inserted in the database.
	 * @param o
	 * @throws Exception
	 */
	//	@Deprecated
	void save(Object o) throws Exception;

	/**
	 * EXPERIMENTAL/NOT FINAL Refresh with latest content in the database.
	 * @param o
	 * @throws Exception
	 */
	@Deprecated
	void refresh(Object o) throws Exception;

	/**
	 * EXPERIMENTAL/NOT FINAL Cause the object to be deleted from the database.
	 * @param o
	 * @throws Exception
	 */
	//	@Deprecated
	void delete(Object o) throws Exception;


	//	@Deprecated
	void startTransaction() throws Exception;

	//	@Deprecated
	void commit() throws Exception;

	//	@Deprecated
	void rollback() throws Exception;

	boolean inTransaction() throws Exception;

	Connection getConnection() throws Exception;
}
