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
package to.etc.webapp.qsql;

import java.sql.*;
import java.util.*;

import javax.annotation.*;

import to.etc.webapp.core.*;
import to.etc.webapp.query.*;

/**
 * WATCH OUT- THIS DOES NOT OBEY OBJECT IDENTITY RULES!! Records loaded through
 * this code are NOT mapped by identity, so multiple queries for the same object
 * WILL return different copies!!
 *
 * <p>This is a poor-man's datacontext that can be used to do JDBC queries using
 * the QCriteria interface.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 10, 2010
 */
public class JdbcDataContext implements QDataContext {
	/** The originating factory. */
	private final QDataContextFactory m_factory;

	/** The underlying connection. */
	private Connection m_dbc;

	private boolean m_ignoreClose;

	@Nonnull
	private List<IRunnable> m_commitHandlerList = Collections.EMPTY_LIST;

	public JdbcDataContext(QDataContextFactory factory, Connection dbc) {
		m_factory = factory;
		internalSetConnection(dbc);
		//		dbc.setAutoCommit(false);
	}

	protected void internalSetConnection(Connection dbc) {
		m_dbc = dbc;
	}

	protected Connection internalGetConnection() throws Exception {
		return m_dbc;
	}

	protected void unclosed() throws Exception {
		if(internalGetConnection() == null)
			throw new IllegalStateException("This JDBC Data Context has been closed.");
	}

	private void unsupported() {
		throw new IllegalStateException("Unsupported call for JdbcDataContext");
	}

	/**
	 * DOES NOTHING.
	 * @see to.etc.webapp.query.QDataContext#attach(java.lang.Object)
	 */
	@Override
	public void attach(@Nonnull Object o) throws Exception {
	}

	@Override
	public void close() {
		if(m_ignoreClose)
			return;
		if(m_dbc != null) {
			try {
				m_dbc.rollback();
			} catch(Exception x) {}
			try {
				m_dbc.close();
			} catch(Exception x) {}
			m_dbc = null;
		}
	}

	@Override
	public void commit() throws Exception {
		unclosed();
		internalGetConnection().commit();
		Exception firstx = null;
		for(IRunnable r : m_commitHandlerList) {
			try {
				r.run();
			} catch(Exception x) {
				if(null == firstx)
					firstx = x;
				else
					x.printStackTrace();
			}
		}
		m_commitHandlerList.clear();
		if(null != firstx)
			throw firstx;
	}

	/**
	 * Unsupported for JDBC code.
	 * @see to.etc.webapp.query.QDataContext#delete(java.lang.Object)
	 */
	@Override
	public void delete(@Nonnull Object o) throws Exception {
		unsupported();
	}

	/**
	 * Locate the specified record by PK.
	 * @see to.etc.webapp.query.QDataContext#find(java.lang.Class, java.lang.Object)
	 */
	@Override
	public <T> T find(@Nonnull Class<T> clz, @Nonnull Object pk) throws Exception {
		unclosed();
		return JdbcQuery.find(this, clz, pk);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#get(java.lang.Class, java.lang.Object)
	 */
	@Override
	public @Nonnull
	<T> T get(@Nonnull Class<T> clz, @Nonnull Object pk) throws Exception {
		T res = find(clz, pk);
		if(res == null) {
			throw new QNotFoundException(clz, pk);
		}
		return res;
	}

	/**
	 * Get an instance; this will return an instance by first trying to load it; if that fails
	 * it will create one but only fill the PK. Use is questionable though.
	 * @see to.etc.webapp.query.QDataContext#getInstance(java.lang.Class, java.lang.Object)
	 */
	@Override
	public @Nonnull <T> T getInstance(@Nonnull Class<T> clz, @Nonnull Object pk) throws Exception {
		unclosed();
		return JdbcQuery.getInstance(this, clz, pk);
	}

	/**
	 * Return the underlying connection verbatim.
	 * @see to.etc.webapp.query.QDataContext#getConnection()
	 */
	@Override
	public @Nonnull Connection getConnection() throws Exception {
		unclosed();
		return internalGetConnection();
	}

	@Override
	public @Nonnull QDataContextFactory getFactory() {
		return m_factory;
	}

	@Override
	public boolean inTransaction() throws Exception {
		return false;
	}

	@Override
	public @Nonnull <T> List<T> query(@Nonnull QCriteria<T> q) throws Exception {
		unclosed();
		return JdbcQuery.query(this, q);
	}

	@Override
	public @Nonnull List<Object[]> query(@Nonnull QSelection< ? > sel) throws Exception {
		unclosed();
		return JdbcQuery.query(this, sel);
	}

	@Override
	public <T> T queryOne(@Nonnull QCriteria<T> q) throws Exception {
		unclosed();
		return JdbcQuery.queryOne(this, q);
	}

	@Override
	public Object[] queryOne(@Nonnull QSelection< ? > q) throws Exception {
		unclosed();
		return JdbcQuery.queryOne(this, q);
	}

	@Override
	public <T> T find(@Nonnull ICriteriaTableDef<T> metatable, @Nonnull Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for JdbcDataContext");
	}

	@Nonnull
	public <R> List<R> query(@Nonnull Class<R> resultInterface, @Nonnull QSelection< ? > sel) throws Exception {
		return QQueryUtils.mapSelectionQuery(this, resultInterface, sel);
	}

	@Override
	@Nullable
	public <R> R queryOne(@Nonnull Class<R> resultInterface, @Nonnull QSelection< ? > sel) throws Exception {
		return QQueryUtils.mapSelectionOneQuery(this, resultInterface, sel);
	}

	@Override
	public @Nonnull <T> T getInstance(@Nonnull ICriteriaTableDef<T> clz, @Nonnull Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for JdbcDataContext");
	}

	/**
	 * Not suppore
	 * @see to.etc.webapp.query.QDataContext#refresh(java.lang.Object)
	 */
	@Override
	public void refresh(@Nonnull Object o) throws Exception {
		unsupported();
	}

	@Override
	public void rollback() throws Exception {
		unclosed();
		internalGetConnection().rollback();
	}

	/**
	 * Not supported
	 * @see to.etc.webapp.query.QDataContext#save(java.lang.Object)
	 */
	@Override
	public void save(@Nonnull Object o) throws Exception {
		unsupported();
	}

	@Override
	public void setIgnoreClose(boolean on) {
		m_ignoreClose = on;
	}

	@Override
	public void startTransaction() throws Exception {
		unclosed();
	}

	@Override
	public void addCommitAction(@Nonnull IRunnable cx) {
		if(m_commitHandlerList == Collections.EMPTY_LIST)
			m_commitHandlerList = new ArrayList<IRunnable>();
		m_commitHandlerList.add(cx);
	}

	/**
	 * {@inheritDoc}
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
