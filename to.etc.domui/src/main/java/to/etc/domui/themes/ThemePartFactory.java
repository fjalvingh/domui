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

import to.etc.domui.server.BrowserVersion;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IExtendedParameterInfo;
import to.etc.domui.server.IParameterInfo;
import to.etc.domui.server.parts.IBufferedPartFactory;
import to.etc.domui.server.parts.IUrlMatcher;
import to.etc.domui.server.parts.PartResponse;
import to.etc.domui.themes.ThemePartFactory.Key;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.util.FileTool;
import to.etc.webapp.core.ServerTools;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * This accepts all urls in the format *.theme.xxx. It generates string resources that
 * depend on the theme map. It reads the original resource as a string and replaces all
 * theme values therein before re-rendering the result to the caller.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 1, 2009
 */
@DefaultNonNull
final public class ThemePartFactory implements IBufferedPartFactory<Key> {
	/**
	 * Accept all resources that have a ".theme." string as a suffix in their
	 * last part, like style.theme.css
	 */
	static public final IUrlMatcher	MATCHER = new IUrlMatcher() {
		@Override public boolean accepts(@Nonnull IParameterInfo parameters) {
			String rurl = parameters.getInputPath();
			int dot1 = rurl.lastIndexOf('.');
			if(dot1 == -1)
				return false;
			int dot2 = rurl.lastIndexOf('.', dot1 - 1);
			if(dot2 == -1)
				return false;
			return rurl.substring(dot2 + 1, dot1).equals("theme");
		}
	};


	static public final class Key {
		private String m_rurl;

		private String m_browserID;

		private BrowserVersion m_bv;

		private int m_iv;

		public Key(BrowserVersion bv, String rurl, int iv) {
			m_bv = bv;
			m_browserID = bv.getBrowserName() + "/" + bv.getMajorVersion();
			m_rurl = rurl;
			m_iv = iv;
		}

		@Override
		public String toString() {
			return "[themed:" + m_rurl + ", browser=" + m_bv + "]";
		}

		public BrowserVersion getBrowserVersion() {
			return m_bv;
		}

		public String getRurl() {
			return m_rurl;
		}

		public int getIv() {
			return m_iv;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((m_browserID == null) ? 0 : m_browserID.hashCode());
			result = prime * result + ((m_rurl == null) ? 0 : m_rurl.hashCode());
			result = prime * result + m_iv;
			return result;
		}

		@Override
		public boolean equals(@Nullable Object obj) {
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
			if(m_rurl == null) {
				return other.m_rurl == null;
			} else if(!m_rurl.equals(other.m_rurl))
				return false;
			else
				return m_iv == other.m_iv;
		}
	}

	@Override
	public @Nonnull Key decodeKey(DomApplication application, @Nonnull IExtendedParameterInfo param) throws Exception {
		String iv = param.getParameter("iv");
		int val = 0;
		if(null != iv)
			val = Integer.parseInt(iv);
		return new Key(param.getBrowserVersion(), param.getInputPath(), val);
	}

	@Override
	public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull Key key, @Nonnull IResourceDependencyList rdl) throws Exception {
		if(!da.inDevelopmentMode()) { 					// Not gotten from WebContent or not in DEBUG mode? Then we may cache!
			pr.setCacheTime(da.getDefaultExpiryTime());
		}
		String content = da.internalGetThemeManager().getThemeReplacedString(rdl, key.getRurl(), key.getBrowserVersion());
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(pr.getOutputStream()));
		pw.append(content);
		pw.close();
		pr.setMime(ServerTools.getExtMimeType(FileTool.getFileExtension(key.getRurl())));
	}
}
