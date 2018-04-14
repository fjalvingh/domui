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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.webapp.core.IRunnable;
import to.etc.webapp.query.ICriteriaTableDef;
import to.etc.webapp.query.IQDataContextListener;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextFactory;
import to.etc.webapp.query.QNotFoundException;
import to.etc.webapp.query.QQueryUtils;
import to.etc.webapp.query.QSelection;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	@NonNull
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
	public void attach(@NonNull Object o) throws Exception {
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
	public void delete(@NonNull Object o) throws Exception {
		unsupported();
	}

	/**
	 * Locate the specified record by PK.
	 * @see to.etc.webapp.query.QDataContext#find(java.lang.Class, java.lang.Object)
	 */
	@Override
	public <T> T find(@NonNull Class<T> clz, @NonNull Object pk) throws Exception {
		unclosed();
		return JdbcQuery.find(this, clz, pk);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#get(java.lang.Class, java.lang.Object)
	 */
	@Override
	public @NonNull
	<T> T get(@NonNull Class<T> clz, @NonNull Object pk) throws Exception {
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
	public @NonNull <T> T getInstance(@NonNull Class<T> clz, @NonNull Object pk) throws Exception {
		unclosed();
		return JdbcQuery.getInstance(this, clz, pk);
	}

	/**
	 * Return the underlying connection verbatim.
	 * @see to.etc.webapp.query.QDataContext#getConnection()
	 */
	@Override
	public @NonNull Connection getConnection() throws Exception {
		unclosed();
		return internalGetConnection();
	}

	@Override
	public @NonNull QDataContextFactory getFactory() {
		return m_factory;
	}

	@Override
	public boolean inTransaction() throws Exception {
		return false;
	}

	@Override
	public @NonNull <T> List<T> query(@NonNull QCriteria<T> q) throws Exception {
		unclosed();
		return JdbcQuery.query(this, q);
	}

	@Override
	public @NonNull List<Object[]> query(@NonNull QSelection< ? > sel) throws Exception {
		unclosed();
		return JdbcQuery.query(this, sel);
	}

	@Override
	public <T> T queryOne(@NonNull QCriteria<T> q) throws Exception {
		unclosed();
		return JdbcQuery.queryOne(this, q);
	}

	@Override
	public Object[] queryOne(@NonNull QSelection< ? > q) throws Exception {
		unclosed();
		return JdbcQuery.queryOne(this, q);
	}

	@Override
	public <T> T find(@NonNull ICriteriaTableDef<T> metatable, @NonNull Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for JdbcDataContext");
	}

	@NonNull
	public <R> List<R> query(@NonNull Class<R> resultInterface, @NonNull QSelection< ? > sel) throws Exception {
		return QQueryUtils.mapSelectionQuery(this, resultInterface, sel);
	}

	@Override
	@Nullable
	public <R> R queryOne(@NonNull Class<R> resultInterface, @NonNull QSelection< ? > sel) throws Exception {
		return QQueryUtils.mapSelectionOneQuery(this, resultInterface, sel);
	}

	@Override
	public @NonNull <T> T getInstance(@NonNull ICriteriaTableDef<T> clz, @NonNull Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for JdbcDataContext");
	}

	/**
	 * Not suppore
	 * @see to.etc.webapp.query.QDataContext#refresh(java.lang.Object)
	 */
	@Override
	public void refresh(@NonNull Object o) throws Exception {
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
	public void save(@NonNull Object o) throws Exception {
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
	public void addCommitAction(@NonNull IRunnable cx) {
		if(m_commitHandlerList == Collections.EMPTY_LIST)
			m_commitHandlerList = new ArrayList<IRunnable>();
		m_commitHandlerList.add(cx);
	}

	/**
	 * {@inheritDoc}
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
