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

/**
 * This must be used when the application uses demand-based logins. It provides a function
 * which returns the URL of the login page, and it has a function which defines the URL of
 * the "access denied" page when a resource gets accessed which is not allowed by rights.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 17, 2009
 */
public interface ILoginDialogFactory {
	/**
	 * Return the URL of a page/JSP to use as a login page. The page must cause
	 * PageContext.login() to be called in whatever way. After a succesful login
	 * this page must also redirect to the specified target URL.
	 * The URL returned <b>must</b> be an URL relative to the webapp root or the
	 * properly defined name of a Page class.
	 *
	 * @param originalTarget
	 * @return
	 */
	String getLoginRURL(String originalTarget);

	/**
	 * Must return the RURL of the page/JSP to use as an "access denied" page. The
	 * URL returned here must be a webapp relative URL. The resource accessed there
	 * will get the failed page class AND the list of rights that is needed to access
	 * that page as parameters.
	 *
	 * @return
	 */
	String getAccessDeniedURL();
}
