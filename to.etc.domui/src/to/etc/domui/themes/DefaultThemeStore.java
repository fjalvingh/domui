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
import to.etc.template.*;

/**
 *
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 10, 2011
 */
public class DefaultThemeStore {
	final private DomApplication m_app;

	private JSTemplate m_stylesheetSource;

	private Map<String, Object> m_themeProperties;

	private List<String> m_themeInheritanceStack;

	private List<String> m_iconInheritanceStack;

	final private ResourceDependencies m_dependencies;

	/** Maps icon names to their real name in whatever resource they are. */
	final private Map<String, String> m_iconMap = new HashMap<String, String>();

	public DefaultThemeStore(DomApplication app, JSTemplate stylesheetSource, Map<String, Object> themeProperties, List<String> themeInheritanceStack, List<String> iconInheritanceStack, ResourceDependencies deps) {
		m_app = app;
		m_stylesheetSource = stylesheetSource;
		m_themeProperties = themeProperties;
		m_themeInheritanceStack = themeInheritanceStack;
		m_iconInheritanceStack = iconInheritanceStack;
		m_dependencies = deps;
	}

	public ResourceDependencies getDependencies() {
		return m_dependencies;
	}

	public JSTemplate getStylesheetSource() {
		return m_stylesheetSource;
	}

	public Map<String, Object> getThemeProperties() {
		return m_themeProperties;
	}

	/**
	 * Locate the specified icon somewhere in the icon resource.
	 * @param icon
	 * @return
	 */
	@Nonnull
	public String getIconURL(@Nonnull String icon) {
		if(!icon.startsWith("ICON/") || icon.startsWith("THEME/"))
			return icon;

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
	 */
	@Nullable
	protected String findIconURLUncached(String icon) {
		int pos = icon.indexOf('/');
		if(pos == -1)
			return null;
		int epos = icon.lastIndexOf('.'); // Extract suffix
		String name;
		if(epos < pos)
			name = icon.substring(pos + 1);
		else {
			name = icon.substring(pos + 1, epos);
			epos = name.lastIndexOf('.'); // More suffix?
			if(epos != -1)
				name = name.substring(0, epos);
		}

		//-- Replace silly characters with '_'
		name = name.replace('-', '_').replace('.', '_');

		//-- Is the icon defined in some icon properties file?
		String real = (String) m_themeProperties.get(name);
		if(null != real)
			return real;

		//-- Not set by properties. We need to scan to see if one of the icon paths contains the source verbatim, starting at subclass moving to super.
		for(int i = m_iconInheritanceStack.size(); --i >= 0;) {
			String sitem = m_iconInheritanceStack.get(i);
			real = sitem + "/" + icon;
			if(m_app.hasApplicationResource(real))
				return real;
		}

		return null;
	}


}
