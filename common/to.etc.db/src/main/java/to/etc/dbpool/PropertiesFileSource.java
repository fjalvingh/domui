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

import org.eclipse.jdt.annotation.NonNull;

import java.io.*;
import java.util.*;

public class PropertiesFileSource extends PoolConfigSource {
	@NonNull
	private final Properties m_extra;

	private Properties m_prop;

	private Properties m_backup;

	public PropertiesFileSource(File src, File back, @NonNull Properties extra) {
		super(src, back);
		m_extra = extra;
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
		String val = m_prop.getProperty(key);
		if(null != val)
			return val;
		return m_extra.getProperty(key);
	}
}
