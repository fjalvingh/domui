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

import to.etc.domui.component.layout.*;
import to.etc.domui.component.layout.title.*;


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
	/** The title for the page in the head's TITLE tag. */
	private String m_pageTitle;

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
	 * Get the page name used for {@link AppPageTitleBar} and {@link BreadCrumb} related code. To set the head title use the
	 * "title" property.
	 * @return
	 */
	public String getPageTitle() {
		return m_pageTitle;
	}

	/**
	 * Set the page name used for {@link AppPageTitleBar} and {@link BreadCrumb} related code. To set the head title use the
	 * "title" property.
	 *
	 * @param pageTitle
	 */
	public void setPageTitle(String pageTitle) {
		m_pageTitle = pageTitle;
	}
}
