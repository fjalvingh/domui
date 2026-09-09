package to.etc.domuidemo;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.SessionCookieConfig;

/**
 * Marks the session cookie Secure, always - which, together with the
 * SameSite=None and Partitioned set on the CookieProcessor in
 * META-INF/context.xml, is the one spelling of the cookie that every browser
 * accepts in every context: as a normal page, and inside the cross-site iframes
 * the documentation site embeds this application in with its !demo() tag.
 *
 * Secure is not optional protection here, it is part of the syntax: a cookie
 * saying SameSite=None or Partitioned without it is malformed and is dropped,
 * and then every page shows "Can't create session, session cookie is blocked by
 * the browser!".
 *
 * It costs nothing on a developer workstation, because browsers treat localhost
 * as a trustworthy origin and accept Secure cookies over plain http there, so a
 * local run and the deployed site get the exact same cookie. The one place it
 * does not work is plain http on some *other* host (a LAN address, say); no
 * cookie setting can fix that one - use localhost or https.
 *
 * This is done here and not with web.xml's session-config/cookie-config/secure
 * because Tomcat ignores that element - it takes the flag from the request being
 * secure, and TLS is terminated by Apache in front of it, so it never is.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class SessionCookieSetup implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		SessionCookieConfig cc = sce.getServletContext().getSessionCookieConfig();
		cc.setSecure(true);
		cc.setHttpOnly(true);
	}
}
