package to.etc.domuidemo;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.SessionCookieConfig;
import to.etc.util.DeveloperOptions;

/**
 * Marks the session cookie Secure, which is what makes the demo work inside the
 * iframes of the documentation site: that is a cross-site frame, so the browser
 * only returns the cookie when it says SameSite=None (set on the CookieProcessor
 * in META-INF/context.xml), and it rejects SameSite=None on a cookie that is not
 * Secure. Without it every embedded page shows "Can't create session, session
 * cookie is blocked by the browser!".
 *
 * This is done here and not with web.xml's session-config/cookie-config/secure
 * because Tomcat ignores that element - it takes the flag from the request being
 * secure, and TLS is terminated by Apache in front of it, so it never is.
 *
 * On a developer workstation the flag is left alone: a Secure cookie is only
 * returned over https or on localhost, and development instances are plain http.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class SessionCookieSetup implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if(DeveloperOptions.isDeveloperWorkstation())
			return;
		SessionCookieConfig cc = sce.getServletContext().getSessionCookieConfig();
		cc.setSecure(true);
		cc.setHttpOnly(true);
	}
}
