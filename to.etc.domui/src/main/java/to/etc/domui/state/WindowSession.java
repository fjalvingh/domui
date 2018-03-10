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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;
import javax.servlet.http.*;

import org.slf4j.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.ConversationContext.ConversationState;
import to.etc.domui.util.*;
import to.etc.net.*;
import to.etc.util.*;

/**
 * Manages conversations and the shelve stack. A WindowSession is in charge of all conversations
 * within a given window, and maintains all state there.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 24, 2008
 */
final public class WindowSession {
	static final Logger LOG = LoggerFactory.getLogger(WindowSession.class);

	@Nonnull
	final private AppSession m_appSession;

	@Nonnull
	final private String m_windowID;

	final private boolean m_developerMode;

	final private int m_id;

	static private int m_nextId;

	/**
	 * Map of all active conversations, indexed by conversation ID.
	 */
	final private Map<String, ConversationContext> m_conversationMap = new HashMap<String, ConversationContext>();

	/**
	 * Recently removed conversations.
	 */
	final private Map<String, Long> m_destroyedConversationMap = new HashMap<String, Long>();

	/**
	 * The stack of shelved pages; pages that can be returned to easily.
	 */
	private final List<IShelvedEntry> m_shelvedPageStack = new ArrayList<IShelvedEntry>();

	private int m_nextCid;

	private boolean m_attached;

	@Nullable
	private Class< ? extends ConversationContext> m_targetConversationClass;

	@Nullable
	private Class< ? extends UrlPage> m_targetPageClass;

	@Nullable
	private IPageParameters m_targetPageParameters;

	@Nullable
	private ConversationContext m_targetConversation;

	@Nullable
	private MoveMode m_targetMode;

	@Nullable
	private String m_targetURL;

	/** Timestamp of the last time this WindowSession was used by a request. This is used to determine if a WindowSession has expired */
	private long m_lastUsed;

	/** When an obituary has been received, this contains the ID of the destruction timer for this session. It contains -1 if no destruction timer is active on this window */
	private int m_obituaryTimer = -1;

	/** The page tag of the last page that had a request for this window session. Used to decide whether an Obituary is out-of-order. */
	private int m_lastRequestedPageTag;

	/** The map of all attribute objects added to this window session. */
	private Map<String, Object> m_map = Collections.EMPTY_MAP;

	public WindowSession(@Nonnull final AppSession session) {
		m_appSession = session;
		m_windowID = DomUtil.generateGUID();
		m_id = nextID();
		m_developerMode = session.getApplication().inDevelopmentMode();
	}

	static private synchronized int nextID() {
		return ++m_nextId;
	}

	@Nonnull
	final public DomApplication getApplication() {
		return m_appSession.getApplication();
	}

	@Nonnull
	final public String getWindowID() {
		return m_windowID;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Conversation stack management.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Return a new, unique ID for a conversation in this user's session.
	 * @return
	 */
	synchronized int nextCID() {
		return ++m_nextCid;
	}

	/**
	 * Locate a conversation by ID. Returns null if the conversation is not found.
	 * @param cid
	 * @return
	 */
	@Nullable
	public ConversationContext findConversation(@Nonnull final String cid) throws Exception {
		ConversationContext cc = m_conversationMap.get(cid);
		if(null != cc)
			internalAttachConversations();
		return cc;
	}

	/**
	 * Return all conversations that contain the specified page class.
	 * @param clz
	 * @return
	 */
	@Nonnull
	List<ConversationContext> findConversationsFor(@Nonnull final Class< ? extends NodeBase> clz) throws Exception {
		List<ConversationContext> res = new ArrayList<ConversationContext>();
		for(ConversationContext cc : m_conversationMap.values()) {
			if(cc.findPage(clz) != null)
				res.add(cc);
		}
		if(res.size() > 0)
			internalAttachConversations();
		return res;
	}

	/**
	 * Add a new conversation to the conversation context.
	 * @param cc
	 */
	void registerConversation(@Nonnull final ConversationContext cc, @Nullable String cid) {
		if(cid == null)
			cid = "c" + nextCID();
		cc.initialize(this, cid);
		m_conversationMap.put(cc.getId(), cc);
	}

	/**
	 * Call the "attach" method for all conversations, indicating that a new request
	 * is going to be handled.
	 * @throws Exception
	 */
	public void internalAttachConversations() throws Exception {
		if(m_attached)
			return;
		for(ConversationContext cc : m_conversationMap.values())
			cc.internalAttach();
		m_attached = true;
	}

	/**
	 * Detach all conversations, called just before the request is done.
	 */
	public void internalDetachConversations() {
		//		System.out.println("detachConversations called with state="+m_attached);
		if(!m_attached)
			return;
		m_attached = false;
		for(ConversationContext cc : m_conversationMap.values()) {
			try {
				cc.internalDetach();
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	/**
	 * Dump all conversations and their resident pages.
	 */
	public void dump() {
		System.out.println("  " + this + ": Conversation list");
		for(ConversationContext cc : m_conversationMap.values()) {
			cc.dump();
		}
		System.out.println("  Page shelve");
		for(int i = 0; i < m_shelvedPageStack.size(); i++) {
			IShelvedEntry se = m_shelvedPageStack.get(i);
			System.out.println("  " + i + ": " + se);
		}

//		System.out.println("  ---- Conversation dump end -----");
	}

	/**
	 * @param sessionDestroyed		indicates that the HttpSession has been invalidated somehow, possibly logoout
	 */
	void destroyWindow(boolean sessionDestroyed) {
		destroyConversations(sessionDestroyed);
		destroyDevelopmentStateFile();
	}


	/**
	 * Closes all conversations. This discards all screen data and resources.
	 * @param sessionDestroyed		indicates that the HttpSession has been invalidated somehow, possibly logoout
	 */
	void destroyConversations(boolean sessionDestroyed) {
		m_attached = false;
		for(ConversationContext cc : m_conversationMap.values()) {
			try {
				cc.internalDestroy(sessionDestroyed);
			} catch(Exception x) {
				//x.printStackTrace();							// Not much use to log exceptions on teardown
			}
		}
		m_conversationMap.clear();
	}

	protected void destroyConversation(@Nonnull final ConversationContext cc) {
		if(null == m_conversationMap.remove(cc.getId()))
			return;

		//-- Discard all pages used by this from the shelve stack
		for(int i = m_shelvedPageStack.size(); --i >= 0;) {
			IShelvedEntry she = m_shelvedPageStack.get(i);
			if(she instanceof ShelvedDomUIPage) {
				ShelvedDomUIPage sdp = (ShelvedDomUIPage) she;
				if(sdp.getPage().getConversation() == cc) {
					m_shelvedPageStack.remove(i);
				}
			}
		}

		try {
			if(cc.getState() == ConversationState.ATTACHED)
				cc.internalDetach();
		} catch(Exception x) {
			LOG.error("Exception on onDetach() of destroyed conversation", x);
		}
		try {
			cc.internalDestroy(false);
		} catch(Exception x) {
			LOG.error("Exception in onDestroy() of destroyed conversation", x);
		}

		//-- Add to destroyed conversation map.
		m_destroyedConversationMap.put(cc.getId(), Long.valueOf(System.currentTimeMillis()));
	}

	/**
	 * Quickly check if a conversation is (recently) destroyed, this should prevent "event reordering problems" like in etc.to bugzilla bug#3138.
	 * @param ccid
	 * @return
	 */
	public boolean isConversationDestroyed(@Nonnull String ccid) {
		boolean isdestroyed = m_destroyedConversationMap.containsKey(ccid);

		//-- Remove entries if it grows too big.
		if(m_destroyedConversationMap.size() > 20) {
			long cts = System.currentTimeMillis() - 5 * 1000;
			for(Iterator<Map.Entry<String, Long>> it = m_destroyedConversationMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, Long> me = it.next();
				if(me.getValue().longValue() < cts)
					it.remove();
			}
		}

		return isdestroyed;
	}

	public ConversationContext createConversation(@Nonnull final Class< ? extends ConversationContext> clz) throws Exception {
		if(clz == null)
			throw new IllegalStateException("Null");
		ConversationContext cc = clz.newInstance(); 	// FIXME Should do something with injection and stuff.
		m_appSession.getApplication().internalCallConversationCreated(cc);
		return cc;
	}

	public void acceptNewConversation(@Nonnull final ConversationContext cc) throws Exception {
		//-- Drop all "old" conversations, then add the new one
//		destroyConversations();							// ORDERED 1
		registerConversation(cc, null); 				// ORDERED 2
		cc.internalAttach(); 							// ORDERED 3
		m_attached = true; 								// jal 20090108 "Pages were kept ATTACHED, causing exception on re-entry"
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Shelving and unshelving page handler.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Shelve the current page, then move to the new one.
	 * @param shelved
	 */
	private void shelvePage(@Nonnull final Page shelved) {
		if(shelved == null)
			throw new IllegalStateException("Missing current page??");
		m_shelvedPageStack.add(new ShelvedDomUIPage(this, shelved));
	}

	/**
	 * Get the current contents of the shelved page stack.
	 * @return
	 */
	@Nonnull
	public List<IShelvedEntry> getShelvedPageStack() {
		return new ArrayList<IShelvedEntry>(m_shelvedPageStack);
	}

	/**
	 * Goto handling in EXCEPTION handling mode: only Redirect is allowed here.
	 * @param ctx
	 * @param currentpg
	 * @param ajax
	 * @return
	 */
	public boolean handleExceptionGoto(@Nonnull final RequestContextImpl ctx, @Nonnull final Page currentpg, boolean ajax) throws Exception {
		MoveMode targetMode = getTargetMode();
		if(targetMode == null)
			return false;

		switch(targetMode){
			default:
				throw new IllegalStateException("UIGoto." + targetMode + " is invalid when calling UIGoto from an exception listener");

			case REPLACE:
			case REDIRECT:
			case NEW:
			case SUB:
				break;
		}
		return handleGoto(ctx, currentpg, ajax);
	}

	/**
	 * This checks whether a new page is to be made resident, instead of the
	 * current page.
	 *
	 * @param currentpg		The page that is <b>current</b> (the one that issued the MOVE command).
	 */
	public boolean handleGoto(@Nonnull final RequestContextImpl ctx, @Nonnull final Page currentpg, boolean ajax) throws Exception {
		//		System.out.println("GOTO: currentpg=" + currentpg + ", shelved=" + currentpg.isShelved());
		if(getTargetMode() == null)
			return false;
		if(getTargetMode() == MoveMode.BACK) {
			// Back requested-> move back, then.
			handleMoveBack(ctx, currentpg, ajax);
			return true;
		}
		if(getTargetMode() == MoveMode.REDIRECT) {
			String tu = m_targetURL;
			if(null == tu)
				throw new IllegalStateException("No URL in redirect?");
			if(tu.startsWith("/")) {
				tu = ctx.getRequestResponse().getHostURL() + tu.substring(1);
			} else if(tu.indexOf(':') == -1) {
				tu = ctx.getRelativePath(tu); 				// Make absolute.
			}
			logUser(ctx, currentpg, "GOTO redirect to " + tu);
			generateRedirect(ctx, tu, ajax);
			return true;
		}

		//-- We move somewhere else. Really?
		Class< ? extends UrlPage> clz = getTargetPageClass(); // New class set?
		if(clz == null)
			return false;
		// jal 30 augustus 2008 switched off to allow for full re-render of the current page (theme change).
		//		if(clz == pg.getClass())					// Already at that page?
		//			return false;							// Piss off then

		ConversationContext cc = getTargetConversation();
		IPageParameters pp = getTargetPageParameters();
		Constructor< ? extends UrlPage> bestpc = null;

		/*
		 *  jal 20131214 Replace handling change. For replace always pop the topmost page first, then enter normal "move" handling.
		 */
		if(getTargetMode() == MoveMode.REPLACE) {
			int cursz = m_shelvedPageStack.size() - 1;		// Last entry on the stack is the one we're replacing
			if(cursz < 0)
				cursz = 0;
			clearShelve(cursz);								// Drop it
		}

		/*
		 * Look back in the page shelve and check if a compatible page is present there. If so
		 * we move back by destroying the pages "above" the target.
		 */
		//-- Locate the specified page/conversation in the page stack,
		int psix = findInPageStack(cc, clz, pp);
		if(psix != -1) {
			/*
			 * Page found. Is it the current page? If so we just ignore the request.
			 */
			if(psix == m_shelvedPageStack.size() - 1) {
				logUser(ctx, currentpg, "GOTO " + getTargetMode() + " to current page?");
//				return false;
			}

			/*
			 * Entry accepted. Discard all stacked entries *above* the selected thing.
			 */
			clearShelve(psix + 1);
			internalAttachConversations();
			IShelvedEntry xse = m_shelvedPageStack.get(psix);
			if(!(xse instanceof ShelvedDomUIPage))
				throw new IllegalStateException("Shelve entry is not a domui page but " + xse);

			Page currentPage = ((ShelvedDomUIPage) xse).getPage();
			if(currentPage == currentpg) {
				logUser(ctx, currentpg, "GOTO " + getTargetMode() + " to current page - ignored");
				return false;
			}
			logUser(ctx, currentpg, "GOTO " + getTargetMode() + " and unshelve page " + currentPage);

			/*
			 * jal 20100224 The old page is destroyed and we're now running in the "new" page's context! Since
			 * unshelve calls user code - which can access that context using PageContext.getXXX calls- we must
			 * make sure it is correct even though the request was for another page and is almost dying.
			 */
			UIContext.internalSet(currentPage);
			currentPage.internalUnshelve();
			generateRedirect(ctx, currentPage, ajax);
			saveWindowState();
			return true;
		}

		//-- Handle the shelve mode,
		if(getTargetMode() == MoveMode.NEW || mustResetShelve(clz)) {
			clearShelve(0);
		} else if(getTargetMode() == MoveMode.SUB) {
			//-- We're shelving the current page- call all shelve handlers.
			currentpg.internalShelve();
		} else if(getTargetMode() == MoveMode.REPLACE) {
			//-- All has been done already.
		} else
			throw new IllegalStateException("Internal: don't know how to handle shelve mode " + getTargetMode());

		/*
		 * Shite. We need to really move. We need to do context handling here. We
		 * have the following possibilities:
		 * <ul>
		 * 	<li>the old page goes out of scope (it's conversation gets cleared, which also drops the page),</li>
		 *	<li>the new page is connecting to the same conversation as the old page</li>
		 *	<li>the new page has a new conversation; the old conversation is put away (nesting)</li>
		 * </ul>
		 */

		/*
		 * If a new conversation is specified make sure the page class can accept it,
		 * Determine the conversation and the constructor to use for the page.
		 */
		if(cc == null) {
			//-- We need a new conversation of the given type...
			Class< ? extends ConversationContext> coclz = getTargetConversationClass();
			if(coclz == null) {
				bestpc = PageMaker.getBestPageConstructor(clz, pp != null); // Get best constructor;
				coclz = PageMaker.getConversationType(bestpc);
			} else {
				//-- Find a constructor for the specified conversation
				bestpc = PageMaker.getPageConstructor(clz, coclz, pp != null);
			}

			//-- Create the conversation to use,
			cc = createConversation(coclz);
			acceptNewConversation(cc);
		} else {
			//-- We JOIN..... Use the conversation specified. The new page class must accept it, of course,
			bestpc = PageMaker.getPageConstructor(clz, cc.getClass(), pp != null);
			//-- Join is acceptable; conversation is ok, and present in 'cc'
		}

		//-- Conversation has been validated now, and it is active. Create and link the new page now.
		if(pp == null)
			pp = new PageParameters();
		Page currentPage = PageMaker.createPageWithContent(bestpc, cc, pp);
		logUser(ctx, currentpg, "GOTO " + getTargetMode() + " to NEW page " + currentPage);
		UIContext.internalSet(currentPage); 					// jal 20100224 Code can run in new page on shelve.
		shelvePage(currentPage);

		//-- Call all of the page's listeners.
		callNewPageCreatedListeners(currentPage);

		//		callNewPageListeners(m_currentPage); 			// jal 20091122 Bug# 605 Move this globally.
		generateRedirect(ctx, currentPage, ajax);
		saveWindowState();
		return true;
	}

	/**
	 * Returns TRUE if the target page is a page which can only be on top of the shelve. For now
	 * it checks if the page == the index page.
	 * @param clz
	 * @return
	 */
	private boolean mustResetShelve(@Nonnull final Class< ? extends UrlPage> clz) {
		Class<?> ac = m_appSession.getApplication().getRootPage();
		if(ac == null)
			return false;

		return clz.getName().equals(ac.getName());
	}

	void generateRedirect(@Nonnull final RequestContextImpl ctx, @Nonnull final Page to, boolean ajax) throws Exception {
		//-- Send a "redirect" to the new page;
		StringBuilder sb = new StringBuilder();
		sb.append(ctx.getRelativePath(to.getBody().getClass().getName()));
		sb.append('.');
		sb.append(ctx.getApplication().getUrlExtension());
		sb.append('?');
		StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
		sb.append('=');
		sb.append(to.getConversation().getFullId());

		//-- If the parameter string is too big we need to keep them in memory.
		IPageParameters pp = to.getPageParameters();
		if(pp.getDataLength() > 1024) {
			//-- We need a large referral
			to.getConversation().setAttribute("__ORIPP", pp);

			//-- Create an unique hash for the page parameters
			String hashString = pp.calculateHashString();				// The unique hash of a page with these parameters

			PageParameters rpp = new PageParameters();
			pp = rpp;
			rpp.addParameter(Constants.PARAM_POST_CONVERSATION_KEY, hashString);
		}

		//-- Add any parameters
		if(pp != null) {
			DomUtil.addUrlParameters(sb, pp, false);
		}
		generateRedirect(ctx, sb.toString(), ajax);
	}

	private void generateRedirect(@Nonnull final RequestContextImpl ctx, @Nonnull final String url, boolean ajax) throws Exception {
		if(ajax)
			ApplicationRequestHandler.generateAjaxRedirect(ctx, url);
		else
			ApplicationRequestHandler.generateHttpRedirect(ctx, url, "Redirecting");
	}

	/**
	 * Moves one shelve entry back. If there's no shelve entry current moves back to the application's index.
	 * @param currentpg
	 */
	private void handleMoveBack(@Nonnull final RequestContextImpl ctx, @Nonnull Page currentpg, boolean ajax) throws Exception {
		int ix = m_shelvedPageStack.size() - 2;
		if(ix < 0) {
			clearShelve(0);									// Discard EVERYTHING

			//-- If we have a root page go there, else
			Class< ? extends UrlPage> clz = getApplication().getRootPage();
			logUser(ctx, currentpg, "GOTO root page");
			if(clz != null) {
				internalSetNextPage(MoveMode.NEW, getApplication().getRootPage(), null, null, null);
				handleGoto(ctx, currentpg, ajax);
			} else {
				//-- Last resort: move to root of the webapp by redirecting to some URL
				generateRedirect(ctx, ctx.getRelativePath(""), ajax);
			}
			saveWindowState();
			return;
		}

		//-- Unshelve and destroy the topmost thingy, then move back to the then-topmost.
		clearShelve(ix + 1); // Destroy everything above;
		IShelvedEntry se = m_shelvedPageStack.get(ix);		// Get the thing to move to,
		se.activate(ctx, ajax);								// Activate this page.
		logUser(ctx, currentpg, "Goto shelved page " + se.getURL());
		saveWindowState();
	}

	private void logUser(@Nonnull RequestContextImpl ctx, @Nonnull Page page, String string) {
		ConversationContext conversation = page.internalGetConversation();
		String cid = conversation == null ? null : conversation.getFullId();
		ctx.getSession().log(new UserLogItem(cid, page.getBody().getClass().getName(), null, null, string));
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Page to page navigation stuff.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Clear all goto stuff at request start time.
	 */
	public void clearGoto() {
		m_targetConversationClass = null;
		m_targetPageClass = null;
		m_targetPageParameters = null;
		m_targetConversation = null;
		m_targetMode = null;
	}

	public void internalSetNextPage(@Nonnull final MoveMode m, @Nullable final Class< ? extends UrlPage> clz, @Nullable final ConversationContext cc,
		@Nullable final Class< ? extends ConversationContext> ccclz, @Nullable final IPageParameters pp) {
		m_targetMode = m;
		m_targetPageClass = clz;
		m_targetConversationClass = ccclz;
		m_targetPageParameters = pp;
		m_targetConversation = cc;
	}

	public void internalSetRedirect(@Nonnull final String targeturl) {
		m_targetMode = MoveMode.REDIRECT;
		m_targetURL = targeturl;
	}

	@Nullable
	public Class< ? extends UrlPage> getTargetPageClass() {
		return m_targetPageClass;
	}

	@Nullable
	public IPageParameters getTargetPageParameters() {
		return m_targetPageParameters;
	}

	@Nullable
	public Class< ? extends ConversationContext> getTargetConversationClass() {
		return m_targetConversationClass;
	}

	@Nullable
	public ConversationContext getTargetConversation() {
		return m_targetConversation;
	}

	@Nullable
	public MoveMode getTargetMode() {
		return m_targetMode;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Page location and creation.							*/
	/*--------------------------------------------------------------*/

	/**
	 * Discards all shelved stuff up to the specified level (inclusive). Calling this
	 * with ix==0 means the entire page shelve and contexts are discarded.
	 */
	private void clearShelve(final int ix) {
		//		System.out.println("CLEARING SHELVE to "+ix);
		if(ix == 0) {
			m_shelvedPageStack.clear();					// Quickly destroy everything.
			destroyConversations(false);
			return;
		} else if(ix < 0)
			throw new IllegalStateException("?? index is invalid: " + ix);

		/*
		 * Discard top-level entries until we reach the specified level.
		 */
		while(m_shelvedPageStack.size() > ix) {
			IShelvedEntry se = m_shelvedPageStack.remove(m_shelvedPageStack.size() - 1);
			se.discard();
			//			System.out.println("Trying to discard " + se.getPage() + " in conversation " + se.getPage().getConversation());
		}
	}

	/**
	 * Discards this page from the page shelf. It discards it's conversation if that is no
	 * longer present on the shelf.
	 * @param pg
	 */
	void discardPage(@Nonnull final Page pg) {
		boolean destroyc = true;
		ConversationContext conversation = pg.internalGetConversation();
		for(int i = m_shelvedPageStack.size(); --i >= 0;) {
			IShelvedEntry se = m_shelvedPageStack.get(i);
			if(se instanceof ShelvedDomUIPage) {
				ShelvedDomUIPage sdp = (ShelvedDomUIPage) se;
				if(sdp.getPage().internalGetConversation() == conversation) {
					destroyc = false;
					break;
				}
			}
		}

		if(destroyc) {
			if(null != conversation)
				destroyConversation(conversation); // Forcefully destroy this conversation,
		} else {
			//-- Remove this page from the conversation.
			pg.getConversation().destroyPage(pg);
		}
	}

	/**
	 * Get a valid Page, either from the shelve stack or some other location. If this is for a full page request
	 * the 'papa' parameters are from the request and must be non-null. For an AJAX request the page parameters,
	 * since they are <b>not repeated</b> in an AJAX request, is null.
	 * Also, it can happen that we are handling here AJAX for expired page - in that case we return null as result.
	 * @param rctx
	 * @param clz
	 * @param papa
	 * @param action AJAX action
	 * @return
	 * @throws Exception
	 */
	public Page tryToMakeOrGetPage(@Nonnull final IRequestContext rctx, @Nonnull final Class< ? extends UrlPage> clz, @Nullable final PageParameters papa, @Nullable final String action)
		throws Exception {
		//-- 1. If a conversation ID is present try to get the page from there,
		ConversationContext cc = null;
		String cid = rctx.getParameter(Constants.PARAM_CONVERSATION_ID);
		if(cid != null) {
			CidPair cida = CidPair.decode(cid);
			cid = cida.getConversationId();
			cc = findConversation(cid);
		}

		//-- Locate the specified page/conversation in the page stack,
		if(cc != null) {
			int psix = findInPageStack(cc, clz, papa);
			if(psix != -1) {
				//-- Entry accepted. Discard all stacked entries *above* the selected thing.
				clearShelve(psix + 1);
				internalAttachConversations();

				//-- We know this is a DomUI page, no?
				ShelvedDomUIPage sdp = (ShelvedDomUIPage) m_shelvedPageStack.get(psix);
				Page pg = sdp.getPage();
				if(pg.isShelved())
					pg.internalUnshelve();
				saveWindowState();
				return pg;
			}
		}

		/*
		 * None of the shelved thingies accept the current page -> check if this is expired AJAX request.
		 */
		if(action != null && papa == null) {
			return null;
		}

		/*
		 * None of the shelved thingies accept the current page -> discard shelve, then create a new history.
		 */
		clearShelve(0); // ORDERED 1; Discard all shelved entries

		/*
		 * We need to create a new page, always. Find the best constructor and it's conversation.
		 */
		Constructor< ? extends UrlPage> bestpc = PageMaker.getBestPageConstructor(clz, true);
		Class< ? extends ConversationContext> ccclz = PageMaker.getConversationType(bestpc); // Get the conversation class to use,

		//-- Create the conversation context
		ConversationContext coco = createConversation(ccclz);

		/*
		 * jal 20120522 We use the cid from the URL, because that is the full CID that the browser knows about. If a new CID was
		 * needed, then the URL generated by the server will have the new CID.
		 */
		//-- Since this is a new page we clear ALL existing conversations
		registerConversation(coco, cid); // ORDERED 2
		ConversationContext.LOG.debug("Created conversation=" + coco + " for new page=" + clz);
		internalAttachConversations(); // ORDERED 3

		//-- Create the page && add to shelve,
		if(null == papa) {
			IllegalStateException ex = new IllegalStateException("Internal: trying to create a page for an AJAX request??");
			//LOG.error("Internal: trying to create a page for an AJAX request??", ex); --useful for developer controlled debugging
			throw ex;
		}
		Page newpg = PageMaker.createPageWithContent(bestpc, coco, papa);
		shelvePage(newpg); // Append the current page to the shelve,

		//-- Call all of the page's listeners.
		callNewPageCreatedListeners(newpg);
		saveWindowState();
		return newpg;
	}

	/**
	 * Call all "new page" listeners when a page is unbuilt or new at this time.
	 *
	 * @param pg
	 * @throws Exception
	 */
	private void callNewPageCreatedListeners(@Nonnull final Page pg) throws Exception {
		for(INewPageInstantiated npi : getApplication().getNewPageInstantiatedListeners()) {
			npi.newPageCreated(pg.getBody());

			//-- Make very sure none of the listeners built the page jal 20130417 switched off for now, unsure why this is a problem..
//			if(pg.getBody().isBuilt())
//				throw new IllegalStateException("Error: INewPageInstantiated#newPageCreated() call in " + npi + " has forced the page to be built - this is not allowed");
		}
	}

// jal 20091122 Bug# 605 Move this globally.
//	private void callNewPageListeners(final Page pg) throws Exception {
//		PageContext.internalSet(pg); // Jal 20081103 Set state before calling add listeners.
//		for(INewPageInstantiated npi : getApplication().getNewPageInstantiatedListeners())
//			npi.newPageInstantiated(m_currentPage.getBody());
//	}

	/**
	 * Check to see if we can use a page stack entry.
	 *
	 * @param cc
	 * @param clz
	 * @param papa	Nonnull for a "new page" request, null for an AJAX request to an existing page.
	 * @return
	 */
	private int findInPageStack(@Nullable final ConversationContext cc, @Nonnull final Class< ? extends UrlPage> clz, @Nullable final IPageParameters papa) throws Exception {
		//		if(cc == null) FIXME jal 20090824 Revisit: this is questionable; why can it be null? Has code path from UIGoto-> handleGoto.
		//			throw new IllegalStateException("The conversation cannot be empty here.");
		for(int ix = m_shelvedPageStack.size(); --ix >= 0;) {
			IShelvedEntry se = m_shelvedPageStack.get(ix);
			if(se instanceof ShelvedDomUIPage) {
				ShelvedDomUIPage sdp = (ShelvedDomUIPage) se;

				if(!sdp.getPage().getBody().getClass().getName().equals(clz.getName()))	// Of the appropriate type?
					continue; 									// No -> not acceptable
				if(cc != null && cc != sdp.getPage().getConversation()) 				// Is in the conversation supplied?
					continue;									// No -> not acceptable

				//-- Page AND context are acceptable; check parameters;
				if(papa == null)								// AJAX request -> page acceptable
					return ix;
				if(papa.equals(sdp.getPage().getPageParameters()))	// New page request -> acceptable if same parameters.
					return ix;
			}
		}
		return -1;												// Nothing acceptable
	}

	public boolean isPageOnStack(@Nonnull final Class< ? extends UrlPage> clz, @Nonnull final IPageParameters papa) throws Exception {
		for(int ix = m_shelvedPageStack.size(); --ix >= 0;) {
			IShelvedEntry se = m_shelvedPageStack.get(ix);
			if(se instanceof ShelvedDomUIPage) {
				ShelvedDomUIPage sdp = (ShelvedDomUIPage) se;
				if(!sdp.getPage().getBody().getClass().getName().equals(clz.getName())) // Of the appropriate type?
					continue; 									// No -> not acceptable

				//-- Page AND context are acceptable; check parameters;
				if(sdp.getPage().getPageParameters().equals(papa)) // Got a page; must make sure the parameters, if present, are equal.
					return true;
			}
		}
		return false;
	}

	@Nonnull
	@Override
	public String toString() {
		return "Window[" + m_id + ":" + m_windowID + "]";
	}

	public long getLastUsed() {
		return m_lastUsed;
	}

	void internalTouched() {
		m_lastUsed = System.currentTimeMillis();
	}

	int getObituaryTimer() {
		return m_obituaryTimer;
	}

	void setObituaryTimer(final int obituaryTimer) {
		m_obituaryTimer = obituaryTimer;
	}

	public void internalSetLastPage(@Nonnull final Page page) {
		synchronized(m_appSession) {
			m_lastRequestedPageTag = page.getPageTag();
		}
	}

	int internalGetLastPageTag() {
		synchronized(m_appSession) {
			return m_lastRequestedPageTag;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Contained objects map (EXPERIMENTAL)				*/
	/*--------------------------------------------------------------*/
	/**
	 * Set a window attribute.
	 * @param name
	 * @param val
	 */
	public void setAttribute(@Nonnull final String name, @Nullable final Object val) {
		if(m_map == Collections.EMPTY_MAP)
			m_map = new HashMap<String, Object>();
		if(val == null)
			m_map.remove(name);
		else {
			m_map.put(name, val);
		}
	}

	/**
	 * Get a window attribute.
	 * @param name
	 * @return
	 */
	@Nullable
	public Object getAttribute(@Nonnull final String name) {
		return m_map.get(name);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Modifying  the shelve stack.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Add or insert a page to the shelve stack. Used to shelve non DomUI stack entries.
	 * @param depth
	 * @param entry
	 */
	public void addShelveEntry(int depth, @Nonnull IShelvedEntry entry) {
		if(depth > 0)
			throw new IllegalArgumentException("Depth must be <= 0");
		int ix = m_shelvedPageStack.size() + depth;			// Depth moves index backwards because it is -ve
		if(ix < 0)
			throw new IllegalArgumentException("Depth of " + depth + " invalid: max is " + -m_shelvedPageStack.size());
		m_shelvedPageStack.add(ix, entry);
	}

	/**
	 * This inserts a (possibly new) entry in the page stack. If the same page is already there
	 * nothing happens and this returns false (stack not modified).
	 * @param depth
	 * @param clz
	 * @param parameters
	 */
	public boolean insertShelveEntry(int depth, @Nonnull Class< ? extends UrlPage> clz, @Nonnull IPageParameters parameters) throws Exception {
		boolean res = null != insertShelveEntryMain(depth, clz, parameters);
		saveWindowState();
		return res;
	}

	/**
	 * This inserts a (possibly new) entry in the page stack. If the same page is already there
	 * nothing happens and this returns false (stack not modified).
	 * @param depth
	 * @param clz
	 * @param parameters
	 */
	@Nullable
	private Page insertShelveEntryMain(int depth, @Nonnull Class< ? extends UrlPage> clz, @Nonnull IPageParameters parameters) throws Exception {
		if(isPageOnStack(clz, parameters))
			return null;

		//-- We need to create a page.
		Constructor< ? extends UrlPage> bestpc = PageMaker.getBestPageConstructor(clz, true);
		Class< ? extends ConversationContext> ccclz = PageMaker.getConversationType(bestpc); 	// Get the conversation class to use,
		ConversationContext coco = createConversation(ccclz);
		boolean ok = false;
		Page prevpage = UIContext.internalGetPage();
		try {
			registerConversation(coco, null); 						// ORDERED 2
			ConversationContext.LOG.debug("Created conversation=" + coco + " for new page=" + clz);
			internalAttachConversations();							// ORDERED 3
			if(coco.getState() == ConversationState.DETACHED)		// Be very sure we're attached.
				coco.internalAttach();

			//-- Create the page && add to shelve,
			Page newpg = PageMaker.createPageWithContent(bestpc, coco, parameters);

			if(depth > 0)
				throw new IllegalArgumentException("Depth must be <= 0");
			int ix = m_shelvedPageStack.size() + depth;			// Depth moves index backwards because it is -ve
			if(ix < 0)
				throw new IllegalArgumentException("Depth of " + depth + " invalid: max is " + -m_shelvedPageStack.size());
			m_shelvedPageStack.add(ix, new ShelvedDomUIPage(this, newpg));

			getApplication().getInjector().injectPageValues(newpg.getBody(), parameters);
			newpg.setInjected(true);
			UIContext.internalSet(newpg);
			newpg.internalFullBuild();								// 20130411 jal Page must be built before stacking it.

			//-- Call all of the page's listeners.
			callNewPageCreatedListeners(newpg);
			newpg.internalShelve();
			ok = true;
			return newpg;
		} finally {
			UIContext.internalSet(prevpage);
			try {
				if(!ok) {
					destroyConversation(coco);

				}
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Developer mode save/restore state during reloads.	*/
	/*--------------------------------------------------------------*/
	/**
	 * Get all of the pages from the shelve stack, and return them as a string based structure for later reload.
	 * @return
	 */
	@Nonnull
	List<SavedPage> getSavedPageList() {
		List<SavedPage> res = new ArrayList<>(m_shelvedPageStack.size());
		for(IShelvedEntry se : m_shelvedPageStack) {
			if(se instanceof ShelvedDomUIPage) {
				ShelvedDomUIPage dp = (ShelvedDomUIPage) se;
				res.add(new SavedPage(dp.getPage().getBody().getClass().getName(), dp.getPage().getPageParameters()));
			}
		}
		return res;
	}

	/**
	 * This will try to resurrect a set of windows from a previously stored stack.
	 * @param string
	 * @param sw
	 * @param pageParameters
	 * @param clz2
	 */
	@Nullable
	public String internalAttemptReload(@Nonnull HttpSession hs, @Nonnull Class< ? extends UrlPage> clz2, @Nonnull PageParameters pageParameters, @Nonnull String oldWindowId) {
		SavedWindow sw = (SavedWindow) hs.getAttribute(oldWindowId);
		List<SavedPage> list;
		if(null != sw) {
			hs.removeAttribute(oldWindowId);								// Remove this after restore
			list = sw.getPageList();
			System.out.println("arh: reload " + oldWindowId + " using session state " + sw);
		} else {
			//-- Can we get it from the state file?
			if(!m_developerMode)
				return null;
			File f = getStateFile(oldWindowId);
			if(!f.exists())
				return null;
			try {
				list = (List<SavedPage>) FileTool.loadSerialized(f);
				if(null == list)
					return null;
			} catch(Exception x) {
				return null;
			} finally {
				FileTool.closeAll(f);										// Always remove the file
			}
			System.out.println("arh: reload " + oldWindowId + " using file " + f + ", " + list);
		}

		String conversationId = null;
		try {
			internalAttachConversations();
			for(SavedPage sp : list) {
				try {
					//-- 1. Load the class by name.
					Class< ? extends UrlPage> clz = m_appSession.getApplication().loadPageClass(sp.getClassName());

					//-- 2. Insert @ location [0]
					Page pg = insertShelveEntryMain(0, clz, sp.getParameters());
					if(null != pg && clz2.getName().equals(sp.getClassName()) && sp.getParameters().equals(pageParameters)) {
						ConversationContext cc = pg.internalGetConversation();
						if(null != cc)
							conversationId = cc.getId();
					}
				} catch(Exception x) {
					System.err.println("domui: developer page reload failed: " + x);
					x.printStackTrace();
					LOG.info("Cannot reload " + sp.getClassName() + ": " + x);
				}
			}
			saveWindowState();								// Save new window's state
			return conversationId;
		} catch(Exception x) {
			System.err.println("domui: developer reload failed: " + x);
			x.printStackTrace();
			return null;
		} finally {
			internalDetachConversations();
		}
	}

	/**
	 * Get the name for the window state file of a given session ID.
	 * @param sessionID
	 * @return
	 */
	@Nonnull
	static private File getStateFile(@Nonnull String sessionID) {
		File tmpdir = FileTool.getTmpDir();
		return new File(tmpdir, "domui-session-" + sessionID);
	}

	/**
	 * Saves the current shelve to a tempfile if we're running in development mode, so that the
	 * window state can be restored after server start/stop.
	 */
	private void saveWindowState() {
		if(!m_developerMode)
			return;
		try {
			FileTool.saveSerialized(getStateFile(getWindowID()), (Serializable) getSavedPageList());
		} catch(Exception x) {
			LOG.error("Failed to save developer mode window state: " + x, x);
		}
	}

	private void destroyDevelopmentStateFile() {
		if(!m_developerMode)
			return;
		File sf = getStateFile(getWindowID());
		if(sf.exists())
			sf.delete();
	}



}
