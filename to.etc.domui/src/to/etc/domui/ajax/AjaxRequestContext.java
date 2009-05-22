package to.etc.domui.ajax;

import java.io.*;
import javax.servlet.http.*;

import to.etc.domui.login.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.server.ajax.*;

public class AjaxRequestContext implements IRpcCallContext {
	private final RequestContextImpl		m_rctx;
	private final AjaxRequestHandler		m_rh;

	public AjaxRequestContext(final AjaxRequestHandler ajaxRequestHandler, final RequestContextImpl ctx) {
		m_rh = ajaxRequestHandler;
		m_rctx = ctx;
	}

	public RequestContextImpl getRctx() {
		return m_rctx;
	}
	private HttpServletResponse	getResponse() {
		return m_rctx.getResponse();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	ServiceCallerCallback interface.					*/
	/*--------------------------------------------------------------*/
	public <T> T createHandlerClass(final Class<T> clz) throws Exception {
		return m_rh.makeCallClass(clz, this);
	}

	public boolean hasRight(final String role) {
		IUser	user = PageContext.getCurrentUser();
		if(user == null)
			return false;
		return user.hasRight(role);
	}
//	public IServiceAuthenticator getAuthenticator() {
//		return m_ajax.getAuthenticator();
//	}

//	private Object	allocateSource(final Class<?> sourcecl) throws Exception {
//		boolean	hasempty = false;
//		Constructor[] car = sourcecl.getConstructors();
//		for(Constructor c : car) {
//			if(! Modifier.isPublic(c.getModifiers()))
//				continue;
//			Class[] par = c.getParameterTypes();
//			if(par.length == 0) {
//				hasempty = true;
//			} else {
//				if(par.length == 1) {
//					if(par[0].isAssignableFrom(ServiceServerContext.class)) {
//						return c.newInstance(new Object[] {this});
//					}
//				}
//			}
//		}
//		if(! hasempty)
//			throw new IllegalArgumentException("The injector source "+sourcecl+" does not have a proper constructor");
//
//		return sourcecl.newInstance();
//	}


	public <T> T allocateOutput(final Class<T> oc, final ResponseFormat rf) throws Exception {
		return null;
	}
	public void outputCompleted(final Object output) throws Exception {
	}
	public Writer getResponseWriter(final ResponseFormat format, final String callname) throws Exception {
		switch(format) {
			default:
				throw new IllegalStateException("Unknown response format: "+format);

			case JSON:
				getResponse().setContentType("text/html"); // Jal 20060922 Do not change to text/javascript!! This makes Prototype eval() the response as a JS program which it is not.
				getResponse().setCharacterEncoding("utf-8");
				getResponse().addHeader("X-ETC-AJAX-CALL", callname);
				return getResponse().getWriter();

			case XML:
				getResponse().setContentType("text/xml");
				getResponse().setCharacterEncoding("utf-8");
				getResponse().addHeader("X-ETC-AJAX-CALL", callname);
				return getResponse().getWriter();
		}
	}
}
