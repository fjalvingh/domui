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

import javax.annotation.*;

/**
 * This handles all requests starting with $xxxx. These indicate resource requests. See
 * {@link InternalResourcePart} for details.
 */
@DefaultNonNull
final public class ResourceRequestHandler implements IFilterRequestHandler {
	private final PartService m_partService;

	@Nonnull
	final private InternalResourcePart m_rp = new InternalResourcePart();

	public ResourceRequestHandler(@Nonnull DomApplication app, @Nonnull PartService partService) {
		m_partService = partService;
	}

	/**
	 * Handles requests for $ resources. It just delegates to a special part.
	 *
	 * @see to.etc.domui.server.IFilterRequestHandler#handleRequest(to.etc.domui.server.RequestContextImpl)
	 */
	@Override
	public boolean handleRequest(@Nonnull RequestContextImpl ctx) throws Exception {
		if(! ctx.getInputPath().startsWith("$"))
			return false;

		m_partService.generate(m_rp, ctx, ctx.getInputPath());
		return true;
	}
}
