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

import java.util.*;

public class EditorResourceType {
	private String m_name;

	private String m_rootURL;

	private Set<String> m_allowedExtensions;

	private Set<String> m_deniedExtensions;

	private int m_acl = 255;

	public EditorResourceType() {}

	public EditorResourceType(String name, String rootURL, int acl, Set<String> allowedExtensions, Set<String> deniedExtensions) {
		m_name = name;
		m_rootURL = rootURL;
		m_acl = acl;
		m_allowedExtensions = allowedExtensions;
		m_deniedExtensions = deniedExtensions;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getRootURL() {
		return m_rootURL;
	}

	public void setRootURL(String rootURL) {
		m_rootURL = rootURL;
	}

	public Set<String> getAllowedExtensions() {
		return m_allowedExtensions;
	}

	public void setAllowedExtensions(Set<String> allowedExtensions) {
		m_allowedExtensions = allowedExtensions;
	}

	public Set<String> getDeniedExtensions() {
		return m_deniedExtensions;
	}

	public void setDeniedExtensions(Set<String> deniedExtensions) {
		m_deniedExtensions = deniedExtensions;
	}

	public int getAcl() {
		return m_acl;
	}

	public void setAcl(int acl) {
		m_acl = acl;
	}
}
