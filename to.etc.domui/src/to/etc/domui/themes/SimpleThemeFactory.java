/*
 * DomUI Java User Interface library
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
package to.etc.domui.themes;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.script.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.resources.*;

public class SimpleThemeFactory implements IThemeFactory {
	static public final SimpleThemeFactory INSTANCE = new SimpleThemeFactory();

	private DomApplication m_application;

	private String m_themeName;

	private String m_styleName;

	private String m_iconName;

	private String m_colorName;

	/**
	 * Factory constructor.
	 */
	private SimpleThemeFactory() {
	}

	private SimpleThemeFactory(DomApplication da, String themeName) {
		m_application = da;
		m_themeName = themeName;
	}

	@Override
	public ITheme getTheme(DomApplication da, String themeName) throws Exception {
		SimpleThemeFactory stf = new SimpleThemeFactory(da, themeName);
		try {
			return stf.createTheme();
		} finally {
			try {
				stf.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Workhorse.
	 * @return
	 */
	private SimpleTheme createTheme() throws Exception {
		//-- Split theme name into theme/icons/color
		String[] ar = m_themeName.split("\\/");
		if(ar.length != 3)
			throw new StyleException("The theme name '" + m_themeName + "' is invalid for the factory SimpleThemeFactory: expecting theme/icon/color");
		m_styleName = ar[0];
		m_iconName = ar[1];
		m_colorName = ar[2];

		ResourceDependencyList rdl = new ResourceDependencyList();
		Map<String, Object> map = new HashMap<String, Object>();
		loadProperties(map, m_application, "$themes/" + m_colorName + ".color.js", rdl);
		loadProperties(map, m_application, "$icons/" + m_iconName + "/icon.props.js", rdl);
		loadProperties(map, m_application, "$themes/" + m_styleName + "/style.props.js", rdl);
		return new SimpleTheme(m_application, m_styleName, map, rdl.createDependencies());
	}

	/**
	 * Discard all resources after theme creation.
	 */
	private void close() {
	}

	static public void loadProperties(Map<String, Object> map, DomApplication da, String rurl, ResourceDependencyList rdl) throws Exception {
		IResourceRef ires = findRef(da, rurl, rdl);
		if(null == ires || !ires.exists())
			return;
		InputStream is = ires.getInputStream();
		ScriptEngine se;
		try {
			//-- Try to load the script as a Javascript file and execute it.
			ScriptEngineManager m = new ScriptEngineManager();
			se = m.getEngineByName("js");
			Bindings b = se.createBindings();

			//-- Add all current map values
			for(Map.Entry<String, Object> e : map.entrySet()) {
				b.put(e.getKey(), e.getValue());
			}

			//-- Add helper classes FIXME TODO

			//-- Execute Javascript;
			Reader r = new InputStreamReader(is, "utf-8");
			se.eval(r);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}

		//-- Ok: the binding now contains stuff to add/replace to the map
		for(Map.Entry<String, Object> e : se.getBindings(ScriptContext.ENGINE_SCOPE).entrySet()) {
			String name = e.getKey();
			if("context".equals(name))
				continue;
			Object v = e.getValue();
			if(v != null) {
				String cn = v.getClass().getName();
				if(cn.startsWith("sun."))
					continue;
				map.put(name, v);
			}
		}
	}
	//	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "OS_OPEN_STREAM", justification = "Stream is closed closing wrapped instance.")
	//	public Map<String, Object> readProperties(@Nonnull DomApplication da, @Nonnull IResourceDependencyList rdl) throws Exception {
	//		String rurl = "$themes/" + m_styleName + "/style.properties";
	//		IResourceRef ires = findRef(da, rurl, rdl);
	//		if(null == ires || !ires.exists())
	//			return new HashMap<String, Object>();
	//
	//		//-- Read the thingy as a property file.
	//		Properties p = new Properties();
	//		InputStream is = ires.getInputStream();
	//		try {
	//			p.load(new InputStreamReader(is, "utf-8")); // wrapped "is" is closed, no need to close reader.
	//		} finally {
	//			try {
	//				is.close();
	//			} catch(Exception x) {}
	//		}
	//
	//		//-- Make the output map
	//		Map<String, Object> map = new HashMap<String, Object>();
	//		for(Object key : p.keySet()) {
	//			String k = (String) key;
	//			String val = p.getProperty(k);
	//			map.put(k, val);
	//		}
	//		return map;
	//	}

	static protected IResourceRef findRef(@Nonnull DomApplication da, @Nonnull String rurl, @Nonnull IResourceDependencyList rdl) throws Exception {
		try {
			IResourceRef ires = da.getResource(rurl, rdl); // Get the source file, abort if not found
			return ires;
		} catch(ThingyNotFoundException x) {}
		return null;
	}
}
