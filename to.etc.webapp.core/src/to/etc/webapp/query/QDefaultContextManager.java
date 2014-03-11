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

import javax.annotation.*;

/**
 * Default implementation of a QContextManager.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 17, 2009
 */
public class QDefaultContextManager implements IQContextManager {
	@Nullable
	private QDataContextFactory m_factory;

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.IQContextManager#setContextFactory(to.etc.webapp.query.QDataContextFactory)
	 */
	@Override
	public synchronized void setContextFactory(@Nonnull QDataContextFactory f) {
		if(m_factory != null)
			throw new IllegalStateException("Already initialized");
		m_factory = f;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.IQContextManager#getDataContextFactory()
	 */
	@Override
	@Nonnull
	public synchronized QDataContextFactory getDataContextFactory() {
		QDataContextFactory factory = m_factory;
		if(factory == null)
			throw new IllegalStateException("QContextManager not initialized");
		return factory;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.IQContextManager#createUnmanagedContext()
	 */
	@Override
	@Nonnull
	public QDataContext createUnmanagedContext() throws Exception {
		return getDataContextFactory().getDataContext();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.IQContextManager#getSharedContextFactory(to.etc.webapp.query.IQContextContainer)
	 */
	@Override
	@Nonnull
	public QDataContextFactory getSharedContextFactory(@Nonnull IQContextContainer cc) {
		QDataContextFactory src = cc.internalGetDataContextFactory(); // Already has a factory here?
		if(src != null)
			return src;

		//-- Create a new shared context factory & store in the container.
		src = new UnclosableContextFactory(cc, getDataContextFactory());
		cc.internalSetDataContextFactory(src);
		return src;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.IQContextManager#getSharedContext(to.etc.webapp.query.IQContextContainer)
	 */
	@Nonnull
	@Override
	public QDataContext getSharedContext(@Nonnull IQContextContainer cc) throws Exception {
		QDataContext dc = cc.internalGetSharedContext();
		if(dc == null) {
			//			System.out.println(".... allocate new shared dataContext");
			dc = getSharedContextFactory(cc).getDataContext();
			cc.internalSetSharedContext(dc);
		}
		return dc;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.IQContextManager#closeSharedContext(to.etc.webapp.query.IQContextContainer)
	 */
	@Override
	public void closeSharedContext(@Nonnull IQContextContainer cc) {
		QDataContext dc = cc.internalGetSharedContext();
		if(dc == null)
			return;
		cc.internalSetSharedContext(null);
		dc.setIgnoreClose(false); // Make sure close gets heeded.
		dc.close();
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
		private IQContextContainer m_contextContainer;

		private QDataContextFactory m_orig;

		/**
		 * Constructor.
		 * @param cc
		 * @param orig
		 */
		public UnclosableContextFactory(IQContextContainer cc, QDataContextFactory orig) {
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
