package to.etc.domui.server.reloader;

import java.io.File;

final class FileRef implements ResourceRef {
	private File		m_src;

	public FileRef(File src) {
		m_src = src;
	}
	public long lastModified() {
		try {
			if(! m_src.exists())
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