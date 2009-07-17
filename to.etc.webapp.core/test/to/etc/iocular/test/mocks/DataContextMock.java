package to.etc.iocular.test.mocks;

import java.sql.*;
import java.util.*;

import to.etc.webapp.query.*;

public class DataContextMock implements QDataContext {
	private int m_alloc = 1;

	public void attach(final Object o) throws Exception {}

	public void setIgnoreClose(boolean on) {}

	public void commit() throws Exception {
		decrement(); // Used in "instance destroy method" test because QDataContext does not expose 'close'
	}

	public void delete(final Object o) throws Exception {}

	public QDataContextFactory getFactory() {
		return null;
	}
	public <T> T find(final Class<T> clz, final Object pk) throws Exception {
		return null;
	}
	public <T> T getInstance(Class<T> clz, Object pk) throws Exception {
		return null;
	}

	public Connection getConnection() throws Exception {
		return null;
	}

	public boolean inTransaction() throws Exception {
		return false;
	}

	public <T> List<T> query(final QCriteria<T> q) throws Exception {
		return null;
	}

	public <T> T queryOne(final QCriteria<T> q) throws Exception {
		return null;
	}
	public List<Object[]> query(QSelection< ? > sel) throws Exception {
		return null;
	}

	public void refresh(final Object o) throws Exception {}

	public void rollback() throws Exception {}

	public void save(final Object o) throws Exception {}

	public void startTransaction() throws Exception {}

	/**
	 * Internal test pps.
	 */
	public void decrement() {
		if(m_alloc != 1)
			throw new IllegalStateException("Use count is not 1 but " + m_alloc);
		m_alloc--;
	}

	public int testGetUseCount() {
		return m_alloc;
	}

	public void close() {
		decrement();
	}
}
