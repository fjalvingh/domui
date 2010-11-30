/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
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
	@Override
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

	@Override
	public void valueBound(HttpSessionBindingEvent arg0) {}

	/**
	 * Session expired- discard session properly.
	 * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
	 */
	@Override
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
