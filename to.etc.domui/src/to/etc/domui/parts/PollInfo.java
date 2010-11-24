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
package to.etc.domui.parts;

import java.io.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.xml.*;

/**
 * This will become a simple, generic framework to pass events to a
 * browser based application by means of polling: the browser is supposed
 * to poll this part every 2 minutes; this part can pass back event
 * commands to the browser when needed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 11, 2010
 */
public class PollInfo implements IUnbufferedPartFactory {
	@Override
	public void generate(DomApplication app, String rurl, RequestContextImpl param) throws Exception {
		param.getResponse().setContentType("text/xml; charset=UTF-8");
		Writer w = param.getResponse().getWriter();
		XmlWriter	xw	= new XmlWriter(w);
		xw.wraw("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		xw.tag("poll-info");


		xw.tagendnl();
	}
}
