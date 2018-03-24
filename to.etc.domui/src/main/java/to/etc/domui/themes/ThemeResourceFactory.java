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

import to.etc.domui.server.DomApplication;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.IResourceFactory;
import to.etc.domui.util.resources.IResourceRef;

import javax.annotation.Nonnull;

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
final public class ThemeResourceFactory implements IResourceFactory {
	static public final String PREFIX = "$THEME/";

	@Override
	public int accept(@Nonnull String name) {
		return name.startsWith(PREFIX) ? 30 : -1;
	}

	/**
	 * A theme resource URL has the format: $THEME/themeName/aaa/bb/ccc. This call
	 * returns the theme from that url in [0] and the rest string in [1].
	 * @param name
	 * @return
	 */
	static public final String[] splitThemeResourceURL(String name) {
		if(!name.startsWith(PREFIX))
			throw new IllegalArgumentException("Not a theme RURL: '" + name + "'");
		String real = name.substring(PREFIX.length()); // Strip $THEME/
		int pos = real.indexOf('/');
		if(pos == -1)
			throw new ThingyNotFoundException("Bad theme URL (missing current theme): " + name);
		String themename = real.substring(0, pos);
		String filename = real.substring(pos + 1);
		if(themename.length() == 0)
			throw new ThingyNotFoundException("Bad theme resource-URL (empty current theme): " + name);
		return new String[]{themename, filename};
	}

	/**
	 * Get a resource from the proper theme.
	 */
	@Override
	@Nonnull
	public IResourceRef getResource(@Nonnull DomApplication da, @Nonnull String themeResourceURL, @Nonnull IResourceDependencyList rdl) throws Exception {
		String[] spl = splitThemeResourceURL(themeResourceURL);
		String themename = spl[0];
		String filename = spl[1];

		//-- Ask the theme manager for the theme represented by this RURL.
		ITheme theme = da.getTheme(themename, rdl);
		if(null == theme)
			throw new IllegalStateException("Unexpected null from theme factory");
		IResourceRef rr = theme.getThemeResource(filename, rdl);
		if(null == rr || !rr.exists()) // FIXME Questionable: just return rr?
			throw new ThingyNotFoundException("The theme resource '" + themeResourceURL + "' cannot be found");
		return rr;
	}
}
