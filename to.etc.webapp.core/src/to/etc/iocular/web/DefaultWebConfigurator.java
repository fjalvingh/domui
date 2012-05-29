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
package to.etc.iocular.web;

import javax.servlet.*;

import to.etc.iocular.*;
import to.etc.iocular.def.*;
import to.etc.iocular.util.*;

/**
 * Default implementation of a web configurator. This version gets used if no
 * specific implementation is defined in the web configuration. The default
 * configurator uses context properties to load the container definitions for
 * session, request and application.
 *
 * @author jal
 * Created on Mar 25, 2007
 */
public class DefaultWebConfigurator implements WebConfigurator {
	@Override
	public WebConfiguration createConfiguration(ServletContext ctx) throws Exception {
		//-- 1. Try bulk
		ContainerDefinition reqd, sesd, appd;
		String cn = ctx.getInitParameter("request-configurator-class");
		if(cn != null) {
			Configurator c = ClassUtil.instanceByName(Configurator.class, cn);
			reqd = c.getContainerDefinition();
		} else {
			throw new UnavailableException("Missing 'request-configurator-class' context-parameter");
		}

		cn = ctx.getInitParameter("session-configurator-class");
		if(cn != null) {
			Configurator c = ClassUtil.instanceByName(Configurator.class, cn);
			sesd = c.getContainerDefinition();
		} else {
			throw new UnavailableException("Missing 'session-configurator-class' context-parameter");
		}

		cn = ctx.getInitParameter("application-configurator-class");
		if(cn != null) {
			Configurator c = ClassUtil.instanceByName(Configurator.class, cn);
			appd = c.getContainerDefinition();
		} else {
			throw new UnavailableException("Missing 'application-configurator-class' context-parameter");
		}
		return new WebConfiguration(appd, sesd, reqd);
	}
}
