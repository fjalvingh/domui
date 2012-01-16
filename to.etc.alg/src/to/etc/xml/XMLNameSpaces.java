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

/**
 * Well-known namespaces.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 28, 2009
 */
final public class XMLNameSpaces {
	private XMLNameSpaces() {
	}

	static public final String	SOAP1_1				= "http://schemas.xmlsoap.org/soap/envelope/";

	static public final String	SOAP1_2				= "http://www.w3.org/2003/05/soap-envelope";

	static public final String	SOAP_WSDL_SOAP		= "http://schemas.xmlsoap.org/wsdl/soap/";

	static public final String	SOAP_WSDL			= "http://schemas.xmlsoap.org/wsdl/";

	static public final String	SOAP_WSDL_1_2		= "http://schemas.xmlsoap.org/wsdl/soap12/";

	static public final String	SOAP_ENCODING		= "http://schemas.xmlsoap.org/soap/encoding/";

	static public final String	XMLSCHEMA			= "http://www.w3.org/2001/XMLSchema";

	static public final String	XMLSCHEMA_INSTANCE	= "http://www.w3.org/2001/XMLSchema-instance";

	/**
	 * The mother of all namespaces bound by definition to the prefix xml
	 */
	static public final String	XMLNAMESPACE		= "http://www.w3.org/XML/1998/namespace";

	static public final String	XHTML1_0			= "http://www.w3.org/1999/xhtml";

	static public final String	XOP_INCLUDE			= "http://www.w3.org/2004/08/xop/include";
}
