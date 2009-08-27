package to.etc.dbpool;

import java.io.*;

/**
 * Some thingy that can retrieve pool parameters.
 * 
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 16, 2007
 */
abstract public class PoolConfigSource {
	private File m_src;

	private File m_backupSrc;

	PoolConfigSource() {}

	public PoolConfigSource(File src, File back) {
		m_src = src;
		m_backupSrc = back;
	}

	public File getBackupSrc() {
		return m_backupSrc;
	}

	public File getSrc() {
		return m_src;
	}

	abstract public String getProperty(String section, String name) throws Exception;

	public boolean getBool(String sec, String name, boolean def) throws Exception {
		String v = getProperty(sec, name);
		if(v == null)
			return def;
		v = v.trim().toLowerCase();
		if(v.length() == 0)
			return def;
		return v.startsWith("1") || v.startsWith("y") || v.startsWith("t");
	}

	public int getInt(String sec, String name, int def) throws Exception {
		String v = getProperty(sec, name);
		if(v == null)
			return def;
		v = v.trim();
		if(v.length() == 0)
			return def;
		try {
			return Integer.parseInt(v);
		} catch(Exception x) {
			throw new IllegalArgumentException("The parameter " + sec + "." + name + " cannot be converted to integer (value=" + v + ")");
		}
	}

	@Override
	public String toString() {
		if(m_backupSrc != null)
			return m_src + " (" + m_backupSrc + ")";
		if(m_src == null)
			return "(parameters)";
		return m_src.toString();
	}

	static PoolConfigSource create(File f) {
		String name = f.getName().toLowerCase();
		if(name.endsWith(".xml")) {
			return new XmlSource(f, new File(f.toString() + ".local"));
		}
		return new PropertiesSource(f, new File(f.toString() + ".local"));
	}
}
