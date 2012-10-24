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

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.js.*;
import to.etc.domui.util.resources.*;

/**
 * Very simple theme engine which uses a theme name defined as themedir/icon/color.
 * <ul>
 *	<li>The stylesheet must be called style.theme.css, and must reside in themes/[stylename].</li>
 *	<li>The completed properties for this are formed by reading the following property files in order:
 *		<ul>
 *			<li>themes/[color].color.js</li>
 *			<li>themes/[icon].icon.js</li>
 *			<li>themes/[style]/style.props.js</li>
 *		</ul>
 *		The resulting property file is then used as the base context for all other theme operations.</li>
 * </ul>
 *
 *
 * The themedir
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
	public @Nonnull ITheme getTheme(@Nonnull DomApplication da, @Nonnull String themeName) throws Exception {
		SimpleThemeFactory stf = new SimpleThemeFactory(da, themeName);
		try {
			return stf.createTheme();
		} finally {
			try {
				stf.close();
			} catch(Exception x) {}
		}
	}

	private RhinoExecutor executor() throws Exception {
		if(m_executor == null) {
			m_executor = RhinoExecutorFactory.getInstance().createExecutor();
			m_executor.eval(Object.class, "icon = new Object();", "internal");
			m_executor.put("themeName", m_themeName);
			m_executor.put("themePath", "$THEME/" + m_themeName + "/");
			m_application.augmentThemeMap(m_executor);
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
		executor().eval(Object.class, "icon = new Object();", "internal");

		loadProperties("$themes/" + m_colorName + ".color.js", rdl);
		loadProperties("$themes/" + m_iconName + ".icons.js", rdl);
		loadProperties("$themes/" + m_styleName + "/style.props.js", rdl);

		List<String> searchpath = new ArrayList<String>(3);
		searchpath.add("$themes/" + m_iconName + "-icons");			// [iconname]-icons
		searchpath.add("$themes/" + m_colorName + "-colors");		// [iconname]-icons
		searchpath.add("$themes/" + m_styleName);					// [style]

		return new SimpleTheme(m_application, m_styleName, executor(), rdl.createDependencies(), searchpath);
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
			executor().eval(Object.class, isr, rurl);
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	static protected IResourceRef findRef(@Nonnull DomApplication da, @Nonnull String rurl, @Nonnull IResourceDependencyList rdl) throws Exception {
		try {
			IResourceRef ires = da.getResource(rurl, rdl); // Get the source file, abort if not found
			return ires;
		} catch(ThingyNotFoundException x) {}
		return null;
	}
}
