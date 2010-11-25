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
 * This is a static-only class which should be used to access
 * options that configure a system on a developer's station. This
 * class accesses the file <b>.developer.properties</b> in the
 * home directory of the user starting the program. Properties in
 * that file can be accessed using calls within this module. The
 * file is read only once; retrieved values are cached for speedy
 * performance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 15, 2006
 */
public class DeveloperOptions {
	/** This becomes T if a .developer.properties exists, indicating that this is a developer's workstation */
	static private boolean				m_isdeveloper;

	static private Properties			m_p;

	static private Map<String, Object>	m_map;

	private DeveloperOptions() {
	}

	/**
	 * Called when this class gets instantiated. This tries to
	 * load the properties file.
	 */
	static synchronized private void initialize() {
		String s = System.getProperty("user.home");
		if(s == null)
			System.out.println("DeveloperOptions: user.home is not set??");
		else {
			File f = new File(new File(s), ".developer.properties");
			if(!f.exists())
				return;
			m_isdeveloper = true;

			InputStream is = null;
			try {
				is = new FileInputStream(f);
				Properties p = new Properties();
				p.load(is);
				m_p = p;
				m_map = new HashMap<String, Object>();
				System.out.println("WARNING: " + f + " used for DEVELOPMENT-TIME OPTIONS!!");
			} catch(Exception x) {
				System.out.println("DeveloperOptions: exception while reading " + f + ": " + x);
			} finally {
				try {
					if(is != null)
						is.close();
				} catch(Exception x) {}
			}
		}
	}

	static {
		initialize();
	}

	/**
	 * Returns T if this is a developer's workstation. It is true if the
	 * .developer.properties file exists in the user's home.
	 * @return
	 */
	static public synchronized boolean isDeveloperWorkstation() {
		return m_isdeveloper;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Getting values.										*/
	/*--------------------------------------------------------------*/

	static synchronized public String getString(final String name) {
		String val = internalGetString(name);
		if(val != null)
			System.out.println("WARNING: Development-time option " + name + " (string) changed to " + val);
		return val;
	}

	static synchronized private String internalGetString(final String name) {
		if(m_map == null)
			return null;
		return m_p.getProperty(name);
	}

	static synchronized public String getString(final String name, final String def) {
		if(m_map == null)
			return def;
		String s = (String) m_map.get(name);
		if(s != null)
			return s;
		s = m_p.getProperty(name);
		if(s == null)
			s = def;
		else
			System.out.println("WARNING: Development-time option " + name + " (string) changed to " + s);
		m_map.put(name, s);
		return s;
	}

	static synchronized public boolean getBool(final String name, final boolean def) {
		if(m_map == null)
			return def;
		Boolean b = (Boolean) m_map.get(name);
		if(b != null)
			return b.booleanValue();
		String s = internalGetString(name);
		if(s == null)
			b = Boolean.valueOf(def);
		else {
			s = s.toLowerCase();
			b = Boolean.valueOf(s.startsWith("t") || s.startsWith("y"));
			System.out.println("WARNING: Development-time option " + name + " (boolean) changed to " + b);
		}
		m_map.put(name, b);
		return b.booleanValue();
	}

	static synchronized public int getInt(final String name, final int def) {
		if(m_map == null)
			return def;
		Integer b = (Integer) m_map.get(name);
		if(b != null)
			return b.intValue();
		String s = internalGetString(name);
		if(s == null)
			b = Integer.valueOf(def);
		else {
			b = Integer.valueOf(s);
			System.out.println("WARNING: Development-time option " + name + " (int) changed to " + b);
		}
		m_map.put(name, b);
		return b.intValue();
	}
}
