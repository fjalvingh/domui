package to.etc.domui.login;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.annotations.UIRights;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.UIContext;
import to.etc.domui.util.IRightsCheckedManually;
import to.etc.domui.util.Msgs;
import to.etc.function.ConsumerEx;
import to.etc.util.StringTool;
import to.etc.webapp.nls.CodeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements core rights handling.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-11-18.
 */
@NonNullByDefault
public class DefaultPageAccessChecker implements IPageAccessChecker {
	@NonNull
	private IUserRightChecker<IUser> m_userRightChecker = new User2RightsChecker();

	/**
	 * Authentication checks: if the page has a "UIRights" annotation we need a logged-in
	 * user to check it's rights against the page's required rights.
	 */
	@Override
	public AccessCheckResult checkAccess(RequestContextImpl ctx, Page page, ConsumerEx<String> logerror) throws Exception {
		if(ctx.getPageParameters().getString("webuia", null) != null)
			throw new IllegalStateException("Cannot be called for an AJAX request");
		UrlPage body = page.getBody();							// The actual, instantiated and injected class - which is unbuilt, though
		UIRights rann = body.getClass().getAnnotation(UIRights.class);		// Get class annotation
		IRightsCheckedManually rcm = body instanceof IRightsCheckedManually ? (IRightsCheckedManually) body : null;

		if(rann == null && rcm == null) {						// Any kind of rights checking is required?
			return AccessCheckResult.accepted();
		}

		//-- Get user's IUser; if not present we need to log in.
		IUser user = UIContext.getCurrentUser(); 				// Currently logged in?
		if(user == null) {
			return AccessCheckResult.needLogin();
		}

		//-- Start access checks, in order. First call the interface, if applicable
		List<UIMessage> errors = new ArrayList<>();
		try {
			if(isAccessAllowed(body, rann, rcm, user))
				return AccessCheckResult.accepted();
		} catch(CodeException cx) {
			errors.add(UIMessage.error(cx));
		} catch(Exception x) {
			errors.add(UIMessage.error(Msgs.unexpectedException, x.toString()));
		}

		/*
		 * Access not allowed: redirect to error page.
		 */
		return AccessCheckResult.refused(page.getBody(), rann == null ? new String[] {} : rann.value(), errors);
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
			throw new CodeException(Msgs.rightsNotAllowed);	// Insufficient rights - details unknown.
		return false;
	}

	private boolean checkRightsAnnotation(@NonNull UrlPage body, @NonNull UIRights rann, @NonNull IUser user) throws Exception {
		if(rann.value().length == 0)						// No rights specified means -> just log in
			return true;
		if(StringTool.isBlank(rann.dataPath())) {
			//-- No special data context - we just check plain general rights
			for(String right : rann.value()) {
				if(m_userRightChecker.hasRight(user, right)) {
					return true;
				}
			}
			return false;										// All worked, so we have access.
		}

		//-- We need the object specified in DataPath.
		PropertyMetaModel< ? > pmm = MetaManager.getPropertyMeta(body.getClass(), rann.dataPath());
		Object dataItem = pmm.getValue(body);					// Get the page property.
		for(String right : rann.value()) {
			if(m_userRightChecker.hasRight(user, right, dataItem)) {
				return true;
			}
		}
		return false;
	}
}
