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

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.resources.*;
import to.etc.util.*;
import to.etc.webapp.core.*;

/**
 * This accepts all urls in the format *.theme.xxx. It generates string resources that
 * depend on the theme map. It reads the original resource as a string and replaces all
 * theme values therein before re-rendering the result to the caller.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 1, 2009
 */
public class ThemePartFactory implements IBufferedPartFactory, IUrlPart {
	static private class Key {
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

		//		public String getBrowserID() {
		//			return m_browserID;
		//		}

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
			if(m_rurl == null) {
				if(other.m_rurl != null)
					return false;
			} else if(!m_rurl.equals(other.m_rurl))
				return false;
			else if(m_iv != other.m_iv)
				return false;
			return true;
		}
	};


	@Override
	public boolean accepts(@Nonnull String rurl) {
		int dot1 = rurl.lastIndexOf('.');
		if(dot1 == -1)
			return false;
		int dot2 = rurl.lastIndexOf('.', dot1 - 1);
		if(dot2 == -1)
			return false;
		return rurl.substring(dot2 + 1, dot1).equals("theme");
	}

	@Override
	public @Nonnull Object decodeKey(@Nonnull String rurl, @Nonnull IExtendedParameterInfo param) throws Exception {
//		String rand = param.getParameter("rand");
//		int rndom = 0;
//		if(null != rand) {
//			RequestContextImpl rcx = (RequestContextImpl) param;
//			IServerSession ses = (rcx.getServerSession(true));
//			if(null != ses) {
//				Integer r = (Integer) ses.getAttribute("random-theme");
//				if(null == r) {
//					rndom = (int) (Math.random() * 20);
//					ses.setAttribute("random-theme", Integer.valueOf(rndom));
//				}
//			}
//		}
//
		String iv = param.getParameter("iv");
		int val = 0;
		if(null != iv)
			val = Integer.parseInt(iv);
		return new Key(param.getBrowserVersion(), rurl, val);
	}

	@Override
	public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull Object k, @Nonnull IResourceDependencyList rdl) throws Exception {
		Key key = (Key) k;

		if(!da.inDevelopmentMode()) { // Not gotten from WebContent or not in DEBUG mode? Then we may cache!
			pr.setCacheTime(da.getDefaultExpiryTime());
		}
		String content = da.getThemeReplacedString(rdl, key.getRurl(), key.getBrowserVersion());
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(pr.getOutputStream()));
		pw.append(content);
		pw.close();
		pr.setMime(ServerTools.getExtMimeType(FileTool.getFileExtension(key.getRurl())));
	}
}
