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
package to.etc.iocular.test.mocks;

import java.sql.*;
import java.util.*;

import to.etc.webapp.core.*;
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

	public Object[] queryOne(QSelection< ? > sel) throws Exception {
		return null;
	}

	public void refresh(final Object o) throws Exception {}

	public void rollback() throws Exception {}

	public void save(final Object o) throws Exception {}

	public void startTransaction() throws Exception {}


	@Override
	public <T> T find(ICriteriaTableDef<T> metatable, Object pk) throws Exception {
		return null;
	}

	@Override
	public <T> T getInstance(ICriteriaTableDef<T> clz, Object pk) throws Exception {
		return null;
	}

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

	@Override
	public void addCommitAction(IRunnable cx) {}
}
