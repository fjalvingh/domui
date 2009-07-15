package to.etc.webapp.query;

import java.util.*;

final public class QContextManager {
	static private QDataContextFactory m_factory;

	private QContextManager() {}

	static public void initialize(final QDataContextFactory f) {
		m_factory = f;
	}

	static public QDataContextFactory createNewSource() {
		if(m_factory == null)
			throw new IllegalStateException("DataContext factory not initialized");
		return m_factory;
	}

	static public QDataContext createUnmanagedContext() throws Exception {
		return createNewSource().getDataContext();
	}

	static public void discardUnmanagedContext(final QDataContext dc) {
		m_factory.releaseDataContext(dc);
	}

	//	static public QDataContext		getContext(final Page pg) throws Exception {
	//		return getContext(pg.getConversation());
	//	}
	//
	static public QDataContext getContext(final IQContextContainer cc) throws Exception {
		QDataContext dc = cc.internalGetSharedContext();
		if(dc == null) {
			dc = createNewSource().getDataContext();
			cc.internalSetSharedContext(dc);
		}
		return dc;
	}

	//	static public QDataContext		getCurrentContext() throws Exception {
	//		return getContext(PageContext.getCurrentConversation());
	//	}
	//
	/**
	 * EXPERIMENTAL DO NOT USE.
	 * @param cc
	 */
	static public void closeSharedContext(final IQContextContainer cc) {
		QDataContext dc = cc.internalGetSharedContext();
		if(dc == null)
			return;
		cc.internalSetSharedContext(null);
		discardUnmanagedContext(dc);
	}

	static public QDataContextFactory getSource(final IQContextContainer cc) {
		QDataContextFactory src = cc.internalGetDataContextFactory();
		if(src == null) {
			src = new QDataContextFactory() {
				public QDataContext getDataContext() throws Exception {
					return getContext(cc);
				}

				public void releaseDataContext(final QDataContext dc) {
				}
				public Iterator<IQueryListener> getListenerIterator() {
					return Collections.EMPTY_LIST.iterator();
				}
			};
			cc.internalSetDataContextFactory(src);
		}
		return src;
	}

}
