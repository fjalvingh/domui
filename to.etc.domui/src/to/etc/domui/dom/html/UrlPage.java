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
package to.etc.domui.dom.html;

import to.etc.domui.util.*;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;

/**
 * The base for all pages that can be accessed thru URL's. This is mostly a
 * dummy class which ensures that all pages/fragments properly extend from DIV,
 * ensuring that the Page logic can replace the "div" tag with a "body" tag for
 * root fragments.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 1, 2008
 */
public class UrlPage extends Div {
	/** Cached bundle for the page. If the bundle is not found this contains null.. */
	private BundleRef m_pageBundle;

	/**
	 * Gets called when a page is reloaded (for ROOT pages only).
	 */
	public void onReload() throws Exception {}

	/**
	 * Called when the page gets destroyed (navigation or such).
	 * @throws Exception
	 */
	public void onDestroy() throws Exception {}

	/**
	 * Returns the bundle defined for the page. This defaults to a bundle with the
	 * same name as the page's class name, but can be overridden by an @UIMenu
	 * annotation on the root class.
	 * @return
	 */
	public BundleRef getPageBundle() {
		if(m_pageBundle == null) {
			m_pageBundle = DomUtil.findPageBundle(this);
			if(m_pageBundle == null)
				throw new ProgrammerErrorException("The page " + this.getClass() + " does not have a page resource bundle");
		}
		return m_pageBundle;
	}

	/**
	 * Lookup and format a message from the page's bundle.
	 * @param key
	 * @param param
	 * @return
	 */
	public String $(String key, Object... param) {
		BundleRef br = getPageBundle();
		if(key.startsWith("~")) // Prevent silly bugs.
			key = key.substring(1);
		return br.formatMessage(key, param);
	}
}
