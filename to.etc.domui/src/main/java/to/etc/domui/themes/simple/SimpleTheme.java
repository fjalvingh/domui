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
package to.etc.domui.themes.simple;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.DomApplication;
import to.etc.domui.themes.ITheme;
import to.etc.domui.themes.StyleException;
import to.etc.domui.themes.ThemeResourceFactory;
import to.etc.domui.util.js.IScriptScope;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.domui.util.resources.ResourceDependencies;

import java.io.InputStream;
import java.util.List;

/**
 * The result of a "simple" theme. It only contains the properties map for colors
 * and icons, and a directory for theme resources.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 27, 2011
 */
//@Immutable
public final class SimpleTheme implements ITheme {
	@NonNull
	final private DomApplication m_da;

	private final String m_themeName;

	@NonNull
	final private String m_styleName;

	@NonNull
	final private ResourceDependencies m_rd;

	@NonNull
	final private IScriptScope m_propertyScope;

	@NonNull
	final private List<String> m_searchpath;

	public SimpleTheme(@NonNull DomApplication da, String themeName, @NonNull String styleName, @NonNull IScriptScope themeProperties, @NonNull ResourceDependencies rd,
		@NonNull List<String> searchpath) {
		m_da = da;
		m_themeName = themeName;
		m_styleName = styleName;
		m_propertyScope = themeProperties;
		m_rd = rd;
		m_searchpath = searchpath;
	}

	@NonNull @Override public String getThemeName() {
		return m_themeName;
	}

	@NonNull @Override public String getStyleSheetName() {
		return ThemeResourceFactory.PREFIX + m_themeName + "/" + "/style.theme.css";
	}

	@NonNull
	@Override
	public ResourceDependencies getDependencies() {
		return m_rd;
	}

	@Override
	@NonNull
	public IScriptScope getPropertyScope() {
		return m_propertyScope;
	}

	@NonNull
	@Override
	public IResourceRef getThemeResource(@NonNull String name, @NonNull IResourceDependencyList rdl) throws Exception {
		//-- "Normal" resource.
		for(String sitem : m_searchpath) {
			String real = sitem + "/" + name;
			IResourceRef rr = m_da.getResource(real, rdl);
			if(rr != null && rr.exists())
				return rr;
		}
		return new IResourceRef() {
			@Override
			public InputStream getInputStream() throws Exception {
				return null;
			}

			@Override
			public boolean exists() {
				return false;
			}
		};

		//		return m_da.getResource("$themes/" + m_styleName + "/" + name, rdl);
	}

	@Override
	@NonNull
	public String translateResourceName(@NonNull String name) {
		try {
			IScriptScope ss = getPropertyScope().getValue(IScriptScope.class, "icon");
			if(null == ss)
				return name;

			//-- Retrieve a value here,
			String val = ss.getValue(String.class, name);		// Is this icon name mapped to something else?
			return val == null ? name : val;
		} catch(Exception x) {
			throw new StyleException("The 'icon' mapping for '" + name + "' results in an exception: " + name);
		}
	}
}
