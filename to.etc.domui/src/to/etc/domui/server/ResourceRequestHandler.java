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
package to.etc.domui.server;

import to.etc.domui.server.parts.*;

public class ResourceRequestHandler implements IFilterRequestHandler {
	//	private DomApplication		m_app;

	private PartRequestHandler m_prh;

	private InternalResourcePart m_rp = new InternalResourcePart();

	public ResourceRequestHandler(DomApplication app, PartRequestHandler prh) {
		//		m_app = app;
		m_prh = prh;
	}

	/**
	 * Handles requests for $ resources. It just delegates to a special part.
	 *
	 * @see to.etc.domui.server.IFilterRequestHandler#handleRequest(to.etc.domui.server.RequestContextImpl)
	 */
	@Override
	public void handleRequest(RequestContextImpl ctx) throws Exception {
		//		String	url = ctx.getInputPath().substring(1);
		//		m_prh.generate(m_rp, ctx, url);
		m_prh.generate(m_rp, ctx, ctx.getInputPath());
	}
}
