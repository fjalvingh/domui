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

import to.etc.domui.dom.*;
import to.etc.domui.dom.html.*;

final public class JavaScriptletContributor extends HeaderContributor {
	private final String m_javascript;

	JavaScriptletContributor(final String javascript) {
		m_javascript = javascript;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_javascript == null) ? 0 : m_javascript.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if(obj == null)
			return false;
		if(this == obj)
			return true;
		if(getClass() != obj.getClass())
			return false;
		JavaScriptletContributor other = (JavaScriptletContributor) obj;
		if(m_javascript == null) {
			if(other.m_javascript != null)
				return false;
		} else if(!m_javascript.equals(other.m_javascript))
			return false;
		return true;
	}

	/**
	 * Generate the specified scriptlet as a script tag.
	 * @see to.etc.domui.dom.header.HeaderContributor#contribute(to.etc.domui.dom.HtmlFullRenderer)
	 */
	@Override
	public void contribute(final HtmlFullRenderer r) throws Exception {
		r.o().tag("script");
		r.o().attr("language", "javascript");
		r.o().endtag();
		r.o().writeRaw("<!--\n"); // Embed JS in comment IMPORTANT: the \n is required!!!
		r.o().writeRaw(m_javascript);
		r.o().writeRaw("\n-->");
		r.o().closetag("script");
	}

	@Override
	public void contribute(OptimalDeltaRenderer r) throws Exception {
		r.o().writeRaw(m_javascript);
	}
}
