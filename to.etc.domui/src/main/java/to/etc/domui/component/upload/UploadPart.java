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
package to.etc.domui.component.upload;

import javax.annotation.*;

import to.etc.domui.parts.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.trouble.*;

/**
 * This thingy accepts file upload requests for a given control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 14, 2008
 */
public class UploadPart implements IUnbufferedPartFactory {
	@Override
	public void generate(@Nonnull DomApplication app, @Nonnull String rurl, @Nonnull RequestContextImpl param) throws Exception {
		try {
			ComponentPartRenderer r = new ComponentPartRenderer();
			r.initialize(app, param, rurl);
			if(!(r.getComponent() instanceof IUploadAcceptingComponent))
				throw new IllegalStateException("The targeted component " + r.getComponent() + " does not accept uploaded files.");

			IUploadAcceptingComponent fu = (IUploadAcceptingComponent) r.getComponent();
			boolean render = fu.handleUploadRequest(param, r.getConversation());

			//-- Render an optimal delta as the response,
			if(render) {
				param.getRequestResponse().setNoCache();
				ApplicationRequestHandler.renderOptimalDelta(param, r.getPage());
			}
		} catch(ThingyNotFoundException x) {
			//-- Page seems to have gone in the meanwhile
			System.err.println("domui: upload target " + rurl + " has gone while the upload commenced");
			param.getRequestResponse().sendError(404, x.getMessage());
		} catch(Exception x) {
			x.printStackTrace();
			throw x;
		}
	}
}
