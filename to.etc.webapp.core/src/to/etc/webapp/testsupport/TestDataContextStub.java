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

import java.sql.*;
import java.util.*;

import javax.annotation.*;

import to.etc.webapp.core.*;
import to.etc.webapp.query.*;

/**
 * Use this as stub for QDataContext, once you want to skip the real database but still need to provide QDataContext instance.
 */
public class TestDataContextStub implements QDataContext {
	private int m_alloc = 1;

	@Override
	public void attach(final @Nonnull Object o) throws Exception {}

	@Override
	public void setIgnoreClose(boolean on) {}

	@Override
	public void commit() throws Exception {
		decrement(); // Used in "instance destroy method" test because QDataContext does not expose 'close'
	}

	@Override
	public void delete(final @Nonnull Object o) throws Exception {}

	@Override
	public @Nonnull QDataContextFactory getFactory() {
		throw new IllegalStateException("Cannot use this");
	}

	@Override
	public <T> T find(final @Nonnull Class<T> clz, final @Nonnull Object pk) throws Exception {
		return null;
	}

	@Override
	public @Nonnull	<T> T get(@Nonnull Class<T> clz, @Nonnull Object pk) throws Exception {
		return clz.newInstance();
	}

	@Override
	public @Nonnull <T> T getInstance(@Nonnull Class<T> clz, @Nonnull Object pk) throws Exception {




		return clz.newInstance();
	}

	@Override
	public @Nonnull Connection getConnection() throws Exception {
		throw new IllegalStateException("Cannot use this");
	}

	@Override
	public boolean inTransaction() throws Exception {
		return false;
	}

	@Override
	public @Nonnull <T> List<T> query(final @Nonnull QCriteria<T> q) throws Exception {
		throw new IllegalStateException("Cannot use this");
	}

	@Override
	public <T> T queryOne(final @Nonnull QCriteria<T> q) throws Exception {
		return null;
	}

	@Override
	public @Nonnull List<Object[]> query(@Nonnull QSelection< ? > sel) throws Exception {
		throw new IllegalStateException("Cannot use this");
	}

	@Override
	public Object[] queryOne(@Nonnull QSelection< ? > sel) throws Exception {
		return null;
	}

	@Override
	public void refresh(final @Nonnull Object o) throws Exception {}

	@Override
	public void rollback() throws Exception {}

	@Override
	public void save(final @Nonnull Object o) throws Exception {}

	@Override
	public void startTransaction() throws Exception {}


	@Override
	public <T> T find(@Nonnull ICriteriaTableDef<T> metatable, @Nonnull Object pk) throws Exception {
		return null;
	}

	@Override
	public @Nonnull <T> T getInstance(@Nonnull ICriteriaTableDef<T> clz, @Nonnull Object pk) throws Exception {
		throw new IllegalStateException("Cannot use this");
	}

	@Override
	@Nonnull
	public <R> List<R> query(@Nonnull Class<R> resultInterface, @Nonnull QSelection< ? > sel) throws Exception {
		throw new IllegalStateException("Stubbed");
	}

	@Override
	@Nullable
	public <R> R queryOne(@Nonnull Class<R> resultInterface, @Nonnull QSelection< ? > sel) throws Exception {
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
	public void addCommitAction(@Nonnull IRunnable cx) {}

	/**
	 *
	 * @see to.etc.webapp.query.QDataContext#addListener(to.etc.webapp.query.IQDataContextListener)
	 */
	@Override
	public void addListener(@Nonnull IQDataContextListener qDataContextListener) {}

	@Override
	public <T> T original(@Nonnull T copy) {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void setKeepOriginals() {
		throw new IllegalStateException("Not implemented");
	}
}
