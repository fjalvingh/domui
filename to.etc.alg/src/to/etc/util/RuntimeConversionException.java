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
package to.etc.util;

/**
 * Thrown when a runtime conversion fails.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
public class RuntimeConversionException extends RuntimeException {
	private String	m_where;

	private String	m_message;

	public RuntimeConversionException() {
	}

	public RuntimeConversionException(Object in, String to) {
		m_message = "Cannot convert object type " + (in == null ? "(null)" : in.getClass().getName()) + " to " + to + "( value=" + in + ")";
	}

	public RuntimeConversionException(Exception cause, Object in, String to) {
		super(cause);
		m_message = "Cannot convert object type " + (in == null ? "(null)" : in.getClass().getName()) + " to " + to + "( value=" + in + ")";
	}

	public RuntimeConversionException(String message) {
		m_message = message;
	}

	public RuntimeConversionException(String message, Throwable cause) {
		super(message, cause);
		m_message = message;
	}

	public RuntimeConversionException(Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		if(m_message == null) {
			if(m_where == null)
				return "Unknown conversion error";
			return "Unknown conversion error: " + m_where;
		}
		if(m_where == null)
			return m_message;
		return m_message + " " + m_where;
	}

	public void setWhere(String s) {
		m_where = s;
	}
}
