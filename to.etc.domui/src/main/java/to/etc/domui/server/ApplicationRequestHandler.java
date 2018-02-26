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
package to.etc.domui.server;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.*;
import javax.servlet.http.*;

import org.slf4j.*;

import to.etc.domui.annotations.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.login.*;
import to.etc.domui.parts.*;
import to.etc.domui.state.*;
import to.etc.domui.themes.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.template.*;
import to.etc.util.*;
import to.etc.webapp.*;
import to.etc.webapp.ajax.renderer.json.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

import static to.etc.domui.util.DomUtil.nullChecked;

/**
 * Main handler for DomUI page requests. This handles all requests that target or come
 * from a DomUI page.
 * FIXME Needs to be split up badly.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public class ApplicationRequestHandler implements IFilterRequestHandler {
	static Logger LOG = LoggerFactory.getLogger(ApplicationRequestHandler.class);

	@Nonnull
	private final DomApplication m_application;

	@Nullable
	private JSTemplate m_exceptionTemplate;

	private static boolean m_logPerf = DeveloperOptions.getBool("domui.logtime", false);

	ApplicationRequestHandler(@Nonnull final DomApplication application) {
		m_application = application;
	}

	/**
	 * Accept .obit, the defined DomUI extension (.ui by default) and the empty URL if a home page is set in {@link DomApplication}.
	 * @see to.etc.domui.server.IFilterRequestHandler#accepts(to.etc.domui.server.IRequestContext)
	 */
	private boolean accepts(@Nonnull IRequestContext ctx) throws Exception {
		return m_application.getUrlExtension().equals(ctx.getExtension()) || ctx.getExtension().equals("obit") || (m_application.getRootPage() != null && ctx.getInputPath().length() == 0);
	}

	@Override
	public boolean handleRequest(@Nonnull final RequestContextImpl ctx) throws Exception {
		if(! accepts(ctx))
			return false;
		ctx.getRequestResponse().setNoCache();					// All replies may not be cached at all!!
		ctx.getRequestResponse().addHeader("X-UA-Compatible", "IE=edge");	// 20110329 jal Force to highest supported mode for DomUI code.
		ctx.getRequestResponse().addHeader("X-XSS-Protection", "0");		// 20130124 jal Disable IE XSS filter, to prevent the idiot thing from seeing the CID as a piece of script 8-(

		handleMain(ctx);
		ctx.getSession().dump();
		return true;
	}

	private void handleMain(@Nonnull final RequestContextImpl ctx) throws Exception {
		Class< ? extends UrlPage> runclass = decodeRunClass(ctx);
		try {
			runClass(ctx, runclass);
		} catch(ThingyNotFoundException | ClientDisconnectedException xxxx) {
			throw xxxx;
		} catch(Exception x) {
			renderApplicationMail(ctx, x);
			//if(!m_application.isShowProblemTemplate() && !m_application.inDevelopmentMode())
			//	throw x;

			tryRenderOopsFrame(ctx, x);
		} catch(Error x) {
			renderApplicationMail(ctx, x);
			if(!m_application.isShowProblemTemplate() && !m_application.inDevelopmentMode())
				throw x;

			String s = x.getMessage();
			if(s != null && s.contains("compilation") && s.contains("problem")) {
				tryRenderOopsFrame(ctx, x);
			} else
				throw x;
		}
	}

	private void renderApplicationMail(@Nonnull final RequestContextImpl ctx, @Nonnull Throwable x) {
		String s = x.getMessage();
		if(s != null && s.contains("compilation") && s.contains("problem")) {
			return;
		}
		ExceptionUtil util = new ExceptionUtil(ctx);
		util.renderEmail(x);
	}

	private void tryRenderOopsFrame(@Nonnull final RequestContextImpl ctx, @Nonnull Throwable x) throws Exception {
		try {
			renderOopsFrame(ctx, x);
		} catch(Exception oopx) {
			System.out.println("Exception while rendering exception page!!?? " + oopx);
			oopx.printStackTrace();
			if(x instanceof Error) {
				throw (Error) x;
			} else {
				throw (Exception) x;
			}
		}
	}

	/**
	 * Decide what class to run depending on the input path.
	 */
	@Nonnull
	private Class< ? extends UrlPage> decodeRunClass(@Nonnull final IRequestContext ctx) {
		if(ctx.getInputPath().length() == 0) {
			/*
			 * We need to EXECUTE the application's main class. We cannot use the .class directly
			 * because the reloader must be able to substitute a new version of the class when
			 * needed.
			 */
			Class< ? extends UrlPage> rootPage = m_application.getRootPage();
			if(null == rootPage)
				throw new ProgrammerErrorException("The DomApplication's 'getRootPage()' method returns null, and there is a request for the root of the web app... Override that method or make sure the root is handled differently.");
			String txt = rootPage.getCanonicalName();
			return m_application.loadPageClass(txt);
		}

		//-- Try to resolve as a class name,
		String s = ctx.getInputPath();
		int pos = s.lastIndexOf('.'); // Always strip whatever extension
		if(pos != -1) {
			int spos = s.lastIndexOf('/') + 1; // Try to locate path component
			if(pos > spos) {
				s = s.substring(spos, pos); // Last component, ex / and last extension.

				//-- This should be a classname now
				return m_application.loadPageClass(s);
			}
		}
		//-- All others- cannot resolve
		throw new IllegalStateException("Cannot decode URL " + ctx.getInputPath());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Initial and initial (full) page rendering.			*/
	/*--------------------------------------------------------------*/
	/**
	 * Intermediary impl; should later use interface impl on class to determine factory
	 * to use.
	 */
	private void runClass(@Nonnull final RequestContextImpl ctx, @Nonnull final Class< ? extends UrlPage> clz) throws Exception {
		//		if(! UrlPage.class.isAssignableFrom(clz))
		//			throw new IllegalStateException("Class "+clz+" is not a valid page class (does not extend "+UrlPage.class.getName()+")");
		//		System.out.println("runClass="+clz);

		/*
		 * If this is a full render request the URL must contain a $CID... If not send a redirect after allocating a window.
		 */
		String action = ctx.getParameter(Constants.PARAM_UIACTION); 			// AJAX action request?
		String cid = ctx.getParameter(Constants.PARAM_CONVERSATION_ID);
		CidPair cida = cid == null ? null : CidPair.decode(cid);

		if(DomUtil.USERLOG.isDebugEnabled()) {
			DomUtil.USERLOG.debug("\n\n\n========= DomUI request =================\nCID=" + cid + "\nAction=" + action + "\n");
		}
		if(!Constants.ACMD_ASYPOLL.equals(action))
//			System.out.println("req: " + cid + " action " + action + ", " + ctx.getParameter(Constants.PARAM_UICOMPONENT));
			logUser(ctx, cid, clz.getName(), "Incoming request on " + cid + " action=" + action);

		//-- If this is an OBITUARY just mark the window as possibly gone, then exit;
		if(Constants.ACMD_OBITUARY.equals(action)) {
			/*
			 * Warning: do NOT access the WindowSession by findWindowSession: that updates the window touched
			 * timestamp and causes obituary timeout handling to fail.
			 */
			int pageTag;
			try {
				pageTag = Integer.parseInt(ctx.getParameter(Constants.PARAM_PAGE_TAG));
			} catch(Exception x) {
				throw new IllegalStateException("Missing or invalid $pt PageTAG in OBITUARY request");
			}
			if(cida == null)
				throw new IllegalStateException("Missing $cid in OBITUARY request");

			if(LOG.isDebugEnabled())
				LOG.debug("OBITUARY received for " + cid + ": pageTag=" + pageTag);
			ctx.getSession().internalObituaryReceived(cida.getWindowId(), pageTag);

			//-- Send a silly response.
			ctx.getOutputWriter("text/html", "utf-8");

//			ctx.getResponse().setContentType("text/html");
//			/*Writer w = */ctx.getResponse().getWriter();
			return; // Obituaries get a zero response.
		}

		// ORDERED!!! Must be kept BELOW the OBITUARY check
		WindowSession cm = null;
		if(cida != null) {
			cm = ctx.getSession().findWindowSession(cida.getWindowId());
		}

		if(cm == null) {
			boolean nonReloadableExpiredDetected = false;
			if(action != null) {
				if(INotReloadablePage.class.isAssignableFrom(clz)) {
					nonReloadableExpiredDetected = true;
				} else {
					// In auto refresh: do not send the "expired" message, but let the refresh handle this.
					if(m_application.getAutoRefreshPollInterval() <= 0) {
						String msg = Msgs.BUNDLE.getString(Msgs.S_EXPIRED);
						generateExpired(ctx, msg);
						logUser(ctx, cid, clz.getName(), msg);
					} else {
						String msg = "Not sending expired message because autorefresh is ON for " + cid;
						LOG.info(msg);
						logUser(ctx, cid, clz.getName(), msg);
					}
					return;
				}
			}

			//-- We explicitly need to create a new Window and need to send a redirect back
			cm = ctx.getSession().createWindowSession();
			String newmsg = "$cid: input windowid=" + cid + " not found - created wid=" + cm.getWindowID();
			if(LOG.isDebugEnabled())
				LOG.debug(newmsg);
			logUser(ctx, cid, clz.getName(), newmsg);

			String conversationId = "x";							// If not reloading a saved set- use x as the default conversation id
			if(m_application.inDevelopmentMode() && cida != null) {
				/*
				 * 20130227 jal The WindowSession we did not find could have been destroyed due to a
				 * reloader event. In that case it's page shelve will be stored in the HttpSession or
				 * perhaps in a state file. Try to resurrect that page shelve as to not lose the navigation history.
				 */
				if(ctx.getRequestResponse() instanceof HttpServerRequestResponse) {
					HttpServerRequestResponse srr = (HttpServerRequestResponse) ctx.getRequestResponse();

					HttpSession hs = srr.getRequest().getSession();
					if(null != hs) {
						String newid = cm.internalAttemptReload(hs, clz, PageParameters.createFrom(ctx), cida.getWindowId());
						if(newid != null)
							conversationId = newid;
					}
				}
			}

			if(nonReloadableExpiredDetected) {
				generateNonReloadableExpired(ctx, cm);
				return;
			}

			//-- EXPERIMENTAL 20121008 jal - if the code was sent through a POST - the data can be huge so we need a workaround for the get URL.
			PageParameters pp = PageParameters.createFrom(ctx);
			if(ctx.getRequestResponse() instanceof HttpServerRequestResponse) {
				HttpServerRequestResponse srr = (HttpServerRequestResponse) ctx.getRequestResponse();

				if("post".equalsIgnoreCase(srr.getRequest().getMethod()) && pp.getDataLength() > 768) {
					redirectForPost(ctx, cm, pp);
					return;
				}
			}
			//-- END EXPERIMENTAL

			StringBuilder sb = new StringBuilder(256);

			//			sb.append('/');
			sb.append(ctx.getRelativePath(ctx.getInputPath()));
			sb.append('?');
			StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
			sb.append('=');
			sb.append(cm.getWindowID());
			sb.append(".").append(conversationId);
			DomUtil.addUrlParameters(sb, ctx, false);
			generateHttpRedirect(ctx, sb.toString(), "Your session has expired. Starting a new session.");
			String expmsg = "Session " + cid + " has expired - starting a new session by redirecting to " + sb.toString();
			logUser(ctx, cid, clz.getName(), expmsg);
			if(DomUtil.USERLOG.isDebugEnabled())
				DomUtil.USERLOG.debug(expmsg);
			return;
		}
		if(cida == null)
			throw new IllegalStateException("Cannot happen: cida is null??");

		/*
		 * Attempt to fix etc.to bugzilla bug# 3183: IE7 sends events out of order. If an action arrives for an earlier-destroyed
		 * conversation just ignore it, and send an empty response to ie, hopefully causing it to die soon.
		 */
		if(action != null) {
			if(cm.isConversationDestroyed(cida.getConversationId())) {		// This conversation was recently destroyed?
				//-- Render a null response
				String msg = "Session " + cid + " was destroyed earlier- assuming this is an out-of-order event and sending empty delta back";
				if(LOG.isDebugEnabled())
					LOG.debug(msg);
				logUser(ctx, cid, clz.getName(), msg);
				System.out.println(msg);
				generateEmptyDelta(ctx);
				return;											// jal 20121122 Must return after sending that delta or the document is invalid!!
			}
		}

		ctx.internalSetWindowSession(cm);
		cm.clearGoto();

		/*
		 * Determine if this is an AJAX request or a normal "URL" request. If it is a non-AJAX
		 * request we'll always respond with a full page re-render, but we must check to see if
		 * the page has been requested with different parameters this time.
		 */
		PageParameters papa = null;								// Null means: ajax request, not a full page.
		if(action == null) {
			papa = PageParameters.createFrom(ctx);

			//-- If this request is a huge post request - get the huge post parameters.
			String hpq = papa.getString(Constants.PARAM_POST_CONVERSATION_KEY, null);
			if(null != hpq) {
				ConversationContext coco = cm.findConversation(cida.getConversationId());
				if(null == coco)
					throw new IllegalStateException("The conversation " + cida.getConversationId() + " containing POST data is missing in windowSession " + cm);

				papa = (PageParameters) coco.getAttribute("__ORIPP");
				if(null == papa)
					throw new IllegalStateException("The conversation " + cid + " no (longer) has the post data??");
			}
		}

		Page page = cm.tryToMakeOrGetPage(ctx, clz, papa, action);
		if(page != null) {
			page.getConversation().mergePersistentParameters(ctx);
			page.internalSetPhase(PagePhase.BUILD);				// Tree can change at will
			page.internalIncrementRequestCounter();
			cm.internalSetLastPage(page);
			if(DomUtil.USERLOG.isDebugEnabled()) {
				DomUtil.USERLOG.debug("Request for page " + page + " in conversation " + cid);
			}
		}

		/*
		 * If this is an AJAX request make sure the page is still the same instance (session lost trouble)
		 */
		if(action != null) {
			String s = ctx.getParameter(Constants.PARAM_PAGE_TAG);
			if(s != null) {
				int pt = Integer.parseInt(s);
				if(page == null || pt != page.getPageTag()) {
					/*
					 * The page tag differs-> session has expired.
					 */
					if(Constants.ACMD_ASYPOLL.equals(action)) {
						generateExpiredPollasy(ctx);
					} else {
						String msg = "Session " + cid + " expired, page will be reloaded (page tag difference) on action=" + action;
						if(DomUtil.USERLOG.isDebugEnabled())
							DomUtil.USERLOG.debug(msg);
						logUser(ctx, cid, clz.getName(), msg);

						// In auto refresh: do not send the "expired" message, but let the refresh handle this.
						if(m_application.getAutoRefreshPollInterval() <= 0) {
							generateExpired(ctx, Msgs.BUNDLE.getString(Msgs.S_EXPIRED));
						} else {
							msg = "Not sending expired message because autorefresh is ON for " + cid;
							LOG.info(msg);
							logUser(ctx, cid, clz.getName(), msg);
						}
					}
					return;
				}
			}
		}

		if(page == null) {
			throw new IllegalStateException("Page can not be null here. Null is already handled inside expired AJAX request handling.");
		}

		UIContext.internalSet(page);

		/*
		 * Handle all out-of-bound actions: those that do not manipulate UI state.
		 */
		if(action != null && action.startsWith("#")) {
			runComponentAction(ctx, page, action.substring(1));
			return;
		//-- If this is a PAGEDATA request - handle that
		} else if(Constants.ACMD_PAGEDATA.equals(action)) {
			runPageData(ctx, page);
			return;
		} else if(Constants.ACMD_PAGEJSON.equals(action)) {
			runPageJson(ctx, page);
			return;
		}

		//-- All commands EXCEPT ASYPOLL have all fields, so bind them to the current component data,
		List<NodeBase> pendingChangeList = Collections.emptyList();
		if(!Constants.ACMD_ASYPOLL.equals(action)) {
			long ts = System.nanoTime();
			pendingChangeList = handleComponentInput(ctx, page); // Move all request parameters to their input field(s)
			if(LOG.isDebugEnabled()) {
				ts = System.nanoTime() - ts;
				LOG.debug("rq: input handling took " + StringTool.strNanoTime(ts));
			}
		}

		if(action != null) {
			runAction(ctx, page, action, pendingChangeList);
			return;
		}

		/*
		 * We are doing a full refresh/rebuild of a page.
		 */
		long ts = System.nanoTime();
		try {
			if(DomUtil.USERLOG.isDebugEnabled())
				DomUtil.USERLOG.debug(cid + ": Full render of page " + page);

			if(page.getBody() instanceof IRebuildOnRefresh) {                // Must fully refresh?
				page.getBody().forceRebuild();                                // Cleanout state
				page.setInjected(false);
				QContextManager.closeSharedContexts(page.getConversation());
				if(DomUtil.USERLOG.isDebugEnabled())
					DomUtil.USERLOG.debug(cid + ": IForceRefresh, cleared page data for " + page);
				logUser(ctx, cid, clz.getName(), "Full page render with forced refresh");
			} else {
				logUser(ctx, cid, clz.getName(), "Full page render");
			}
			if(!page.isInjected()) {
				ctx.getApplication().getInjector().injectPageValues(page.getBody(), nullChecked(papa));
				page.setInjected(true);
			}

			/*
			 * This is a (new) page request. We need to check rights on the page before
			 * it is presented. The rights check is moved here (since 2013/01/24) because
			 * any analysis of data-related or interface-related rights require the class
			 * to be instantiated.
			 *
			 * 20090415 jal Authentication checks: if the page has a "UIRights" annotation we need a logged-in
			 * user to check it's rights against the page's required rights.
			 * FIXME This is fugly. Should this use the registerExceptionHandler code? If so we need to extend it's meaning to include pre-page exception handling.
			 */
			if(!checkAccess(cm, ctx, page))
				return;

			m_application.internalCallPageFullRender(ctx, page);

			page.getBody().onReload();

			// ORDERED
			page.getConversation().processDelayedResults(page);

			//-- Call the 'new page added' listeners for this page, if it is still unbuilt. Fixes bug# 605
			callNewPageBuiltListeners(page);
			page.internalFullBuild();                            // Cause full build

			//-- EXPERIMENTAL Handle stored messages in session
			List<UIMessage> ml = (List<UIMessage>) cm.getAttribute(UIGoto.SINGLESHOT_MESSAGE);
			if(ml != null) {
				if(ml.size() > 0) {
					page.getBody().build();
					for(UIMessage m : ml) {
						if(DomUtil.USERLOG.isDebugEnabled())
							DomUtil.USERLOG.debug(cid + ": page reload message = " + m.getMessage());

						//page.getBody().addGlobalMessage(m);
						MessageFlare mf = MessageFlare.display(page.getBody(), m);
						mf.setTestID("SingleShotMsg");
					}
				}
				cm.setAttribute(UIGoto.SINGLESHOT_MESSAGE, null);
			}
			page.callRequestStarted();

			List<IGotoAction> al = (List<IGotoAction>) cm.getAttribute(UIGoto.PAGE_ACTION);
			if(al != null && al.size() > 0) {
				page.getBody().build();
				for(IGotoAction ga : al) {
					if(DomUtil.USERLOG.isDebugEnabled())
						DomUtil.USERLOG.debug(cid + ": page reload action = " + ga);
					ga.executeAction(page.getBody());
				}
				cm.setAttribute(UIGoto.PAGE_ACTION, null);
			}

			m_application.internalCallPageComplete(ctx, page);
			page.getBody().internalOnBeforeRender();
			page.internalDeltaBuild(); // If listeners changed the page-> rebuild those parts
			// END ORDERED

			//-- Start the main rendering process. Determine the browser type.
			Writer w;
			if(page.isRenderAsXHTML()) {
				w = ctx.getOutputWriter("application/xhtml+xml; charset=UTF-8", "utf-8");
			} else {
				w = ctx.getOutputWriter("text/html; charset=UTF-8", "utf-8");
			}
			IBrowserOutput out = new PrettyXmlOutputWriter(w);

			HtmlFullRenderer hr = m_application.findRendererFor(ctx.getBrowserVersion(), out);
			hr.render(ctx, page);

			//-- 20100408 jal If an UIGoto was done in createContent handle that
			if(cm.handleGoto(ctx, page, false))
				return;
		} catch(SessionInvalidException x) {
			//-- Mid-air collision between logout and some other action..
			logUser(ctx, cid, clz.getName(), "Session exception: " + x);
			renderUserError(ctx, "The session has been invalidated; perhaps you are logged out");
			//System.err.println("domui debug: session invalidation exception");
		} catch(ConversationDestroyedException x) {
			logUser(ctx, cid, clz.getName(), "Conversation exception: " + x);
			renderUserError(ctx, "Your conversation with the server has been destroyed. Please refresh the page.");
		} catch(Exception ex) {
			Exception x = WrappedException.unwrap(ex);

			if(!(x instanceof ValidationException)) {
				logUser(ctx, cid, clz.getName(), "Page exception: " + x);
			}

			//-- 20100504 jal Exception in page means it's content is invalid, so force a full rebuild
			try {
				page.getBody().forceRebuild();
			} catch(ConversationDestroyedException xx) {
				logUser(ctx, cid, clz.getName(), "Conversation exception: " + xx);
				renderUserError(ctx, "Your conversation with the server has been destroyed. Please refresh the page.");
			} catch(SessionInvalidException xx) {
				logUser(ctx, cid, clz.getName(), "Session exception: " + x);
				renderUserError(ctx, "The session has been invalidated; perhaps you have logged out in another window?");
			} catch(Exception xxx) {
				System.err.println("Double exception in handling full page build exception");
				System.err.println("Original exception: " + x);
				System.err.println("Second one on forceRebuild: " + xxx);
				x.printStackTrace();
				xxx.printStackTrace();
			}
			page.getBody().forceRebuild();

			if(x instanceof NotLoggedInException) { // Better than repeating code in separate exception handlers.
				String url = m_application.handleNotLoggedInException(ctx, page, (NotLoggedInException) x);
				if(url != null) {
					generateHttpRedirect(ctx, url, "You need to be logged in");
					return;
				}
			}

			IExceptionListener xl = ctx.getApplication().findExceptionListenerFor(x);
			if(xl != null && xl.handleException(ctx, page, null, x)) {
				if(cm.handleExceptionGoto(ctx, page, false)) {
					AppSession aps = ctx.getSession();
					if(aps.incrementExceptionCount() > 10) {
						aps.clearExceptionRetryCount();
						throw new IllegalStateException("Loop in exception handling in a full page (new page) render", x);
					}
					return;
				}
			}

			checkFullExceptionCount(page, x); // Rethrow, but clear state if page throws up too much.
		} finally {
			page.callAfterRenderListeners();
			page.internalClearDeltaFully();
		}

		//-- Full render completed: indicate that and reset the exception count
		page.setFullRenderCompleted(true);
		page.setPageExceptionCount(0);
		ctx.getSession().clearExceptionRetryCount();
		if(m_logPerf) {
			ts = System.nanoTime() - ts;
			System.out.println("domui: full render took " + StringTool.strNanoTime(ts));
		}

		//-- Start any delayed actions now.
		page.getConversation().startDelayedExecution();
	}

	/**
	 * Try to render a terse error to the user.
	 */
	private void renderUserError(RequestContextImpl ctx, String s) {
		try {
			ctx.sendError(503, "It appears this session was logged out in mid-flight (" + s + ")");
		} catch(Exception x) {
			//-- Willfully ignore, nothing else we can do here.
		}
	}

	private void logUser(@Nonnull RequestContextImpl ctx, @Nullable String cid, @Nonnull String pageName, String string) {
		ctx.getSession().log(new UserLogItem(cid, pageName, null, null, string));
	}

	private void logUser(@Nonnull RequestContextImpl ctx, @Nonnull Page page, String string) {
		ConversationContext conversation = page.internalGetConversation();
		String cid = conversation == null ? null : conversation.getFullId();
		ctx.getSession().log(new UserLogItem(cid, page.getBody().getClass().getName(), null, null, string));
	}

	/**
	 * Handle out-of-bound component requests. These are not allowed to change the tree but must return a result
	 * by themselves.
	 */
	private void runComponentAction(@Nonnull RequestContextImpl ctx, @Nonnull Page page, @Nonnull String action) throws Exception {
		m_application.internalCallPageAction(ctx, page);
		page.callRequestStarted();
		try {
			NodeBase wcomp = null;
			String wid = ctx.getParameter("webuic");
			if(wid != null) {
				wcomp = page.findNodeByID(wid);
			}
			if(wcomp == null)
				return;
			page.setTheCurrentNode(wcomp);
			wcomp.componentHandleWebDataRequest(ctx, action);
		} finally {
			page.callRequestFinished();
			page.setTheCurrentNode(null);
		}
	}

	private void runPageData(@Nonnull RequestContextImpl ctx, @Nonnull Page page) throws Exception {
		m_application.internalCallPageAction(ctx, page);
		page.callRequestStarted();

		NodeBase wcomp = null;
		String wid = ctx.getParameter("webuic");
		if(wid != null) {
			wcomp = page.findNodeByID(wid);
			// jal 20091120 The code below was active but is nonsense because we do not return after generateExpired!?
			//			if(wcomp == null) {
			//				generateExpired(ctx, NlsContext.getGlobalMessage(Msgs.S_BADNODE, wid));
			//				//				throw new IllegalStateException("Unknown node '"+wid+"'");
			//			}
		}
		if(wcomp == null)
			return;

		page.setTheCurrentNode(wcomp);

		try {
			IComponentUrlDataProvider dp = (IComponentUrlDataProvider) wcomp;
			dp.provideUrlData(ctx);
		} finally {
			page.callRequestFinished();
			page.setTheCurrentNode(null);
		}
	}

	/**
	 * Call a component's JSON request handler, and render back the result.
	 */
	private void runPageJson(@Nonnull RequestContextImpl ctx, @Nonnull Page page) throws Exception {
		m_application.internalCallPageAction(ctx, page);
		page.callRequestStarted();

		NodeBase wcomp = null;
		String wid = ctx.getParameter("webuic");
		if(wid != null) {
			wcomp = page.findNodeByID(wid);
		}
		if(wcomp == null)
			return;

		page.setTheCurrentNode(wcomp);

		try {
			if(!(wcomp instanceof IComponentJsonProvider))
				throw new ProgrammerErrorException("The component " + wcomp + " must implement " + IComponentJsonProvider.class.getName() + " to be able to accept JSON data requests");

			IComponentJsonProvider dp = (IComponentJsonProvider) wcomp;
			PageParameters pp = PageParameters.createFrom(ctx);
			Object value = dp.provideJsonData(pp);							// Let the component return something to render.
			renderJsonLikeResponse(ctx, value);
		} finally {
			page.callRequestFinished();
			page.setTheCurrentNode(null);
		}
	}

	@Nonnull
	final private JSONRegistry m_jsonRegistry = new JSONRegistry();

	private void renderJsonLikeResponse(@Nonnull RequestContextImpl ctx, @Nonnull Object value) throws Exception {
		Writer w = ctx.getOutputWriter("application/javascript", "utf-8");
		if(value instanceof String) {
			//-- String return: we'll assume this is a javascript response by itself.
			w.write((String) value);
		} else {
			//-- Object return: render as JSON
			JSONRenderer jr = new JSONRenderer(m_jsonRegistry, new IndentWriter(w), false);
			jr.render(value);
		}
	}

	private void generateNonReloadableExpired(RequestContextImpl ctx, WindowSession cm) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(ExpiredSessionPage.class.getName()).append('.').append(DomApplication.get().getUrlExtension());
		sb.append('?');
		StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
		sb.append('=');
		sb.append(cm.getWindowID());
		sb.append(".x"); // Dummy conversation ID
		generateAjaxRedirect(ctx, sb.toString());
	}

	/**
	 * Fix for huge POST requests being resent as a get.
	 */
	private void redirectForPost(RequestContextImpl ctx, WindowSession cm, @Nonnull PageParameters pp) throws Exception {
		//-- Create conversation
		ConversationContext cc = cm.createConversation(ConversationContext.class);
		cm.acceptNewConversation(cc);

		//-- Now: store the original PageParameters inside this conversation.
		cc.setAttribute("__ORIPP", pp);

		//-- Create an unique hash for the page parameters
		String hashString = pp.calculateHashString();			// The unique hash of a page with these parameters

		StringBuilder sb = new StringBuilder(256);

		//			sb.append('/');
		sb.append(ctx.getRelativePath(ctx.getInputPath()));
		sb.append('?');
		StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
		sb.append('=');
		sb.append(cm.getWindowID());
		sb.append(".");
		sb.append(cc.getId());
		sb.append("&");
		sb.append(Constants.PARAM_POST_CONVERSATION_KEY).append("=").append(hashString);
		generateHttpRedirect(ctx, sb.toString(), "Your session has expired. Starting a new session.");
	}

	/**
	 * Check if an exception is thrown every time; if so reset the page and rebuild it again.
	 */
	private void checkFullExceptionCount(Page page, Exception x) throws Exception {
		//-- Full renderer aborted. Handle exception counting.
		if(!page.isFullRenderCompleted()) {						// Has the page at least once rendered OK?
			//-- This page is initially unrenderable; the error is not due to state changes. Just rethrow and give up.
			throw x;
		}

		//-- The page was initially renderable; the current problem is due to state changes. Increment the exception count and if too big clear the page before throwing up.
		page.setPageExceptionCount(page.getPageExceptionCount() + 1);
		if(page.getPageExceptionCount() >= 2) {
			//-- Just destroy the stuff - it keeps dying on you.
			page.getConversation().destroy();
			throw new RuntimeException("The page keeps dying on you.. The page has been destroyed so that a new one will be allocated on the next refresh.", x);
		}

		//-- Just throw it now.
		throw x;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handle existing page events.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Authentication checks: if the page has a "UIRights" annotation we need a logged-in
	 * user to check it's rights against the page's required rights.
	 */
	private boolean checkAccess(final WindowSession cm, final RequestContextImpl ctx, final Page page) throws Exception {
		if(ctx.getParameter("webuia") != null)
			throw new IllegalStateException("Cannot be called for an AJAX request");
		UrlPage body = page.getBody();							// The actual, instantiated and injected class - which is unbuilt, though
		UIRights rann = body.getClass().getAnnotation(UIRights.class);		// Get class annotation
		IRightsCheckedManually rcm = body instanceof IRightsCheckedManually ? (IRightsCheckedManually) body : null;

		if(rann == null && rcm == null) {						// Any kind of rights checking is required?
			return true;										// No -> allow access.
		}

		//-- Get user's IUser; if not present we need to log in.
		IUser user = UIContext.getCurrentUser(); 				// Currently logged in?
		if(user == null) {
			redirectToLoginPage(cm, ctx);
			return false;
		}

		//-- Start access checks, in order. First call the interface, if applicable
		String failureReason = null;
		try {
			if(null != rcm) {
				boolean allowed = rcm.isAccessAllowedBy(user);	// Call interface: it explicitly allows
				if(allowed)
					return true;

				//-- False indicates "I do not give access, but I do not deny it either". So move on to the next check.
			}

			if(null != rann) {
				if(checkRightsAnnotation(body, rann, user)) { // Check annotation rights
					return true;
				}

				//-- Just exit with a null failureReason - this indicates that a list of rights will be rendered.
			} else
				throw new CodeException(Msgs.BUNDLE, Msgs.RIGHTS_NOT_ALLOWED);	// Insufficient rights - details unknown.
		} catch(CodeException cx) {
			failureReason = cx.getMessage();
		} catch(Exception x) {
			failureReason = x.toString();
		}

		/*
		 * Access not allowed: redirect to error page.
		 */
		ILoginDialogFactory ldf = m_application.getLoginDialogFactory();
		String rurl = ldf == null ? null : ldf.getAccessDeniedURL();
		if(rurl == null) {
			rurl = DomApplication.get().getAccessDeniedPageClass().getName() + "." + m_application.getUrlExtension();
		}

		//-- Add info about the failed thingy.
		StringBuilder sb = new StringBuilder(128);
		sb.append(rurl);
		DomUtil.addUrlParameters(sb, new PageParameters(AccessDeniedPage.PARAM_TARGET_PAGE, body.getClass().getName()), true);

		//-- If we have a message use it
		if(null != failureReason || rann == null) {
			if(failureReason == null)
				failureReason = "Empty reason - this should not happen!";
			sb.append("&").append(AccessDeniedPage.PARAM_REFUSAL_MSG).append("=");
			StringTool.encodeURLEncoded(sb, failureReason);
		} else {
			//-- All required rights
			int ix = 0;
			for(String r : rann.value()) {
				sb.append("&r").append(ix).append("=");
				ix++;
				StringTool.encodeURLEncoded(sb, r);
			}
		}
		generateHttpRedirect(ctx, sb.toString(), "Access denied");
		logUser(ctx, cm.getWindowID(), page.getBody().getClass().getName(), sb.toString());
		return false;
	}

	private void redirectToLoginPage(final WindowSession cm, final RequestContextImpl ctx) throws Exception {
		//-- Create the after-login target URL.
		StringBuilder sb = new StringBuilder(256);
		sb.append(ctx.getRelativePath(ctx.getInputPath()));
		sb.append('?');
		StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
		sb.append('=');
		sb.append(cm.getWindowID());
		sb.append(".x"); 												// Dummy conversation ID
		DomUtil.addUrlParameters(sb, ctx, false);

		//-- Obtain the URL to redirect to from a thingy factory (should this happen here?)
		ILoginDialogFactory ldf = m_application.getLoginDialogFactory();
		if(ldf == null)
			throw new NotLoggedInException(sb.toString()); // Force login exception.
		String target = ldf.getLoginRURL(sb.toString()); // Create a RURL to move to.
		if(target == null)
			throw new IllegalStateException("The Login Dialog Handler=" + ldf + " returned an invalid URL for the login dialog.");

		//-- Make this an absolute URL by appending the webapp path
		target = ctx.getRelativePath(target);
		generateHttpRedirect(ctx, target, "You need to login before accessing this function");
	}

	private boolean checkRightsAnnotation(@Nonnull UrlPage body, @Nonnull UIRights rann, @Nonnull IUser user) throws Exception {
		if(StringTool.isBlank(rann.dataPath())) {
			//-- No special data context - we just check plain general rights
			for(String right : rann.value()) {
				if(!user.hasRight(right)) {
					return false;
				}
			}
			return true;										// All worked, so we have access.
		}

		//-- We need the object specified in DataPath.
		PropertyMetaModel< ? > pmm = MetaManager.getPropertyMeta(body.getClass(), rann.dataPath());
		Object dataItem = pmm.getValue(body);					// Get the page property.
		for(String right : rann.value()) {
			if(!user.hasRight(right, dataItem)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Sends a redirect as a 304 MOVED command. This should be done for all full-requests.
	 */
	static public void generateHttpRedirect(RequestContextImpl ctx, String to, String rsn) throws Exception {
		to = appendPersistedParameters(to, ctx);
		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter("text/html; charset=UTF-8", "utf-8"));
		out.writeRaw("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" + "<html><head><script language=\"javascript\"><!--\n"
			+ "location.replace(" + StringTool.strToJavascriptString(to, true) + ");\n" + "--></script>\n" + "</head><body>" + rsn + "</body></html>\n");
	}

	/**
	 * Generate an AJAX redirect command. Should be used by all COMMAND actions.
	 */
	static public void generateAjaxRedirect(RequestContextImpl ctx, String url) throws Exception {
		if(LOG.isInfoEnabled())
			LOG.info("redirecting to " + url);
		url = appendPersistedParameters(url, ctx);

		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter("text/xml; charset=UTF-8", "utf-8"));
		out.tag("redirect");
		out.attr("url", url);
		out.endAndCloseXmltag();
	}

	private static String appendPersistedParameters(String url, RequestContextImpl ctx) {
		Set<String> nameSet = ctx.getApplication().getPersistentParameterSet();
		if(nameSet.size() == 0)
			return url;
		Map<String, String> map = ctx.getPersistedParameterMap();
		StringBuilder sb = new StringBuilder(url);
		boolean first = ! url.contains("?");
		for(Entry<String, String> entry : map.entrySet()) {
			if(first) {
				sb.append('?');
				first = false;
			} else {
				sb.append('&');
			}
			StringTool.encodeURLEncoded(sb, entry.getKey());
			sb.append('=');
			StringTool.encodeURLEncoded(sb, entry.getValue());
		}
		return sb.toString();
	}


	/**
	 * Generates an EXPIRED message when the page here does not correspond with
	 * the page currently in the browser. This causes the browser to do a reload.
	 */
	private void generateExpired(final RequestContextImpl ctx, final String message) throws Exception {
		//-- We stay on the same page. Render tree delta as response
		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter("text/xml; charset=UTF-8", "utf-8"));
		out.tag("expired");
		out.endtag();

		out.tag("msg");
		out.endtag();
		out.text(message);
		out.closetag("msg");
		out.closetag("expired");
	}

	private void generateEmptyDelta(final RequestContextImpl ctx) throws Exception {
		//-- We stay on the same page. Render tree delta as response
		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter("text/xml; charset=UTF-8", "utf-8"));
		out.tag("delta");
		out.endtag();
		out.closetag("delta");
	}

	/**
	 * Generates an 'expiredOnPollasy' message when server receives pollasy call from expired page.
	 * Since pollasy calls are frequent, expired here means that user has navigated to some other page in meanwhile, and that response should be ignored by browser.
	 */
	private void generateExpiredPollasy(final RequestContextImpl ctx) throws Exception {
		//-- We stay on the same page. Render tree delta as response
		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter("text/xml; charset=UTF-8", "utf-8"));
		out.tag("expiredOnPollasy");
		out.endtag();
		out.closetag("expiredOnPollasy");
	}

	/**
	 * Walk the request parameter list and bind all values that came from an input thingy
	 * to the appropriate Node. Nodes whose value change will leave a trail in the pending
	 * change list which will later be used to fire change events, if needed.
	 * <p>This collects a list of nodes whose input values have changed <b>and</b> that have
	 * an onValueChanged listener. This list will later be used to call the change handles
	 * on all these nodes (bug# 664).
	 */
	private List<NodeBase> handleComponentInput(@Nonnull final IRequestContext ctx, @Nonnull final Page page) throws Exception {
		//-- Just walk all parameters in the input request.
		List<NodeBase> changed = new ArrayList<>();
		for(String name : ctx.getParameterNames()) {
			String[] values = ctx.getParameters(name); 				// Get the value;
			//-- Locate the component that the parameter is for;
			if(name.startsWith("_")) {
				NodeBase nb = page.findNodeByID(name); 				// Can we find this literally?
				if(nb != null) {
					//-- Try to bind this value to the component.
					if(nb.acceptRequestParameter(values)) { 		// Make the thingy accept the parameter(s)
						//-- This thing has changed.
						if(nb instanceof IHasChangeListener) { 			// Can have a value changed thingy?
							IHasChangeListener ch = (IHasChangeListener) nb;
							if(ch.getOnValueChanged() != null) {
								changed.add(nb);
							}
						}
					}
				}
			}
		}
		return changed;
	}


	private void runAction(final RequestContextImpl ctx, final Page page, final String action, List<NodeBase> pendingChangeList) throws Exception {
		//		System.out.println("# action="+action);
		long ts = System.nanoTime();

		m_application.internalCallPageAction(ctx, page);
		page.callRequestStarted();

		if(!Constants.ACMD_ASYPOLL.equals(action))
			page.controlToModel();

		NodeBase wcomp = null;
		String wid = ctx.getParameter(Constants.PARAM_UICOMPONENT);
		if(wid != null) {
			wcomp = page.findNodeByID(wid);
			// jal 20091120 The code below was active but is nonsense because we do not return after generateExpired!?
			//			if(wcomp == null) {
			//				generateExpired(ctx, NlsContext.getGlobalMessage(Msgs.S_BADNODE, wid));
			//				//				throw new IllegalStateException("Unknown node '"+wid+"'");
			//			}
		}

		boolean inhibitlog = false;
		page.setTheCurrentNode(wcomp);
		try {
			/*
			 * If we have pending changes execute them before executing any actual command. Also: be
			 * very sure the changed component is part of that list!! Fix for bug# 664.
			 */
			//-- If we are a vchange command *and* the node that changed still exists make sure it is part of the changed list.
			if((Constants.ACMD_VALUE_CHANGED.equals(action) || Constants.ACMD_CLICKANDCHANGE.equals(action)) && wcomp != null) {
				if(!pendingChangeList.contains(wcomp))
					pendingChangeList.add(wcomp);
			}

			//-- Call all "changed" handlers.
			for(NodeBase n : pendingChangeList) {
				if(DomUtil.USERLOG.isDebugEnabled()) {
					DomUtil.USERLOG.debug("valueChanged on " + DomUtil.getComponentDetails(n));
					logUser(ctx, page, "valueChanged on " + DomUtil.getComponentDetails(n));
				}

				n.internalOnValueChanged();
			}

			// FIXME 20100331 jal Odd wcomp==null logic. Generalize.
			if(Constants.ACMD_CLICKED.equals(action)) {
				handleClicked(ctx, page, wcomp);
			} else if(Constants.ACMD_CLICKANDCHANGE.equals(action)) {
				if(wcomp != null && wcomp.getClicked() != null)
					handleClicked(ctx, page, wcomp);
			} else if(Constants.ACMD_VALUE_CHANGED.equals(action)) {
				//-- Don't do anything at all - everything is done beforehand (bug #664).
			} else if(Constants.ACMD_DEVTREE.equals(action)) {
				handleDevelopmentShowCode(page, wcomp);
			} else if(Constants.ACMD_ASYPOLL.equals(action)) {
				inhibitlog = true;
				//-- Async poll request..
				//			} else if("WEBUIDROP".equals(action)) {
				//				handleDrop(ctx, page, wcomp);
			} else if(wcomp == null && isSafeToIgnoreUnknownNodeOnAction(action)) {
				//-- Don't do anything at all - it is safe to ignore late and obsoleted events
				inhibitlog = true;
			} else if(wcomp == null) {
				if(!action.endsWith("?"))
					throw new IllegalStateException("Unknown node '" + wid + "' for action='" + action + "'");
			} else {
				wcomp.componentHandleWebAction(ctx, action);
			}
			ConversationContext conversation = page.internalGetConversation();
			if(null != conversation && conversation.isValid())
				page.modelToControl();
		} catch(ValidationException x) {
			/*
			 * When an action handler failed because it accessed a component which has a validation error
			 * we just continue - the failed validation will have posted an error message.
			 */
			if(LOG.isDebugEnabled())
				LOG.debug("rq: ignoring validation exception " + x);
			page.modelToControl();
		} catch(MsgException msg) {
			MsgBox.error(page.getBody(), msg.getMessage());
			logUser(ctx, page, "error message: " + msg.getMessage());
			page.modelToControl();
		} catch(Exception ex) {
			logUser(ctx, page, "Action handler exception: " + ex);
			Exception x = WrappedException.unwrap(ex);
			if(x instanceof NotLoggedInException) { // FIXME Fugly. Generalize this kind of exception handling somewhere.
				String url = m_application.handleNotLoggedInException(ctx, page, (NotLoggedInException) x);
				if(url != null) {
					generateAjaxRedirect(ctx, url);
					return;
				}
			}
			try {
				page.modelToControl();
			} catch(Exception xxx) {
				System.out.println("Double exception on modelToControl: " + xxx);
				xxx.printStackTrace();
			}

			IExceptionListener xl = ctx.getApplication().findExceptionListenerFor(x);
			if(xl == null) // No handler?
				throw x; // Move on, nothing to see here,
			if(wcomp != null && !wcomp.isAttached()) {
				wcomp = page.getTheCurrentControl();
				System.out.println("DEBUG: Report exception on a " + (wcomp == null ? "unknown control/node" : wcomp.getClass()));
			}
			if(wcomp == null || !wcomp.isAttached())
				throw new IllegalStateException("INTERNAL: Cannot determine node to report exception /on/", x);

			if(!xl.handleException(ctx, page, wcomp, x))
				throw x;
		}
		page.callRequestFinished();

		if(m_logPerf && !inhibitlog) {
			ts = System.nanoTime() - ts;
			System.out.println("domui: Action handling took " + StringTool.strNanoTime(ts));
		}
		if(!page.isDestroyed()) 								// jal 20090827 If an exception handler or whatever destroyed conversation or page exit...
			page.getConversation().processDelayedResults(page);

		//-- Determine the response class to render; exit if we have a redirect,
		WindowSession cm = ctx.getWindowSession();
		if(cm.handleGoto(ctx, page, true))
			return;

		//-- Call the 'new page added' listeners for this page, if it is now unbuilt due to some action calling forceRebuild() on it. Fixes bug# 605
		callNewPageBuiltListeners(page);

		//-- We stay on the same page. Render tree delta as response
		try {
			renderOptimalDelta(ctx, page, inhibitlog);
		} catch(NotLoggedInException x) { 						// FIXME Fugly. Generalize this kind of exception handling somewhere.
			String url = m_application.handleNotLoggedInException(ctx, page, x);
			if(url != null) {
				generateHttpRedirect(ctx, url, "You need to be logged in");
			}
		} catch(Exception x) {
			logUser(ctx, page, "Delta render failed: " + x);
			throw x;
		}
	}

	/**
	 * Defines the actions that could arrive too late due to race conditions in client javascript, when target elements are already removed from DOM at server side.
	 * It is safe to just ignore such obsoleted events, rather than giving error response.
	 */
	private boolean isSafeToIgnoreUnknownNodeOnAction(@Nonnull String action) {
		return (Constants.ACMD_LOOKUP_TYPING.equals(action) || Constants.ACMD_LOOKUP_TYPING_DONE.equals(action) || Constants.ACMD_NOTIFY_CLIENT_POSITION_AND_SIZE.equals(action));
	}

	/**
	 * Called in DEVELOPMENT mode when the source code for a page is requested (double escape press). It shows
	 * the nodes from the entered one upto the topmost one, and when selected tries to open the source code
	 * by sending a command to the local Eclipse.
	 */
	private void handleDevelopmentShowCode(Page page, NodeBase wcomp) {
		if(null == wcomp)
			return;

		//-- If a tree is already present ignore the click.
		List<InternalParentTree> res = page.getBody().getDeepChildren(InternalParentTree.class);
		if(res.size() > 0)
			return;
		InternalParentTree ipt = new InternalParentTree(wcomp);
		page.getBody().add(0, ipt);
	}

	static public void renderOptimalDelta(final RequestContextImpl ctx, final Page page) throws Exception {
		renderOptimalDelta(ctx, page, false);
	}

	static private void renderOptimalDelta(final RequestContextImpl ctx, final Page page, boolean inhibitlog) throws Exception {
		// ORDERED
		//-- 20100519 jal Force full rebuild before rendering, always. See bug 688.
		page.getBody().internalOnBeforeRender();
		page.internalDeltaBuild();
		ctx.getApplication().internalCallPageComplete(ctx, page);
		page.internalDeltaBuild();
		// /ORDERED

		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter("text/xml; charset=UTF-8", "utf-8"));

		long ts = System.nanoTime();
		//		String	usag = ctx.getUserAgent();
		HtmlFullRenderer fullr = ctx.getApplication().findRendererFor(ctx.getBrowserVersion(), out);
		OptimalDeltaRenderer dr = new OptimalDeltaRenderer(fullr, ctx, page);
		dr.render();
		if(m_logPerf && !inhibitlog) {
			ts = System.nanoTime() - ts;
			System.out.println("domui: Optimal Delta rendering using " + fullr + " took " + StringTool.strNanoTime(ts));
		}
		page.getConversation().startDelayedExecution();
	}

	/**
	 * Call all "new page" listeners when a page is unbuilt or new at this time.
	 */
	private void callNewPageBuiltListeners(final Page pg) throws Exception {
		if(pg.getBody().isBuilt())
			return;
		pg.internalFullBuild();
		for(INewPageInstantiated npi : m_application.getNewPageInstantiatedListeners())
			npi.newPageBuilt(pg.getBody());
	}

	/**
	 * Called when the action is a CLICK event on some thingy. This causes the click handler for
	 * the object to be called.
	 */
	private void handleClicked(IRequestContext ctx, Page page, @Nullable NodeBase b) throws Exception {
		if(b == null) {
			logUser((RequestContextImpl) ctx, page, "User clicked to fast - node has disappeared");
			System.out.println("User clicked too fast? Node not found. Ignoring.");
			return;
		}
		String msg = "Clicked on " + DomUtil.getComponentDetails(b);
		logUser((RequestContextImpl) ctx, page, msg);
		if(DomUtil.USERLOG.isDebugEnabled()) {
			DomUtil.USERLOG.debug(msg);
		}

		ClickInfo cli = new ClickInfo(ctx);
		b.internalOnClicked(cli);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	If a page failed, show a neater response.			*/
	/*--------------------------------------------------------------*/

	private void renderOopsFrame(@Nonnull RequestContextImpl ctx, @Nonnull Throwable x) throws Exception {
		x.printStackTrace();
		if(ctx.getRequestResponse() instanceof HttpServerRequestResponse) {
			HttpServerRequestResponse srr = (HttpServerRequestResponse) ctx.getRequestResponse();
			HttpServletResponse resp = srr.getResponse();
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				// Fail with proper response code.
		}

		ThemeManager themeManager = ctx.getApplication().internalGetThemeManager();

		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("x", x);
		dataMap.put("ctx", ctx);
		dataMap.put("app", ctx.getRelativePath(""));
		String sheet = themeManager.getThemedResourceRURL(ctx, "THEME/style.theme.css");
		if(null == sheet)
			throw new IllegalStateException("Unexpected null??");
		dataMap.put("stylesheet", sheet);

		String theme = themeManager.getThemedResourceRURL(ctx, "THEME/");
		dataMap.put("theme", theme);

		StringBuilder sb = new StringBuilder();
		dumpException(sb, x);
		dataMap.put("stacktrace", sb.toString());
		dataMap.put("message", StringTool.htmlStringize(x.toString()));
		dataMap.put("ctx", ctx);
		ExceptionUtil util = new ExceptionUtil(ctx);
		dataMap.put("util", util);

		//util.renderEmail(x);

		Writer w = ctx.getRequestResponse().getOutputWriter("text/html", "utf-8");
		JSTemplate xt = getExceptionTemplate();
		xt.execute(w, dataMap);
		w.flush();
		w.close();
	}

	@Nonnull
	public JSTemplate getExceptionTemplate() throws Exception {
		JSTemplate xt = m_exceptionTemplate;
		if(xt == null) {
			JSTemplateCompiler jtc = new JSTemplateCompiler();
			File src = new File(getClass().getResource("exceptionTemplate.html").getFile());
			if(src.exists() && src.isFile()) {
				Reader r = new FileReader(src);
				try {
					xt = jtc.compile(r, src.getAbsolutePath());
				} finally {
					FileTool.closeAll(r);
				}
			} else {
				xt = jtc.compile(ApplicationRequestHandler.class, "exceptionTemplate.html", "utf-8");
			}
		}
		return xt;
	}

	static private void dumpException(@Nonnull StringBuilder a, @Nonnull Throwable x) {
		Set<String> allset = new HashSet<>();
		StackTraceElement[] ssear = x.getStackTrace();
		for(StackTraceElement sse : ssear) {
			allset.add(sse.toString());
		}

		dumpSingle(a, x, Collections.EMPTY_SET);

		Throwable curr = x;
		for(;;) {
			Throwable cause = curr.getCause();
			if(cause == null || cause == curr)
				break;

			a.append("\n\n     Caused by ").append(cause.toString()).append("\n");
			dumpSingle(a, cause, allset);
			curr = cause;
		}
	}

	static private void dumpSingle(@Nonnull StringBuilder sb, @Nonnull Throwable x, @Nonnull Set<String> initset) {
		//-- Try to render openable stack trace elements as links.
		List<StackTraceElement> list = Arrays.asList(x.getStackTrace());

		//-- Remove from the end the server stuff
		int ix = findName(list, AppFilter.class.getName());
		if(ix != -1) {
			list = new ArrayList<>(stripFrames(list, ix + 1));
		}

		//-- Remove from the end all names in initset.
		for(int i = list.size(); --i >= 0;) {
			String str = list.get(i).toString();
			if(!initset.contains(str))
				break;
			list.remove(i);
		}

		for(StackTraceElement ste : list) {
			appendTraceLink(sb, ste);
		}
		if(x instanceof SQLException) {
			SQLException sx = (SQLException) x;
			while(sx.getNextException() != null) {
				sx = sx.getNextException();
				sb.append("SQL NextException: ");
				sb.append(sx.toString());
				sb.append("<br>");
			}
		}
	}


	private static int findName(@Nonnull List<StackTraceElement> list, String name) {
		for(int i = list.size(); --i >= 0;) {
			String cn = list.get(i).getClassName();
			if(name.equals(cn))
				return i;
		}
		return -1;
	}

	private static List<StackTraceElement> stripFrames(@Nonnull List<StackTraceElement> list, int from) {
		return list.subList(0, from - 1);
	}

	private static void appendTraceLink(@Nonnull StringBuilder sb, @Nonnull StackTraceElement ste) {
		sb.append("        <a class='exc-stk-l' href=\"#\" onclick=\"linkClicked('");
		//-- Get name for the thingy,
		String name;
		if(ste.getLineNumber() <= 0)
			name = ste.getClassName().replace('.', '/') + ".java@" + ste.getMethodName();
		else
			name = ste.getClassName().replace('.', '/') + ".java#" + ste.getLineNumber();
		sb.append(name);
		sb.append("')\">");
		sb.append(ste.toString()).append("</a><br>");
	}

}
