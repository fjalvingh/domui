package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.util.FileTool;
import to.etc.webapp.query.IQContextContainer;
import to.etc.webapp.query.QContextContainer;
import to.etc.webapp.query.QDataContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-18.
 */
abstract public class AbstractConversationContext implements IQContextContainer {
	static public final Logger LOG = LoggerFactory.getLogger(AbstractConversationContext.class);

	/** The map of all attribute objects added to this conversation. */
	@NonNull
	private Map<String, Object> m_map = new HashMap<>();

	@Nullable
	private WindowSession m_manager;

	@NonNull
	protected ConversationState m_state = ConversationState.DETACHED;

	/** True when this context was destroyed because the session was invalidated. */
	protected boolean m_sessionDestroyed;

	@NonNull
	private List<File> m_uploadList = Collections.EMPTY_LIST;

	final void initialize(@NonNull final WindowSession m) {
		if(m == null)
			throw new IllegalStateException("Internal: manager cannot be null, dude");
		if(m_manager != null)
			throw new IllegalStateException("Internal: manager is ALREADY set, dude");
	}

	protected void destroy() {
		m_manager = null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Lifecycle management								*/
	/*--------------------------------------------------------------*/
	/**
	 * Called when a new request which accesses this context is entering the
	 * server. This should restore the context to a usable state.
	 */
	public void onAttach() throws Exception {}

	/**
	 * Called when the request has terminated, the response has been rendered and the
	 * server is about to exit all handling for the request. This must discard any
	 * data that should not be stored between requests, and it must discard any
	 * resource like database connections and the like.
	 */
	public void onDetach() throws Exception {}

	public void onDestroy() throws Exception {}

	void internalAttach() throws Exception {
		LOG.debug("Attaching " + this);
		if(m_state != ConversationState.DETACHED)
			throw new IllegalStateException("Wrong state for ATTACH: " + m_state);
		for(Object o : m_map.values()) {
			if(o instanceof IConversationStateListener) {
				try {
					((IConversationStateListener) o).conversationAttached(this);
				} catch(Exception x) {
					x.printStackTrace();
					LOG.error("In calling attach listener", x);
				}
			}
		}
		try {
			onAttach();
		} finally {
			m_state = ConversationState.ATTACHED;
		}
	}

	void internalDetach() throws Exception {
		LOG.debug("Detaching " + this);
		if(m_state != ConversationState.ATTACHED)
			throw new IllegalStateException("Wrong state for DETACH: " + m_state + " in " + this);
		for(Object o : m_map.values()) {
			if(o instanceof IConversationStateListener) {
				try {
					((IConversationStateListener) o).conversationDetached(this);
				} catch(Exception x) {
					x.printStackTrace();
					LOG.error("In calling detach listener", x);
				}
			}
		}
		try {
			onDetach();
		} finally {
			m_state = ConversationState.DETACHED;
		}
	}

	/**
	 * @param sessionDestroyed		indicates that the HttpSession has been invalidated somehow, possibly logout
	 */
	void internalDestroy(boolean sessionDestroyed) throws Exception {
		LOG.info("Destroying " + this);
		if(m_state == ConversationState.DESTROYED) {
			if(!sessionDestroyed)
				throw new IllegalStateException("Wrong state for DESTROY: " + m_state);
			return;
		}

		for(Object o : m_map.values()) {
			if(o instanceof IConversationStateListener) {
				try {
					((IConversationStateListener) o).conversationDestroyed(this);
				} catch(Exception x) {
					if(! sessionDestroyed) {
						x.printStackTrace();
						LOG.error("In calling destroy listener", x);
					}
				}
			}
		}
		getWindowSession().getApplication().internalCallConversationDestroyed(this);
		try {
			onDestroy();
		} catch(Exception x) {
			if(! sessionDestroyed)
				throw x;

			//-- Ignore trouble during session invalidate
		} finally {
			m_state = ConversationState.DESTROYED;
			m_sessionDestroyed = sessionDestroyed;
			discardTempFiles();
		}
	}

	public void checkAttached() {
		if(m_state != ConversationState.ATTACHED)
			throw new ConversationDestroyedException(toString(), String.valueOf(m_state));
	}



	/**
	 * Experimental interface: get the WindowSession for this page(set).
	 */
	@NonNull
	public WindowSession getWindowSession() {
		if(null != m_manager)
			return m_manager;
		throw new IllegalStateException("Not initialized?");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Contained objects map								*/
	/*--------------------------------------------------------------*/
	/**
	 * Set an attribute for this context.
	 */
	public void setAttribute(final String name, final Object val) {
		Object old = m_map.put(name, val);

		if(old != null) {
			if(old instanceof IConversationStateListener) {
				try {
					((IConversationStateListener) old).conversationDetached(this);
				} catch(Exception x) {
					x.printStackTrace();
					LOG.error("In calling detach listener", x);
				}
			}
		}
	}

	/**
	 * Get a specific attribute from this context.
	 */
	@Nullable
	public Object getAttribute(final String name) {
		return m_map.get(name);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Upload code.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Register a file that was uploaded and that needs to be deleted at end of conversation time.
	 */
	public void registerTempFile(@NonNull final File f) {
		if(m_uploadList == Collections.EMPTY_LIST)
			m_uploadList = new ArrayList<File>();
		m_uploadList.add(f);
	}

	protected void discardTempFiles() {
		for(File f : m_uploadList) {
			try {
				if(f.isDirectory())
					FileTool.deleteDir(f);
				else
					f.delete();
			} catch(Exception x) {}
		}
		m_uploadList.clear();
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Debug														*/
	/*----------------------------------------------------------------------*/
	public void dump() {
		for(File df : m_uploadList) {
			System.out.println("      Uploaded file: " + df);
		}
	}



	/*--------------------------------------------------------------*/
	/*	CODING:	IQContextContainer implementation.					*/
	/*--------------------------------------------------------------*/
	@Override
	@NonNull
	public QContextContainer getContextContainer(@NonNull String key) {
		key = "cc-" + key;
		QContextContainer cc = (QContextContainer) getAttribute(key);
		if(null == cc) {
			cc = new DomUIContextContainer();
			setAttribute(key, cc);
		}
		return cc;
	}

	@Override
	@NonNull
	public List<QContextContainer> getAllContextContainers() {
		List<QContextContainer> ccl = new ArrayList<QContextContainer>();
		for(Object o : m_map.values()) {
			if(o instanceof QContextContainer) {
				ccl.add((QContextContainer) o);
			}
		}
		return ccl;
	}

	static private final class DomUIContextContainer extends QContextContainer implements IConversationStateListener {
		@Override
		public void conversationNew(@NonNull AbstractConversationContext cc) throws Exception {
			QDataContext c = internalGetSharedContext();
			if(c instanceof IConversationStateListener)
				((IConversationStateListener) c).conversationNew(cc);
		}

		@Override
		public void conversationAttached(@NonNull AbstractConversationContext cc) throws Exception {
			QDataContext c = internalGetSharedContext();
			if(c instanceof IConversationStateListener)
				((IConversationStateListener) c).conversationAttached(cc);
		}

		@Override
		public void conversationDetached(@NonNull AbstractConversationContext cc) throws Exception {
			QDataContext c = internalGetSharedContext();
			if(c instanceof IConversationStateListener)
				((IConversationStateListener) c).conversationDetached(cc);
		}

		@Override
		public void conversationDestroyed(@NonNull AbstractConversationContext cc) throws Exception {
			QDataContext c = internalGetSharedContext();
			if(c instanceof IConversationStateListener)
				((IConversationStateListener) c).conversationDestroyed(cc);
		}
	}
}
