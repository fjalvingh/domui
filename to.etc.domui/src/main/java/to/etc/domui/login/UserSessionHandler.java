package to.etc.domui.login;

import to.etc.webapp.eventmanager.DbEventManager;
import to.etc.webapp.eventmanager.ListenerType;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QDataContext;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-03-22.
 */
final public class UserSessionHandler {
	static public void initialize() throws Exception {
		DbEventManager.getInstance().addListener(EvSessionLogout.class, ListenerType.DELAYED, obj -> {
			UILogin.logoutOtherSessions();
		});
	}

	/**
	 * Force logout all other sessions of a user, including possible sessions
	 * on other servers.
	 */
	static public void logoutOtherSessions() throws Exception {
		IUser current = UILogin.getCurrentUser();
		if(current == null)
			return;

		//-- Locally log out all other sessions
		UILogin.logoutOtherSessions();

		//-- Ask all other servers to do the same
		try(QDataContext dc = QContextManager.createUnmanagedContext()) {
			DbEventManager.getInstance().postRemoteEvent(dc.getConnection(), new EvSessionLogout(current.getLoginID()));
		}
	}
}
