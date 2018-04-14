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
package to.etc.domui.themes.fragmented;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.DomApplication;
import to.etc.domui.themes.ITheme;
import to.etc.domui.themes.StyleException;
import to.etc.domui.themes.ThemeResourceFactory;
import to.etc.domui.util.js.IScriptScope;
import to.etc.domui.util.resources.ByteArrayResourceRef;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.domui.util.resources.ResourceDependencies;

import java.io.InputStream;
import java.util.List;

/**
 * This contains the results for a fragmented theme.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 10, 2011
 */
public class FragmentedThemeStore implements ITheme {
	final private DomApplication m_app;

	final private byte[] m_styleSheetBytes;

	final private List<String> m_themeInheritanceStack;

	final private ResourceDependencies m_dependencies;

	final private IScriptScope m_propertyScope;

	private final String m_themeName;

	public FragmentedThemeStore(DomApplication app, String themeName, byte[] tbytes, IScriptScope themeProperties, List<String> themeInheritanceStack, ResourceDependencies deps) {
		m_app = app;
		m_propertyScope = themeProperties;
		m_themeInheritanceStack = themeInheritanceStack;
		m_dependencies = deps;
		m_styleSheetBytes = tbytes;
		m_themeName = themeName;
	}

	@NonNull @Override public String getThemeName() {
		return m_themeName;
	}

	@NonNull @Override public String getStyleSheetName() {
		return ThemeResourceFactory.PREFIX + m_themeName + "/style.theme.css";
	}

	private byte[] getStyleSheetBytes() {
		return m_styleSheetBytes;
	}

	@Override
	@NonNull
	public ResourceDependencies getDependencies() {
		return m_dependencies;
	}

	@Override
	public @NonNull IScriptScope getPropertyScope() {
		return m_propertyScope;
	}

	@Override
	public @NonNull String translateResourceName(@NonNull String name) {
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

	/**
	 * Return a resource reference within this theme.
	 * @see to.etc.domui.themes.ITheme#getThemeResource(java.lang.String, to.etc.domui.util.resources.IResourceDependencyList)
	 */
	@Override
	@NonNull
	public IResourceRef getThemeResource(@NonNull String name, @NonNull IResourceDependencyList rdl) throws Exception {
		//-- Are we looking for the root stylesheet? We have that as the expanded fragments...
		if("style.theme.css".equals(name)) {
			byte[] data = getStyleSheetBytes();
			return new ByteArrayResourceRef(data, "style.theme.css", getDependencies());
		}

		//-- "Normal" resource.
		for(int i = m_themeInheritanceStack.size(); --i >= 0;) {
			String sitem = m_themeInheritanceStack.get(i);
			String real = "$" + sitem + "/" + name;
			IResourceRef rr = m_app.getResource(real, rdl);
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
	}
}
