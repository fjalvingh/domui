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
package to.etc.domui.testsupport;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

/**
 * A dummy test request context used for testing.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 9, 2008
 */
public class TestRequestContext implements IRequestContext {
	private DomApplication m_app;

	private final String m_input;

	private AppSession m_session;

	private StringWriter m_sw;

	private final Map<String, String[]> m_parameterMap = new HashMap<String, String[]>();

	private WindowSession m_conversationManager;

	public TestRequestContext() {
		m_input = "test/page.html";
	}

	@Override
	public DomApplication getApplication() {
		if(m_app == null)
			m_app = new DomApplication() {
				@Override
				public Class< ? extends UrlPage> getRootPage() {
					return null;
				}
			};
		return m_app;
	}

	@Override
	public WindowSession getWindowSession() {
		if(m_conversationManager == null)
			m_conversationManager = new WindowSession(getSession());
		return m_conversationManager;
	}

	@Override
	public String getExtension() {
		int pos = m_input.lastIndexOf('.');
		if(pos == -1)
			return "";
		return m_input.substring(pos + 1);
	}

	@Override
	public String getInputPath() {
		return m_input;
	}

	@Override
	public Writer getOutputWriter() throws IOException {
		if(m_sw == null)
			m_sw = new StringWriter();
		return m_sw;
	}

	@Override
	public String getRelativePath(final @Nonnull String rel) {
		return "webapp/" + rel;
	}

	@Override
	public String getThemedPath(String in) {
		return "themes/" + in;
	}

	@Override
	public AppSession getSession() {
		if(m_session == null)
			m_session = new AppSession(getApplication());
		return m_session;
	}

	@Override
	public String getUserAgent() {
		return "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9) Gecko/2008060309 Firefox/3.0";
	}

	@Override
	public String getParameter(final String name) {
		String[] v = getParameters(name);
		if(v == null || v.length != 1)
			return null;
		return v[0];
	}

	@Override
	public String[] getParameterNames() {
		return m_parameterMap.keySet().toArray(new String[m_parameterMap.size()]);
	}

	@Override
	public String[] getParameters(final String name) {
		return m_parameterMap.get(name);
	}

	@Override
	public String getRemoteUser() {
		return "VPC";
	}

	@Override
	public BrowserVersion getBrowserVersion() {
		return null;
	}
	/**
	 * FIXME Does this need more?
	 * @see to.etc.domui.server.IRequestContext#hasPermission(java.lang.String)
	 */
	@Override
	public boolean hasPermission(final @Nonnull String permissionName) {
		return true;
	}
}
