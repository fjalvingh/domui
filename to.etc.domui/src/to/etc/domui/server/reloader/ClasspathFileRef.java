package to.etc.domui.server.reloader;

import java.io.*;

import to.etc.domui.util.resources.*;

/**
 * Holds a reference to a file entry to access a timestamp, for resource changed checking only.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2009
 */
final class ClasspathFileRef implements IModifyableResource {
	private File m_src;

	public ClasspathFileRef(File src) {
		m_src = src;
	}

	@Override
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
		return "[ClasspathFile " + m_src.toString() + "]";
	}
}
