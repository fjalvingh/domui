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
package to.etc.webapp.testsupport;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.webapp.core.IRunnable;
import to.etc.webapp.query.ICriteriaTableDef;
import to.etc.webapp.query.IQDataContextListener;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextFactory;
import to.etc.webapp.query.QSelection;

import java.sql.Connection;
import java.util.List;

/**
 * Use this as stub for QDataContext, once you want to skip the real database but still need to provide QDataContext instance.
 */
public class TestDataContextStub implements QDataContext {
	private int m_alloc = 1;

	@Override
	public void attach(final @NonNull Object o) throws Exception {}

	@Override
	public void setIgnoreClose(boolean on) {}

	@Override
	public void commit() throws Exception {
		decrement(); // Used in "instance destroy method" test because QDataContext does not expose 'close'
	}

	@Override
	public void delete(final @NonNull Object o) throws Exception {}

	@Override
	public @NonNull QDataContextFactory getFactory() {
		throw new IllegalStateException("Cannot use this");
	}

	@Override
	public <T> T find(final @NonNull Class<T> clz, final @NonNull Object pk) throws Exception {
		return null;
	}

	@Override
	public @NonNull	<T> T get(@NonNull Class<T> clz, @NonNull Object pk) throws Exception {
		return clz.newInstance();
	}

	@Override
	public @NonNull <T> T getInstance(@NonNull Class<T> clz, @NonNull Object pk) throws Exception {




		return clz.newInstance();
	}

	@Override
	public @NonNull Connection getConnection() throws Exception {
		throw new IllegalStateException("Cannot use this");
	}

	@Override
	public boolean inTransaction() throws Exception {
		return false;
	}

	@Override
	public @NonNull <T> List<T> query(final @NonNull QCriteria<T> q) throws Exception {
		throw new IllegalStateException("Cannot use this");
	}

	@Override
	public <T> T queryOne(final @NonNull QCriteria<T> q) throws Exception {
		return null;
	}

	@Override
	public @NonNull List<Object[]> query(@NonNull QSelection< ? > sel) throws Exception {
		throw new IllegalStateException("Cannot use this");
	}

	@Override
	public Object[] queryOne(@NonNull QSelection< ? > sel) throws Exception {
		return null;
	}

	@Override
	public void refresh(final @NonNull Object o) throws Exception {}

	@Override
	public void rollback() throws Exception {}

	@Override
	public void save(final @NonNull Object o) throws Exception {}

	@Override
	public void startTransaction() throws Exception {}


	@Override
	public <T> T find(@NonNull ICriteriaTableDef<T> metatable, @NonNull Object pk) throws Exception {
		return null;
	}

	@Override
	public @NonNull <T> T getInstance(@NonNull ICriteriaTableDef<T> clz, @NonNull Object pk) throws Exception {
		throw new IllegalStateException("Cannot use this");
	}

	@Override
	@NonNull
	public <R> List<R> query(@NonNull Class<R> resultInterface, @NonNull QSelection< ? > sel) throws Exception {
		throw new IllegalStateException("Stubbed");
	}

	@Override
	@Nullable
	public <R> R queryOne(@NonNull Class<R> resultInterface, @NonNull QSelection< ? > sel) throws Exception {
		throw new IllegalStateException("Stubbed");
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

	@Override
	public void close() {
		decrement();
	}

	@Override
	public void addCommitAction(@NonNull IRunnable cx) {}

	/**
	 *
	 * @see to.etc.webapp.query.QDataContext#addListener(to.etc.webapp.query.IQDataContextListener)
	 */
	@Override
	public void addListener(@NonNull IQDataContextListener qDataContextListener) {}

	@Override
	public <T> T original(@NonNull T copy) {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void setKeepOriginals() {
		throw new IllegalStateException("Not implemented");
	}
}
