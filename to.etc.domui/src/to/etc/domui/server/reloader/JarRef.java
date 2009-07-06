package to.etc.domui.server.reloader;

import java.io.*;
import java.util.zip.*;

public class JarRef implements IResourceRef {
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
