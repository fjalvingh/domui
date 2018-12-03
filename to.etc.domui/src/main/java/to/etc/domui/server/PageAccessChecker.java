package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.annotations.UIRights;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.login.AccessDeniedPage;
import to.etc.domui.login.ILoginDialogFactory;
import to.etc.domui.login.IUser;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.UIContext;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IRightsCheckedManually;
import to.etc.domui.util.Msgs;
import to.etc.function.ConsumerEx;
import to.etc.util.StringTool;
import to.etc.webapp.nls.CodeException;

/**
 * Implements core rights handling.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-11-18.
 */
@NonNullByDefault
public class PageAccessChecker {
	/**
	 * Authentication checks: if the page has a "UIRights" annotation we need a logged-in
	 * user to check it's rights against the page's required rights.
	 *
	 * WARNING: Functional duplicate exists in {@link UIContext#hasRightsOn(Class)}.
	 */
	public PageAccessCheckResult checkAccess(RequestContextImpl ctx, Page page, ConsumerEx<String> logerror) throws Exception {
		if(ctx.getParameter("webuia") != null)
			throw new IllegalStateException("Cannot be called for an AJAX request");
		UrlPage body = page.getBody();							// The actual, instantiated and injected class - which is unbuilt, though
		UIRights rann = body.getClass().getAnnotation(UIRights.class);		// Get class annotation
		IRightsCheckedManually rcm = body instanceof IRightsCheckedManually ? (IRightsCheckedManually) body : null;

		if(rann == null && rcm == null) {						// Any kind of rights checking is required?
			return PageAccessCheckResult.Accepted;				// No -> allow access.
		}

		//-- Get user's IUser; if not present we need to log in.
		IUser user = UIContext.getCurrentUser(); 				// Currently logged in?
		if(user == null) {
			//m_commandWriter.redirectToLoginPage(ctx, cm);
			return PageAccessCheckResult.NeedLogin;
		}

		//-- Start access checks, in order. First call the interface, if applicable
		String failureReason = null;
		try {
			if(isAccessAllowed(body, rann, rcm, user))
				return PageAccessCheckResult.Accepted;
		} catch(CodeException cx) {
			failureReason = cx.getMessage();
		} catch(Exception x) {
			failureReason = x.toString();
		}

		/*
		 * Access not allowed: redirect to error page.
		 */
		renderAccessFailure(ctx, logerror, body, rann, failureReason);
		return PageAccessCheckResult.Refused;
	}

	private boolean isAccessAllowed(UrlPage body, @Nullable UIRights rann, @Nullable IRightsCheckedManually rcm, IUser user) throws Exception {
		if(null != rcm) {
			boolean allowed = rcm.isAccessAllowedBy(user);	// Call interface: it explicitly allows
			if(allowed)
				return true;

			//-- False indicates "I do not give access, but I do not deny it either". So move on to the next check.
		}

		if(null != rann) {
			if(checkRightsAnnotation(body, rann, user)) {	// Check annotation rights
				return true;
			}

			//-- Just exit with a null failureReason - this indicates that a list of rights will be rendered.
		} else
			throw new CodeException(Msgs.BUNDLE, Msgs.RIGHTS_NOT_ALLOWED);	// Insufficient rights - details unknown.
		return false;
	}

	private void renderAccessFailure(RequestContextImpl ctx, ConsumerEx<String> logerror, UrlPage body, @Nullable UIRights rann, @Nullable String failureReason) throws Exception {
		ILoginDialogFactory ldf = ctx.getApplication().getLoginDialogFactory();
		String rurl = ldf == null ? null : ldf.getAccessDeniedURL();
		if(rurl == null) {
			rurl = DomApplication.get().getAccessDeniedPageClass().getName() + "." + ctx.getApplication().getUrlExtension();
		}

		//-- Add info about the failed thingy.
		StringBuilder sb = new StringBuilder(128);
		sb.append(rurl);
		DomUtil.addUrlParameters(sb, new PageParameters(AccessDeniedPage.PARAM_TARGET_PAGE, body.getClass().getName()), true);

		//-- If we have a message use it
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
			logerror.accept(redirect);
		}
	}

	private boolean checkRightsAnnotation(@NonNull UrlPage body, @NonNull UIRights rann, @NonNull IUser user) throws Exception {
		if(rann.value().length == 0)						// No rights specified means -> just log in
			return true;
		if(StringTool.isBlank(rann.dataPath())) {
			//-- No special data context - we just check plain general rights
			for(String right : rann.value()) {
				if(user.hasRight(right)) {
					return true;
				}
			}
			return false;										// All worked, so we have access.
		}

		//-- We need the object specified in DataPath.
		PropertyMetaModel< ? > pmm = MetaManager.getPropertyMeta(body.getClass(), rann.dataPath());
		Object dataItem = pmm.getValue(body);					// Get the page property.
		for(String right : rann.value()) {
			if(user.hasRight(right, dataItem)) {
				return true;
			}
		}
		return false;
	}
}
