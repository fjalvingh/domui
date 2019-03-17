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
package to.etc.domui.util;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.UrlPage;

/**
 * Listener for page created and page destroyed events (with a bad name, sigh).
 */
public interface INewPageInstantiated {
	/**
	 * Called when the page is still "unbuilt", just after creation of it.
	 */
	void newPageCreated(@NonNull UrlPage body) throws Exception;

	/**
	 * Called whenever a page is "built", also when it is built *again* due to a forceRebuild.
	 */
	default void newPageBuilt(@NonNull UrlPage body) throws Exception {}

	/**
	 * Called whenever a page is destroyed. You can no longer do things with the page as it's dead already.
	 */
	default void pageDestroyed(@NonNull UrlPage body) throws Exception {}
}

