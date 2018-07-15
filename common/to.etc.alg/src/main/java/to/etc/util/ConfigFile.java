/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.util;

import java.io.*;
import java.util.*;

/**
 * Encapsulates a config file. The config file looks like a properties file, but
 * it's keys are case-independent. The config file can be set to be reread
 * every n seconds. It can also write the file.
 * The file format is the same as the property file format, and allows comments
 * before items.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@Deprecated
public class ConfigFile implements ConfigSource {
	/** The thing containing the keys for the file (lowercased). */
	private List<PropData>	m_v;

	/** The hashtable. */
	private Map<String, PropData>	m_ht;

	/** The last time the file was read, */
	private long		m_read_ts;

	/** The reread interval in seconds; when 0 it is not reread. */
	private int			m_reread_s;

	/** The full path where the file was found. */
	private File		m_path;

	/** Used as a "backup" file for parameters if not found in the current file */
	private ConfigFile	m_default_cf;

	static private class SubSource implements ConfigSource {
		private String			m_key;

		private ConfigSource	m_cs;

		public SubSource(ConfigSource cs, String key) {
			m_key = key;
			m_cs = cs;
		}

		public String getOption(String key) throws Exception {
			return m_cs.getOption(m_key + "." + key);
		}

		public Object getSourceObject() {
			return m_cs.getSourceObject() + " -> " + m_key;
		}

		public ConfigSource getSubSource(String key) throws Exception {
			return new SubSource(this, key);
		}
	}


	public ConfigFile() {
	}

	public ConfigFile(ConfigFile default_cf) {
		m_default_cf = default_cf;
	}

	public ConfigFile(File cf) throws Exception {
		setFile(cf);
	}

	public ConfigFile(File f, File localf) throws Exception {
		if(localf != null && localf.exists()) {
			m_default_cf = new ConfigFile(f);
			setFile(localf);
		} else
			setFile(f);
	}

	/**
	 *	Called to set the file to use. The file MUST exist or it is not set.
	 */
	public synchronized void setFile(File f) throws Exception {
		if(!f.exists() || !f.isFile() || !f.canRead())
			throw new IOException(f + ": not accessible.");
		m_path = f;
		m_read_ts = 0; // Force reread next access.
	}

	/**
	 *	Sets a file and searches the path for it. The classpath is scanned. If
	 *  no extension is set this looks for .properties
	 */
	public void setSearchFile(String fin) throws Exception {
		//-- 1. Check for environment override.
		int pos = FileTool.findFilenameExtension(fin);
		String envname = fin;
		if(pos != -1)
			envname = fin.substring(0, pos);

		String fn = System.getProperty(envname.toUpperCase());
		if(fn != null) // Property exists?
		{
			File f = new File(fn);
			if(f.exists())
				setFile(f);

			throw new Exception("Environment variable " + fin + " points to inaccessible file " + fn);
		}

		String ext = FileTool.getFileExtension(fin); // Does the thing have an extension?
		//		System.out.println(">> fin="+fin+", ext="+ext);
		if(ext.length() == 0)
			fin = fin + ".properties"; // No: add it,

		File f = new File(fin); // Can we access this as a file?
		if(f.exists() && f.canRead()) {
			setFile(f);
			return;
		}

		//-- Walk the path...
		File path = StringTool.findFileOnEnv(fin, "java.class.path");
		if(path == null)
			throw new Exception("\nLoad config: Cannot locate " + fin);
		setFile(path);
	}

	public ConfigSource getSubSource(String key) {
		return new SubSource(this, key);
	}

	public File getFile() {
		return m_path;
	}

	@Override
	public String toString() {
		return m_path == null ? "undefined file" : m_path.toString();
	}


	/**
	 *	Called from all helpers, this reads and -if necessary- rereads the table.
	 */
	private void init() throws Exception {
		/* Current time. */
		long cts = 0;

		synchronized(this) {
			if(m_read_ts != 0) // Data was read?
			{
				if(m_reread_s == 0)
					return; // Yes, and no reread requested
				cts = System.currentTimeMillis();
				if(cts < m_reread_s + m_read_ts)
					return;
			}
		}

		//-- We have to (re) read. Prepare a hashtable,
		PropReader pr = new PropReader();
		pr.readFile(m_path); // Read and decode,

		synchronized(this) {
			m_ht = pr.m_ht;
			m_v = pr.m_v;
			m_read_ts = System.currentTimeMillis();
		}
	}


	/**
	 *	Returns a string property.
	 */
	public String getProp(String name, String defval) throws Exception {
		init();
		synchronized(this) {
			PropData pd = m_ht.get(name);
			if(pd == null) {
				//-- Not found here- try the defaults file
				if(m_default_cf == null)
					return defval; // No defaults file
				return m_default_cf.getProp(name, defval); // Return it's value.
			}
			return pd.m_val;
		}
	}

	/**
	 *	Returns an int property.
	 */
	public int getProp(String name, int defval) throws Exception {
		String sv = getProp(name, null);
		if(sv == null)
			return defval;

		try {
			return Integer.parseInt(sv);
		} catch(Exception ex) {
			throw new Exception(m_path + ": propery " + name + " must be numeric");
		}

	}

	/**
	 *	Sets a string property. This always sets the base, not the defaults file.
	 */
	public void setProp(String key, String val, String comment) throws Exception {
		init();
		synchronized(this) {
			PropData pd = m_ht.get(key.toLowerCase());
			if(pd == null) {
				pd = new PropData();
				pd.m_key = key;
				pd.m_blankLines = 1; // Write 1 blank line before the property
				m_v.add(pd);
				m_ht.put(key.toLowerCase(), pd);
			}
			pd.m_val = val;
			pd.m_cmt = comment;
			PropWriter pw = new PropWriter();
			pw.init(m_path, m_ht, m_v);
			pw.writePropFile();
		}
	}

	/**
	 * Sets a int property.
	 */
	public void setProp(String key, int val, String comment) throws Exception {
		setProp(key, Integer.toString(val), comment);
	}


	public String getOption(String key) throws Exception {
		return getProp(key, null);
	}

	public Object getSourceObject() {
		return m_path;
	}
}

class PropReader {
	private String			m_line;

	private int				m_mode;

	private int				m_blankLines;

	private StringBuffer	m_cmt, m_lrb;

	protected Map<String, PropData>	m_ht	= new HashMap<String, PropData>();

	protected List<PropData>	m_v		= new ArrayList<PropData>();


	/**
	 *	Reads the properties file and inserts it in order into the table.
	 */
	protected void readFile(File f) throws Exception {
		LineNumberReader lr = new LineNumberReader(new BufferedReader(new FileReader(f), 1024));
		try {
			//-- Now start reading line by line or whatever..
			m_cmt = new StringBuffer();
			m_lrb = new StringBuffer();
			m_mode = 0; // scan for line.
			m_blankLines = 0;
			for(;;) {
				m_line = lr.readLine();
				if(m_line == null)
					break;

				if(m_line.trim().compareTo("") == 0) {
					m_blankLines++;
					continue;
				}

				if(m_mode == 0)
					scanStart();
				else
					scanDataLine();
			}
		} finally {
			try {
				lr.close();
			} catch(Exception x) {}
		}
	}

	private void scanStart() throws Exception {
		int ix = 0;
		char c = 0;
		for(;;) {
			if(ix >= m_line.length())
				return;

			c = m_line.charAt(ix);
			if(!Character.isWhitespace(c))
				break;

			ix++;
		}

		if(c == '#') {
			m_cmt.append(m_line);
			m_cmt.append('\n');
			return;
		}
		scanDataLine();
	}


	/**
	 *	Scans a data line. Returns T if the line was a continued one.
	 */
	private boolean scanDataLine() throws Exception {
		//-- Treat as data line. Must contain '='. Does the line end in \?
		int ix = m_line.length();
		char c = 0;
		while(ix > 0) // Skip back whitespace,
		{
			ix--;
			c = m_line.charAt(ix);
			if(!Character.isWhitespace(c))
				break;
		}

		if(c == '\\') {
			//-- Line continuation. Handle it,
			appendLine(m_line.substring(0, ix).trim());
			m_mode = 1;
			return true;
		} else {
			appendLine(m_line.trim());
			addData();
			m_blankLines = 0;
			m_mode = 0;
			return false;
		}
	}

	private void appendLine(String s) {
		//-- Scan the line and handle escape stuff to be compatible with the Properties format
		int len = m_line.length();
		int ix = 0;
		while(ix < len) {
			char c = m_line.charAt(ix++);
			if(c == '\\') {
				if(ix < len)
					c = m_line.charAt(ix++);
			}
			m_lrb.append(c);
		}
	}


	/**
	 *	Adds an actual data item to the vector & the hash.
	 */
	private void addData() throws Exception {
		//-- Ok: strip the data.
		String s = m_lrb.toString();
		int ix = s.indexOf('=');
		if(ix == -1)
			throw new Exception("Illegal format: need key = value..");
		String key = s.substring(0, ix).trim();
		String val = s.substring(ix + 1);

		//-- Add an item.
		PropData pd = new PropData();
		pd.m_cmt = m_cmt.toString();
		m_cmt.setLength(0);
		pd.m_key = key;
		pd.m_val = val;
		pd.m_blankLines = m_blankLines;
		m_lrb.setLength(0);

		m_v.add(pd);
		key = key.toLowerCase();
		PropData pd2 = m_ht.get(key);
		if(pd2 != null)
			throw new Exception("Duplicate key " + key);
		m_ht.put(key, pd);
	}


}

class PropWriter {
	protected Map<String, PropData>	m_ht		= new HashMap<String, PropData>();

	protected List<PropData>	m_v			= new ArrayList<PropData>();

	protected File		m_propFile	= null;

	public void init(File propFile, Map<String, PropData> ht, List<PropData> v) {
		m_propFile = propFile;
		m_ht = ht;
		m_v = v;
	}

	public void writePropFile() throws IOException {
		PrintWriter pw = new PrintWriter(new FileOutputStream(m_propFile));

		for(PropData pd : m_v) {
			if((pd.m_cmt != null) && (pd.m_cmt.length() != 0))
				writeCmtLine(pw, pd.m_cmt);
			pw.println(pd.m_key + "=" + pd.m_val);
			for(int i = 0; i < pd.m_blankLines; i++) {
				pw.println("");
			}
		}
		pw.close();
	}

	private void writeCmtLine(PrintWriter pw, String cmt) {
		StringBuffer sb = new StringBuffer(cmt.length());
		boolean chkhash = true;
		for(int i = 0; i < cmt.length(); i++) {
			char c = cmt.charAt(i);
			if(c == '\n') {
				chkhash = true;
				pw.println(sb.toString());
				sb.setLength(0);
			} else if(c == '#') {
				chkhash = false;
				sb.append(c);
			} else {
				if(chkhash)
					sb.append('#');
				sb.append(c);
				chkhash = false;
			}
		}
		if(sb.length() > 0)
			pw.println(sb.toString());
	}
}

class PropData {
	/// The comment thing before this.
	public String	m_cmt;

	/// The key part,
	public String	m_key;

	/// The value part,
	public String	m_val;

	/// The number of blank lines found before this property.
	public int		m_blankLines;

}
