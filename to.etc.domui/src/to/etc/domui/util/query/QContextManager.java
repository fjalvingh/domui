package to.etc.domui.util.query;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;

final public class QContextManager {
	static private final String	KEY = QContextManager.class.getName();
	static private final String	SRCKEY = QDataContextSource.class.getName();
	static private QDataContextSource	m_factory;

	private QContextManager() {
	}

	static public void	initialize(QDataContextSource f) {
		m_factory = f;
	}

	static public QDataContextSource		createNewSource() {
		if(m_factory == null)
			throw new IllegalStateException("DataContext factory not initialized");
		return m_factory;
	}
	static public QDataContext		createUnmanagedContext() throws Exception {
		return createNewSource().getDataContext();
	}
	static public void				discardUnmanagedContext(QDataContext dc) {
		m_factory.releaseDataContext(dc);
	}

	static public QDataContext		getContext(Page pg) throws Exception {
		return getContext(pg.getConversation());
	}

	static public QDataContext		getContext(ConversationContext cc) throws Exception {
		QDataContext	dc	= (QDataContext) cc.getAttribute(KEY);
		if(dc == null) {
			dc	= createNewSource().getDataContext();
			cc.setAttribute(KEY, dc);
		}
		return dc;
	}

	static public QDataContext		getCurrentContext() throws Exception {
		return getContext(PageContext.getCurrentConversation());
	}

	/**
	 * EXPERIMENTAL DO NOT USE.
	 * @param cc
	 */
	static public void				closeSharedContext(ConversationContext cc) {
		QDataContext	dc	= (QDataContext) cc.getAttribute(KEY);
		if(dc == null)
			return;
		cc.setAttribute(KEY, null);
		discardUnmanagedContext(dc);
	}

	static public QDataContextSource	getSource(final Page p) {
		return getSource(p.getConversation());
	}

	static public QDataContextSource	getSource(final ConversationContext cc) {
		QDataContextSource	src = (QDataContextSource) cc.getAttribute(SRCKEY);
		if(src == null) {
			src	= new QDataContextSource() {
				public QDataContext getDataContext() throws Exception {
					return getContext(cc);
				}
				public void releaseDataContext(QDataContext dc) {
//					throw new IllegalStateException("Not allowed to release this context");
				}
			};
			cc.setAttribute(SRCKEY, src);
		}
		return src;
	}

}
