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
package to.etc.domui.test.util;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

public class TestUtil {
	static private AppSession m_session;

	static private DomApplication m_application;

	static public synchronized DomApplication getApplication() {
		if(m_application == null) {
			m_application = new DomApplication() {
				@Override
				public Class< ? extends UrlPage> getRootPage() {
					return null;
				}
			};
			//			DomApplication.internalSetCurrent(m_application);
		}
		return m_application;
	}

	static public AppSession getAppSession() {
		getApplication();
		if(m_session == null) {
			m_session = new AppSession(getApplication());
		}
		return m_session;
	}

	/**
	 * Create a page structure valid for testing pps.
	 * @param pg
	 * @return
	 */
	static private Page initPage(UrlPage pg, PageParameters pp) throws Exception {
		getApplication();
		Page p = new Page(pg);
		WindowSession ws = new WindowSession(getAppSession());
		ConversationContext cc = new ConversationContext();
		ws.acceptNewConversation(cc);
		p.internalInitialize(pp, cc);
		cc.internalRegisterPage(p, pp);
		return p;
	}

	static public Page createPage(Class< ? extends UrlPage> clz, PageParameters pp) throws Exception {
		if(pp == null)
			pp = new PageParameters();
		UrlPage pg = clz.newInstance(); // Should have parameterless ctor
		Page p = initPage(pg, pp);
		return p;
	}

	static public Page createPage(Class< ? extends UrlPage> clz) throws Exception {
		return createPage(clz, null);
	}
}
