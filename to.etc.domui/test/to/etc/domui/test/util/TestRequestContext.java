package to.etc.domui.test.util;

import java.io.*;
import java.util.*;

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
	public String getRelativePath(final String rel) {
		return "webapp/" + rel;
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
	public String getRelativeThemePath(final String frag) {
		return "themes/blue/" + frag;
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
	public boolean hasPermission(final String permissionName) {
		return true;
	}

	@Override
	public String translateResourceName(final String in) {
		return in;
	}
}
