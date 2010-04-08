package to.etc.domui.server;

import java.util.*;

import org.slf4j.*;

import to.etc.domui.annotations.*;
import to.etc.domui.dom.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.login.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

/**
 * Mostly silly handler to handle direct DOM requests. Phaseless handler for testing
 * direct/delta building only using a reloadable class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public class ApplicationRequestHandler implements IFilterRequestHandler {
	static Logger LOG = LoggerFactory.getLogger(ApplicationRequestHandler.class);

	private final DomApplication m_application;

	public ApplicationRequestHandler(final DomApplication application) {
		m_application = application;
	}

	public void handleRequest(final RequestContextImpl ctx) throws Exception {
		ServerTools.generateNoCache(ctx.getResponse()); // All replies may not be cached at all!!
		handleMain(ctx);
		ctx.getSession().dump();
	}

	private void handleMain(final RequestContextImpl ctx) throws Exception {
		Class< ? extends UrlPage> runclass = decodeRunClass(ctx);
		runClass(ctx, runclass);
	}

	/**
	 * Decide what class to run depending on the input path.
	 * @param ctx
	 * @return
	 */
	private Class< ? extends UrlPage> decodeRunClass(final IRequestContext ctx) {
		if(ctx.getInputPath().length() == 0) {
			/*
			 * We need to EXECUTE the application's main class. We cannot use the .class directly
			 * because the reloader must be able to substitute a new version of the class when
			 * needed.
			 */
			String txt = m_application.getRootPage().getCanonicalName();
			return m_application.loadPageClass(txt);
			//			return m_application.getRootPage();
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
	 *
	 * @param ctx
	 * @param clz
	 * @throws Exception
	 */
	private void runClass(final RequestContextImpl ctx, final Class< ? extends UrlPage> clz) throws Exception {
		//		if(! UrlPage.class.isAssignableFrom(clz))
		//			throw new IllegalStateException("Class "+clz+" is not a valid page class (does not extend "+UrlPage.class.getName()+")");
		//		System.out.println("runClass="+clz);

		/*
		 * If this is a full render request the URL must contain a $CID... If not send a redirect after allocating a window.
		 */
		String action = ctx.getRequest().getParameter("webuia"); // AJAX action request?
		String cid = ctx.getParameter(Constants.PARAM_CONVERSATION_ID);
		String[] cida = DomUtil.decodeCID(cid);

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
			ctx.getSession().internalObituaryReceived(cida[0], pageTag);

			//-- Send a silly response.
			ctx.getResponse().setContentType("text/html");
			/*Writer w = */ctx.getResponse().getWriter();
			//			w.append("<html><body><p>Obituary?</body></html>\n");
			return; // Obituaries get a zero response.
		}

		// ORDERED!!! Must be kept BELOW the OBITUARY check
		WindowSession cm = null;
		if(cida != null) {
			cm = ctx.getSession().findWindowSession(cida[0]);
		}

		if(cm == null) {
			if(action != null) {
				generateExpired(ctx, Msgs.BUNDLE.getString(Msgs.S_EXPIRED));
				return;
			}

			//-- We explicitly need to create a new Window and need to send a redirect back
			cm = ctx.getSession().createWindowSession();
			if(LOG.isDebugEnabled())
				LOG.debug("$cid: input windowid=" + cid + " not found - created wid=" + cm.getWindowID());
			StringBuilder sb = new StringBuilder(256);

			//			sb.append('/');
			sb.append(ctx.getRelativePath(ctx.getInputPath()));
			sb.append('?');
			StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
			sb.append('=');
			sb.append(cm.getWindowID());
			sb.append(".x"); // Dummy conversation ID
			DomUtil.addUrlParameters(sb, ctx, false);
			generateHttpRedirect(ctx, sb.toString(), "Your session has expired. Starting a new session.");
			return;
		}
		ctx.internalSetWindowSession(cm);
		cm.clearGoto();

		/*
		 * 20090415 jal Authentication checks: if the page has a "UIRights" annotation we need a logged-in
		 * user to check it's rights against the page's required rights.
		 * FIXME This is fugly. Should this use the registerExceptionHandler code? If so we need to extend it's meaning to include pre-page exception handling.
		 *
		 */
		if(!checkAccess(cm, ctx, clz))
			return;

		/*
		 * Determine if this is an AJAX request or a normal "URL" request. If it is a non-AJAX
		 * request we'll always respond with a full page re-render, but we must check to see if
		 * the page has been requested with different parameters this time.
		 */
		PageParameters papa = null;
		if(action == null) {
			papa = PageParameters.createFrom(ctx); // Create page parameters from the request,
		}

		Page page = cm.makeOrGetPage(ctx, clz, papa);
		cm.internalSetLastPage(page);
		//		Page page = PageMaker.makeOrGetPage(ctx, clz, papa);

		/*
		 * If this is an AJAX request make sure the page is still the same instance (session lost trouble)
		 */
		if(action != null) {
			String s = ctx.getParameter(Constants.PARAM_PAGE_TAG);
			if(s != null) {
				int pt = Integer.parseInt(s);
				if(pt != page.getPageTag()) {
					/*
					 * The page tag differs-> session has expired.
					 */
					generateExpired(ctx, Msgs.BUNDLE.getString(Msgs.S_EXPIRED));
					return;
				}
			}
		}
		PageContext.internalSet(page);

		//-- All commands EXCEPT ASYPOLL have all fields, so bind them to the current component data,
		List<NodeBase> pendingChangeList = Collections.EMPTY_LIST;
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
			ctx.getApplication().getInjector().injectPageValues(page.getBody(), ctx, papa);

			if(page.getBody() instanceof IRebuildOnRefresh) { // Must fully refresh?
				page.getBody().forceRebuild(); // Cleanout state
				QContextManager.closeSharedContext(page.getConversation());
			}

			page.getBody().onReload();

			//-- EXPERIMENTAL Handle stored messages in session
			List<UIMessage> ml = (List<UIMessage>) cm.getAttribute(UIGoto.SINGLESHOT_MESSAGE);
			if(ml != null) {
				if(ml.size() > 0) {
					page.getBody().build();
					for(UIMessage m : ml)
						page.getBody().addGlobalMessage(m);
				}
				cm.setAttribute(UIGoto.SINGLESHOT_MESSAGE, null);
			}

			// ORDERED
			page.getConversation().processDelayedResults(page);

			//-- Call the 'new page added' listeners for this page, if it is still unbuilt. Fixes bug# 605
			callNewPageListeners(page);
			// END ORDERED

			//-- Start the main rendering process. Determine the browser type.
			if(page.getBody() instanceof IXHTMLPage)
				ctx.getResponse().setContentType("application/xhtml+xml; charset=UTF-8");
			else
				ctx.getResponse().setContentType("text/html; charset=UTF-8");
			ctx.getResponse().setCharacterEncoding("UTF-8");
			IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter());

			//		String	usag = ctx.getUserAgent();
			HtmlFullRenderer hr = m_application.findRendererFor(ctx.getBrowserVersion(), out);

			hr.render(ctx, page);

			//-- 20100408 jal If an UIGoto was done in createContent handle that
			if(cm.handleGoto(ctx, page, false))
				return;
		} catch(Exception x) {
			if(x instanceof NotLoggedInException) { // Better than repeating code in separate exception handlers.
				String url = m_application.handleNotLoggedInException(ctx, page, (NotLoggedInException) x);
				if(url != null) {
					generateHttpRedirect(ctx, url, "You need to be logged in");
					return;
				}
			}
			if(x instanceof QNotFoundException) {
				String url = m_application.handleQNotFoundException(ctx, page, (QNotFoundException) x);
				if(url != null) {
					generateHttpRedirect(ctx, url, "Data not found");
					return;
				}
			}
			checkFullExceptionCount(page, x); // Rethrow, but clear state if page throws up too much.
		} finally {
			page.clearDeltaFully();
		}

		//-- Full render completed: indicate that and reset the exception count
		page.setFullRenderCompleted(true);
		page.setPageExceptionCount(0);
		if(LOG.isDebugEnabled()) {
			ts = System.nanoTime() - ts;
			LOG.debug("rq: full render took " + StringTool.strNanoTime(ts));
		}

		//-- Start any delayed actions now.
		page.getConversation().startDelayedExecution();
	}

	/**
	 * Check if an exception is thrown every time; if so reset the page and rebuild it again.
	 * @param page
	 * @param x
	 * @throws Exception
	 */
	private void checkFullExceptionCount(Page page, Exception x) throws Exception {
		//-- Full renderer aborted. Handle exception counting.
		if(!page.isFullRenderCompleted()) { // Has the page at least once rendered OK?
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
	 * FIXME This is fugly. Should this use the registerExceptionHandler code? If so we need to extend it's meaning to include pre-page exception handling.
	 *
	 * @param cm
	 * @param ctx
	 * @param clz
	 * @throws Exception
	 */
	private boolean checkAccess(final WindowSession cm, final RequestContextImpl ctx, final Class< ? extends UrlPage> clz) throws Exception {
		UIRights rann = clz.getAnnotation(UIRights.class);
		if(rann == null)
			return true;
		//-- Get user's IUser; if not present we need to log in.
		IUser user = PageContext.getCurrentUser(); // Currently logged in?
		if(user == null) {
			//-- Create the after-login target URL.
			StringBuilder sb = new StringBuilder(256);
			//				sb.append('/');
			sb.append(ctx.getRelativePath(ctx.getInputPath()));
			sb.append('?');
			StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
			sb.append('=');
			sb.append(cm.getWindowID());
			sb.append(".x"); // Dummy conversation ID
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
			return false;
		}

		//-- Issue rights check,
		boolean allowed = true;
		for(String right : rann.value()) {
			if(!user.hasRight(right)) {
				allowed = false;
				break;
			}
		}
		if(allowed)
			return true;

		/*
		 * Access not allowed: redirect to error page.
		 */
		ILoginDialogFactory ldf = m_application.getLoginDialogFactory();
		String rurl = ldf == null ? null : ldf.getAccessDeniedURL();
		if(rurl == null) {
			rurl = AccessDeniedPage.class.getName() + "." + m_application.getUrlExtension();
		}

		//-- Add info about the failed thingy.
		StringBuilder sb = new StringBuilder(128);
		sb.append(rurl);
		sb.append("?targetPage=");
		StringTool.encodeURLEncoded(sb, clz.getName());

		//-- All required rights
		int ix = 0;
		for(String r : rann.value()) {
			sb.append("&r" + ix + "=");
			ix++;
			StringTool.encodeURLEncoded(sb, r);
		}
		generateHttpRedirect(ctx, sb.toString(), "Access denied");
		return false;
	}

	/**
	 * Sends a redirect as a 304 MOVED command. This should be done for all full-requests.
	 *
	 * @param ctx
	 * @param to
	 * @param rsn
	 * @throws Exception
	 */
	static public void generateHttpRedirect(final RequestContextImpl ctx, final String to, final String rsn) throws Exception {
		//		ctx.getResponse().sendRedirect(sb.toString());	// Force redirect.

		ctx.getResponse().setContentType("text/html; charset=UTF-8");
		ctx.getResponse().setCharacterEncoding("UTF-8");
		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter());
		out.writeRaw("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" + "<html><head><script language=\"javascript\"><!--\n"
			+ "location.replace(" + StringTool.strToJavascriptString(to, true) + ");\n" + "--></script>\n" + "</head><body>" + rsn + "</body></html>\n");
	}

	/**
	 * Generate an AJAX redirect command. Should be used by all COMMAND actions.
	 * @param ctx
	 * @param url
	 * @throws Exception
	 */
	static public void generateAjaxRedirect(final RequestContextImpl ctx, final String url) throws Exception {
		if(LOG.isInfoEnabled())
			LOG.info("redirecting to " + url);

		ctx.getResponse().setContentType("text/xml; charset=UTF-8");
		ctx.getResponse().setCharacterEncoding("UTF-8");
		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter());
		out.tag("redirect");
		out.attr("url", url);
		out.endAndCloseXmltag();
	}


	/**
	 * Generates an EXPIRED message when the page here does not correspond with
	 * the page currently in the browser. This causes the browser to do a reload.
	 * @param ctx
	 * @throws Exception
	 */
	private void generateExpired(final RequestContextImpl ctx, final String message) throws Exception {
		//-- We stay on the same page. Render tree delta as response
		ctx.getResponse().setContentType("text/xml; charset=UTF-8");
		ctx.getResponse().setCharacterEncoding("UTF-8");
		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter());
		out.tag("expired");
		out.endtag();

		out.tag("msg");
		out.endtag();
		out.text(message);
		out.closetag("msg");
		out.closetag("expired");
	}

	/**
	 * Walk the request parameter list and bind all values that came from an input thingy
	 * to the appropriate Node. Nodes whose value change will leave a trail in the pending
	 * change list which will later be used to fire change events, if needed.
	 * <p>This collects a list of nodes whose input values have changed <b>and</b> that have
	 * an onValueChanged listener. This list will later be used to call the change handles
	 * on all these nodes (bug# 664).
	 *
	 * @param ctx
	 * @param page
	 * @throws Exception
	 */
	private List<NodeBase> handleComponentInput(final IRequestContext ctx, final Page page) throws Exception {
		//-- Just walk all parameters in the input request.
		List<NodeBase> changed = new ArrayList<NodeBase>();
		for(String name : ctx.getParameterNames()) {
			String[] values = ctx.getParameters(name); // Get the value;
			//			System.out.println("input: "+name+", value="+values[0]);

			//-- Locate the component that the parameter is for;
			if(name.startsWith("_")) {
				NodeBase nb = page.findNodeByID(name); // Can we find this literally?
				if(nb != null) {
					//-- Try to bind this value to the component.
					if(nb.acceptRequestParameter(values)) { // Make the thingy accept the parameter(s)
						//-- This thing has changed.
						if(nb instanceof IInputNode< ? >) { // Can have a value changed thingy?
							IInputNode< ? > ch = (IInputNode< ? >) nb;
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

		NodeBase wcomp = null;
		String wid = ctx.getRequest().getParameter("webuic");
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
			if(Constants.ACMD_VALUE_CHANGED.equals(action) && wcomp != null && wcomp instanceof IHasChangeListener) {
				if(!pendingChangeList.contains(wcomp))
					pendingChangeList.add(wcomp);
			}

			//-- Call all "changed" handlers.
			for(NodeBase n : pendingChangeList) {
				if(n instanceof IHasChangeListener) {
					IHasChangeListener chb = (IHasChangeListener) n;
					IValueChanged<NodeBase> vc = (IValueChanged<NodeBase>) chb.getOnValueChanged();
					if(vc != null) { // Well, other listeners *could* have changed this one, you know
						vc.onValueChanged(n);
					}
				}
			}

			// FIXME 20100331 jal Odd wcomp==null logic. Generalize.
			if(Constants.ACMD_CLICKED.equals(action)) {
				handleClicked(ctx, page, wcomp);
			} else if(Constants.ACMD_VALUE_CHANGED.equals(action)) {
				//-- Don't do anything at all - everything is done beforehand (bug #664).
			} else if(Constants.ACMD_ASYPOLL.equals(action)) {
				inhibitlog = true;
				//-- Async poll request..
				//			} else if("WEBUIDROP".equals(action)) {
				//				handleDrop(ctx, page, wcomp);
			} else if(wcomp == null && (Constants.ACMD_LOOKUP_TYPING.equals(action) || Constants.ACMD_LOOKUP_TYPING_DONE.equals(action))) {
				//-- Don't do anything at all - value is already selected by some of previous ajax requests, it is safe to ignore remaineing late lookup typing events
				inhibitlog = true;
			} else if(wcomp == null) {
				throw new IllegalStateException("Unknown node '" + wid + "' for action='" + action + "'");
			} else {
				wcomp.componentHandleWebAction(ctx, action);
			}
		} catch(ValidationException x) {
			/*
			 * When an action handler failed because it accessed a component which has a validation error
			 * we just continue - the failed validation will have posted an error message.
			 */
			if(LOG.isDebugEnabled())
				LOG.debug("rq: ignoring validation exception " + x);
		} catch(Exception x) {
			if(x instanceof NotLoggedInException) { // FIXME Fugly. Generalize this kind of exception handling somewhere.
				String url = m_application.handleNotLoggedInException(ctx, page, (NotLoggedInException) x);
				if(url != null) {
					generateAjaxRedirect(ctx, url);
					return;
				}
			}
			if(x instanceof QNotFoundException) { // FIXME Fugly also?
				String url = m_application.handleQNotFoundException(ctx, page, (QNotFoundException) x);
				if(url != null) {
					generateAjaxRedirect(ctx, url);
					return;
				}
			}

			IExceptionListener xl = ctx.getApplication().findExceptionListenerFor(x);
			if(xl == null) // No handler?
				throw x; // Move on, nothing to see here,
			if(wcomp != null && wcomp.getPage() == null) {
				wcomp = page.getTheCurrentControl();
				System.out.println("DEBUG: Report exception on a " + wcomp.getClass());
			}
			if(wcomp == null || wcomp.getPage() == null)
				throw new IllegalStateException("INTERNAL: Cannot determine node to report exception /on/", x);

			if(!xl.handleException(ctx, page, wcomp, x))
				throw x;
		}
		if(LOG.isInfoEnabled() && !inhibitlog) {
			ts = System.nanoTime() - ts;
			LOG.info("rq: Action handling took " + StringTool.strNanoTime(ts));
		}
		if(!page.isDestroyed()) // jal 20090827 If an exception handler or whatever destroyed conversation or page exit...
			page.getConversation().processDelayedResults(page);

		//-- Determine the response class to render; exit if we have a redirect,
		WindowSession cm = ctx.getWindowSession();
		if(cm.handleGoto(ctx, page, true))
			return;

		//-- Call the 'new page added' listeners for this page, if it is now unbuilt due to some action calling forceRebuild() on it. Fixes bug# 605
		callNewPageListeners(page);

		//-- We stay on the same page. Render tree delta as response
		try {
			renderOptimalDelta(ctx, page, inhibitlog);
		} catch(NotLoggedInException x) { // FIXME Fugly. Generalize this kind of exception handling somewhere.
			String url = m_application.handleNotLoggedInException(ctx, page, x);
			if(url != null) {
				generateHttpRedirect(ctx, url, "You need to be logged in");
				return;
			}
		}
	}

	static public void renderOptimalDelta(final RequestContextImpl ctx, final Page page) throws Exception {
		renderOptimalDelta(ctx, page, false);
	}

	static private void renderOptimalDelta(final RequestContextImpl ctx, final Page page, boolean inhibitlog) throws Exception {
		ctx.getResponse().setContentType("text/xml; charset=UTF-8");
		ctx.getResponse().setCharacterEncoding("UTF-8");
		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter());

		long ts = System.nanoTime();
		//		String	usag = ctx.getUserAgent();
		HtmlFullRenderer fullr = ctx.getApplication().findRendererFor(ctx.getBrowserVersion(), out);
		OptimalDeltaRenderer dr = new OptimalDeltaRenderer(fullr, ctx, page);
		dr.render();
		if(LOG.isInfoEnabled() && !inhibitlog) {
			ts = System.nanoTime() - ts;
			LOG.info("rq: Optimal Delta rendering using " + fullr + " took " + StringTool.strNanoTime(ts));
		}
		page.getConversation().startDelayedExecution();
	}

	/**
	 * Call all "new page" listeners when a page is unbuilt or new at this time.
	 *
	 * @param pg
	 * @throws Exception
	 */
	private void callNewPageListeners(final Page pg) throws Exception {
		if(pg.getBody().isBuilt())
			return;
		//		PageContext.internalSet(pg); // Jal 20081103 Set state before calling add listeners.
		pg.build();
		for(INewPageInstantiated npi : m_application.getNewPageInstantiatedListeners())
			npi.newPageInstantiated(pg.getBody());
	}

	/**
	 * Called when the action is a CLICK event on some thingy. This causes the click handler for
	 * the object to be called.
	 *
	 * @param ctx
	 * @param page
	 * @param cid
	 * @throws Exception
	 */
	private void handleClicked(final IRequestContext ctx, final Page page, final NodeBase b) throws Exception {
		if(b == null) {
			System.out.println("User clicked too fast? Node not found. Ignoring.");
			return;
			//			throw new IllegalStateException("Clicked must have a node!!");
		}
		b.internalOnClicked();
	}
}
