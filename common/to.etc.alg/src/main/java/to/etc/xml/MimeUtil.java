/*
 * DomUI Java User Interface - shared code
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
package to.etc.xml;

import java.util.*;

public class MimeUtil {
	private final Map<String, String>	m_extraMap	= new HashMap<String, String>();

	/**
	 * The idiotic JSDK returns the entire content type header, including any subproperties (like charset) instead of
	 * decoding it as it bloody should. This separates the content mime type from any parameters. Real useful to do
	 * this for every bloody servlet.
	 *
	 * FIXME This needs a proper quote-handling subproperty decoder but I do not feel like building that again.
	 */
	public String parseHeader(final String mime) {
		m_extraMap.clear();
		if(mime == null)
			return null;
		int pos = mime.indexOf(';');
		if(pos == -1)
			return unquote(mime.trim());
		String resmime = unquote(mime.substring(0, pos).trim()).trim();
		String rest = mime.substring(pos + 1); // Any rest..
		String[] plist = rest.split(";");
		for(String pair : plist) {
			//-- Split into name=value
			pos = pair.indexOf('=');
			if(pos != -1) {
				String name = pair.substring(0, pos).trim();
				String value = pair.substring(pos + 1).trim();
				m_extraMap.put(name.toLowerCase(), unquote(value.trim()));
			}
		}
		return resmime;
	}

	static private String unquote(final String in) {
		String s = in.trim();
		int l = s.length();
		if(l < 2)
			return in;
		char a = s.charAt(0);
		char b = s.charAt(l - 1);
		if(a != b)
			return in;
		if(a == '\"' || a == '\'')
			return s.substring(1, l - 1);
		return in;
	}

	public String getExtra(final String name) {
		return m_extraMap.get(name.toLowerCase());
	}
}
