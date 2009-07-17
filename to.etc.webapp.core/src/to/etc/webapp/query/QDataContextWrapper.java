package to.etc.webapp.query;

import java.sql.*;
import java.util.*;

/**
 * A wrapper for a QDataContext, to easily override functions in it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 17, 2009
 */
public class QDataContextWrapper implements QDataContext {
	private QDataContext	m_original;

	/**
	 * This class is useful only when overridden, hence the protected constructor.
	 * @param original
	 */
	protected QDataContextWrapper(QDataContext original) {
		m_original = original;
	}
	public QDataContext getOriginal() {
		return m_original;
	}

	public void attach(Object o) throws Exception {
		m_original.attach(o);
	}

	public void close() {
		m_original.close();
	}

	public void commit() throws Exception {
		m_original.commit();
	}

	public void delete(Object o) throws Exception {
		m_original.delete(o);
	}

	public <T> T find(Class<T> clz, Object pk) throws Exception {
		return m_original.find(clz, pk);
	}

	public Connection getConnection() throws Exception {
		return m_original.getConnection();
	}

	public QDataContextFactory getFactory() {
		return m_original.getFactory();
	}

	public <T> T getInstance(Class<T> clz, Object pk) throws Exception {
		return m_original.getInstance(clz, pk);
	}

	public boolean inTransaction() throws Exception {
		return m_original.inTransaction();
	}

	public <T> List<T> query(QCriteria<T> q) throws Exception {
		return m_original.query(q);
	}

	public List<Object[]> query(QSelection< ? > sel) throws Exception {
		return m_original.query(sel);
	}

	public <T> T queryOne(QCriteria<T> q) throws Exception {
		return m_original.queryOne(q);
	}

	@Deprecated
	public void refresh(Object o) throws Exception {
		m_original.refresh(o);
	}

	public void rollback() throws Exception {
		m_original.rollback();
	}

	public void save(Object o) throws Exception {
		m_original.save(o);
	}

	public void startTransaction() throws Exception {
		m_original.startTransaction();
	}
}
