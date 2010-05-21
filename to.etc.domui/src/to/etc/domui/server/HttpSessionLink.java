package to.etc.domui.server;

import javax.servlet.http.*;

import to.etc.domui.server.reloader.*;
import to.etc.domui.state.*;

/**
 * This is contained in an HttpSession and refers to the AppSession
 * there.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
final public class HttpSessionLink implements IReloadedClassesListener, HttpSessionBindingListener {
	private AppSession m_appSession;

	private ReloadingContextMaker m_reloader;

	public HttpSessionLink(ReloadingContextMaker reloader) {
		m_reloader = reloader;
	}

	/**
	 * When classes are reloaded we MUST invalidate the current appSession.
	 *
	 * @see to.etc.domui.server.reloader.IReloadedClassesListener#classesReloaded()
	 */
	public void classesReloaded() {
		AppSession old;
		synchronized(this) {
			old = m_appSession;
			m_appSession = null;
		}
		if(old != null) {
			old.internalDestroy();
		}
	}

	public void valueBound(HttpSessionBindingEvent arg0) {}

	/**
	 * Session expired- discard session properly.
	 * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
	 */
	public void valueUnbound(HttpSessionBindingEvent arg0) {
		classesReloaded();
		m_reloader.removeListener(this); // Drop me from the class reloader list
	}

	AppSession getAppSession(DomApplication app) {
		AppSession s;
		synchronized(this) {
			if(m_appSession == null)
				m_appSession = app.createSession();
			else if(m_appSession.getApplication() != app)
				throw new IllegalStateException("Different DomApplication instances??");
			s = m_appSession;
		}
		return s;
	}
}
