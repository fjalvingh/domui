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

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;

import javax.annotation.*;

@DefaultNonNull
final public class PartRequestHandler implements IFilterRequestHandler {
	final private PartService m_partService;

	public PartRequestHandler(@Nonnull PartService partService) {
		m_partService = partService;
	}

	/**
	 * Accept urls that end in .part or that have a first segment containing .part. The part before the ".part" must be a
	 * valid class name containing an {@link IPartFactory}.
	 * @see to.etc.domui.server.IFilterRequestHandler#accepts(to.etc.domui.server.IRequestContext)
	 */
	@Override
	public boolean accepts(@Nonnull IRequestContext ri) throws Exception {
		String in = ri.getInputPath();
		if(in.endsWith(".part"))
			return true;
		int pos = in.indexOf('/'); // First component
		if(pos < 0)
			return false;
		String seg = in.substring(0, pos);
		return seg.endsWith(".part");
	}

	//	static private void dumpHeaders(RequestContextImpl ctx) {
	//		for(Enumeration<String> e = ctx.getRequest().getHeaderNames(); e.hasMoreElements();) {
	//			String name = e.nextElement();
	//			System.out.println("  hdr " + name + ": " + ctx.getRequest().getHeader(name));
	//		}
	//	}

	/**
	 * Entrypoint for when the class name is inside the URL (direct entry).
	 *
	 * @see to.etc.domui.server.IFilterRequestHandler#handleRequest(to.etc.domui.server.RequestContextImpl)
	 */
	@Override
	public boolean handleRequest(@Nonnull final RequestContextImpl ctx) throws Exception {






		String input = ctx.getInputPath();
		//		dumpHeaders(ctx);
		boolean part = false;
		if(input.endsWith(".part")) {
			input = input.substring(0, input.length() - 5); // Strip ".part" off the name
			part = true;
		}
		int pos = input.indexOf('/'); // First path component is the factory name,
		String fname, rest;
		if(pos == -1) {
			fname = input;
			rest = "";
		} else {
			fname = input.substring(0, pos);
			rest = input.substring(pos + 1);
		}
		if(fname.endsWith(".part")) {
			fname = fname.substring(0, fname.length() - 5);
			part = true;
		}

		if(!part)
			throw new ThingyNotFoundException("Not a part: " + input);

		IPartFactory factory = m_partService.getPartFactoryByClassName(fname);
		if(factory == null)
			throw new ThingyNotFoundException("The part factory '" + fname + "' cannot be located.");
		m_partService.renderUrlPart(factory, ctx, rest);
	}
}
