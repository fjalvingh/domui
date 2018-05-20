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

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.state.AppSession;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

final public class NormalContextMaker extends AbstractContextMaker {
	@NonNull
	final private String m_applicationClassName;

//	@NonNull
//	final private ConfigParameters m_config;
//
	private DomApplication m_application;

	public NormalContextMaker(@NonNull String applicationClassName, @NonNull ConfigParameters pp) throws Exception {
		super(pp);
		m_applicationClassName = applicationClassName;
//		m_config = pp;

		//-- Load class,
		Class< ? > clz;
		try {
			clz = getClass().getClassLoader().loadClass(applicationClassName);
		} catch(ClassNotFoundException x) {
			throw new IllegalStateException("The main application class '" + m_applicationClassName + "' cannot be found: " + x, x);
		}

		/*
		 * We have to create/replace the application class.
		 */
		try {
			m_application = (DomApplication) clz.newInstance();
		} catch(Exception x) {
			throw new IllegalStateException("The main application class '" + m_applicationClassName + "' cannot be INSTANTIATED: " + x, x);
		}

		m_application.internalInitialize(pp, false);
	}

	/**
	 * Create a normal context.
	 * @see to.etc.domui.server.IContextMaker#createContext(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handleRequest(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws Exception {
		//-- Get session,
		try {
			HttpSession sess = request.getSession(true);
			AppSession appSession;
			synchronized(sess) {
				appSession = (AppSession) sess.getAttribute(AppSession.class.getName());
				if(appSession == null) {
					appSession = m_application.createSession();
					sess.setAttribute(AppSession.class.getName(), appSession);
				}
			}

			HttpServerRequestResponse requestResponse = HttpServerRequestResponse.create(m_application, request, response);
			RequestContextImpl ctx = new RequestContextImpl(requestResponse, m_application, appSession);
			execute(requestResponse, ctx, chain);
		} finally {
			//			DomApplication.internalSetCurrent(null);
		}
	}
}
