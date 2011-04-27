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

import to.etc.domui.server.*;
import to.etc.domui.util.js.*;
import to.etc.domui.util.resources.*;

/**
 * This contains the results for a fragmented theme.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 10, 2011
 */
public class FragmentedThemeStore implements ITheme {
	final private DomApplication m_app;

	private byte[] m_styleSheetBytes;

	final private List<String> m_themeInheritanceStack;

	final private ResourceDependencies m_dependencies;

	/** Maps icon names to their real name in whatever resource they are. */
	final private Map<String, String> m_iconMap = new HashMap<String, String>();

	final private IScriptScope m_propertyScope;

	public FragmentedThemeStore(DomApplication app, byte[] tbytes, IScriptScope themeProperties, List<String> themeInheritanceStack, ResourceDependencies deps) {
		m_app = app;
		m_propertyScope = themeProperties;
		m_themeInheritanceStack = themeInheritanceStack;
		m_dependencies = deps;
		m_styleSheetBytes = tbytes;
	}

	private byte[] getStyleSheetBytes() {
		return m_styleSheetBytes;
	}

	public ResourceDependencies getDependencies() {
		return m_dependencies;
	}

	@Override
	public IScriptScope getPropertyScope() {
		return m_propertyScope;
	}

	/**
	 * Return a resource reference within this theme.
	 * @see to.etc.domui.themes.ITheme#getThemeResource(java.lang.String, to.etc.domui.util.resources.IResourceDependencyList)
	 */
	@Override
	public IResourceRef getThemeResource(String name, IResourceDependencyList rdl) throws Exception {
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

	//	/**
	//	 * Locate the specified theme resource from the theme, and return the URL
	//	 * needed to get it. Any THEME/ things have already been stripped; the name
	//	 * passed is something simple like "btnOkay.png".
	//	 *
	//	 * @param icon
	//	 * @return
	//	 */
	//	private String getIconURL(@Nonnull String icon) throws Exception {
	//		synchronized(m_iconMap) {
	//			String res = m_iconMap.get(icon);
	//			if(res != null)
	//				return res;
	//
	//			res = findIconURLUncached(icon);
	//			if(res == null)
	//				throw new ThingyNotFoundException(icon + ": image not found");
	//			m_iconMap.put(icon, res);
	//			return res;
	//		}
	//	}

	//	@Nullable
	//	private String getThemePath(String fileName) throws Exception {
	//		for(int i = m_themeInheritanceStack.size(); --i >= 0;) {
	//			String sitem = m_themeInheritanceStack.get(i);
	//			String real = "$" + sitem + "/" + fileName;
	//			if(m_app.hasApplicationResource(real))
	//				return real;
	//		}
	//		return null;
	//	}
}
