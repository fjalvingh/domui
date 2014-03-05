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
package to.etc.domui.ajax;

import java.lang.annotation.*;

import to.etc.domui.annotations.*;
import to.etc.domui.server.*;
import to.etc.util.*;
import to.etc.webapp.ajax.renderer.*;

public class URLParameterProvider implements IParameterProvider {
	private final RequestContextImpl m_ctx;

	public URLParameterProvider(final RequestContextImpl rctx) {
		m_ctx = rctx;
	}

	@Override
	public Object findParameterValue(final Class< ? > targetcl, final Annotation[] annotations, final int paramIndex, final AjaxParam ap) throws Exception {
		String[] pv = m_ctx.getParameters(ap.value()); // Parameter by name
		if(pv == null || pv.length == 0)
			return NO_VALUE;
		if(pv.length > 1)
			throw new RpcException("The value for the injector parameter '" + ap.value() + "' must be a single request value");
		if(ap.json()) {
			//        	System.out.println("ajax: json value for parameter '"+name+"' is "+pv[0]);
			//-- Convert the input from JSON to whatever object is needed,
			if(targetcl == Object.class) {
				//-- Generic assignment
				return JSONParser.parseJSON(pv[0]);
			} else {
				//-- Assign using a base class structure
				return JSONParser.parseJSON(pv[0], targetcl);
			}
		}

		//-- Autoconvert to proper type, using an URL converter
		return RuntimeConversions.convertTo(pv[0], targetcl);
	}
}
