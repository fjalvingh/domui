package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.dom.IBrowserOutput;
import to.etc.domui.dom.PrettyXmlOutputWriter;
import to.etc.domui.login.ILoginDialogFactory;
import to.etc.domui.state.ConversationContext;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.WindowSession;
import to.etc.domui.util.Constants;
import to.etc.domui.util.DomUtil;
import to.etc.util.StringTool;

/**
 * Writes responses in the required format.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-11-18.
 */
@NonNullByDefault
public class ResponseCommandWriter {
	public ResponseCommandWriter() {
	}

	private void renderHeaders(RequestContextImpl ctx) throws Exception {
		IRequestResponse rr = ctx.getRequestResponse();
		DomApplication domApplication = DomApplication.get();
		ctx.renderResponseHeaders(null);
		//domApplication.applyPageHeaderTransformations(ctx.getPageName(), domApplication.getDefaultHTTPHeaderMap()).forEach((header, value) -> rr.addHeader(header, value));
	}

	/**
	 * Generates an EXPIRED message when the page here does not correspond with
	 * the page currently in the browser. This causes the browser to do a reload.
	 */
	public void generateExpired(RequestContextImpl ctx, String message) throws Exception {
		renderHeaders(ctx);

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

	public void generateEmptyDelta(RequestContextImpl ctx) throws Exception {
		//-- We stay on the same page. Render tree delta as response
		renderHeaders(ctx);
		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter("text/xml; charset=UTF-8", "utf-8"));
		out.tag("delta");
		out.endtag();
		out.closetag("delta");
	}

	/**
	 * Generates an 'expiredOnPollasy' message when server receives pollasy call from expired page.
	 * Since pollasy calls are frequent, expired here means that user has navigated to some other page in meanwhile, and that response should be ignored by browser.
	 */
	public void generateExpiredPollasy(RequestContextImpl ctx) throws Exception {
		//-- We stay on the same page. Render tree delta as response
		renderHeaders(ctx);
		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter("text/xml; charset=UTF-8", "utf-8"));
		out.tag("expiredOnPollasy");
		out.endtag();
		out.closetag("expiredOnPollasy");
	}

	public void redirectToLoginPage(RequestContextImpl ctx, WindowSession cm) throws Exception {
		//-- Create the after-login target URL.
		StringBuilder sb = new StringBuilder(256);
		sb.append(ctx.getInputPath());							// [security] Must be relative!!
		sb.append('?');
		StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
		sb.append('=');
		sb.append(cm.getWindowID());
		sb.append(".x"); 												// Dummy conversation ID
		DomUtil.addUrlParameters(sb, ctx, false);

		//-- Obtain the URL to redirect to from a thingy factory (should this happen here?)
		ILoginDialogFactory ldf = ctx.getApplication().getLoginDialogFactory();
		if(ldf == null)
			throw new IllegalStateException("No login factory configured");
		String target = ldf.getLoginRURL(sb.toString());				// Create a RURL to move to.
		if(target == null)
			throw new IllegalStateException("The Login Dialog Handler=" + ldf + " returned an invalid URL for the login dialog.");

		//-- Make this an absolute URL by appending the webapp path
		target = ctx.getRelativePath(target);
		ApplicationRequestHandler.generateHttpRedirect(ctx, target, "You need to login before accessing this function");
	}

	/**
	 * Fix for huge POST requests being resent as a get.
	 */
	public void redirectForPost(RequestContextImpl ctx, WindowSession cm, @NonNull PageParameters pp) throws Exception {
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
		ApplicationRequestHandler.generateHttpRedirect(ctx, sb.toString(), "Your session has expired. Starting a new session.");
	}

}
