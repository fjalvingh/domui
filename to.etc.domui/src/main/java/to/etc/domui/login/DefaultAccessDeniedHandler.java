package to.etc.domui.login;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.annotations.UIRights;
import to.etc.domui.server.ApplicationRequestHandler;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.PageParameters;
import to.etc.domui.util.DomUtil;
import to.etc.function.ConsumerEx;
import to.etc.util.StringTool;

/**
 * Reports the access denied error using the AccessDeniedPage. The data
 * block is saved in the session.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-12-18.
 */
public class DefaultAccessDeniedHandler implements IAccessDeniedHandler {
	@Override public void handleAccessDenied(RequestContextImpl ctx, @NonNull AccessCheckResult result, ConsumerEx<String> logSink) throws Exception {
		ILoginDialogFactory ldf = ctx.getApplication().getLoginDialogFactory();
		String rurl = ldf == null ? null : ldf.getAccessDeniedURL();
		if(rurl == null) {
			rurl = DomApplication.get().getAccessDeniedPageClass().getName() + "." + ctx.getApplication().getUrlExtension();
		}

		//-- Add info about the failed thingy.
		StringBuilder sb = new StringBuilder(128);
		sb.append(rurl);
		DomUtil.addUrlParameters(sb, new PageParameters(AccessDeniedPage.PARAM_TARGET_PAGE, result.getPage().getClass().getName()), true);

		//-- If we have a message use it
		String failureReason = null;
		if(result.getMessageList().size() > 0) {
			failureReason = result.getMessageList().get(0).getMessage();
		}
		UIRights rann = result.getRights();

		if(null == failureReason) {
			if(rann != null)
				failureReason = "Empty reason - this should not happen!";
		}
		sb.append("&").append(AccessDeniedPage.PARAM_REFUSAL_MSG).append("=");
		StringTool.encodeURLEncoded(sb, failureReason);

		//-- All required rights
		int ix = 0;
		if(null != rann) {
			for(String r : rann.value()) {
				sb.append("&r").append(ix).append("=");
				ix++;
				StringTool.encodeURLEncoded(sb, r);
			}
			String redirect = sb.toString();
			ApplicationRequestHandler.generateHttpRedirect(ctx, redirect, "Access denied");
			logSink.accept("Redirecting to " + redirect);
		}
	}
}
