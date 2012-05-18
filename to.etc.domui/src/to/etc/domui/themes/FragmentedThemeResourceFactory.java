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

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.util.resources.*;

/**
 * This provides resources for the current theme: it handles all names starting with $currentTheme/ and
 * resolves them inside the current theme space.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 15, 2011
 */
public class FragmentedThemeResourceFactory implements IResourceFactory {
	static public final String CURRENT = "$currentTheme/";

	@Override
	public int accept(@Nonnull String name) {
		return name.startsWith(CURRENT) ? 30 : -1;
	}

	@Override
	public @Nonnull IResourceRef getResource(@Nonnull DomApplication da, @Nonnull String name, @Nullable IResourceDependencyList rdl) throws Exception {
		String real = name.substring(CURRENT.length());
		ITheme theme = da.getTheme(rdl);

		//-- If this is the virtual "style.theme.css" file we need to return a special thingy
		if("style.theme.css".equals(real)) {
			byte[] data = ((FragmentedThemeStore) theme).getStyleSheetBytes();
			return new ByteArrayResourceRef(data, "style.theme.css", theme.getDependencies());
		}

		String iurl = theme.getIconURL(real);
		if(iurl.startsWith("$"))
			iurl = iurl.substring(1);
		return da.getAppFileOrResource(iurl);
	}
}
