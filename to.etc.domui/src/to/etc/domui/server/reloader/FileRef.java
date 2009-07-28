package to.etc.domui.server.reloader;

import java.io.*;

/**
 * Holds a reference to a file entry to access a timestamp.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2009
 */
final class FileRef implements IResourceRef {
	private File m_src;

	public FileRef(File src) {
		m_src = src;
	}

	public long lastModified() {
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
		return m_src.toString();
	}
}
