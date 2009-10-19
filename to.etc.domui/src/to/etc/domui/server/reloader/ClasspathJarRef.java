package to.etc.domui.server.reloader;

import java.io.*;

import to.etc.domui.util.resources.*;

/**
 * A reference to a .jar file containing some resource. This has special code to handle
 * resources loaded from a jar to prevent per-classloader caching of loaded resources.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 19, 2009
 */
public class ClasspathJarRef implements IModifyableResource {
	private File m_src;

	public ClasspathJarRef(File src) {
		m_src = src;
	}

	public long getLastModified() {
		try {
			if(!m_src.exists())
				return -1;
			return m_src.lastModified();
		} catch(Exception x) {
			return -1;
		}
	}

	@Override
	public String toString() {
		return "[ClasspathJar: " + m_src.toString() + "]";
	}
}
