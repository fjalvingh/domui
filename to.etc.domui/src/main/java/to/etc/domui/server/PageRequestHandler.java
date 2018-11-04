package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import to.etc.domui.component.misc.InternalParentTree;
import to.etc.domui.component.misc.MessageFlare;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.HtmlFullRenderer;
import to.etc.domui.dom.IBrowserOutput;
import to.etc.domui.dom.PrettyXmlOutputWriter;
import to.etc.domui.dom.errors.IExceptionListener;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.ClickInfo;
import to.etc.domui.dom.html.IHasChangeListener;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.PagePhase;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.parts.IComponentJsonProvider;
import to.etc.domui.parts.IComponentUrlDataProvider;
import to.etc.domui.state.AppSession;
import to.etc.domui.state.CidPair;
import to.etc.domui.state.ConversationContext;
import to.etc.domui.state.ConversationDestroyedException;
import to.etc.domui.state.IGotoAction;
import to.etc.domui.state.INotReloadablePage;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.UIContext;
import to.etc.domui.state.UIGoto;
import to.etc.domui.state.UserLogItem;
import to.etc.domui.state.WindowSession;
import to.etc.domui.trouble.ClientDisconnectedException;
import to.etc.domui.trouble.ExpiredSessionPage;
import to.etc.domui.trouble.MsgException;
import to.etc.domui.trouble.NotLoggedInException;
import to.etc.domui.trouble.SessionInvalidException;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Constants;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.INewPageInstantiated;
import to.etc.domui.util.IRebuildOnRefresh;
import to.etc.domui.util.Msgs;
import to.etc.util.IndentWriter;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.ajax.renderer.json.JSONRegistry;
import to.etc.webapp.ajax.renderer.json.JSONRenderer;
import to.etc.webapp.query.QContextManager;

import javax.servlet.http.HttpSession;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static to.etc.domui.util.DomUtil.nullChecked;

/**
 * Handle all page related requests from a client. Each request gets its own instance.
 *
 * This class contains most of the code that once resided in ApplicationRequestHandler.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-11-18.
 */
@NonNullByDefault
final public class PageRequestHandler {
	static final public Logger LOG = ApplicationRequestHandler.LOG;

	private final DomApplication m_application;

	private final ApplicationRequestHandler m_applicationRequestHandler;

	private final ResponseCommandWriter m_commandWriter;

	private final PageAccessChecker m_accessChecker;

	private final RequestContextImpl m_ctx;

	private final CidPair m_cida;

	@Nullable
	private final String m_action;

	private final String m_cid;

	private final Class<? extends UrlPage> m_runclass;

	public PageRequestHandler(DomApplication application, ApplicationRequestHandler applicationRequestHandler, ResponseCommandWriter commandWriter, PageAccessChecker checker, RequestContextImpl ctx) {
		m_application = application;
		m_applicationRequestHandler = applicationRequestHandler;
		m_accessChecker = checker;
		m_commandWriter = commandWriter;
		m_ctx = ctx;

		//-- Get core things into fields.
		/*
		 * If this is a full render request the URL must contain a $CID... If not send a redirect after allocating a window.
		 */
		m_action = ctx.getParameter(Constants.PARAM_UIACTION);			// AJAX action request?
		String cid = m_cid = ctx.getParameter(Constants.PARAM_CONVERSATION_ID);
		m_cida = cid == null ? null : CidPair.decodeLax(cid);
		m_runclass = PageUtil.decodeRunClass(m_ctx);
	}

	public void executeRequest() throws Exception {
		m_ctx.getRequestResponse().setNoCache();			// All replies may not be cached at all!!
		m_ctx.getRequestResponse().addHeader("X-UA-Compatible", "IE=edge");	// 20110329 jal Force to highest supported mode for DomUI code.
		m_ctx.getRequestResponse().addHeader("X-XSS-Protection", "0");		// 20130124 jal Disable IE XSS filter, to prevent the idiot thing from seeing the CID as a piece of script 8-(

		handleMain();
		m_ctx.getSession().dump();							// Log session info if enabled
	}

	private void handleMain() throws Exception {
		try {
			runClass();
		} catch(ThingyNotFoundException | ClientDisconnectedException xxxx) {
			throw xxxx;
		} catch(Exception x) {
			renderApplicationMail(x);
			//if(!m_application.isShowProblemTemplate() && !m_application.inDevelopmentMode())
			//	throw x;

			tryRenderOopsFrame(x);
		} catch(Error x) {
			renderApplicationMail(x);
			if(!m_application.isShowProblemTemplate() && !m_application.inDevelopmentMode())
				throw x;

			String s = x.getMessage();
			if(s != null && s.contains("compilation") && s.contains("problem")) {
				tryRenderOopsFrame(x);
			} else
				throw x;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Initial and initial (full) page rendering.			*/
	/*--------------------------------------------------------------*/
	/**
	 * Intermediary impl; should later use interface impl on class to determine factory
	 * to use.
	 */
	private void runClass() throws Exception {
		if(DomUtil.USERLOG.isDebugEnabled()) {
			DomUtil.USERLOG.debug("\n\n\n========= DomUI request =================\nCID=" + m_cid + "\nAction=" + m_action + "\n");
		}
		if(!Constants.ACMD_ASYPOLL.equals(m_action))
//			System.out.println("req: " + cid + " action " + action + ", " + m_ctx.getParameter(Constants.PARAM_UICOMPONENT));
			logUser("Incoming request on " + m_cid + " action=" + m_action);

		//-- If this is an OBITUARY just mark the window as possibly gone, then exit;
		if(Constants.ACMD_OBITUARY.equals(m_action)) {
			/*
			 * Warning: do NOT access the WindowSession by findWindowSession: that updates the window touched
			 * timestamp and causes obituary timeout handling to fail.
			 */
			int pageTag;
			try {
				pageTag = Integer.parseInt(m_ctx.getParameter(Constants.PARAM_PAGE_TAG));
			} catch(Exception x) {
				throw new IllegalStateException("Missing or invalid $pt PageTAG in OBITUARY request");
			}
			if(m_cida == null)
				throw new IllegalStateException("Missing $cid in OBITUARY request");

			if(LOG.isDebugEnabled())
				LOG.debug("OBITUARY received for " + m_cid + ": pageTag=" + pageTag);
			m_ctx.getSession().internalObituaryReceived(m_cida.getWindowId(), pageTag);

			//-- Send a silly response.
			m_ctx.getOutputWriter("text/html", "utf-8");

//			m_ctx.getResponse().setContentType("text/html");
//			/*Writer w = */m_ctx.getResponse().getWriter();
			return; 										// Obituaries get a zero response.
		}

		// ORDERED!!! Must be kept BELOW the OBITUARY check
		WindowSession windowSession = null;
		if(m_cida != null) {
			windowSession = m_ctx.getSession().findWindowSession(m_cida.getWindowId());
		}

		if(windowSession == null) {
			boolean nonReloadableExpiredDetected = false;
			if(m_action != null) {
				if(INotReloadablePage.class.isAssignableFrom(m_runclass)) {
					nonReloadableExpiredDetected = true;
				} else {
					// In auto refresh: do not send the "expired" message, but let the refresh handle this.
					if(m_application.getAutoRefreshPollInterval() <= 0) {
						String msg = Msgs.BUNDLE.getString(Msgs.S_EXPIRED);
						m_commandWriter.generateExpired(m_ctx, msg);
						logUser(msg);
					} else {
						String msg = "Not sending expired message because autorefresh is ON for " + m_cid;
						LOG.info(msg);
						logUser(msg);
					}
					return;
				}
			}

			//-- We explicitly need to create a new Window and need to send a redirect back
			windowSession = m_ctx.getSession().createWindowSession();
			String newmsg = "$cid: input windowid=" + m_cid + " not found - created wid=" + windowSession.getWindowID();
			if(LOG.isDebugEnabled())
				LOG.debug(newmsg);
			logUser(newmsg);

			String conversationId = "x";							// If not reloading a saved set- use x as the default conversation id
			if(m_application.inDevelopmentMode() && m_cida != null) {
				/*
				 * 20130227 jal The WindowSession we did not find could have been destroyed due to a
				 * reloader event. In that case it's page shelve will be stored in the HttpSession or
				 * perhaps in a state file. Try to resurrect that page shelve as to not lose the navigation history.
				 */
				if(m_ctx.getRequestResponse() instanceof HttpServerRequestResponse) {
					HttpServerRequestResponse srr = (HttpServerRequestResponse) m_ctx.getRequestResponse();

					HttpSession hs = srr.getRequest().getSession();
					if(null != hs) {
						m_ctx.internalSetWindowSession(windowSession);			// Should prevent issues when reloading

						String newid = windowSession.internalAttemptReload(hs, m_runclass, PageParameters.createFrom(m_ctx), m_cida.getWindowId());
						if(newid != null)
							conversationId = newid;
					}
				}
			}

			if(nonReloadableExpiredDetected) {
				generateNonReloadableExpired(windowSession);
				return;
			}

			//-- EXPERIMENTAL 20121008 jal - if the code was sent through a POST - the data can be huge so we need a workaround for the get URL.
			PageParameters pp = PageParameters.createFrom(m_ctx);
			if(m_ctx.getRequestResponse() instanceof HttpServerRequestResponse) {
				HttpServerRequestResponse srr = (HttpServerRequestResponse) m_ctx.getRequestResponse();

				if("post".equalsIgnoreCase(srr.getRequest().getMethod()) && pp.getDataLength() > 768) {
					m_commandWriter.redirectForPost(m_ctx, windowSession, pp);
					return;
				}
			}
			//-- END EXPERIMENTAL

			StringBuilder sb = new StringBuilder(256);

			//			sb.append('/');
			sb.append(m_ctx.getRelativePath(m_ctx.getInputPath()));
			sb.append('?');
			StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
			sb.append('=');
			sb.append(windowSession.getWindowID());
			sb.append(".").append(conversationId);
			DomUtil.addUrlParameters(sb, m_ctx, false);
			ApplicationRequestHandler.generateHttpRedirect(m_ctx, sb.toString(), "Your session has expired. Starting a new session.");
			String expmsg = "Session " + m_cid + " has expired - starting a new session by redirecting to " + sb.toString();
			logUser(expmsg);
			if(DomUtil.USERLOG.isDebugEnabled())
				DomUtil.USERLOG.debug(expmsg);
			return;
		}
		if(m_cida == null)
			throw new IllegalStateException("Cannot happen: cida is null??");
		String conversationId = m_cida.getConversationId();

		/*
		 * Attempt to fix etc.to bugzilla bug# 3183: IE7 sends events out of order. If an action arrives for an earlier-destroyed
		 * conversation just ignore it, and send an empty response to ie, hopefully causing it to die soon.
		 */
		if(m_action != null) {
			if(windowSession.isConversationDestroyed(conversationId)) {		// This conversation was recently destroyed?
				//-- Render a null response
				String msg = "Session " + m_cid + " was destroyed earlier- assuming this is an out-of-order event and sending empty delta back";
				if(LOG.isDebugEnabled())
					LOG.debug(msg);
				logUser(msg);
				System.out.println(msg);
				m_commandWriter.generateEmptyDelta(m_ctx);
				return;											// jal 20121122 Must return after sending that delta or the document is invalid!!
			}
		}

		m_ctx.internalSetWindowSession(windowSession);
		windowSession.clearGoto();

		/*
		 * Determine if this is an AJAX request or a normal "URL" request. If it is a non-AJAX
		 * request we'll always respond with a full page re-render, but we must check to see if
		 * the page has been requested with different parameters this time.
		 */
		PageParameters papa = null;								// Null means: ajax request, not a full page.
		if(m_action == null) {
			papa = PageParameters.createFrom(m_ctx);

			//-- If this request is a huge post request - get the huge post parameters.
			String hpq = papa.getString(Constants.PARAM_POST_CONVERSATION_KEY, null);
			if(null != hpq) {
				ConversationContext coco = windowSession.findConversation(conversationId);
				if(null == coco)
					throw new IllegalStateException("The conversation " + conversationId + " containing POST data is missing in windowSession " + windowSession);

				papa = (PageParameters) coco.getAttribute("__ORIPP");
				if(null == papa)
					throw new IllegalStateException("The conversation " + m_cid + " no (longer) has the post data??");
			}
		}

		Page page = windowSession.tryToMakeOrGetPage(m_ctx, conversationId, m_runclass, papa, m_action);
		if(page != null) {
			page.getConversation().mergePersistentParameters(m_ctx);
			page.internalSetPhase(PagePhase.BUILD);				// Tree can change at will
			page.internalIncrementRequestCounter();
			windowSession.internalSetLastPage(page);
			if(DomUtil.USERLOG.isDebugEnabled()) {
				DomUtil.USERLOG.debug("Request for page " + page + " in conversation " + m_cid);
			}
		}

		/*
		 * If this is an AJAX request make sure the page is still the same instance (session lost trouble)
		 */
		if(m_action != null) {
			String s = m_ctx.getParameter(Constants.PARAM_PAGE_TAG);
			if(s != null) {
				int pt = Integer.parseInt(s);
				if(page == null || pt != page.getPageTag()) {
					/*
					 * The page tag differs-> session has expired.
					 */
					if(Constants.ACMD_ASYPOLL.equals(m_action)) {
						m_commandWriter.generateExpiredPollasy(m_ctx);
					} else {
						String msg = "Session " + m_cid + " expired, page will be reloaded (page tag difference) on action=" + m_action;
						if(DomUtil.USERLOG.isDebugEnabled())
							DomUtil.USERLOG.debug(msg);
						logUser(msg);

						// In auto refresh: do not send the "expired" message, but let the refresh handle this.
						if(m_application.getAutoRefreshPollInterval() <= 0) {
							m_commandWriter.generateExpired(m_ctx, Msgs.BUNDLE.getString(Msgs.S_EXPIRED));
						} else {
							msg = "Not sending expired message because autorefresh is ON for " + m_cid;
							LOG.info(msg);
							logUser(msg);
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
		if(m_action != null && m_action.startsWith("#")) {
			runComponentAction(page, m_action.substring(1));
			return;
			//-- If this is a PAGEDATA request - handle that
		} else if(Constants.ACMD_PAGEDATA.equals(m_action)) {
			runPageData(page);
			return;
		}

		//-- All commands EXCEPT ASYPOLL have all fields, so bind them to the current component data,
		List<NodeBase> pendingChangeList = Collections.emptyList();
		if(!Constants.ACMD_ASYPOLL.equals(m_action) && m_action != null) {
			long ts = System.nanoTime();
			pendingChangeList = handleComponentInput(page); // Move all request parameters to their input field(s)
			if(LOG.isDebugEnabled()) {
				ts = System.nanoTime() - ts;
				LOG.debug("rq: input handling took " + StringTool.strNanoTime(ts));
			}
		}

		if(m_action != null) {
			runAction(page, m_action, pendingChangeList);
			return;
		}

		/*
		 * We are doing a full refresh/rebuild of a page.
		 */
		long ts = System.nanoTime();
		try {
			if(DomUtil.USERLOG.isDebugEnabled())
				DomUtil.USERLOG.debug(m_cid + ": Full render of page " + page);

			if(page.getBody() instanceof IRebuildOnRefresh) {                // Must fully refresh?
				page.getBody().forceRebuild();                                // Cleanout state
				page.setInjected(false);
				QContextManager.closeSharedContexts(page.getConversation());
				if(DomUtil.USERLOG.isDebugEnabled())
					DomUtil.USERLOG.debug(m_cid + ": IForceRefresh, cleared page data for " + page);
				logUser("Full page render with forced refresh");
			} else {
				logUser("Full page render");
			}
			if(!page.isInjected()) {
				m_ctx.getApplication().getInjector().injectPageValues(page.getBody(), nullChecked(papa));
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
			if(!checkAccess(windowSession, page))
				return;

			m_application.internalCallPageFullRender(m_ctx, page);

			page.getBody().onReload();

			// ORDERED
			page.getConversation().processDelayedResults(page);

			//-- Call the 'new page added' listeners for this page, if it is still unbuilt. Fixes bug# 605
			callNewPageBuiltListeners(page);
			page.internalFullBuild();                            // Cause full build

			//-- EXPERIMENTAL Handle stored messages in session
			List<UIMessage> ml = (List<UIMessage>) windowSession.getAttribute(UIGoto.SINGLESHOT_MESSAGE);
			if(ml != null) {
				if(ml.size() > 0) {
					page.getBody().build();
					for(UIMessage m : ml) {
						if(DomUtil.USERLOG.isDebugEnabled())
							DomUtil.USERLOG.debug(m_cid + ": page reload message = " + m.getMessage());

						//page.getBody().addGlobalMessage(m);
						MessageFlare mf = MessageFlare.display(page.getBody(), m);
						mf.setTestID("SingleShotMsg");
					}
				}
				windowSession.setAttribute(UIGoto.SINGLESHOT_MESSAGE, null);
			}
			page.callRequestStarted();

			List<IGotoAction> al = (List<IGotoAction>) windowSession.getAttribute(UIGoto.PAGE_ACTION);
			if(al != null && al.size() > 0) {
				page.getBody().build();
				for(IGotoAction ga : al) {
					if(DomUtil.USERLOG.isDebugEnabled())
						DomUtil.USERLOG.debug(m_cid + ": page reload action = " + ga);
					ga.executeAction(page.getBody());
				}
				windowSession.setAttribute(UIGoto.PAGE_ACTION, null);
			}

			m_application.internalCallPageComplete(m_ctx, page);
			page.getBody().internalOnBeforeRender();
			page.internalDeltaBuild(); 							// If listeners changed the page-> rebuild those parts
			// END ORDERED

			//-- Start the main rendering process. Determine the browser type.
			Writer w;
			if(page.isRenderAsXHTML()) {
				w = m_ctx.getOutputWriter("application/xhtml+xml; charset=UTF-8", "utf-8");
			} else {
				w = m_ctx.getOutputWriter("text/html; charset=UTF-8", "utf-8");
			}
			IBrowserOutput out = new PrettyXmlOutputWriter(w);

			HtmlFullRenderer hr = m_application.findRendererFor(m_ctx.getBrowserVersion(), out);
			hr.render(m_ctx, page);

			//-- 20100408 jal If an UIGoto was done in createContent handle that
			if(windowSession.handleGoto(m_ctx, page, false))
				return;
		} catch(SessionInvalidException x) {
			//-- Mid-air collision between logout and some other action..
			logUser("Session exception: " + x);
			renderUserError("The session has been invalidated; perhaps you are logged out");
			//System.err.println("domui debug: session invalidation exception");
		} catch(ConversationDestroyedException x) {
			logUser("Conversation exception: " + x);
			renderUserError("Your conversation with the server has been destroyed. Please refresh the page.");
		} catch(Exception ex) {
			Exception x = WrappedException.unwrap(ex);

			if(!(x instanceof ValidationException)) {
				logUser("Page exception: " + x);
			}

			//-- 20100504 jal Exception in page means it's content is invalid, so force a full rebuild
			try {
				page.getBody().forceRebuild();
			} catch(ConversationDestroyedException xx) {
				logUser("Conversation exception: " + xx);
				renderUserError("Your conversation with the server has been destroyed. Please refresh the page.");
			} catch(SessionInvalidException xx) {
				logUser("Session exception: " + x);
				renderUserError("The session has been invalidated; perhaps you have logged out in another window?");
			} catch(Exception xxx) {
				System.err.println("Double exception in handling full page build exception");
				System.err.println("Original exception: " + x);
				System.err.println("Second one on forceRebuild: " + xxx);
				x.printStackTrace();
				xxx.printStackTrace();
			}
			page.getBody().forceRebuild();

			if(x instanceof NotLoggedInException) { // Better than repeating code in separate exception handlers.
				String url = m_application.handleNotLoggedInException(m_ctx, (NotLoggedInException) x);
				if(url != null) {
					ApplicationRequestHandler.generateHttpRedirect(m_ctx, url, "You need to be logged in");
					return;
				}
			}

			IExceptionListener xl = m_ctx.getApplication().findExceptionListenerFor(x);
			if(xl != null && xl.handleException(m_ctx, page, null, x)) {
				if(windowSession.handleExceptionGoto(m_ctx, page, false)) {
					AppSession aps = m_ctx.getSession();
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
		m_ctx.getSession().clearExceptionRetryCount();
		if(PageUtil.m_logPerf) {
			ts = System.nanoTime() - ts;
			System.out.println("domui: full render took " + StringTool.strNanoTime(ts));
		}

		//-- Start any delayed actions now.
		page.getConversation().startDelayedExecution();
	}

	/**
	 * Check whether we have access to the page or not. If we have access this returns true; if
	 * we do not it returns false, and it has already done whatever is needed:
	 * <ul>
	 *	<li>If we have no access because we need to be logged in the code will have redirected us to the login screen</li>
	 	<li>If we really have no permission the handler will have re</li>
	 * </ul>
	 */
	private boolean checkAccess(WindowSession windowSession, Page page) throws Exception {
		PageAccessCheckResult result = m_accessChecker.checkAccess(m_ctx, page, a -> logUser(a));
		switch(result) {
			default:
				throw new IllegalArgumentException(result + "?");

			case NeedLogin:
				m_commandWriter.redirectToLoginPage(m_ctx, windowSession);
				return false;

			case Accepted:
				return true;

			case Refused:
				return false;
		}
	}

	/**
	 * Try to render a terse error to the user.
	 */
	private void renderUserError(String s) {
		try {
			m_ctx.sendError(503, "It appears this session was logged out in mid-flight (" + s + ")");
		} catch(Exception x) {
			//-- Willfully ignore, nothing else we can do here.
		}
	}

	private void logUser(String string) {
		m_ctx.getSession().log(new UserLogItem(m_cid, m_runclass.getName(), null, null, string));
	}

	private void logUser(Page page, String string) {
		ConversationContext conversation = page.internalGetConversation();
		String cid = conversation == null ? null : conversation.getFullId();
		m_ctx.getSession().log(new UserLogItem(cid, page.getBody().getClass().getName(), null, null, string));
	}

	/**
	 * Handle out-of-bound component requests. These are not allowed to change the tree but must return a result
	 * by themselves.
	 */
	private void runComponentAction(Page page, @NonNull String action) throws Exception {
		m_application.internalCallPageAction(m_ctx, page);
		page.callRequestStarted();
		try {
			NodeBase wcomp = null;
			String wid = m_ctx.getParameter("webuic");
			if(wid != null) {
				wcomp = page.findNodeByID(wid);
			}
			if(wcomp == null)
				return;
			page.setTheCurrentNode(wcomp);
			wcomp.componentHandleWebDataRequest(m_ctx, action);
		} finally {
			page.callRequestFinished();
			page.setTheCurrentNode(null);
		}
	}

	private void runPageData(Page page) throws Exception {
		m_application.internalCallPageAction(m_ctx, page);
		page.callRequestStarted();

		NodeBase wcomp = null;
		String wid = m_ctx.getParameter("webuic");
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
			dp.provideUrlData(m_ctx);
		} finally {
			page.callRequestFinished();
			page.setTheCurrentNode(null);
		}
	}

	/**
	 * Call a component's JSON request handler, and render back the result.
	 */
	private void runPageJson(@NonNull Page page, @Nullable NodeBase wcomp) throws Exception {
		try {
			if(!(wcomp instanceof IComponentJsonProvider))
				throw new ProgrammerErrorException("The component " + wcomp + " must implement " + IComponentJsonProvider.class.getName() + " to be able to accept JSON data requests");

			IComponentJsonProvider dp = (IComponentJsonProvider) wcomp;
			PageParameters pp = PageParameters.createFromAll(m_ctx);
			Object value = dp.provideJsonData(pp);							// Let the component return something to render.
			renderJsonLikeResponse(value);
		} finally {
			page.callRequestFinished();
			page.setTheCurrentNode(null);
		}
	}

	@NonNull
	final private JSONRegistry m_jsonRegistry = new JSONRegistry();

	private void renderJsonLikeResponse(@NonNull Object value) throws Exception {
		Writer w = m_ctx.getOutputWriter("application/javascript", "utf-8");
		if(value instanceof String) {
			//-- String return: we'll assume this is a javascript response by itself.
			w.write((String) value);
		} else {
			//-- Object return: render as JSON
			JSONRenderer jr = new JSONRenderer(m_jsonRegistry, new IndentWriter(w), false);
			jr.render(value);
		}
	}

	private void generateNonReloadableExpired(WindowSession cm) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(ExpiredSessionPage.class.getName()).append('.').append(DomApplication.get().getUrlExtension());
		sb.append('?');
		StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
		sb.append('=');
		sb.append(cm.getWindowID());
		sb.append(".x"); // Dummy conversation ID
		ApplicationRequestHandler.generateAjaxRedirect(m_ctx, sb.toString());
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
	 * Walk the request parameter list and bind all values that came from an input thingy
	 * to the appropriate Node. Nodes whose value change will leave a trail in the pending
	 * change list which will later be used to fire change events, if needed.
	 * <p>This collects a list of nodes whose input values have changed <b>and</b> that have
	 * an onValueChanged listener. This list will later be used to call the change handles
	 * on all these nodes (bug# 664).
	 */
	private List<NodeBase> handleComponentInput(@NonNull Page page) throws Exception {
		//-- Just walk all parameters in the input request.
		List<NodeBase> changed = new ArrayList<>();
		for(String name : m_ctx.getParameterNames()) {
			String[] values = m_ctx.getParameters(name); 				// Get the value;
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


	private void runAction(Page page, String action, List<NodeBase> pendingChangeList) throws Exception {
		//		System.out.println("# action="+action);
		long ts = System.nanoTime();

		m_application.internalCallPageAction(m_ctx, page);
		page.callRequestStarted();

		if(!Constants.ACMD_ASYPOLL.equals(action))
			page.controlToModel();

		NodeBase wcomp = null;
		String wid = m_ctx.getParameter(Constants.PARAM_UICOMPONENT);
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

		//-- Non-delta actions
		if(Constants.ACMD_PAGEJSON.equals(action)) {
			try {
				runPageJson(page, wcomp);
				return;
			} finally {
				page.callRequestFinished();
			}
		}

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
					logUser(page, "valueChanged on " + DomUtil.getComponentDetails(n));
				}

				n.internalOnValueChanged();
			}

			// FIXME 20100331 jal Odd wcomp==null logic. Generalize.
			if(Constants.ACMD_CLICKED.equals(action)) {
				handleClicked(page, wcomp);
			} else if(Constants.ACMD_CLICKANDCHANGE.equals(action)) {
				if(wcomp != null && wcomp.getClicked() != null)
					handleClicked(page, wcomp);
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
				wcomp.componentHandleWebAction(m_ctx, action);
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
			logUser(page, "error message: " + msg.getMessage());
			page.modelToControl();
		} catch(Exception ex) {
			logUser(page, "Action handler exception: " + ex);
			Exception x = WrappedException.unwrap(ex);
			if(x instanceof NotLoggedInException) { // FIXME Fugly. Generalize this kind of exception handling somewhere.
				String url = m_application.handleNotLoggedInException(m_ctx, (NotLoggedInException) x);
				if(url != null) {
					ApplicationRequestHandler.generateAjaxRedirect(m_ctx, url);
					return;
				}
			}
			try {
				page.modelToControl();
			} catch(Exception xxx) {
				System.out.println("Double exception on modelToControl: " + xxx);
				xxx.printStackTrace();
			}

			IExceptionListener xl = m_ctx.getApplication().findExceptionListenerFor(x);
			if(xl == null) // No handler?
				throw x; // Move on, nothing to see here,
			if(wcomp != null && !wcomp.isAttached()) {
				wcomp = page.getTheCurrentControl();
				System.out.println("DEBUG: Report exception on a " + (wcomp == null ? "unknown control/node" : wcomp.getClass()));
			}
			if(wcomp == null || !wcomp.isAttached())
				throw new IllegalStateException("INTERNAL: Cannot determine node to report exception /on/", x);

			if(!xl.handleException(m_ctx, page, wcomp, x))
				throw x;
		}
		page.callRequestFinished();

		if(PageUtil.m_logPerf && !inhibitlog) {
			ts = System.nanoTime() - ts;
			System.out.println("domui: Action handling took " + StringTool.strNanoTime(ts));
		}
		if(!page.isDestroyed()) 								// jal 20090827 If an exception handler or whatever destroyed conversation or page exit...
			page.getConversation().processDelayedResults(page);

		//-- Determine the response class to render; exit if we have a redirect,
		WindowSession cm = m_ctx.getWindowSession();
		if(cm.handleGoto(m_ctx, page, true))
			return;

		//-- Call the 'new page added' listeners for this page, if it is now unbuilt due to some action calling forceRebuild() on it. Fixes bug# 605
		callNewPageBuiltListeners(page);

		//-- We stay on the same page. Render tree delta as response
		try {
			PageUtil.renderOptimalDelta(m_ctx, page, inhibitlog);
		} catch(NotLoggedInException x) { 						// FIXME Fugly. Generalize this kind of exception handling somewhere.
			String url = m_application.handleNotLoggedInException(m_ctx, x);
			if(url != null) {
				ApplicationRequestHandler.generateHttpRedirect(m_ctx, url, "You need to be logged in");
			}
		} catch(Exception x) {
			logUser(page, "Delta render failed: " + x);
			throw x;
		}
	}

	/**
	 * Defines the actions that could arrive too late due to race conditions in client javascript, when target elements are already removed from DOM at server side.
	 * It is safe to just ignore such obsoleted events, rather than giving error response.
	 */
	private boolean isSafeToIgnoreUnknownNodeOnAction(@NonNull String action) {
		return (Constants.ACMD_LOOKUP_TYPING.equals(action) || Constants.ACMD_LOOKUP_TYPING_DONE.equals(action) || Constants.ACMD_NOTIFY_CLIENT_POSITION_AND_SIZE.equals(action));
	}

	/**
	 * Called in DEVELOPMENT mode when the source code for a page is requested (double escape press). It shows
	 * the nodes from the entered one upto the topmost one, and when selected tries to open the source code
	 * by sending a command to the local Eclipse.
	 */
	private void handleDevelopmentShowCode(Page page, @Nullable NodeBase wcomp) {
		if(null == wcomp)
			return;

		//-- If a tree is already present ignore the click.
		List<InternalParentTree> res = page.getBody().getDeepChildren(InternalParentTree.class);
		if(res.size() > 0)
			return;
		InternalParentTree ipt = new InternalParentTree(wcomp);
		page.getBody().add(0, ipt);
	}

	/**
	 * Call all "new page" listeners when a page is unbuilt or new at this time.
	 */
	private void callNewPageBuiltListeners(Page pg) throws Exception {
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
	private void handleClicked(Page page, @Nullable NodeBase b) throws Exception {
		if(b == null) {
			logUser(page, "User clicked to fast - node has disappeared");
			System.out.println("User clicked too fast? Node not found. Ignoring.");
			return;
		}
		String msg = "Clicked on " + DomUtil.getComponentDetails(b);
		logUser(page, msg);
		if(DomUtil.USERLOG.isDebugEnabled()) {
			DomUtil.USERLOG.debug(msg);
		}

		ClickInfo cli = new ClickInfo(m_ctx);
		b.internalOnClicked(cli);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	If a page failed, show a neater response.			*/
	/*--------------------------------------------------------------*/
	/**
	 * In case of a (non compile) exception, check to see if mail must be sent and if yes do so.
	 */
	private void renderApplicationMail(@NonNull Throwable x) {
		String s = x.getMessage();
		if(s != null && s.contains("compilation") && s.contains("problem")) {
			return;
		}
		ExceptionUtil util = new ExceptionUtil(m_ctx);
		util.renderEmail(x);
	}

	/**
	 * Render the exception details screen.
	 */
	private void tryRenderOopsFrame(@NonNull Throwable x) throws Exception {
		try {
			m_applicationRequestHandler.getOopsRenderer().renderOopsFrame(m_ctx, x);
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
}
