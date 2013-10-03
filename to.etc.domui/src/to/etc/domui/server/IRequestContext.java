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

import javax.annotation.*;

import to.etc.domui.state.*;

/**
 * Interface representing the context for a request to the server. In a real server request
 * this encapsulates HttpServletRequest and HttpServletResponse, and interfaces with those
 * to get data.
 * The interface should be used as much as possible, since JUnit tests cannot use the
 * real server implementation of it (due to lack of HttpServlet* thingies there).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 29, 2010
 */
public interface IRequestContext extends IExtendedParameterInfo {
	/**
	 * Return the DomApplication instance.
	 * @return
	 */
	@Nonnull
	public DomApplication getApplication();

	/**
	 * Return this-user's AppSession.
	 * @return
	 */
	@Nonnull
	public AppSession getSession();

	/**
	 * Return the WindowSession for this request. The WindowSession represents one of the possible
	 * multiple open browser windows. Since each browser window shares a single HttpSession (sigh)
	 * the WindowSession is used to separate the administration for separate browser windows.
	 * @return
	 */
	@Nonnull
	public WindowSession getWindowSession();

	/**
	 * Return the name extension of the input URL without it's "." character. This extension is defined
	 * as the part after the <i>last</i> dot in the <i>last</i> "directory name fragment" of the name.
	 * For example, for the url "/demo/css/style.css" this call will return "css" (no dot). If the page
	 * has no extension this returns the empty string.
	 * @return
	 */
	@Nonnull
	public String getExtension();

	/**
	 * Return the input path <i>relative to the webapp's root</i>. The webapp context path is <i>not</i>
	 * part of this path, and the path never starts with a slash. So for the webapp "demo" with input
	 * URL "http://localhost/demo/css/style.css" this will return "css/style.css".
	 * @return
	 */
	@Nonnull
	public String getInputPath();

	/**
	 * Returns the value of the "User-Agent" header to determine the browser type.
	 * @return
	 */
	@Nullable
	public String getUserAgent();

	/**
	 * Creates a full path from an application-relative path. So if the root of the application
	 * is "http://localhost/demo/", calling this with "img/button.png" will return the string
	 * "http://localhost/demo/img/button.png".
	 * @param rel
	 * @return
	 */
	@Nonnull
	public String getRelativePath(@Nonnull String rel);

	/**
	 * Returns the writer to use to generate text-based output to this context.
	 * @return
	 * @throws IOException
	 */
	@Nonnull
	public Writer getOutputWriter() throws IOException;

	//	/**
	//	 * Create a full path from a path relative to the current theme. It adds the path
	//	 * to the current theme before the parameter ("$themes/blue" if the current theme
	//	 * is "blue"). The result is an application-relative path. To get a full path that
	//	 * needs to be passed into {@link #getRelativePath(String)} so that the webapp
	//	 * context is added.
	//	 * @param frag
	//	 * @return
	//	 */
	//	@Nonnull
	//	public String getRelativeThemePath(@Nonnull String frag);

	//	public String translateResourceName(String in);
}
