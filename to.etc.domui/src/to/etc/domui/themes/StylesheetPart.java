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
package to.etc.domui.themes;

import java.io.*;
import java.util.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.resources.*;

/**
 * This returns the DomUI stylesheet for one browser, as constructed for a single browser version.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 10, 2011
 */
public class StylesheetPart implements IBufferedPartFactory {
	static private class Key {
		private String m_browserID;

		private BrowserVersion m_bv;

		public Key(BrowserVersion bv) {
			m_bv = bv;
			m_browserID = bv.getBrowserName() + "/" + bv.getMajorVersion();
		}

		@Override
		public String toString() {
			return "[stylesheet: browser=" + m_bv + "]";
		}

		public BrowserVersion getBrowserVersion() {
			return m_bv;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((m_browserID == null) ? 0 : m_browserID.hashCode());
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
			Key other = (Key) obj;
			if(m_browserID == null) {
				if(other.m_browserID != null)
					return false;
			} else if(!m_browserID.equals(other.m_browserID))
				return false;
			return true;
		}
	}

	/**
	 * Create a key from only browser name + major version.
	 * @see to.etc.domui.server.parts.IBufferedPartFactory#decodeKey(java.lang.String, to.etc.domui.server.IExtendedParameterInfo)
	 */
	@Override
	public Object decodeKey(String rurl, IExtendedParameterInfo param) throws Exception {
		return new Key(param.getBrowserVersion());
	}

	@Override
	public void generate(PartResponse pr, DomApplication da, Object k, ResourceDependencyList rdl) throws Exception {
		Key key = (Key) k;

		if(!da.inDevelopmentMode()) { // Not gotten from WebContent or not in DEBUG mode? Then we may cache!
			pr.setCacheTime(da.getDefaultExpiryTime());
		}

		DefaultThemeStore dts = da.getThemeStore(rdl); // Get theme store and add it's dependencies to rdl
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(pr.getOutputStream()));
		Map<String, Object>	map = new HashMap<String, Object>(dts.getThemeProperties());
		map.put("browser", key);
		dts.getStylesheetSource().execute(pw, map);
		pw.close();
		pr.setMime("text/css");
	}
}
