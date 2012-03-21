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

import org.mozilla.javascript.*;

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

	final private byte[] m_styleSheetBytes;

	final private List<String> m_themeInheritanceStack;

	final private ResourceDependencies m_dependencies;

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

	@Override
	public String translateResourceName(String name) {
		//-- Get the 'icon' instance
		Object iobj = getPropertyScope().getValue("icon");
		if(null == iobj)
			return name;
		if(iobj == UniqueTag.NOT_FOUND)
			return name;
		if(!(iobj instanceof IScriptScope))
			throw new StyleException("The 'icon' style variable is not a Javascript map but an " + iobj.getClass() + ": " + iobj);

		//-- Retrieve a value here,
		iobj = ((IScriptScope) iobj).getValue(name); // Is this icon name mapped to something else?
		if(null == iobj)
			return name;
		if(!(iobj instanceof String))
			throw new StyleException("The 'icon' mapping for '" + name + "' results in a non-string object: " + iobj);

		return (String) iobj;
	}


	/**
	 * Return a resource reference within this theme.
	 * @see to.etc.domui.themes.ITheme#getThemeResource(java.lang.String, to.etc.domui.util.resources.IResourceDependencyList)
	 */
	@Override
	@Nonnull
	public IResourceRef getThemeResource(@Nonnull String name, @Nonnull IResourceDependencyList rdl) throws Exception {
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
