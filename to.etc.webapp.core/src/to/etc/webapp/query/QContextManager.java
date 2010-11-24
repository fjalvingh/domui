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

/**
 * Fugly singleton helper class to globally access database stuff.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 15, 2009
 */
final public class QContextManager {
	/** The actual implementation handling all manager chores. */
	static private IQContextManager m_instance;

	private QContextManager() {}

	/**
	 * Override the default implementation of QContextManager with your own. This <b>must</b> be
	 * called before QContextManager is ever used.
	 * @param cm
	 */
	static synchronized public void setImplementation(IQContextManager cm) {
		if(m_instance != null)
			throw new IllegalStateException("The QContextManager has already been used, setting a different implementation is no longer possible");
		m_instance = cm;
	}

	/**
	 * Returns the instance of the manager used to satisfy all calls. If no instance is
	 * set this will create the default handler instance.
	 * @return
	 */
	static private synchronized IQContextManager instance() {
		if(m_instance == null)
			m_instance = new QDefaultContextManager();
		return m_instance;
	}

	/**
	 * Initialize the QContextManager with a literal QDataContextFactory.
	 * @param f
	 */
	static public void initialize(final QDataContextFactory f) {
		instance().setContextFactory(f);
	}

	/**
	 * Return the default QDataContextFactory. This is the root of *all* default connections
	 * allocated through DomUI. This either returns the single factory, or it asks the delegate
	 * to get a factory, allowing the delegate to return a user-specific factory.
	 *
	 * @return
	 */
	static synchronized public QDataContextFactory getDataContextFactory() {
		return instance().getDataContextFactory();
	}

	/**
	 * Create an unmanaged (manually closed) context factory.
	 * @return
	 * @throws Exception
	 */
	static public QDataContext createUnmanagedContext() throws Exception {
		return instance().createUnmanagedContext();
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
	static public QDataContextFactory getDataContextFactory(final IQContextContainer cc) {
		return instance().getSharedContextFactory(cc);
	}

	/**
	 * Gets a shared QDataContext from the container. If it is not already present it
	 * will be allocated, stored in the container for later reuse and returned. The context
	 * is special in that it cannot be closed() using it's close() call - it is silently
	 * ignored.
	 */
	static public QDataContext getContext(final IQContextContainer cc) throws Exception {
		return instance().getSharedContext(cc);
	}

	/**
	 * If the specified container contains a shared context close it.
	 * @param cc
	 */
	static public void closeSharedContext(final IQContextContainer cc) {
		instance().closeSharedContext(cc);
	}
}
