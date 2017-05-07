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
package to.etc.domui.component.layout.title;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public abstract class BasePageTitleBar extends Div {
	private String m_title;

	private boolean m_showAsModified;

	public BasePageTitleBar() {}

	public BasePageTitleBar(final String title) {
		m_title = title;
	}

	/**
	 * Return the title that is used by this bar. If no user title is set this returns the
	 * calculated title (from annotations and metadata).
	 * @return
	 */
	public String getPageTitle() {
		if(m_title != null) {
			return m_title;
		}
		return DomUtil.calcPageTitle(getPage().getBody().getClass());
	}

	public boolean isShowAsModified() {
		return m_showAsModified;
	}

	public void setPageTitle(String ttl) {
		m_title = ttl;
	}

	public void setShowAsModified(boolean showAsModified) {
		m_showAsModified = showAsModified;
	}
}
