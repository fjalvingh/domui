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
	final static private Map<String, IQContextFactorySquared> m_instanceMap = new HashMap<String, IQContextFactorySquared>();

	final static private Map<String, Exception> m_initializedMap = new HashMap<>();

	private QContextManager() {}

	/**
	 * Override the default implementation of QContextManager with your own. This <b>must</b> be
	 * called before QContextManager is ever used.
	 * @param cm
	 */
	static synchronized public void setImplementation(@Nonnull String key, @Nonnull IQContextFactorySquared cm) {
		IQContextFactorySquared m = m_instanceMap.get(key);
		if(m != null) {
			Exception where = m_initializedMap.get(key);
			throw new IllegalStateException("Context factory for [" + key + "] has already been used, setting a different implementation is no longer possible", where);
		}

		Exception where = null;
		try {
			throw new RuntimeException("The instance was previously initialized here");
		} catch(Exception x) {
			where = x;
		}
		m_instanceMap.put(key, cm);
		m_initializedMap.put(key, where);
	}

	static public void setImplementation(@Nonnull String key, @Nonnull final QDataContextFactory factory) {
		setImplementation(key, new IQContextFactorySquared() {
			@Override
			@Nonnull
			public QDataContextFactory getDataContextFactory() {
				return factory;
			}
		});
	}

	/**
	 * Returns the instance of the manager used to satisfy all calls. If no instance is
	 * set this will create the default handler instance.
	 * @return
	 */
	@Nonnull
	static public synchronized IQContextFactorySquared instance(@Nonnull String key) {
		IQContextFactorySquared m = m_instanceMap.get(key);
		if(m != null)
			return m;
		throw new IllegalStateException("No context factory-factory found for key=" + key + " - call setImplementation() for that key before using me.");
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
		return instance(key).getDataContextFactory().getDataContext();
	}

	/**
	 * Return the DEFAULT unmanaged (manually closed) context factory.
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	static public QDataContext createUnmanagedContext() throws Exception {
		return createUnmanagedContext(DEFAULT);
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
	static public QDataContextFactory getDataContextFactory(@Nonnull String key, @Nonnull final QContextContainer cc) {
		QDataContextFactory src = cc.internalGetDataContextFactory(); 			// Already has a factory here?
		if(src != null)
			return src;

		//-- Create a new shared context factory & store in the container.
		src = new UnclosableContextFactory(cc, getDataContextFactory());
		cc.internalSetDataContextFactory(src);
		return src;
	}

	/**
	 * Gets a shared QDataContext from the container. If it is not already present it
	 * will be allocated, stored in the container for later reuse and returned. The context
	 * is special in that it cannot be closed() using it's close() call - it is silently
	 * ignored.
	 */
	@Nonnull
	static public QDataContext getContext(@Nonnull String key, @Nonnull final QContextContainer cc) throws Exception {
		QDataContext dc = cc.internalGetSharedContext();
		if(dc == null) {
			//			System.out.println(".... allocate new shared dataContext");
			dc = getDataContextFactory(key, cc).getDataContext();
			dc.setIgnoreClose(true);
			cc.internalSetSharedContext(dc);
		}
		return dc;
	}

	@Nonnull
	static public QDataContext getContext(@Nonnull String key, @Nonnull final IQContextContainer cc) throws Exception {
		return getContext(key, cc.getContextContainer(key));
	}

	/**
	 * If the specified container contains a shared context close it.
	 * @param cc
	 */
	static public void closeSharedContext(@Nonnull String key, @Nonnull final QContextContainer cc) {
		QDataContext dc = cc.internalGetSharedContext();
		if(dc == null)
			return;
		cc.internalSetSharedContext(null);
		dc.setIgnoreClose(false); 								// Make sure close gets heeded.
		dc.close();
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

	/**
	 * This is a QDataContext factory which attaches itself to some IQContextContainer and
	 * caches connections in there. It reuses any existing connection in the container, and
	 * will inhibit the closing of it's QDataSources.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jul 15, 2009
	 */
	static private class UnclosableContextFactory implements QDataContextFactory {
		private QContextContainer m_contextContainer;

		private QDataContextFactory m_orig;

		/**
		 * Constructor.
		 * @param cc
		 * @param orig
		 */
		public UnclosableContextFactory(QContextContainer cc, QDataContextFactory orig) {
			if(cc == null)
				throw new NullPointerException("Container cannot be null");
			if(orig == null)
				throw new NullPointerException("Root factory cannot be null");

			m_orig = orig;
			m_contextContainer = cc;
		}

		@Override
		public @Nonnull QDataContext getDataContext() throws Exception {
			//-- First check the container for something usable
			QDataContext dc = m_contextContainer.internalGetSharedContext();
			if(dc != null)
				return dc;
			dc = m_orig.getDataContext();
			dc.setIgnoreClose(true);
			m_contextContainer.internalSetSharedContext(dc); // Store allocated thingy
			return dc;
		}

		@Override
		public @Nonnull QEventListenerSet getEventListeners() {
			return m_orig.getEventListeners();
		}

		@Override
		public @Nonnull QQueryExecutorRegistry getQueryHandlerList() {
			return m_orig.getQueryHandlerList();
		}
	}
}
