package to.etc.domui.server;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This listens for http session events, and keeps track of all
 * active sessions. We need to be able to access sessions to be
 * able to logout all "other" sessions when a password change
 * is requested.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-03-22.
 */
final class ActiveSessionListener implements HttpSessionListener {
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		AppFilter.addSession(se.getSession());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		AppFilter.removeSession(se.getSession());
	}
}
