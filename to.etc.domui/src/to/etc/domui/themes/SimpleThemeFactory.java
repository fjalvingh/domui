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

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.js.*;
import to.etc.domui.util.resources.*;

/**
 * Very simple theme engine which uses a theme name defined as themedir/icon/color. The themedir
 * must contain all resources; the icon and color names are used to read Javascript property files
 * containing properties for colors and icons to use within theme-related files.
 *
 * <p>All code in here, except the call to getTheme(), is always singlethreaded.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 27, 2011
 */
public class SimpleThemeFactory implements IThemeFactory {
	static public final SimpleThemeFactory INSTANCE = new SimpleThemeFactory();

	private DomApplication m_application;

	private String m_themeName;

	private String m_styleName;

	private String m_iconName;

	private String m_colorName;

	/** A Javascript execution environment. */
	private RhinoExecutor m_executor;

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

	private RhinoExecutor executor() {
		if(m_executor == null) {
			m_executor = RhinoExecutorFactory.getInstance().createExecutor();
		}
		return m_executor;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Creating a theme store instance.					*/
	/*--------------------------------------------------------------*/
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

		/*
		 * Prime the execution environment with objects needed.
		 */
		executor().eval("icon = new Object();");

		loadProperties("$themes/" + m_colorName + ".color.js", rdl);
		loadProperties("$icons/" + m_iconName + "/icon.props.js", rdl);
		loadProperties("$themes/" + m_styleName + "/style.props.js", rdl);
		return new SimpleTheme(m_application, m_styleName, executor(), rdl.createDependencies());
	}

	/**
	 * Discard all resources after theme creation.
	 */
	private void close() {
	}

	/**
	 * Load property files as Javascript files. All of the data is contained in one object.
	 * @param map
	 * @param da
	 * @param rurl
	 * @param rdl
	 * @throws Exception
	 */
	private void loadProperties(String rurl, ResourceDependencyList rdl) throws Exception {
		IResourceRef ires = findRef(m_application, rurl, rdl);
		if(null == ires || !ires.exists())
			return;

		//-- Load the Javascript && execute in the executor's context.
		InputStream is = ires.getInputStream();
		try {
			InputStreamReader isr = new InputStreamReader(is, "utf-8");
			executor().eval(isr, rurl);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
		//
		//		//-- Ok: the binding now contains stuff to add/replace to the map
		//		for(Map.Entry<String, Object> e : se.getBindings(ScriptContext.ENGINE_SCOPE).entrySet()) {
		//			String name = e.getKey();
		//			if("context".equals(name))
		//				continue;
		//			Object v = e.getValue();
		//			if(v != null) {
		//				String cn = v.getClass().getName();
		//				if(cn.startsWith("sun."))
		//					continue;
		//				map.put(name, v);
		//			}
		//		}
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
