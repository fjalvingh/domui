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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.server.ApplicationRequestHandler;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IFilterRequestHandler;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.trouble.NotLoggedInException;

@NonNullByDefault
final public class PartRequestHandler implements IFilterRequestHandler {
	final private PartService m_partService;

	public PartRequestHandler(@NonNull PartService partService) {
		m_partService = partService;
	}

	/**
	 * Entrypoint for when the class name is inside the URL (direct entry).
	 *
	 * @see to.etc.domui.server.IFilterRequestHandler#handleRequest(to.etc.domui.server.RequestContextImpl)
	 */
	@Override
	public boolean handleRequest(@NonNull final RequestContextImpl ctx) throws Exception {
		try {
			return m_partService.render(ctx);
		} catch(NotLoggedInException x) {
			String url = DomApplication.get().handleNotLoggedInException(ctx, x);
			if(url != null) {
				ApplicationRequestHandler.generateHttpRedirect(ctx, url, "You need to be logged in");
				return true;
			}
			throw x;
		}
	}
}
