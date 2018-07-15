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
