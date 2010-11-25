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
package to.etc.net;

public class HttpCallException extends Exception {
	private int		m_code;

	private String	m_url;

	private String	m_message;

	private String	m_errorStream;

	public HttpCallException(String url, int code, String message) {
		super(message);
		m_code = code;
		m_url = url;
		m_message = message;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("HTTP error ");
		sb.append(m_code);
		sb.append(": ");
		sb.append(m_message);
		sb.append(" on ");
		sb.append(m_url);
		return sb.toString();
	}

	public String getErrorStream() {
		return m_errorStream;
	}

	public void setErrorStream(String errorStream) {
		m_errorStream = errorStream;
	}
}
