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
