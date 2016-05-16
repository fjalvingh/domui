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
package to.etc.domui.state;

import java.util.*;

import javax.annotation.*;
import javax.servlet.http.*;

import org.slf4j.*;

import to.etc.domui.server.*;
import to.etc.domui.util.janitor.*;

/**
 * Generic session implementation. The session is specific for the application, and the
 * session type can be overridden by letting DomApplication return a new instance that
 * extends this class.
 * <p>The AppSession also handles user request locking. To prevent us from having to
 * synchronize the entire DOM and PAGE contexts all over the place we lock access to all
 * session-related data as soon as a request touches the session. All requests to get
 * the session pass thru RequestContext.getSession(). So this indicates that per-session
 * data is needed. Before returning the session we check if another call is currently
 * using the session; if so we block until that call leaves the session. When the session
 * is free the current request will claim it using a lock in the session object. When a
 * request terminates it is the responsibility of the toplevel request handler to always
 * unlock the request.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public class AppSession implements HttpSessionBindingListener, IAttributeContainer {
	static private final Logger LOG = LoggerFactory.getLogger(AppSession.class);

	@Nonnull
	final private DomApplication m_application;

	@Nonnull
	final private Map<String, Object> m_objCache = new HashMap<String, Object>();

	/**
	 * Not-null if some other request is executing for this session. Prevent multi-user access
	 * to shared variables.
	 */
	@Nullable
	private Thread m_lockingThread;

	private int m_exceptionRetryCount;

	@Nonnull
	private Map<String, WindowSession> m_windowMap = new HashMap<String, WindowSession>();

	@Nonnull
	private Map<String, Object> m_attributeMap = Collections.EMPTY_MAP;

	public AppSession(@Nonnull DomApplication da) {
		m_application = da;
	}

	final public void internalDestroy() {
		LOG.debug("Destroying AppSession " + this);
		destroyWindowSessions();
		try {
			destroy();
		} catch(Throwable x) {
			LOG.warn("Exception when destroying session", x);
		}
		unbindAll();
	}

	/**
	 * Questionable use.
	 * @return
	 */
	@Nonnull
	public DomApplication getApplication() {
		return m_application;
	}

	/**
	 * Override to get control when this user's session is destroyed.
	 */
	public void destroy() {}

	/**
	 * Unused, needed for interface.
	 * @see javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.http.HttpSessionBindingEvent)
	 */
	@Override
	final public void valueBound(final HttpSessionBindingEvent arg0) {}

	/**
	 * Called for non-debug sessions, where this is directly bound to a
	 * HttpSession. When that session closes this causes the destroy() methods
	 * to be called.
	 * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
	 */
	@Override
	final public void valueUnbound(final HttpSessionBindingEvent arg0) {
		internalDestroy();
	}

	Object findCachedObject(final String classname) {
		return m_objCache.get(classname);
	}

	void putCachedObject(final String classname, final Object o) {
		m_objCache.put(classname, o);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Session locking.									*/
	/*--------------------------------------------------------------*/
	/**
	 * INTERNAL USE ONLY.
	 * Enter the session-controlled monitor: only one thread at-a-time may
	 * access session-related data. This call can be called multiple times
	 * for a thread and will not block. But it is not nested: a single call
	 * to unlockSession() will release the lock.
	 */
	public void internalLockSession() {
		Thread t = Thread.currentThread();

		synchronized(this) {
			for(;;) {
				if(m_lockingThread == null) { // Not claimed at this point?
					m_lockingThread = t; // Claimed by me.
					return;
				}
				if(m_lockingThread == t) // Already claimed by me?
					return; // Useless call, then

				//-- Someone else has locked me. Wait until I'm released.
				try {
					wait();
				} catch(InterruptedException ix) {
					throw new RuntimeException("Waiting for session lock was interrupted.", ix);
				}
			}
		}
	}

	/**
	 * INTERNAL USE ONLY.
	 * Leave the session-controlled monitor. THIS CALL DOES NOT NEST!
	 */
	public void internalUnlockSession() {
		Thread t = Thread.currentThread();
		synchronized(this) {
			if(m_lockingThread == null)
				throw new IllegalStateException("Trying to unlock an AppSession while it's not being owned..");
			if(m_lockingThread != t)
				throw new IllegalStateException("Trying to unlock an AppSession while it's not being owned BY YOU");
			m_lockingThread = null;
			notify();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	WindowSession management							*/
	/*--------------------------------------------------------------*/
	/**
	 * Discards all of the WindowSessions, and force them to destroy themselves.
	 */
	private void destroyWindowSessions() {
		Map<String, WindowSession> map;
		synchronized(this) {
			map = m_windowMap;
			m_windowMap = new HashMap<String, WindowSession>();
		}

		for(WindowSession cm : map.values()) {
			cm.destroyWindow(true);
			m_application.internalCallWindowSessionDestroyed(cm);
		}
	}

	/**
	 * Walks all WindowSessions and checks to see if they have not been used for more than the window
	 * session timeout. All WindowSessions that have expired will then be destroyed. This only checks
	 * the "normal" timestamp. Obituary handling is elsewhere.
	 */
	final public void internalCheckExpiredWindowSessions() {
		List<WindowSession> droplist = null;
		long ets = System.currentTimeMillis() - (long) m_application.getWindowSessionTimeout() * 1000 * 60l;
		synchronized(this) {
			for(WindowSession cm : m_windowMap.values()) {
				if(cm.getLastUsed() < ets) {
					if(droplist == null)
						droplist = new ArrayList<WindowSession>(10);
					droplist.add(cm);
				}
			}
			if(droplist == null)
				return;

			//-- Discard all droplist entries from the hashmap
			for(WindowSession cm : droplist)
				m_windowMap.remove(cm.getWindowID());
		}

		//-- Force state closed.
		for(WindowSession cm : droplist) {
			System.out.println("cm: dropping window session " + cm.getWindowID() + " due to timeout");
			logUser(cm.getWindowID(), "cm: dropping window session " + cm.getWindowID() + " due to timeout");
			try {
				cm.destroyWindow(false);
				m_application.internalCallWindowSessionDestroyed(cm);
			} catch(Exception x) {
				logUser(cm.getWindowID(), "Exception in destroyConversations: " + x);
				LOG.warn("Exception in destroyConversations", x);
			}
		}
	}

	/**
	 * Create a new WindowSession. The thingy has a new, globally-unique ID.
	 * @return
	 */
	@Nonnull
	final public synchronized WindowSession createWindowSession() {
		WindowSession cm = new WindowSession(this);
		m_windowMap.put(cm.getWindowID(), cm);
		cm.internalTouched();
		m_application.internalCallWindowSessionCreated(cm);
		return cm;
	}

	/**
	 * Try to locate the WindowSession with the specified ID in this HttpSession. Returns
	 * null if not found.
	 *
	 * @param wid
	 * @return
	 */
	@Nullable
	final public synchronized WindowSession findWindowSession(@Nonnull final String wid) {
		WindowSession cm = m_windowMap.get(wid);
		if(cm != null) {
			cm.internalTouched();
			if(!resurrectWindowSession(cm))
				return null;
		}
		return cm;
	}

	/**
	 * Marks the WindowSession as recently used, and cancels any obituary processing on it.
	 * @param cm
	 */
	private synchronized boolean resurrectWindowSession(@Nonnull final WindowSession cm) {
		cm.internalTouched();
		int tm = cm.getObituaryTimer();							// Obituary timer has started?
		if(tm == -1)											// Nope, nothing wrong
			return true;
		cm.setObituaryTimer(-1);
		boolean res = Janitor.getJanitor().cancelJob(tm);
		System.out.println("session: resurrected window " + cm.getWindowID() + ", canceltask=" + res);
		logUser(cm.getWindowID(), "session: resurrected window " + cm.getWindowID() + ", canceltask=" + res);
		return res;
	}

	/**
	 * Mark the WindowSession as "possibly deleted". Called from the server when an Obituary has
	 * been received, indicating that some page in the window was closed. This state causes the
	 * window to be discarded if no request to it is received within a minute. For normal navigation
	 * this always works, because the destruction of an old page is followed immediately by the
	 * restore of another page in the same window, hence a request on the windowSession.
	 *
	 * <h2>Problems with obituary receive order</h2>
	 * <p>To bloody ^**^&)*&^)&*%&^% complicate matters further the reception of the Obituary is
	 * often out-of-order, meaning that the request for the NEW page is received BEFORE the Obituary
	 * of the old page is received. This of course f*cks up the process, again. To fix this we keep
	 * track of when the last request was received, keeping in mind that the request and the obituary
	 * are always close together in time. For now we do not mark a WindowSession as possibly deleted
	 * if it's previous request is before but close to the obituary's request.</p>
	 *
	 * @param cm			The WindowSession for which the obituary was received.
	 * @param obitPageTag	The page tag of the page that has died.
	 * @throws Exception
	 */
	public synchronized void internalObituaryReceived(final String cid, final int obitPageTag) throws Exception {
		final WindowSession cm = m_windowMap.get(cid);
		if(cm == null) {
			logUser(cid, "Obituary ignored: the window ID is unknown");
			LOG.info("Obituary ignored: the window ID is unknown");
			return;
		}

		if(cm.getObituaryTimer() != -1) { // Already marked?
			logUser(cid, "Obituary ignored: the kill timer has already been started");
			LOG.info("Obituary ignored: the kill timer has already been started");
			return;
		}
		if(obitPageTag != cm.internalGetLastPageTag()) {// Some other page is already present?
			LOG.info("Obituary ignored: the last page has a different page tag (the corpse arrived too late)");
			logUser(cid, "Obituary ignored: the last page has a different page tag (the corpse arrived too late)");
			return;
		}
		long ts = System.currentTimeMillis() - 2000; // 2 seconds ago....
		if(cm.getLastUsed() > ts) { // Used less than 2 seconds ago?
			logUser(cid, "Obituary ignored: the last request was less than 2 secs ago (order problem)");
			LOG.info("Obituary ignored: the last request was less than 2 secs ago (order problem)");
			return;
		}

		/*
		 * This might be a deadish thing. Post a timer which will destroy this WindowSession after
		 * a minute (...). The timer will be cancelled if another request is received for this
		 * window session before the timer expires.
		 */
		int timer = Janitor.getJanitor().addTask(60, true, "DropWindow", new JanitorTask() {
			@Override
			public void run() throws Exception {
				internalDropWindowSession(cm);
			}
		});
		cm.setObituaryTimer(timer);
		LOG.info("Obituary kill timer " + timer + " set to kill window=" + cm.getWindowID());
		logUser(cid, "Obituary kill timer " + timer + " set to kill window=" + cm.getWindowID());
	}

	/**
	 * Internal timeout handler for a WindowSession whose Obituary timer has fired. This deletes
	 * the expired WindowSession from the known window map, then destroys all conversations therein.
	 * @param cm
	 */
	void internalDropWindowSession(final WindowSession cm) {
		if(LOG.isInfoEnabled())
			LOG.info("session: destroying WindowSession=" + cm.getWindowID() + " because it's obituary was received.");
		logUser(cm.getWindowID(), "session: destroying WindowSession=" + cm.getWindowID() + " because it's obituary was received.");
		synchronized(this) {
			if(cm.getObituaryTimer() == -1) 					// Was cancelled?
				return; 										// Do not drop it then.
			m_windowMap.remove(cm.getWindowID()); 				// Atomically remove the thingy.
		}
		cm.destroyWindow(false);								// Discard all of it's contents.
		m_application.internalCallWindowSessionDestroyed(cm);
	}

	/**
	 * Helper utility to dump the session's conversational state.
	 */
	public synchronized void dump() {
		if(LOG.isDebugEnabled()) {
			System.out.println("============= AppSession's WindowList =============");
			for(WindowSession cm : m_windowMap.values()) {
				cm.dump();
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IAttributeContainer implementation.					*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.server.IAttributeContainer#getAttribute(java.lang.String)
	 */
	@Override
	@Nullable
	public Object getAttribute(@Nonnull String name) {
		return m_attributeMap.get(name);
	}

	@Override
	public void setAttribute(@Nonnull String name, @Nullable Object value) {
		if(m_attributeMap == Collections.EMPTY_MAP)
			m_attributeMap = new HashMap<String, Object>();
		if(value == null) {
			Object item = m_attributeMap.remove(name);
			if(item instanceof IAppSessionBindingListener)
				((IAppSessionBindingListener) item).unboundFromSession(this, name);
		} else {
			m_attributeMap.put(name, value);
			if(value instanceof IAppSessionBindingListener)
				((IAppSessionBindingListener) value).boundToSession(this, name);
		}
	}

	private void	unbindAll() {
		if(m_attributeMap.size() == 0)
			return;
		for(String name: m_attributeMap.keySet()) {
			Object value = m_attributeMap.get(name);
			if(value instanceof IAppSessionBindingListener) {
				try {
					((IAppSessionBindingListener) value).unboundFromSession(this, name);
				} catch(Exception x) {
					x.printStackTrace();
				}
			}
		}
	}

	/**
	 * Saves this session's windows and their shelve stacks into the HttpSession allowing
	 * a session to be "resurrected" after a development mode reload.
	 *
	 * @param httpSession
	 */
	synchronized void saveOldState(@Nonnull HttpSession httpSession) {
		for(WindowSession ws : m_windowMap.values()) {
			List<SavedPage> wl = ws.getSavedPageList();
			SavedWindow sw = new SavedWindow(ws.getWindowID(), wl);
			httpSession.setAttribute(ws.getWindowID(), sw);
			System.out.println("appSession: saved " + sw);
		}
	}

	public synchronized int getExceptionRetryCount() {
		return m_exceptionRetryCount;
	}

	public synchronized int incrementExceptionCount() {
		return ++m_exceptionRetryCount;
	}

	public synchronized void clearExceptionRetryCount() {
		m_exceptionRetryCount = 0;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	User events log.									*/
	/*--------------------------------------------------------------*/

	@Nonnull
	final private LinkedList<UserLogItem> m_itemList = new LinkedList<>();

	/**
	 * Add a log item to the round-robin list of per user log entries.
	 * @param uli
	 */
	public synchronized void log(@Nonnull UserLogItem uli) {
		while(m_itemList.size() > 400)
			m_itemList.removeFirst();
		m_itemList.addLast(uli);
	}

	/**
	 * Get a copy of the currently collected log entries.
	 * @return
	 */
	@Nonnull
	synchronized public List<UserLogItem> getLogItems() {
		return new ArrayList<>(m_itemList);
	}

	public void logUser(@Nonnull String cid, @Nonnull String message) {
		log(new UserLogItem(cid, null, null, null, message));
	}
}
