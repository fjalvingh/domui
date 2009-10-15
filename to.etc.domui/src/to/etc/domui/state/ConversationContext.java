package to.etc.domui.state;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import to.etc.domui.component.delayed.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

/**
 * A page's conversational context: a base class. Every page is part of a
 * conversation. For simple pages the conversation mostly "is" the page,
 * meaning that when the page ends the conversation ends also.
 *
 * Conversations are mostly useful for pages that must share data. In that
 * case a single, strongly typed, conversation instance must be created
 * which will contain the data maintained by the multiple pages. The pages
 * themselves are also part of the conversation; when the conversation
 * terminates all page instances associated with the conversation also cease
 * to exist.
 *
 * <h2>Overview</h2>
 * A set of pages can share a given "Conversation Context". The context
 * represents the set of data that the pages handle within a single user
 * interaction. The conversation context is maintained mostly by the server,
 * and it's construction, destruction and initialization-before-request
 * gets handled by the server itself, on request.
 *
 * Pages themselves are always part of a conversation. Pages that do not
 * specify any conversation data are part of a single-shot conversation,
 * where the conversation data is cleared as soon as another page gets
 * instantiated. This ensures that pages are not cached "forever" in
 * the HttpSession.
 *
 * A page joins a conversation if the page gets "linked" to with the conversation
 * context specified. A page which specifies a specific Conversation
 * context and which is created anew without an existing context will cause
 * the Conversation context to be created automatically.
 *
 * Conversations can be nested, and child conversations can access their
 * parents. What state is shared between children and parents (Hibernate session?)
 * is defined by the user.
 *
 * A conversation context is strongly typed, derives from ConversationContext,
 * and is defined on each class which participates in the same conversation.
 *
 * A page identifies that it requires a specific conversation type by
 * specifying it in it's constructor. This can also be used to accept multiple
 * types as a conversation context: simply specify multiple constructors.
 *
 * The page constructor will associate new pages with their appropriate
 * context automatically, by looking in the known context list of a
 * session. This means that when a given page starts another page which accepts
 * the same context it will be created using that context by default.
 *
 * <h2>Life cycle of a ConversationContext</h2>
 * <h3>Constructing new contexts</h3>
 * When a page is constructed we check it's constructors. We try to use constructors
 * with contexts first by checking if any of the contexts are "current". If so we
 * instantiate the page and associate it with that context.
 *
 * If none of the contexts is active currently we check if a constructor without
 * context is present; if so we create a "single-shot" context which gets destroyed
 * as soon as the page is left and we instantiate the page.
 *
 * If we have context constructors only we abort if there's &gt; 1 constructor with
 * a context - we cannot decide which context to create.
 *
 * If a single constructor is found specifying a context we'll try to instantiate the
 * class using that conversation context. For that we request a Conversation Context
 * instance from the engine, then call the Page constructor adding any other stuff it
 * needs. This creates a "new" context. The new context gets initialized (using IOC
 * or whatever) immediately, and so is usable by the page immediately.
 *
 * <h3>On request start and request end</h3>
 * Conversation contexts can hold data that needs to be cleared between requests, like
 * database connections or large resources. For resources not maintained by the IOC
 * layer this is the responsibility of the application programmer. Each context has the
 * onAttach() and onDetach() methods which are called when the context is made live and
 * before it gets put to sleep again.
 *
 * <h3>Destruction of contexts</h3>
 * When a Conversation gets destroyed it's onClose() handler gets called, which again can
 * be used to discard any long-lived data maintained by the context.
 *
 * Contexts are destroyed by the framework mostly automatically, as follows:
 * <ul>
 * 	<li>When a page calls it's destroyConversation() method. This will cause the conversation
 *		to be destroyed as soon as the current request ends. All pages associated with the
 * 		conversation are destroyed also. A conversation that is marked as "to be destroyed" cannot
 * 		be used for attaching new pages. If you link to another page which requires the
 * 		same conversation type this will cause a *new* conversation to be created of the same type
 * 		as the to-be-destroyed conversation.</li>
 *	<li>When a new page is linked which specifies a new context, and when this is not marked as a
 *		"nested" context.</li>
 * </ul>
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 23, 2008
 */
public class ConversationContext implements IQContextContainer {
	static public final Logger LOG = Logger.getLogger(ConversationContext.class.getName());

	static enum ConversationState {
		DETACHED, ATTACHED, DESTROYED
	}

	/** The conversation ID, unique within the user's session. */
	private String m_id;

	private String m_fullId;

	/** The pages that are part of this conversation, indexed by [className] */
	private final Map<String, Page> m_pageMap = new HashMap<String, Page>();

	/** The map of all attribute objects added to this conversation. */
	private Map<String, Object> m_map = Collections.EMPTY_MAP;

	private WindowSession m_manager;

	private DelayedActivitiesManager m_delayManager;

	private ConversationState m_state = ConversationState.DETACHED;

	private List<File> m_uploadList = Collections.EMPTY_LIST;

	void setId(final String id) {
		m_id = id;
	}

	/**
	 * Return the ID for this conversation.
	 * @return
	 */
	final public String getId() {
		return m_id;
	}

	final void setManager(final WindowSession m) {
		if(m == null)
			throw new IllegalStateException("Internal: manager cannot be null, dude");
		if(m_manager != null)
			throw new IllegalStateException("Internal: manager is ALREADY set, dude");
		m_manager = m;
		m_fullId = m.getWindowID() + "." + m_id;
	}

	public String getFullId() {
		return m_fullId;
	}

	@Override
	public String toString() {
		return "conversation[" + getId() + "]";
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Lifecycle management								*/
	/*--------------------------------------------------------------*/
	/**
	 * Called when a new request which accesses this context is entering the
	 * server. This should restore the context to a usable state.
	 * @throws Exception
	 */
	public void onAttach() throws Exception {}

	/**
	 * Called when the request has terminated, the response has been rendered and the
	 * server is about to exit all handling for the request. This must discard any
	 * data that should not be stored between requests, and it must discard any
	 * resource like database connections and the like.
	 *
	 * @throws Exception
	 */
	public void onDetach() throws Exception {}

	public void onDestroy() throws Exception {}

	void internalAttach() throws Exception {
		LOG.fine("Attaching " + this);
		if(m_state != ConversationState.DETACHED)
			throw new IllegalStateException("Wrong state for ATTACH: " + m_state);
		for(Object o : m_map.values()) {
			if(o instanceof ConversationStateListener) {
				try {
					((ConversationStateListener) o).conversationAttached(this);
				} catch(Exception x) {
					x.printStackTrace();
					LOG.log(Level.SEVERE, "In calling attach listener", x);
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
		LOG.fine("Detaching " + this);
		if(m_state != ConversationState.ATTACHED)
			throw new IllegalStateException("Wrong state for DETACH: " + m_state + " in " + this);
		for(Object o : m_map.values()) {
			if(o instanceof ConversationStateListener) {
				try {
					((ConversationStateListener) o).conversationDetached(this);
				} catch(Exception x) {
					x.printStackTrace();
					LOG.log(Level.SEVERE, "In calling detach listener", x);
				}
			}
		}
		try {
			onDetach();
		} finally {
			m_state = ConversationState.DETACHED;
		}
	}

	void internalDestroy() throws Exception {
		LOG.info("Destroying " + this);
		if(m_state == ConversationState.DESTROYED)
			throw new IllegalStateException("Wrong state for DESTROY: " + m_state);

		//-- Call the DESTROY handler for all attached pages, then disconnect them
		for(Page pg : m_pageMap.values()) {
			try {
				pg.getBody().onDestroy();
			} catch(Exception x) {
				System.err.println("Exception in page " + pg.getBody() + "'s onDestroy handler: " + x);
				x.printStackTrace();
			}
		}
		m_pageMap.clear();

		if(m_delayManager != null) {
			m_delayManager.terminate();
			m_delayManager = null;
		}

		for(Object o : m_map.values()) {
			if(o instanceof ConversationStateListener) {
				try {
					((ConversationStateListener) o).conversationDestroyed(this);
				} catch(Exception x) {
					x.printStackTrace();
					LOG.log(Level.SEVERE, "In calling destroy listener", x);
				}
			}
		}
		try {
			onDestroy();
		} finally {
			m_state = ConversationState.DESTROYED;
			discardUploadFiles();
		}
	}

	public void checkAttached() {
		if(m_state != ConversationState.ATTACHED)
			throw new IllegalStateException("Accessing conversation " + this + " in " + m_state + " invalid - only usable in ATTACHED state");
	}

	/**
	 * Force this context to destroy itself.
	 */
	public void destroy() {
		m_manager.destroyConversation(this);
		m_manager = null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Conversation page management.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns any cached page in this context.
	 * @param clz
	 * @return
	 */
	Page findPage(final Class< ? extends NodeBase> clz) {
		return m_pageMap.get(clz.getName());
	}

	public void internalRegisterPage(final Page p, final PageParameters papa) {
		m_pageMap.put(p.getBody().getClass().getName(), p);
		p.internalInitialize(papa, this);
	}

	void destroyPage(final Page pg) {
		//-- Call the page's DESTROY handler while still attached
		try {
			pg.getBody().onDestroy();
		} catch(Exception x) {
			System.err.println("Exception in page " + pg.getBody() + "'s onDestroy handler: " + x);
			x.printStackTrace();
		}
		m_pageMap.remove(pg.getBody().getClass().getName());
	}

	/**
	 * Experimental interface: get the WindowSession for this page(set).
	 * @return
	 */
	public WindowSession getWindowSession() {
		return m_manager;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Contained objects map (EXPERIMENTAL)				*/
	/*--------------------------------------------------------------*/
	/**
	 * EXPERIMENTAL DO NOT USE.
	 * @param name
	 * @param val
	 */
	public void setAttribute(final String name, final Object val) {
		if(m_map == Collections.EMPTY_MAP)
			m_map = new HashMap<String, Object>();
		Object old = m_map.put(name, val);

		if(old != null) {
			if(old instanceof ConversationStateListener) {
				try {
					((ConversationStateListener) old).conversationDetached(this);
				} catch(Exception x) {
					x.printStackTrace();
					LOG.log(Level.SEVERE, "In calling detach listener", x);
				}
			}
		}
	}

	/**
	 * EXPERIMENTAL DO NOT USE.
	 * @param name
	 * @return
	 */
	public Object getAttribute(final String name) {
		return m_map.get(name);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Delayed activities scheduling.						*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @return
	 */
	synchronized DelayedActivitiesManager getDelayedActivitiesManager() {
		if(m_delayManager == null)
			m_delayManager = new DelayedActivitiesManager(this);
		return m_delayManager;
	}

	public DelayedActivityInfo scheduleDelayed(final AsyncContainer container, final IActivity a) {
		return getDelayedActivitiesManager().schedule(a, container);
	}

	public void startDelayedExecution() {
		if(m_delayManager != null)
			m_delayManager.start();
	}

	public void processDelayedResults(final Page pg) throws Exception {
		if(m_delayManager == null)
			return;
		m_delayManager.processDelayedResults(pg);
	}

	public boolean hasDelayedActions() {
		return m_delayManager == null ? false : m_delayManager.callbackRequired();
	}

	/**
	 * Registers a node as a thingy which needs to be called every polltime seconds to
	 * update the screen. This is not an asy action by itself (it starts no threads) but
	 * it will cause the poll handler to start, and will use the same response mechanism
	 * as the asy callback code.
	 * @param nc
	 */
	public <T extends NodeContainer & IPolledForUpdate> void registerPoller(T nc) {
		getDelayedActivitiesManager().registerPoller(nc);
	}

	public <T extends NodeContainer & IPolledForUpdate> void unregisterPoller(T nc) {
		getDelayedActivitiesManager().unregisterPoller(nc);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Upload code.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Register a file that was uploaded and that needs to be deleted at end of conversation time.
	 * @param f
	 */
	public void registerUploadTempFile(final File f) {
		if(m_uploadList == Collections.EMPTY_LIST)
			m_uploadList = new ArrayList<File>();
		m_uploadList.add(f);
	}

	protected void discardUploadFiles() {
		for(File f : m_uploadList) {
			try {
				f.delete();
			} catch(Exception x) {}
		}
		m_uploadList.clear();
	}

	public void dump() {
		System.out.println("    Conversation: " + getId() + " in state " + m_state);
		if(m_delayManager == null)
			System.out.println("      No delayed actions pending");
		else {
			System.out.println("      Delayed action manager is present");
		}

		for(File df : m_uploadList) {
			System.out.println("      Uploaded file: " + df);
		}

		StringBuilder sb = new StringBuilder(128);
		for(Page pg : m_pageMap.values()) {
			sb.setLength(0);
			PageParameters pp = pg.getPageParameters();
			sb.append("      Resident page: ");
			sb.append(pg.getBody().getClass().getName());
			sb.append(" [");
			sb.append(pg.getPageTag());
			sb.append("] ");
			if(pp == null)
				sb.append("(no parameters)");
			else {
				sb.append(pp.toString());
			}

			System.out.println(sb.toString());
		}
	}

	ConversationState getState() {
		return m_state;
	}

	public boolean isValid() {
		return m_state == ConversationState.ATTACHED;
	}

	static private final String KEY = QContextManager.class.getName();

	static private final String SRCKEY = QDataContextFactory.class.getName();

	/*--------------------------------------------------------------*/
	/*	CODING:	IQContextContainer implementation.					*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 */
	public QDataContext internalGetSharedContext() {
		return (QDataContext) getAttribute(KEY);
	}

	/**
	 *
	 * @see to.etc.webapp.query.IQContextContainer#internalSetSharedContext(to.etc.webapp.query.QDataContext)
	 */
	public void internalSetSharedContext(final QDataContext c) {
		setAttribute(KEY, c);
	}

	public QDataContextFactory internalGetDataContextFactory() {
		return (QDataContextFactory) getAttribute(SRCKEY);
	}

	public void internalSetDataContextFactory(final QDataContextFactory s) {
		setAttribute(SRCKEY, s);
	}
}
