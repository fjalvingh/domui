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

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.util.js.IScriptScope;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.domui.util.resources.ResourceDependencies;

/**
 * A theme.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 12, 2011
 */
public interface ITheme {
	/** The theme name: the part of the URL inside all theme resources that identifies the theme to take the resource from */
	@NonNull String getThemeName();

	/**
	 * The dependencies for this theme instance. This will be used by the engine to check
	 * if this instance needs to be reloaded because it's source files have changed in
	 * development mode.
	 * @return
	 */
	@NonNull
	ResourceDependencies getDependencies();

	/**
	 *
	 * @param name
	 * @param rdl
	 * @return
	 * @throws Exception
	 */
	@NonNull
	IResourceRef getThemeResource(@NonNull String name, @NonNull IResourceDependencyList rdl) throws Exception;

	@NonNull
	IScriptScope getPropertyScope();

	@NonNull
	String translateResourceName(@NonNull String name);

	/**
	 * Returns the name for this theme's stylesheet.
	 * @return
	 */
	@NonNull
	String getStyleSheetName() throws Exception;

}

