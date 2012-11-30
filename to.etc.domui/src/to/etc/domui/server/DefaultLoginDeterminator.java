package to.etc.domui.server;

import javax.annotation.*;
import javax.servlet.http.*;

import to.etc.domui.login.*;
import to.etc.domui.state.*;

/**
 * This login determinator uses declarative security to determine the logged-in user.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 30, 2012
 */
final public class DefaultLoginDeterminator implements ILoginDeterminator {
	@Override
	@Nullable
	public String getLoginData(@Nonnull HttpServletRequest req) throws Exception {
		//-- Decode: input must be for a logged-on user and for a .jsp/.ajax.
		String remoteUser = req.getRemoteUser();
		if(remoteUser == null || remoteUser.length() == 0) {
			HttpSession hs = req.getSession();
			Object sval = hs.getAttribute(UIContext.LOGIN_KEY); // Try to find the key,
			if(sval == null)
				return null;
			if(!(sval instanceof IUser))
				return null;
			remoteUser = ((IUser) sval).getLoginID();
		}
		String url = req.getRequestURI();
		int pos = url.lastIndexOf('.');
		if(pos == -1)
			return null;
		String ext = url.substring(pos + 1).toLowerCase();
		if(!"ajax".equals(ext) && !"jsp".equals(ext) && !"ui".equals(ext))
			return null;
		return remoteUser;
	}
}
