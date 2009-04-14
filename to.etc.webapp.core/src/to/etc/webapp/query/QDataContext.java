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
	public <T> List<T>		query(QCriteria<T> q) throws Exception;
	public <T> T			queryOne(QCriteria<T> q) throws Exception;

	public <T> T			find(Class<T> clz, Object pk) throws Exception;

	/**
	 * If the object was from an earlier database session reattach it to another, live session.
	 * @param o
	 * @throws Exception
	 */
	public void			attach(Object o) throws Exception;

	/**
	 * EXPERIMENTEEL/NIET DEFINITIEF Cause the object to be inserted in the database.
	 * @param o
	 * @throws Exception
	 */
//	@Deprecated
	public void			save(Object o) throws Exception;

	/**
	 * EXPERIMENTEEL/NIET DEFINITIEF Refresh with latest content in the database.
	 * @param o
	 * @throws Exception
	 */
	@Deprecated
	public void			refresh(Object o) throws Exception;

	/**
	 * EXPERIMENTEEL/NIET DEFINITIEF Cause the object to be deleted from the database.
	 * @param o
	 * @throws Exception
	 */
//	@Deprecated
	public void			delete(Object o) throws Exception;


//	@Deprecated
	public void			startTransaction() throws Exception;
//	@Deprecated
	public void			commit() throws Exception;
//	@Deprecated
	public void			rollback() throws Exception;

	public boolean		inTransaction() throws Exception;

	public Connection	getConnection() throws Exception;
}
