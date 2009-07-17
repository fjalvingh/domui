package to.etc.webapp.query;

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

	//	/** jal 20090715 removed, replaced with QDataContext.close()
	//	 * Pending removal: will be done using QDataContext.close().
	//	 * Release an unmanaged (manually closed) context factory.
	//	 * @param dc
	//	 */
	//	static public void discardUnmanagedContext(final QDataContext dc) {
	//		m_factory.releaseDataContext(dc);
	//	}

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
		QDataContextFactory src = cc.internalGetDataContextFactory();		// Already has a factory here?
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
	static public QDataContext getContext(final IQContextContainer cc) throws Exception {
		QDataContext dc = cc.internalGetSharedContext();
		if(dc == null) {
			System.out.println(".... allocate new shared dataContext");
			dc = getDataContextFactory(cc).getDataContext();
			cc.internalSetSharedContext(dc);
		}
		return dc;
	}

	/**
	 * Wrap the DataContext in a wrapper which disables the close command.
	 * @param dc
	 * @return
	 */
	static public QDataContext	createUnclosableDataContext(QDataContext dc) {
		return new QDataContextWrapper(dc) {
			@Override
			public void close() {
			}
		};
	}

	/**
	 * If the specified container contains a shared context close it.
	 * @param cc
	 */
	static public void closeSharedContext(final IQContextContainer cc) {
		QDataContext dc = cc.internalGetSharedContext();
		if(dc == null)
			return;
		cc.internalSetSharedContext(null);
		if(dc instanceof QDataContextWrapper) {
			dc = ((QDataContextWrapper)dc).getOriginal();
		}
		dc.close();
	}

	/**
	 * This is a QDataContext factory which attaches itself to some IQContextContainer and
	 * caches connections in there. It reuses any existing connection in the container, and
	 * will inhibit the closing of it's QDataSource.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jul 15, 2009
	 */
	static private class UnclosableContextFactory implements QDataContextFactory {
		private IQContextContainer	m_contextContainer;
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

		QDataContextFactory getOriginal() {
			return m_orig;
		}

		public QDataContext getDataContext() throws Exception {
			//-- First check the container for something usable
			QDataContext	dc	= m_contextContainer.internalGetSharedContext();
			if(dc != null)
				return dc;
			dc	= m_orig.getDataContext();
			dc	= createUnclosableDataContext(dc);	// Wrap to make it unclosable
			m_contextContainer.internalSetSharedContext(dc);	// Store allocated thingy
			return dc;
		}
		public QEventListenerSet getEventListeners() {
			return m_orig.getEventListeners();
		}
	}
}
