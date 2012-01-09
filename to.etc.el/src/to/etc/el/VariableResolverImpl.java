/*
 * DomUI Java User Interface - shared code
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
package to.etc.el;

import javax.servlet.jsp.*;
import javax.servlet.jsp.el.*;

/**
 * Resolver for JSP pages, using the different names for JSP context
 * variables as defined in the JSP spec 2.0.
 */
public class VariableResolverImpl implements VariableResolver {
	private PageContext m_pc;

	public VariableResolverImpl(PageContext pc) {
		m_pc = pc;
	}

	public Object resolveVariable(String pName) throws ELException {
		// Check for implicit objects
		if("pageContext".equals(pName))
			return m_pc;
		else if("pageScope".equals(pName))
			return ImplicitObjects.getImplicitObjects(m_pc).getPageScopeMap();
		else if("requestScope".equals(pName))
			return ImplicitObjects.getImplicitObjects(m_pc).getRequestScopeMap();
		else if("sessionScope".equals(pName))
			return ImplicitObjects.getImplicitObjects(m_pc).getSessionScopeMap();
		else if("applicationScope".equals(pName))
			return ImplicitObjects.getImplicitObjects(m_pc).getApplicationScopeMap();
		else if("param".equals(pName))
			return ImplicitObjects.getImplicitObjects(m_pc).getParamMap();
		else if("paramValues".equals(pName))
			return ImplicitObjects.getImplicitObjects(m_pc).getParamsMap();
		else if("header".equals(pName))
			return ImplicitObjects.getImplicitObjects(m_pc).getHeaderMap();
		else if("headerValues".equals(pName))
			return ImplicitObjects.getImplicitObjects(m_pc).getHeadersMap();
		else if("initParam".equals(pName))
			return ImplicitObjects.getImplicitObjects(m_pc).getInitParamMap();
		else if("cookie".equals(pName))
			return ImplicitObjects.getImplicitObjects(m_pc).getCookieMap();
		else
			return m_pc.findAttribute(pName);
	}
}
