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

import to.etc.domui.util.resources.*;

/**
 * A theme.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 12, 2011
 */
public interface ITheme {
	/**
	 * The dependencies for this theme instance. This will be used by the engine to check
	 * if this instance needs to be reloaded because it's source files have changed in
	 * development mode.
	 * @return
	 */
	@Nonnull
	ResourceDependencies getDependencies();

	/**
	 *
	 * @param name
	 * @param rdl
	 * @return
	 * @throws Exception
	 */
	@Nullable
	IResourceRef getThemeResource(String name, IResourceDependencyList rdl) throws Exception;


	/**
	 * Get a resource from the theme's inheritance path.
	 * @param path
	 * @return
	 */
	String getThemePath(String path) throws Exception;

	/**
	 * Return the read-only properties for a theme.
	 * @return
	 */
	@Nonnull
	Map<String, Object> getThemeProperties();

	//	/**
	//	 * Return the primary css stylesheet template. This gets expanded for every browser
	//	 * type separately.
	//	 * @return
	//	 */
	//	JSTemplate getStylesheetTemplate();

	/**
	 * Find the specified icon in the theme, and return the proper RURL for it.
	 * @param icon
	 * @return
	 */
	@Nullable
	String getIconURL(@Nonnull String icon) throws Exception;
}

