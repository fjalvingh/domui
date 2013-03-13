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

import to.etc.domui.parts.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.*;
import to.etc.domui.util.upload.*;

/**
 * This thingy accepts file upload requests for a given control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 14, 2008
 */
public class UploadPart implements IUnbufferedPartFactory {
	@Override
	public void generate(DomApplication app, String rurl, RequestContextImpl param) throws Exception {
		try {
			ComponentPartRenderer r = new ComponentPartRenderer();
			r.initialize(app, param, rurl);
			FileUpload fu = (FileUpload) r.getComponent();
			UploadItem[] uiar = param.getFileParameter(fu.getInput().getActualID());
			if(uiar != null) {
				for(UploadItem ui : uiar) {
					fu.getFiles().add(ui);
					r.getConversation().registerUploadTempFile(ui.getFile());
				}
			}
			fu.forceRebuild();

			//-- Render an optimal delta as the response,
			ServerTools.generateNoCache(param.getResponse()); // Do not allow the browser to cache
			ApplicationRequestHandler.renderOptimalDelta(param, r.getPage());
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
