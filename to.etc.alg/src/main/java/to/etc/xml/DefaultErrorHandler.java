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

import org.xml.sax.*;

public class DefaultErrorHandler implements ErrorHandler {
	/** This string buffer receives error messages while the document gets parsed. */
	private StringBuilder	m_xmlerr_sb	= new StringBuilder();

	private boolean			m_errors;

	private void genErr(SAXParseException exception, String type) {
		//		exception.printStackTrace();
		if(m_xmlerr_sb.length() > 0)
			m_xmlerr_sb.append("\n");
		String id = exception.getPublicId();
		if(id == null || id.length() == 0)
			id = exception.getPublicId();
		if(id == null || id.length() == 0)
			id = "unknown-source";
		m_xmlerr_sb.append(id);
		m_xmlerr_sb.append('(');
		m_xmlerr_sb.append(Integer.toString(exception.getLineNumber()));
		m_xmlerr_sb.append(':');
		m_xmlerr_sb.append(Integer.toString(exception.getColumnNumber()));
		m_xmlerr_sb.append(") ");
		m_xmlerr_sb.append(type);
		m_xmlerr_sb.append(":");
		m_xmlerr_sb.append(exception.getMessage());
	}

	public final void warning(SAXParseException exception) throws SAXException {
		genErr(exception, "warning");
	}

	public final void error(SAXParseException exception) throws SAXException {
		m_errors = true;
		genErr(exception, "error");
	}

	public final void fatalError(SAXParseException exception) throws SAXException {
		m_errors = true;
		genErr(exception, "fatal");
	}

	public String getErrors() {
		return m_xmlerr_sb.toString();
	}

	public boolean hasErrors() {
		return m_errors;
	}
}
