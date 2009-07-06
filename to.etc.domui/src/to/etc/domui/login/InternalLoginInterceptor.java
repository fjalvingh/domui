package to.etc.domui.login;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;

/**
 * Internal RequestInterceptor used to handle login chores. When a request causes a
 * login req exception this will cause a redirect to be sent to an URL returned by
 * the ILoginDialogHandler. This redirected thing must cause a login page to be
 * displayed and handled. The login page is responsible for returning to the
 * target when login was succesful.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 15, 2009
 */
public class InternalLoginInterceptor implements IRequestInterceptor {
	public void after(IRequestContext rc, Exception x) throws Exception {
		if(!(x instanceof NotLoggedInException))
			return;
		RequestContextImpl ci = (RequestContextImpl) rc;
		ILoginDialogFactory ldf = ci.getApplication().getLoginDialogFactory();
		if(ldf != null)
			return;

	}

	public void before(IRequestContext rc) throws Exception {}
}
