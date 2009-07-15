package to.etc.webapp.query;

import java.util.*;

/**
 * Fugly helper class to globally access database stuff.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 15, 2009
 */
final public class QContextManager {
	static private QDataContextFactory m_factory;

	static private FactoryProvider m_provider;

	static public interface FactoryProvider {
		QDataContextFactory getFactory();
	}

	private QContextManager() {}

	/**
	 * Initialize the QContextManager with a literal QDataContextFactory. This is for simple (normal) uses.
	 * @param f
	 */
	static synchronized public void initialize(final QDataContextFactory f) {
		if(m_provider != null || m_factory != null)
			throw new IllegalStateException("Already initialized");
		m_factory = f;
	}

	/**
	 * Initialize the QContextManager with a factory factory; this is for complex uses where creation of
	 * the factory needs to be delayed to the point where it is actually needed. This is used in VP, to
	 * create the factory with a known "current user".
	 * @param f
	 */
	static synchronized public void initialize(final FactoryProvider f) {
		if(m_provider != null || m_factory != null)
			throw new IllegalStateException("Already initialized");
		m_provider = f;
	}

	/**
	 * Return the default QDataContextFactory. This is the root of *all* default connections
	 * allocated through DomUI. This either returns the single factory, or it asks the delegate
	 * to get a factory, allowing the delegate to return a user-specific factory.
	 *
	 * @return
	 */
	static synchronized public QDataContextFactory getDataContextFactory() {
		if(m_factory != null)
			return m_factory;
		if(m_provider != null) {
			QDataContextFactory f = m_provider.getFactory();
			if(f == null)
				throw new NullPointerException("The factory for QDataContextFactory's returned null!?");
			return f;
		}
		throw new IllegalStateException("QContextManager not initialized");
	}

	/**
	 * Create an unmanaged (manually closed) context factory.
	 * @return
	 * @throws Exception
	 */
	static public QDataContext createUnmanagedContext() throws Exception {
		return getDataContextFactory().getDataContext();
	}

	/**
	 * Pending removal: will be done using QDataContext.close().
	 * Release an unmanaged (manually closed) context factory.
	 * @param dc
	 */
	static public void discardUnmanagedContext(final QDataContext dc) {
		m_factory.releaseDataContext(dc);
	}

	static public QDataContext getContext(final IQContextContainer cc) throws Exception {
		QDataContext dc = cc.internalGetSharedContext();
		if(dc == null) {
			dc = getDataContextFactory().getDataContext();
			cc.internalSetSharedContext(dc);
		}
		return dc;
	}

	/**
	 * EXPERIMENTAL DO NOT USE.
	 * @param cc
	 */
	static public void closeSharedContext(final IQContextContainer cc) {
		QDataContext dc = cc.internalGetSharedContext();
		if(dc == null)
			return;
		cc.internalSetSharedContext(null);
		QDataContextFactory f = dc.getSource();
		if(f instanceof UnclosableContextFactory) {
			f = ((UnclosableContextFactory) f).getOriginal();
		}
		f.releaseDataContext(dc);

		discardUnmanagedContext(dc);
	}

	/**
	 * Get/create a shared context factory. This is special in that it will not normally
	 * be closed.
	 *
	 * @param cc
	 * @return
	 */
	static public QDataContextFactory getDataContextFactory(final IQContextContainer cc) {
		QDataContextFactory src = cc.internalGetDataContextFactory();
		if(src != null)
			return src;
		src = new UnclosableContextFactory(getDataContextFactory());
		cc.internalSetDataContextFactory(src);
		return src;
	}

	/**
	 * Proxies a QDataContextFactory to prevent it from closing it's connections.
	 *
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jul 15, 2009
	 */
	static private class UnclosableContextFactory implements QDataContextFactory {
		private QDataContextFactory m_orig;

		public UnclosableContextFactory(QDataContextFactory orig) {
			m_orig = orig;
		}

		QDataContextFactory getOriginal() {
			return m_orig;
		}

		public QDataContext getDataContext() throws Exception {
			return m_orig.getDataContext();
		}

		public Iterator<IQueryListener> getListenerIterator() {
			return m_orig.getListenerIterator();
		}

		public void releaseDataContext(QDataContext dc) {}
	}
}
