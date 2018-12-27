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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.state.AppSession;
import to.etc.domui.state.WindowSession;
import to.etc.domui.themes.ITheme;
import to.etc.domui.themes.IThemeVariant;

import java.io.IOException;
import java.io.Writer;

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
	 */
	@NonNull
	DomApplication getApplication();

	/**
	 * Get the theme for this user or, if nothing is set specifically the application-default theme.
	 * @return
	 */
	@NonNull ITheme getCurrentTheme() throws Exception;

	void setThemeName(String userThemeName);

	@NonNull IThemeVariant getThemeVariant();

	void setThemeVariant(@NonNull IThemeVariant variant);

	/**
	 * Get the generic server request/response object for this context.
	 */
	@NonNull
	IRequestResponse getRequestResponse();

	/**
	 * Return this-user's AppSession.
	 */
	@NonNull
	AppSession getSession();

	@Nullable
	IServerSession getServerSession(boolean create);

	/**
	 * Return the WindowSession for this request. The WindowSession represents one of the possible
	 * multiple open browser windows. Since each browser window shares a single HttpSession (sigh)
	 * the WindowSession is used to separate the administration for separate browser windows.
	 */
	@NonNull
	WindowSession getWindowSession();

	/**
	 * Return the name extension of the input URL without it's "." character. This extension is defined
	 * as the part after the <i>last</i> dot in the <i>last</i> "directory name fragment" of the name.
	 * For example, for the url "/demo/css/style.css" this call will return "css" (no dot). If the page
	 * has no extension this returns the empty string.
	 */
	@NonNull
	String getExtension();

	/**
	 * Return the input path <i>relative to the webapp's root</i>. The webapp context path is <i>not</i>
	 * part of this path, and the path never starts with a slash. So for the webapp "demo" with input
	 * URL "http://localhost/demo/css/style.css" this will return "css/style.css".
	 */
	@Override
	@NonNull
	String getInputPath();

	/**
	 * Returns the URL context string, defined as the part inside the input URL that is before the name in the URL.
	 * Specifically:
	 * <ul>
	 *	<li>This uses the {@link #getInputPath()} URL as the basis, so the path never starts with the webapp context</li>
	 *	<li>If the URL's last part has a suffix then the last part is assumed to be the pageName, and everything
	 *		before this pageName is the urlContextString</li>
	 *	<li>If an urlContextString is present then it always ends in a /</li>
	 *	<li>If the URL is just a pageName then this returns the empty string</li>
	 * </ul>
	 *
	 * The following always holds: {@link #getUrlContextString()} + {@link #getPageName()} + m_extension = {@link #getInputPath()}.
	 */
	@NonNull
	String getUrlContextString();

	/**
	 * Returns the last part of the URL, provided that part has an extension. If not there is no page name.
	 */
	@Nullable
	String getPageName();

	/**
	 * Returns the value of the "User-Agent" header to determine the browser type.
	 */
	@Nullable
	String getUserAgent();

	/**
	 * Creates a full path from an application-relative path. So if the root of the application
	 * is "http://localhost/demo/", calling this with "img/button.png" will return the string
	 * "http://localhost/demo/img/button.png".
	 */
	@NonNull
	String getRelativePath(@NonNull String rel);

	void setPersistedParameter(@NonNull String name, @NonNull String value);

	/**
	 * Returns the buffered writer to use to generate text-based output to this context.
	 */
	@NonNull
	Writer getOutputWriter(@NonNull String contentType, @Nullable String encoding) throws IOException;
}
