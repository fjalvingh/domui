package to.etc.domui.login;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.dom.html.Page;
import to.etc.domui.server.RequestContextImpl;
import to.etc.function.ConsumerEx;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 5-12-18.
 */
@NonNullByDefault
public interface IPageAccessChecker {
	/**
	 * Checks the page's access rights against whatever is the currently logged in user.
	 */
	AccessCheckResult checkAccess(RequestContextImpl ctx, Page page, ConsumerEx<String> logerror) throws Exception;
}
