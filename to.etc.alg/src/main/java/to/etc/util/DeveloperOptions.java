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

import javax.annotation.*;

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

	@Nullable
	static private Properties			m_p;

	@Nonnull
	static private Set<String> m_warnedSet = new HashSet<>();

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
			String prevent = System.getProperty("developer.properties");
			if ("false".equalsIgnoreCase(prevent)){
				return;
			}
			File f = new File(new File(s), ".developer.properties");
			if(!f.exists())
				return;

			InputStream is = null;
			try {
				is = new FileInputStream(f);
				Properties p = new Properties();
				p.load(is);
				m_p = p;
				System.out.println("WARNING: " + f + " used for DEVELOPMENT-TIME OPTIONS!!");
				m_isdeveloper = DeveloperOptions.getBool("developer.workstation", true);
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
	/**
	 * Returns the developer option specified as a string. Return null if the option is not present.
	 */
	@Nullable
	static synchronized public String getString(@Nonnull final String name) {
		return internalGetString(name);
	}

	/**
	 * Returns the developer option specified by name as a string. If the option is not present in the
	 * file return the default value.
	 *
	 * @param name
	 * @param def
	 * @return
	 */
	@Nonnull
	static synchronized public String getString(@Nonnull final String name, @Nonnull final String def) {
		String s = internalGetString(name);
		return s == null ? def : s;
	}

	/**
	 * Returns the developer option specified by name as a boolean. If the option is not present in the
	 * file return the default value.
	 *
	 * @param name
	 * @param def
	 * @return
	 */
	static synchronized public boolean getBool(@Nonnull final String name, final boolean def) {
		String s = internalGetString(name);
		if(null == s)
			return def;
		s = s.toLowerCase();
		return s.startsWith("t") || s.startsWith("y");
	}

	/**
	 * Returns the developer option specified by name as an integer. If the option is not present in the
	 * file return the default value.
	 *
	 * @param name
	 * @param def
	 * @return
	 */
	static synchronized public int getInt(@Nonnull final String name, final int def) {
		String s = internalGetString(name);
		if(null == s)
			return def;
		return Integer.decode(s.trim());
	}

	@Nullable
	static synchronized private String internalGetString(@Nonnull final String name) {
		Properties p = m_p;
		if(null == p)
			return null;
		String value = p.getProperty(name);
		if(null == value)
			return null;

		if(m_warnedSet.add(name))
			System.out.println("WARNING: Development-time option " + name + " changed to " + value);
		return value;
	}

	static public boolean isBackGroundDisabled() {
		return ! getBool("domui.background", true);				// set domui.background to false to disable background tasks
	}
}
