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

import java.io.*;

import javax.annotation.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import to.etc.util.*;

/**
 * Utility class for handling SAX documents
 *
 * @author <a href="mailto:wouter.van.vliet@itris.nl">Wouter van Vliet</a>
 * Created on 17 dec. 2014
 */
public class SAXTools {

	static public void parseSAX(@Nonnull Reader r, @Nullable String id, @Nullable ContentHandler ch, @Nullable ErrorHandler eh) throws Exception {
		XMLReader xr = XMLReaderFactory.createXMLReader();
		xr.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
		xr.setFeature("http://xml.org/sax/features/namespaces", true);

		xr.setContentHandler(ch);
		xr.setErrorHandler(eh);
		InputSource ips = new InputSource(r);
		ips.setPublicId("incoming:" + id);
		xr.parse(ips);
	}

	static public void parseSAX(@Nonnull Class< ? > base, @Nullable String name, @Nullable ContentHandler ch, @Nullable ErrorHandler eh) throws Exception {
		Reader r = FileTool.getResourceReader(base, name);
		try {
			parseSAX(r, base.getName() + "/" + name, ch, eh);
		} finally {
			try {
				r.close();
			} catch(Exception x) {}
		}
	}

	static public void parseSAX(@Nonnull Class< ? > base, @Nullable String name, @Nullable ContentHandler ch) throws Exception {
		DefaultErrorHandler deh = new DefaultErrorHandler();
		parseSAX(base, name, ch, deh);
		if(deh.hasErrors())
			throw new SAXException("SAX Parse errors: " + deh.getErrors());
	}

}
