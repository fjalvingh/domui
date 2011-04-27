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

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.resources.*;

/**
 * This handles resource references that start with $THEME; indicating resources
 * to get from a theme. The name to get is split into two parts: the last part is
 * the <i>resource name</i>; it is the part after the last slash. The first part
 * is the <i>theme name</i>, it is everything after $THEME/ and before the last
 * slash. The <i>theme name</i> is provided to the current theme factory to get
 * that theme's {@link ITheme} which knows how to get resources for that theme.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 27, 2011
 */
public class ThemeResourceFactory implements IResourceFactory {
	static public final String PREFIX = "$THEME/";

	@Override
	public int accept(String name) {
		return name.startsWith(PREFIX) ? 30 : -1;
	}

	/**
	 * Get a resource from the proper theme.
	 *
	 * @see to.etc.domui.util.resources.IResourceFactory#getResource(to.etc.domui.server.DomApplication, java.lang.String, to.etc.domui.util.resources.IResourceDependencyList)
	 */
	@Override
	public IResourceRef getResource(DomApplication da, String name, IResourceDependencyList rdl) throws Exception {
		//-- Split into file name and theme name.
		String real = name.substring(PREFIX.length()); // Strip $THEME/
		int pos = real.lastIndexOf('/');
		if(pos == -1)
			throw new ThingyNotFoundException("Bad theme URL (missing current theme): " + name);
		String themename = real.substring(0, pos);
		String filename = real.substring(pos + 1);
		if(themename.length() == 0)
			throw new ThingyNotFoundException("Bad theme URL (empty current theme): " + name);

		//-- Ask the theme manager for the theme represented by this RURL.
		ITheme theme = da.getTheme(themename, rdl);
		if(null == theme)
			throw new IllegalStateException("Unexpected null from theme factory");
		IResourceRef rr = theme.getThemeResource(filename, rdl);
		if(null == rr || !rr.exists()) // FIXME Questionable: just return rr?
			throw new ThingyNotFoundException("The theme resource '" + name + "' cannot be found");
		return rr;
		//
		//
		//		//-- If this is the virtual "style.theme.css" file we need to return a special thingy
		//		if("style.theme.css".equals(real)) {
		//			byte[] data = ((FragmentedThemeStore) theme).getStyleSheetBytes();
		//			return new ByteArrayResourceRef(data, "style.theme.css", theme.getDependencies());
		//		}
		//
		//		String iurl = theme.getIconURL(real);
		//		if(iurl.startsWith("$"))
		//			iurl = iurl.substring(1);
		//		return da.getAppFileOrResource(iurl);
	}
}
