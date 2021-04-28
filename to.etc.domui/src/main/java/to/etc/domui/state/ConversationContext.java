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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.delayed.AsyncContainer;
import to.etc.domui.component.delayed.IAsyncListener;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Page;
import to.etc.domui.server.RequestContextImpl;
import to.etc.parallelrunner.IAsyncRunnable;
import to.etc.util.WrappedException;
import to.etc.webapp.query.IQContextContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
public class ConversationContext extends AbstractConversationContext implements IQContextContainer {
	//static public final Logger LOG = LoggerFactory.getLogger(ConversationContext.class);

	/** The conversation ID, unique within the user's session. */
	@Nullable
	private String m_id;

	@Nullable
	private String m_fullId;

	/** The pages that are part of this conversation, indexed by [className] */
	@NonNull
	private final Map<String, Page> m_pageMap = new HashMap<>();

	@Nullable
	private DelayedActivitiesManager m_delayManager;

	@NonNull
	private Map<String, String> m_persistedParameterMap = new HashMap<>();

	/**
	 * The contexts of all (live) subPages in the conversation.
	 */
	@NonNull
	private Set<SubConversationContext> m_subConversationSet = new HashSet<>();

	/**
	 * Return the ID for this conversation.
	 */
	final public String getId() {
		if(null == m_id)
			throw new IllegalStateException("ID is null??");
		return m_id;
	}

	final void initialize(@NonNull final WindowSession m, @NonNull String id) {
		if(m_id != null)
			throw new IllegalStateException("ID set twice?");
		super.initialize(m);
		m_id = id;
		m_fullId = m.getWindowID() + "." + m_id;
	}

	/**
	 * Updates persistent conversation parameters from the request
	 * context, and restores in there the parameters already registered.
	 */
	public void mergePersistentParameters(RequestContextImpl ctx) {
		Set<String> nameSet = ctx.getApplication().getPersistentParameterSet();
		if(nameSet.size() == 0)
			return;
		for(String name : nameSet) {
			String value = ctx.getPageParameters().getString(name, null);
			if(null != value)
				m_persistedParameterMap.put(name, value);
		}
		ctx.updatePersistentParameters(m_persistedParameterMap);
	}

	public void savePersistedParameter(String name, String value) {
		Set<String> nameSet = getWindowSession().getApplication().getPersistentParameterSet();
		if(! nameSet.contains(name))
			throw new IllegalStateException("The parameter name '" + name + "' is not registered as a persistent parameter. Add it in DomApplication.initialize() using addPersistentParameter");
		m_persistedParameterMap.put(name, value);
	}

	public String getFullId() {
		if(null == m_fullId)
			throw new IllegalStateException("fullID is null??");
		return m_fullId;
	}

	@NonNull
	@Override
	public String toString() {
		return "conversation[" + getFullId() + "]";
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Lifecycle management								*/
	/*--------------------------------------------------------------*/
	/**
	 * @param sessionDestroyed		indicates that the HttpSession has been invalidated somehow, possibly logout
	 */
	@Override
	void internalDestroy(boolean sessionDestroyed) throws Exception {
		LOG.info("Destroying " + this);
		if(getState() == ConversationState.DESTROYED) {
			if(!sessionDestroyed)
				throw new IllegalStateException("Wrong state for DESTROY: " + getState());
			return;
		}

		//-- Call the DESTROY handler for all attached pages, then disconnect them
		for(Page pg : new ArrayList<>(m_pageMap.values())) {
			try {
				destroyPage(pg);
				//pg.getBody().onDestroy();
			} catch(Exception x) {
				if(! sessionDestroyed) {
					System.err.println("Exception in page " + pg.getBody() + "'s onDestroy handler: " + x);
					x.printStackTrace();
				}
			}
		}
		m_pageMap.clear();

		// Destroy all subconversations
		for(SubConversationContext subContext : m_subConversationSet) {
			try {
				destroySubConversation(subContext);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
		m_subConversationSet.clear();

		if(m_delayManager != null) {
			m_delayManager.terminate();
			m_delayManager = null;
		}
		super.internalDestroy(sessionDestroyed);
	}

	@Override void internalAttach() throws Exception {
		super.internalAttach();
		for(SubConversationContext sc : m_subConversationSet) {
			sc.internalAttach();
		}
	}

	@Override void internalDetach() throws Exception {
		super.internalDetach();
		for(SubConversationContext sc : m_subConversationSet) {
			sc.internalDetach();
		}
	}

	/**
	 * Force this context to destroy itself.
	 */
	@Override
	public void destroy() {
		getWindowSession().destroyConversation(this);
		super.destroy();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Conversation page management.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns any cached page in this context.
	 */
	Page findPage(final Class< ? extends NodeBase> clz) {
		return m_pageMap.get(clz.getName());
	}

	public void internalRegisterPage(@NonNull final Page p, @NonNull final IPageParameters papa) {
		m_pageMap.put(p.getBody().getClass().getName(), p);
		p.internalInitialize(papa, this);
	}

	void destroyPage(@NonNull final Page pg) {
		//-- Call the page's DESTROY handler while still attached
		try {
			pg.internalOnDestroy();
		} catch(Exception x) {
			System.err.println("Exception in page " + pg.getBody() + "'s onDestroy handler: " + x);
			x.printStackTrace();
		}
		m_pageMap.remove(pg.getBody().getClass().getName());
		getWindowSession().getApplication().callPageDestroyedListeners(pg);
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	SubConversation management									*/
	/*----------------------------------------------------------------------*/

	/**
	 * Register and activate a subconversation context.
	 */
	public void addSubConversation(SubConversationContext suc) {
		if(m_subConversationSet.add(suc)) {
			if(suc.getState() == ConversationState.DETACHED) {
				suc.initialize(getWindowSession());
				try {
					suc.internalAttach();
				} catch(Exception x) {
					throw WrappedException.wrap(x);			// Really great those checked exceptions 8-(
				}
			}
		}
	}

	/**
	 * Detaches and discards a subconversation.
	 */
	public void removeAndDestroySubConversation(SubConversationContext suc) throws Exception {
		if(! m_subConversationSet.remove(suc))
			return;
		destroySubConversation(suc);
	}

	private void destroySubConversation(SubConversationContext suc) throws Exception {

		//-- Detach and destroy
		if(suc.getState() == ConversationState.ATTACHED) {
			suc.internalDetach();
		}
		if(suc.getState() == ConversationState.DETACHED) {
			suc.internalDestroy(false);
		}
		suc.clear();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Delayed activities scheduling.						*/
	/*--------------------------------------------------------------*/
	synchronized DelayedActivitiesManager getDelayedActivitiesManager() {
		if(m_delayManager == null)
			m_delayManager = new DelayedActivitiesManager();
		return m_delayManager;
	}

	/**
	 * Schedule an activity to be started later. This calls the {@link IAsyncListener#onActivityScheduled(IAsyncRunnable)} method of
	 * all listeners, and stores their "keep object" for later use.
	 */
	public DelayedActivityInfo scheduleDelayed(@NonNull final AsyncContainer container, @NonNull final IAsyncRunnable a) throws Exception {
		return getDelayedActivitiesManager().schedule(a, container);
	}

	public void startDelayedExecution() {
		if(m_delayManager != null)
			m_delayManager.start();
	}

	public void processDelayedResults(final Page pg) throws Exception {
		DelayedActivitiesManager delayManager = m_delayManager;
		if(delayManager == null)
			return;
		delayManager.processDelayedResults(pg);
	}

	/**
	 * If the page has asynchronous stuff, this returns true.
	 */
	public boolean isPollCallbackRequired() {
		DelayedActivitiesManager delayManager = m_delayManager;
		return delayManager != null && delayManager.callbackRequired();
	}

	/**
	 * Registers a node as a thingy which needs to be called every polltime seconds to
	 * update the screen. This is not an asy action by itself (it starts no threads) but
	 * it will cause the poll handler to start, and will use the same response mechanism
	 * as the asy callback code.
	 */
	public <T extends NodeContainer & IPolledForUpdate> void registerPoller(T nc) {
		getDelayedActivitiesManager().registerPoller(nc);
	}

	public <T extends NodeContainer & IPolledForUpdate> void unregisterPoller(T nc) {
		getDelayedActivitiesManager().unregisterPoller(nc);
	}

	//	/**
	//	 * Forces the activity manager to enable continuous polling by the client system, at least every interval millis.
	//	 */
	//	public void internalSetContinuousPolling(int interval) {
	//		getDelayedActivitiesManager().setContinuousPolling(interval);
	//	}

	@Override
	public void dump() {
		System.out.println("    Conversation: " + getId() + " in state " + getState());
		if(m_delayManager == null)
			System.out.println("      No delayed actions pending");
		else {
			System.out.println("      Delayed action manager is present");
		}

		StringBuilder sb = new StringBuilder(128);
		for(Page pg : m_pageMap.values()) {
			sb.setLength(0);
			IPageParameters pp = pg.getPageParameters();
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
		super.dump();
	}
}
