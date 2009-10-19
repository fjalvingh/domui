package to.etc.domui.server.reloader;

import java.io.*;
import java.net.*;

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

	private URLClassLoader m_resourceLoader;

	private long m_resourceLoaderTS;

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

	/**
	 * This returns a classloader to use to load the resource; it creates a new classloader (in debug mode) if the
	 * underlying .jar has changed.
	 * @return
	 */
	public synchronized ClassLoader getResourceLoader() {
		try {
			long cts = m_src.lastModified(); // Jar's timestamp
			if(m_resourceLoader != null && m_resourceLoaderTS == cts)
				return m_resourceLoader;

			//-- Jar has changed!! Create a new loader && update;
			URL url = m_src.toURL();

			m_resourceLoader = new URLClassLoader(new URL[]{url}, null);
			m_resourceLoaderTS = cts;
			return m_resourceLoader;
		} catch(Exception x) {
			return null;
		}
	}

	@Override
	public String toString() {
		return "[ClasspathJar: " + m_src.toString() + "]";
	}
}
