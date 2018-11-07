package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.HtmlFullRenderer;
import to.etc.domui.dom.IBrowserOutput;
import to.etc.domui.dom.PrettyXmlOutputWriter;
import to.etc.domui.dom.html.OptimalDeltaRenderer;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.Constants;
import to.etc.util.DeveloperOptions;
import to.etc.util.StringTool;
import to.etc.webapp.ProgrammerErrorException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-11-18.
 */
final public class PageUtil {
	static boolean m_logPerf = DeveloperOptions.getBool("domui.logtime", false);

	private PageUtil() {
	}

	/**
	 * Decide what class to run depending on the input path.
	 */
	@NonNull
	static Class< ? extends UrlPage> decodeRunClass(@NonNull final IRequestContext ctx) {
		DomApplication application = ctx.getApplication();
		if(ctx.getInputPath().length() == 0) {
			/*
			 * We need to EXECUTE the application's main class. We cannot use the .class directly
			 * because the reloader must be able to substitute a new version of the class when
			 * needed.
			 */
			Class< ? extends UrlPage> rootPage = application.getRootPage();
			if(null == rootPage)
				throw new ProgrammerErrorException("The DomApplication's 'getRootPage()' method returns null, and there is a request for the root of the web app... Override that method or make sure the root is handled differently.");
			String txt = rootPage.getCanonicalName();
			return application.loadPageClass(txt);
		}

		//-- Try to resolve as a class name,
		String s = ctx.getInputPath();
		int pos = s.lastIndexOf('.');							// Always strip whatever extension
		if(pos != -1) {
			int spos = s.lastIndexOf('/') + 1;					// Try to locate path component
			if(pos > spos) {
				s = s.substring(spos, pos); 						// Last component, ex / and last extension.

				//-- This should be a classname now
				return application.loadPageClass(s);
			}
		}

		//-- All others- cannot resolve
		throw new IllegalStateException("Cannot decode URL " + ctx.getInputPath());
	}


	// FIXME MOVE
	static public void renderOptimalDelta(RequestContextImpl ctx, Page page) throws Exception {
		renderOptimalDelta(ctx, page, false);
	}

	// FIXME MOVE
	static void renderOptimalDelta(RequestContextImpl ctx, Page page, boolean inhibitlog) throws Exception {
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
	 * Defines the actions that could arrive too late due to race conditions in client javascript, when target elements are already removed from DOM at server side.
	 * It is safe to just ignore such obsoleted events, rather than giving error response.
	 */
	static boolean isSafeToIgnoreUnknownNodeOnAction(@NonNull String action) {
		return Constants.ACMD_LOOKUP_TYPING.equals(action)
			|| Constants.ACMD_LOOKUP_TYPING_DONE.equals(action)
			|| Constants.ACMD_NOTIFY_CLIENT_POSITION_AND_SIZE.equals(action)
			|| action.endsWith("?")
			;
	}
}

