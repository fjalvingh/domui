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

import to.etc.domui.server.IRequestContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checks the user's access and if granted returns a IUser for the credentials
 * passed. If not correct this returns null. The IUser returned will be cached
 * in the session.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 14, 2009
 */
public interface ILoginAuthenticator {
	IUser authenticateUser(final String userid, final String credentials) throws Exception;

	IUser authenticateByCookie(String uid, long ts, String string) throws Exception;

	String calcCookieHash(String userid, long ts) throws Exception;

	default @Nullable IUser authenticateByRequest(@Nonnull IRequestContext rx) throws Exception {
		return null;
	}
}
