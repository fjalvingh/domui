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
package to.etc.domui.component.dynaima;

import java.io.*;

import javax.annotation.*;

import to.etc.domui.parts.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.trouble.*;
import to.etc.util.*;

public class DynaImaPart implements IUnbufferedPartFactory {
	@Override
	public void generate(@Nonnull DomApplication app, @Nonnull String rurl, @Nonnull RequestContextImpl param) throws Exception {
		DynaRenderer cpr = new DynaRenderer();
		cpr.generate(app, param, rurl);							// Decode input to get to the component in question.
	}

	static public class DynaRenderer extends ComponentPartRenderer {
		private DynaIma m_ima;

		public void generate(DomApplication app, RequestContextImpl param, String rurl) throws Exception {
			initialize(app, param, rurl);
			if(getArgs().length != 3)
				throw new IllegalStateException("Invalid input URL '" + rurl + "': must be in format cid/pageclass/componentID");

			if(!(getComponent() instanceof DynaIma))
				throw new ThingyNotFoundException("The component " + getComponent().getActualID() + " on page " + getPage().getBody() + " is not an HtmlEditor instance");
			m_ima = (DynaIma) getComponent();

			//-- Check: do we already *have* a cached copy in the image? If not generate one...
			String mime;
			int size;
			byte[][] data;
			synchronized(m_ima) {
				if(m_ima.getCachedData() == null) {
					m_ima.initializeCached();
				}
				mime = m_ima.getCachedMime();
				size = m_ima.getCachedSize();
				data = m_ima.getCachedData();
			}

			//-- Render output.
			if(data == null) {									// No data in image?
				throw new ThingyNotFoundException("No image in " + rurl);
			}

			OutputStream os = param.getRequestResponse().getOutputStream(mime, null, size);
			FileTool.save(os, data); 							// Flush to output
		}
	}
}
