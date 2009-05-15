package to.etc.iocular.test.mocks;

import java.sql.*;
import java.util.*;

import to.etc.webapp.query.*;

public class DataContextMock implements QDataContext {
	public void attach(final Object o) throws Exception {
	}

	public void commit() throws Exception {
	}

	public void delete(final Object o) throws Exception {
	}

	public <T> T find(final Class<T> clz, final Object pk) throws Exception {
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

	public void refresh(final Object o) throws Exception {
	}

	public void rollback() throws Exception {
	}
	public void save(final Object o) throws Exception {
	}

	public void startTransaction() throws Exception {
	}
}
