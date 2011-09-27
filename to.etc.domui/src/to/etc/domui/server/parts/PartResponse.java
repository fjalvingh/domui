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
package to.etc.domui.server.parts;

import java.io.*;

/**
 * Describes the response for a part.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 13, 2009
 */
public class PartResponse {
	private OutputStream m_os;

	private String m_mime;

	private int m_cacheTime;

	/**
	 * Contains any extra data that is generated. Currently used to pass image size back to button.
	 */
	private Object m_extra;

	public PartResponse(OutputStream os) {
		m_os = os;
	}

	public OutputStream getOutputStream() {
		return m_os;
	}

	public String getMime() {
		return m_mime;
	}

	public void setMime(String mime) {
		m_mime = mime;
	}

	/**
	 * The time the response may be cached by the browser without inquiry, in seconds.
	 * @return
	 */
	public int getCacheTime() {
		return m_cacheTime;
	}

	/**
	 * Set the time the response may be cached by the browser without inquiry, in seconds.
	 * @param cacheTime
	 */
	public void setCacheTime(int cacheTime) {
		m_cacheTime = cacheTime;
	}

	public Object getExtra() {
		return m_extra;
	}

	public void setExtra(Object extra) {
		m_extra = extra;
	}
}
