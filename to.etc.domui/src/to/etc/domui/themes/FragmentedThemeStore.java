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

import java.util.*;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.resources.*;

/**
 *
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 10, 2011
 */
public class FragmentedThemeStore implements ITheme {
	final private DomApplication m_app;

	private byte[] m_styleSheetBytes;

	final private Map<String, Object> m_themeProperties;

	final private List<String> m_themeInheritanceStack;

	final private ResourceDependencies m_dependencies;

	/** Maps icon names to their real name in whatever resource they are. */
	final private Map<String, String> m_iconMap = new HashMap<String, String>();

	public FragmentedThemeStore(DomApplication app, byte[] tbytes, Map<String, Object> themeProperties, List<String> themeInheritanceStack, ResourceDependencies deps) {
		m_app = app;
		m_themeProperties = themeProperties;
		m_themeInheritanceStack = themeInheritanceStack;
		m_dependencies = deps;
		m_styleSheetBytes = tbytes;
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

		String iurl = theme.getIconURL(real);
		if(iurl.startsWith("$"))
			iurl = iurl.substring(1);
		return da.getAppFileOrResource(iurl);


		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getStyleSheetBytes() {
		return m_styleSheetBytes;
	}

	public ResourceDependencies getDependencies() {
		return m_dependencies;
	}

	public Map<String, Object> getThemeProperties() {
		return m_themeProperties;
	}

	/**
	 * Locate the specified theme resource from the theme, and return the URL
	 * needed to get it. Any THEME/ things have already been stripped; the name
	 * passed is something simple like "btnOkay.png".
	 *
	 * @param icon
	 * @return
	 */
	private String getIconURL(@Nonnull String icon) throws Exception {
		synchronized(m_iconMap) {
			String res = m_iconMap.get(icon);
			if(res != null)
				return res;

			res = findIconURLUncached(icon);
			if(res == null)
				throw new ThingyNotFoundException(icon + ": image not found");
			m_iconMap.put(icon, res);
			return res;
		}
	}

	/**
	 * Uncached search for an iconized image. If the thing is not found return null, else
	 * return the actual path for the icon.
	 *
	 * @param icon
	 * @return
	 * @throws Exception
	 */
	private String findIconURLUncached(String icon) throws Exception {
		//-- Strip entire suffix (everything from 1st dot in name).
		int pos = icon.indexOf('.');
		String name = pos == -1 ? icon : icon.substring(0, pos); // Get name ex suffix;

		//-- Replace silly characters with '_'
		name = name.replace('-', '_').replace(' ', '_');
		String real = (String) m_themeProperties.get(name);
		if(null != real)
			return real;

		//-- Try to locate in the theme's inheritance stack.
		return getThemePath(icon);
	}

	@Nullable
	private String getThemePath(String path) throws Exception {
		for(int i = m_themeInheritanceStack.size(); --i >= 0;) {
			String sitem = m_themeInheritanceStack.get(i);
			String real = "$" + sitem + "/" + path;
			if(m_app.hasApplicationResource(real))
				return real;
		}
		return null;
	}
}
