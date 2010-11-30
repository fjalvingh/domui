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
package to.etc.domui.server;

import java.io.*;

import to.etc.domui.state.*;


public interface IRequestContext extends IExtendedParameterInfo {
	public DomApplication getApplication();

	public AppSession getSession();

	public WindowSession getWindowSession();

	public String getExtension();

	public String getInputPath();

	public String getUserAgent();

	public String getRemoteUser();

	public String getRelativePath(String rel);

	public Writer getOutputWriter() throws IOException;

	public String getRelativeThemePath(String frag);

	public String translateResourceName(String in);

	/**
	 * This checks if the currently logged on user has the named permission. This permission is
	 * what the J2EE code stupidly calls a "role", which it isn't of course.
	 * This should be very fast as it's called very often.
	 *
	 * @param permissionName
	 * @return
	 */
	public boolean hasPermission(String permissionName);
}
