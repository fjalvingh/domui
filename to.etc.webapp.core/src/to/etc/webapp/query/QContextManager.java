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
package to.etc.webapp.query;

import java.util.*;

import javax.annotation.*;

/**
 * Fugly singleton helper class to globally access database stuff.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 15, 2009
 */
final public class QContextManager {
	static public final String DEFAULT = "default-context";

	/** The actual implementation handling all manager chores. */
	final static private Map<String, IQContextManager> m_instanceMap = new HashMap<String, IQContextManager>();

	private QContextManager() {}

	/**
	 * Override the default implementation of QContextManager with your own. This <b>must</b> be
	 * called before QContextManager is ever used.
	 * @param cm
	 */
	static synchronized public void setImplementation(@Nonnull String key, @Nonnull IQContextManager cm) {
		IQContextManager m = m_instanceMap.get(key);
		if(m != null)
			throw new IllegalStateException("The QContextManager has already been used, setting a different implementation is no longer possible");
		m_instanceMap.put(key, cm);
	}

	/**
	 * Returns the instance of the manager used to satisfy all calls. If no instance is
	 * set this will create the default handler instance.
	 * @return
	 */
	@Nonnull
	static private synchronized IQContextManager instance(@Nonnull String key) {
		IQContextManager m = m_instanceMap.get(key);
		if(m == null) {
			m = new QDefaultContextManager();
			m_instanceMap.put(key, m);
		}
		return m;
	}

	/**
	 * Initialize the QContextManager with a literal QDataContextFactory.
	 * @param f
	 */
	static public void initialize(@Nonnull String key, @Nonnull final QDataContextFactory f) {
		instance(key).setContextFactory(f);
	}

	/**
	 * Utility method to initialize the "default" connection provider.
	 * @param f
	 */
	static public void initialize(@Nonnull final QDataContextFactory f) {
		initialize(DEFAULT, f);
	}

	/**
	 * Return the named QDataContextFactory. This is the root of *all* default connections
	 * allocated through DomUI. This either returns the single factory, or it asks the delegate
	 * to get a factory, allowing the delegate to return a user-specific factory.
	 *
	 * @return
	 */
	@Nonnull
	static synchronized public QDataContextFactory getDataContextFactory(@Nonnull String key) {
		return instance(key).getDataContextFactory();
	}

	/**
	 * Return the DEFAULT QDataContextFactory. This is the root of *all* default connections
	 * allocated through DomUI. This either returns the single factory, or it asks the delegate
	 * to get a factory, allowing the delegate to return a user-specific factory.
	 *
	 * @return
	 */
	@Nonnull
	static synchronized public QDataContextFactory getDataContextFactory() {
		return instance(DEFAULT).getDataContextFactory();
	}

	/**
	 * Return the unmanaged (manually closed) context factory.
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	static public QDataContext createUnmanagedContext(@Nonnull String key) throws Exception {
		return instance(key).createUnmanagedContext();
	}

	/**
	 * Return the DEFAULT unmanaged (manually closed) context factory.
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	static public QDataContext createUnmanagedContext() throws Exception {
		return instance(DEFAULT).createUnmanagedContext();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Shared DataContext and DataContextFactories.		*/
	/*--------------------------------------------------------------*/
	/**
	 * Get/create a shared context factory. The context factory gets attached
	 * to the container it is shared in, and will always try to re-use any
	 * QDataContext already present in the container. In addition, all data contexts
	 * allocated thru this mechanism have a disabled close() method, preventing
	 * them from closing the shared connection.
	 *
	 * @param cc
	 * @return
	 */
	@Nonnull
	static public QDataContextFactory getDataContextFactory(@Nonnull String key, final QContextContainer cc) {
		return instance(key).getSharedContextFactory(cc);
	}

	/**
	 * Gets a shared QDataContext from the container. If it is not already present it
	 * will be allocated, stored in the container for later reuse and returned. The context
	 * is special in that it cannot be closed() using it's close() call - it is silently
	 * ignored.
	 */
	@Nonnull
	static public QDataContext getContext(@Nonnull String key, @Nonnull final QContextContainer cc) throws Exception {
		return instance(key).getSharedContext(cc);
	}

	@Nonnull
	static public QDataContext getContext(@Nonnull String key, @Nonnull final IQContextContainer cc) throws Exception {
		return instance(key).getSharedContext(cc.getContextContainer(key));
	}

	/**
	 * If the specified container contains a shared context close it.
	 * @param cc
	 */
	static public void closeSharedContext(@Nonnull String key, @Nonnull final QContextContainer cc) {
		instance(key).closeSharedContext(cc);
	}

	static public void closeSharedContexts(@Nonnull final IQContextContainer cc) {
		for(QContextContainer cm : cc.getAllContextContainers()) {
			QDataContext dc = cm.internalGetSharedContext();
			if(null != dc) {
				dc.setIgnoreClose(false);
				dc.close();
				cm.internalSetSharedContext(null);
			}
		}
	}

	@Nonnull
	public static QDataContextFactory getDataContextFactory(@Nonnull String key, @Nonnull IQContextContainer container) {
		return getDataContextFactory(key, container.getContextContainer(key));
	}
}
