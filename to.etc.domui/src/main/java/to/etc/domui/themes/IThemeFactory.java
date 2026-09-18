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
import to.etc.domui.server.DomApplication;

/**
 * The application's theme. Exactly one of these is in use; it is set at initialization
 * time with {@link DomApplication#setThemeFactory(IThemeFactory)} and cannot change
 * afterwards.
 *
 * <p>The single thing that can differ per user session is the {@link IThemeVariant}, so
 * a factory creates one {@link ITheme} per variant.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 11, 2011
 */
public interface IThemeFactory {
	/**
	 * Create the theme for the variant passed. The result is cached by the application, so
	 * the factory should not do caching itself.
	 */
	@NonNull
	ITheme getTheme(@NonNull DomApplication da, @NonNull IThemeVariant variant) throws Exception;

	/**
	 * The variant used for sessions that did not select one of their own.
	 */
	@NonNull
	default IThemeVariant getDefaultVariant() {
		return DefaultThemeVariant.INSTANCE;
	}
}
