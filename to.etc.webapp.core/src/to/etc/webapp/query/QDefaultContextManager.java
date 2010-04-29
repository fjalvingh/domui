package to.etc.webapp.query;

/**
 * Default implementation of a QContextManager.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 17, 2009
 */
public class QDefaultContextManager implements IQContextManager {
	private QDataContextFactory m_factory;

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.IQContextManager#setContextFactory(to.etc.webapp.query.QDataContextFactory)
	 */
	public synchronized void setContextFactory(QDataContextFactory f) {
		if(m_factory != null)
			throw new IllegalStateException("Already initialized");
		m_factory = f;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.IQContextManager#getDataContextFactory()
	 */
	public synchronized QDataContextFactory getDataContextFactory() {
		if(m_factory == null)
			throw new IllegalStateException("QContextManager not initialized");
		return m_factory;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.IQContextManager#createUnmanagedContext()
	 */
	public QDataContext createUnmanagedContext() throws Exception {
		return getDataContextFactory().getDataContext();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.IQContextManager#getSharedContextFactory(to.etc.webapp.query.IQContextContainer)
	 */
	public QDataContextFactory getSharedContextFactory(IQContextContainer cc) {
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
	public QDataContext getSharedContext(IQContextContainer cc) throws Exception {
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
	public void closeSharedContext(IQContextContainer cc) {
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

		public QDataContext getDataContext() throws Exception {
			//-- First check the container for something usable
			QDataContext dc = m_contextContainer.internalGetSharedContext();
			if(dc != null)
				return dc;
			dc = m_orig.getDataContext();
			dc.setIgnoreClose(true);
			m_contextContainer.internalSetSharedContext(dc); // Store allocated thingy
			return dc;
		}

		public QEventListenerSet getEventListeners() {
			return m_orig.getEventListeners();
		}

		@Override
		public QQueryExecutorRegistry getQueryHandlerList() {
			return m_orig.getQueryHandlerList();
		}
	}
}
