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

public class W3CEncodingException extends RuntimeException {
	private String	m_reason;

	private String	m_location;

	private String	m_value;

	public W3CEncodingException(String msg) {
		m_reason = msg;
	}

	public W3CEncodingException(String msg, String value) {
		m_reason = msg;
		m_value = value;
	}

	public W3CEncodingException() {
		super("Invalid value");
	}

	public String getLocation() {
		return m_location;
	}

	public W3CEncodingException setLocation(String location) {
		m_location = location;
		return this;
	}

	public String getReason() {
		return m_reason;
	}

	public W3CEncodingException setReason(String reason) {
		m_reason = reason;
		return this;
	}

	public String getValue() {
		return m_value;
	}

	public W3CEncodingException setValue(String value) {
		m_value = value;
		return this;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder(128);
		sb.append(m_reason);
		if(m_value != null) {
			sb.append(" value='");
			sb.append(m_value);
			sb.append("'");
		}
		if(m_location != null) {
			sb.append(" location=");
			sb.append(m_location);
		}
		return sb.toString();
	}
}
