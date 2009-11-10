package to.etc.domui.server;

import java.util.logging.*;

import to.etc.domui.annotations.*;
import to.etc.domui.dom.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.login.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

/**
 * Mostly silly handler to handle direct DOM requests. Phaseless handler for testing
 * direct/delta building only using a reloadable class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public class ApplicationRequestHandler implements IFilterRequestHandler {
	static Logger LOG = Logger.getLogger(ApplicationRequestHandler.class.getName());

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
		if(Constants.OBITUARY.equals(action)) {
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

			if(LOG.isLoggable(Level.FINE))
				LOG.fine("OBITUARY received for " + cid + ": pageTag=" + pageTag);
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
				generateExpired(ctx, NlsContext.getGlobalMessage(Msgs.S_EXPIRED));
				return;
				//				throw new IllegalStateException("AJAX request '"+action+"' has no WindowID/CID");
			}

			//-- We explicitly need to create a new Window and need to send a redirect back
			cm = ctx.getSession().createWindowSession();
			if(LOG.isLoggable(Level.FINE))
				LOG.fine("$cid: input windowid=" + cid + " not found - created wid=" + cm.getWindowID());
			StringBuilder sb = new StringBuilder(256);

			//			sb.append('/');
			sb.append(ctx.getRelativePath(ctx.getInputPath()));
			sb.append('?');
			StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
			sb.append('=');
			sb.append(cm.getWindowID());
			sb.append(".x"); // Dummy conversation ID
			DomUtil.addUrlParameters(sb, ctx, false);
			generateRedirect(ctx, sb.toString(), "Your session has expired. Starting a new session.");
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
					generateExpired(ctx, NlsContext.getGlobalMessage(Msgs.S_EXPIRED));
					return;
				}
			}
		}
		PageContext.internalSet(page);

		//-- All commands EXCEPT ASYPOLL have all fields, so bind them to the current component data,
		if(!Constants.ASYPOLL.equals(action)) {
			long ts = System.nanoTime();
			handleComponentInput(ctx, page); // Move all request parameters to their input field(s)
			if(LOG.isLoggable(Level.FINE)) {
				ts = System.nanoTime() - ts;
				LOG.fine("rq: input handling took " + StringTool.strNanoTime(ts));
			}
		}

		if(action != null) {
			runAction(ctx, page, action);
			return;
		} else {
			ctx.getApplication().getInjector().injectPageValues(page.getBody(), ctx, papa);

			if(page.getBody() instanceof IRebuildOnRefresh) { // Must fully refresh?
				page.getBody().forceRebuild(); // Cleanout state
				QContextManager.closeSharedContext(page.getConversation());
			}
			page.getBody().onReload();

			//-- EXPERIMENTAL Handle stored message in session
			String message = (String) cm.getAttribute(UIGoto.SINGLESHOT_ERROR);
			if(message != null) {
				page.getBody().build();
				page.getBody().addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.S_PAGE_CLEARED, message));
				cm.setAttribute(UIGoto.SINGLESHOT_ERROR, null);
			}
		}
		page.getConversation().processDelayedResults(page);

		//-- Start the main rendering process. Determine the browser type.
		long ts = System.nanoTime();
		ctx.getResponse().setContentType("text/html; charset=UTF-8");
		ctx.getResponse().setCharacterEncoding("UTF-8");
		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter());

		//		String	usag = ctx.getUserAgent();
		FullHtmlRenderer hr = m_application.findRendererFor(ctx.getBrowserVersion(), out);

		try {
			hr.render(ctx, page);
		} catch(Exception x) {
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
			throw x;
		} finally {
			page.clearDeltaFully();
		}

		//-- Full render completed: indicate that and reset the exception count
		page.setFullRenderCompleted(true);
		page.setPageExceptionCount(0);
		if(LOG.isLoggable(Level.FINE)) {
			ts = System.nanoTime() - ts;
			LOG.fine("rq: full render took " + StringTool.strNanoTime(ts));
		}

		//-- Start any delayed actions now.
		page.getConversation().startDelayedExecution();
	}

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
			generateRedirect(ctx, target, "You need to login before accessing this function");
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
		generateRedirect(ctx, sb.toString(), "Access denied");
		return false;
	}

	private void generateRedirect(final RequestContextImpl ctx, final String to, final String rsn) throws Exception {
		//		ctx.getResponse().sendRedirect(sb.toString());	// Force redirect.

		ctx.getResponse().setContentType("text/html; charset=UTF-8");
		ctx.getResponse().setCharacterEncoding("UTF-8");
		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter());
		out.writeRaw("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" + "<html><head><script language=\"javascript\"><!--\n"
			+ "location.replace(" + StringTool.strToJavascriptString(to, true) + ");\n" + "--></script>\n" + "</head><body>" + rsn + "</body></html>\n");
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
	 *
	 * @param ctx
	 * @param page
	 * @throws Exception
	 */
	private void handleComponentInput(final IRequestContext ctx, final Page page) throws Exception {
		//-- Just walk all parameters in the input request.
		for(String name : ctx.getParameterNames()) {
			String[] values = ctx.getParameters(name); // Get the value;
			//			System.out.println("input: "+name+", value="+values[0]);

			//-- Locate the component that the parameter is for;
			if(name.startsWith("_")) {
				NodeBase nb = page.findNodeByID(name); // Can we find this literally?
				if(nb != null) {
					//-- Try to bind this value to the component.
					nb.acceptRequestParameter(values); // Make the thingy accept the parameter(s)
				}
			}
		}
	}


	private void runAction(final RequestContextImpl ctx, final Page page, final String action) throws Exception {
		//		System.out.println("# action="+action);
		long ts = System.nanoTime();

		NodeBase wcomp = null;
		String wid = ctx.getRequest().getParameter("webuic");
		if(wid != null) {
			wcomp = page.findNodeByID(wid);
			if(wcomp == null) {
				generateExpired(ctx, NlsContext.getGlobalMessage(Msgs.S_BADNODE, wid));
				//				throw new IllegalStateException("Unknown node '"+wid+"'");
			}
		}

		boolean inhibitlog = false;
		try {
			if("clicked".equals(action)) {
				handleClicked(ctx, page, wcomp);
			} else if("vchange".equals(action)) {
				handleValueChanged(ctx, page, wcomp);
			} else if(Constants.ASYPOLL.equals(action)) {
				inhibitlog = true;
				//-- Async poll request..
				//			} else if("WEBUIDROP".equals(action)) {
				//				handleDrop(ctx, page, wcomp);
			} else {
				if(wcomp == null)
					throw new IllegalStateException("Unknown node '" + wid + "' for action='" + action + "'");
				wcomp.componentHandleWebAction(ctx, action);
			}
		} catch(ValidationException x) {
			/*
			 * When an action handler failed because it accessed a component which has a validation error
			 * we just continue - the failed validation will have posted an error message.
			 */
			if(LOG.isLoggable(Level.FINE))
				LOG.fine("rq: ignoring validation exception " + x);
		} catch(Exception x) {
			IExceptionListener xl = ctx.getApplication().findExceptionListenerFor(x);
			if(xl == null) // No handler?
				throw x; // Move on, nothing to see here,
			if(!xl.handleException(ctx, page, wcomp, x))
				throw x;
		}
		if(LOG.isLoggable(Level.INFO) && !inhibitlog) {
			ts = System.nanoTime() - ts;
			LOG.info("rq: Action handling took " + StringTool.strNanoTime(ts));
		}
		if(!page.isDestroyed()) // jal 20090827 If an exception handler or whatever destroyed conversation or page exit...
			page.getConversation().processDelayedResults(page);

		//-- Determine the response class to render; exit if we have a redirect,
		WindowSession cm = ctx.getWindowSession();
		if(cm.handleGoto(ctx, page))
			return;

		//-- We stay on the same page. Render tree delta as response
		renderOptimalDelta(ctx, page, inhibitlog);
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
		HtmlRenderer base = new HtmlRenderer(ctx.getBrowserVersion(), out);
		//		DeltaRenderer	dr	= new DeltaRenderer(base, out);
		OptimalDeltaRenderer dr = new OptimalDeltaRenderer(base, out);
		dr.render(ctx, page);
		if(LOG.isLoggable(Level.INFO) && !inhibitlog) {
			ts = System.nanoTime() - ts;
			LOG.info("rq: Optimal Delta rendering took " + StringTool.strNanoTime(ts));
		}
		page.getConversation().startDelayedExecution();
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
		if(b == null)
			throw new IllegalStateException("Clicked must have a node!!");
		b.internalOnClicked();
	}

	private void handleValueChanged(final IRequestContext ctx, final Page page, final NodeBase b) throws Exception {
		if(b == null)
			throw new IllegalStateException("onValueChanged must have a node!!");
		if(!(b instanceof IInputNode< ? >))
			throw new IllegalStateException("Internal: node " + b + " must be an IInputValue node");
		IInputNode< ? > in = (IInputNode< ? >) b;

		IValueChanged<NodeBase, Object> c = (IValueChanged<NodeBase, Object>) in.getOnValueChanged();
		if(c == null)
			throw new IllegalStateException("? Node " + b.getActualID() + " does not have a ValueChanged handler??");
		Object value = null;
		try {
			value = in.getValue();
		} catch(Exception x) {}
		c.onValueChanged(b, value);
	}
}
