package to.etc.domui.server;

import java.io.*;

import javax.servlet.*;

/**
 * Hides the method to get app parameters. Currently proxies to
 * FilterConfig.
 * 
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public class ConfigParameters {
	private FilterConfig m_fc;

	private File m_webFileRoot;

	ConfigParameters(FilterConfig fc, File webFileRoot) {
		m_fc = fc;
		m_webFileRoot = webFileRoot;
	}

	public String getString(String name) {
		return m_fc.getInitParameter(name);
	}

	public File getWebFileRoot() {
		return m_webFileRoot;
	}
}
