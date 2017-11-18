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
package to.etc.domui.login;

import javax.annotation.*;

/**
 * Represents a logged-in user. This base interface only knows data that must be known about
 * any logged-in user. Extras can be obtained if you know the implementation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 14, 2009
 */
public interface IUser {
	/**
	 * The user's login ID.
	 * @return
	 */
	String getLoginID();

	/**
	 * Return a display name for the user; this usually is the full formal name.
	 * @return
	 */
	String getDisplayName();

	boolean hasRight(@Nonnull String r);

	/**
	 * EXPERIMENTAL INTERFACE, DO NOT USE Determines if right r is enabled for the specified data element. The implementation
	 * will decide how to map this. The dataElement can be a "primary element" meaning something that rights are explicitly
	 * assigned on, or it can be something that can be linked to such a "priomary element". In the latter case it is the
	 * implementation's responsibility to obtain the primary element from the data passed and apply the rights check there.
	 * If data-bound permissions are not implemented this MUST return getRight(r).
	 *
	 * @param r
	 * @param dataElement
	 * @return
	 */
	<T> boolean hasRight(@Nonnull String r, @Nullable T dataElement);

	default boolean canImpersonate() {
		return false;
	}
}
