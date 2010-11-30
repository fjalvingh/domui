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
package to.etc.domui.component.htmleditor;

/**
 * Folder representation used by the editor's file browser.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 30, 2008
 */
public class EditorFolder {
	private String m_name;

	private boolean m_hasChildren;

	private int m_acl = 255;

	public EditorFolder() {}

	public EditorFolder(String name, boolean hasChildren, int acl) {
		m_name = name;
		m_hasChildren = hasChildren;
		m_acl = acl;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public boolean isHasChildren() {
		return m_hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		m_hasChildren = hasChildren;
	}

	public int getAcl() {
		return m_acl;
	}

	public void setAcl(int acl) {
		m_acl = acl;
	}
}
