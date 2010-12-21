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
package to.etc.domui.pages.generic;

import to.etc.domui.component.layout.title.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

public class BasicPage<T> extends UrlPage {
	private Class<T> m_baseClass;

	private String m_pageTitle;

	private BasePageTitleBar m_titleBar;

	public BasicPage(Class<T> baseClass) {
		m_baseClass = baseClass;
	}

	public BasicPage(Class<T> baseClass, String txt) {
		m_baseClass = baseClass;
		m_pageTitle = txt;
	}

	public Class<T> getBaseClass() {
		return m_baseClass;
	}

	@Override
	public void createContent() throws Exception {
		addPageHeaders();
		//		add(new VerticalSpacer(5)); // add little space between title bar and other components
		addPageTitleBar();
	}

	public String getPageTitle() {
		return m_pageTitle;
	}

	public void setPageTitle(String pageTitle) throws Exception {
		if(m_pageTitle != pageTitle) {
			m_pageTitle = pageTitle;
			if(m_titleBar != null) {
				m_titleBar.setPageTitle(pageTitle);
			}
		}
	}

	protected void addPageHeaders() throws Exception {
	}

	/**
	 * Override only when using some custom page title bar.
	 * By default this method returns {@link DomApplication#getDefaultPageTitle(String)}.
	 */
	protected void addPageTitleBar() {
		m_titleBar = DomApplication.get().getDefaultPageTitle(m_pageTitle);
		add(m_titleBar);
	}

	public void clearGlobalMessages() {
		IErrorFence fence = DomUtil.getMessageFence(this);
		fence.clearGlobalMessages(this, null);
	}

	public BasePageTitleBar getTitleBar() {
		return m_titleBar;
	}
}
