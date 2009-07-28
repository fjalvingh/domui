package to.etc.domui.server.reloader;

import java.io.*;
import java.util.zip.*;

/**
 * Holds a reference to a file from a .jar, and allows access to the modified timestamp in the jar.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2009
 */
class JarRef implements IResourceRef {
	private File m_jar;

	private String m_rel;

	public JarRef(File jar, String rel) {
		m_jar = jar;
		m_rel = rel;
	}

	public long lastModified() {
		return getTimestamp(m_jar, m_rel);
	}

	static long getTimestamp(File jar, String rel) {
		if(!jar.exists())
			return -1;

		//-- Unzip the jar's entries to find the one we like
		InputStream is = null;
		ZipInputStream zis = null;
		try {
			is = new FileInputStream(jar);
			zis = new ZipInputStream(is);
			ZipEntry ze;
			while(null != (ze = zis.getNextEntry())) { // Walk entry
				if(ze.getName().equals(rel)) {
					return ze.getTime();
				}
			}
		} catch(Exception xz) {} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
			try {
				if(zis != null)
					zis.close();
			} catch(Exception x) {}
		}
		return -1;
	}

	@Override
	public String toString() {
		return m_jar + "!" + m_rel;
	}
}
