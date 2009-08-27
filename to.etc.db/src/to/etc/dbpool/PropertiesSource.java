package to.etc.dbpool;

import java.io.*;
import java.util.*;

public class PropertiesSource extends PoolConfigSource {
	private Properties m_prop;

	private Properties m_backup;

	public PropertiesSource(File src, File back) {
		super(src, back);
	}

	private synchronized void init() throws Exception {
		if(m_prop != null)
			return;
		if(!getSrc().exists())
			throw new IllegalArgumentException("The pool manager config file " + getSrc() + " does not exist.");

		//-- Load properties and backup properties.
		InputStream is = null;
		try {
			is = new FileInputStream(getSrc());
			m_prop = new Properties();
			m_prop.load(is);
			is.close();
			is = null;

			//-- Load any backup file.
			if(getBackupSrc() != null && getBackupSrc().exists()) {
				is = new FileInputStream(getBackupSrc());
				m_backup = new Properties();
				m_backup.load(is);
			}
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	@Override
	public String getProperty(String section, String name) throws Exception {
		init();
		String key = section + "." + name;
		if(m_backup != null) {
			String res = m_backup.getProperty(key);
			if(res != null)
				return res;
		}
		return m_prop.getProperty(key);
	}
}
