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
package to.etc.domui.dom.header;

import to.etc.domui.dom.IContributorRenderer;

/**
 * Javascript contributor which obtains the Javascript to use from the
 * current theme.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 3, 2008
 */
public class ThemedJavascriptContributor extends HeaderContributor {
	private final boolean m_offline;

	private String m_path;

	ThemedJavascriptContributor(String path) {
		m_path = path;
		m_offline = false;
	}

	ThemedJavascriptContributor(String path, boolean offline) {
		m_path = path;
		m_offline = offline;
	}

	@Override public boolean isOfflineCapable() {
		return m_offline;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_path == null) ? 0 : m_path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		ThemedJavascriptContributor other = (ThemedJavascriptContributor) obj;
		if(m_path == null) {
			return other.m_path == null;
		} else
			return m_path.equals(other.m_path);
	}

	@Override
	public void contribute(IContributorRenderer r) throws Exception {
		r.renderLoadJavascript(m_path, false, false);
	}
}
