package to.etc.server.servicer;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.servlet.http.*;

import to.etc.server.ajax.*;
import to.etc.server.nls.*;
import to.etc.server.servlet.*;

public class ServiceServerContext extends ContextServletContextBase implements ServiceCallerCallback {
	static private ThreadLocal<ServiceServerContext>	m_current	= new ThreadLocal<ServiceServerContext>();

	private AjaxServlet									m_ajax;

	private HttpSession									m_session;

	/** All of the setter source classes currently allocated */
	private Map<Class< ? >, Object>						m_sourceMap	= new HashMap<Class< ? >, Object>();

	public ServiceServerContext(AjaxServlet servlet, HttpServletRequest request, HttpServletResponse response, boolean post) {
		super(servlet, request, response, post);
		m_ajax = servlet;
	}

	static public ServiceServerContext getCurrent() {
		return m_current.get();
	}

	final public HttpSession getSession() {
		if(m_session == null)
			m_session = getRequest().getSession(true);
		return m_session;
	}

	/**
	 * url: DisViewerActions.ajax?method=
	 * @throws Exception
	 */
	@Override
	public void execute() throws Exception {
		m_current.set(this);

		//-- Force the locale
		NlsContext.setLocale(getRequest().getLocale());

		try {
			//-- Massage the URL into something edible, and remove the .ajax extension.
			String rurl = getRequest().getServletPath();

			//		String	rurl = getRequest().getPathInfo();
			if(rurl == null)
				throw new ServiceException("Missing url segment");
			int sx = 0;
			int ex = rurl.length();
			if(rurl.startsWith("/"))
				sx = 1;
			if(rurl.endsWith("/"))
				ex = ex - 1;
			int pos = rurl.lastIndexOf('.'); // Remove the suffix (.ajax usually)
			if(pos != -1)
				ex = pos;
			rurl = rurl.substring(sx, ex); // Base name contains class and method.
			if(rurl.equals("bulk")) {
				executeBulkRequest();
				return;
			}

			//-- If a format override is present get it,
			String s = getRequest().getParameter("_format"); // Format override present in request?
			ResponseFormat rf = null;
			if(s != null)
				rf = ResponseFormat.valueOf(s);
			List<Class< ? extends Object>> sourceList = new ArrayList<Class< ? extends Object>>(m_ajax.getRequestCaller().getSourceClassesList());
			sourceList.add(0, getClass());
			m_ajax.getRequestCaller().executeSingleCall(this, sourceList, rurl, rf);
		} catch(ServiceException sx) {
			sx.setContext(this);
			throw sx;
		} catch(Exception x) {
			throw new ServiceException(this, x.toString(), x);
		} finally {
			releaseSources();
			m_current.set(null);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Bulk call.											*/
	/*--------------------------------------------------------------*/

	/**
	 * Handles a bulk request using either JSON or XML. A bulk request
	 * is a set of calls executed in sequence. The bulk request must
	 * specify either a parameter containing the bulk request's data, or
	 * the input must be a stream containing it [not implemented yet].
	 */
	private void executeBulkRequest() throws Exception {
		String json = getRequest().getParameter("json");
		String xml = getRequest().getParameter("xml");
		if(xml != null && json != null)
			;
		else if(json != null) {
			m_ajax.getBulkCaller().executeBulkJSON(this, json);
			return;
		} else if(xml != null) {
			throw new IllegalStateException("xml bulk call not implemented yet");
		}
		throw new IllegalStateException("Bulk requests must be called using json= or xml= as parameter, or with an appropriate mime type and the call data in the request input stream (body)");
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Rendering											*/
	/*--------------------------------------------------------------*/


	/**
	 * Called when the request ends, this releases all of the source classes
	 * allocated.
	 */
	private void releaseSources() {
		for(Object o : m_sourceMap.values()) {
			try {
				if(o instanceof Closeable)
					((Closeable) o).close();
			} catch(Exception x) {
				getServlet().log("Releasing class instance " + o.getClass().getName(), x);
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	ServiceCallerCallback interface.					*/
	/*--------------------------------------------------------------*/
	//	public boolean hasUserAnyRole(String[] roles) {
	//		for(String s : roles) {
	//			if(getRequest().isUserInRole(s))
	//				return true;
	//		}
	//		return false;
	//	}
	public IServiceAuthenticator getAuthenticator() {
		return m_ajax.getAuthenticator();
	}

	public Object getInjectorSource(Class< ? > sourcecl) throws Exception {
		Object o = m_sourceMap.get(sourcecl);
		if(o != null)
			return o;
		if(sourcecl.isAssignableFrom(getClass()))
			return this;
		int mod = sourcecl.getModifiers();
		if(Modifier.isAbstract(mod) || Modifier.isInterface(mod) || !Modifier.isPublic(mod))
			return null;

		//-- If this one has a Constructor accepting a Request take that....
		o = allocateSource(sourcecl);
		m_sourceMap.put(sourcecl, o);
		return o;
	}

	private Object allocateSource(Class< ? > sourcecl) throws Exception {
		boolean hasempty = false;
		Constructor[] car = sourcecl.getConstructors();
		for(Constructor c : car) {
			if(!Modifier.isPublic(c.getModifiers()))
				continue;
			Class[] par = c.getParameterTypes();
			if(par.length == 0) {
				hasempty = true;
			} else {
				if(par.length == 1) {
					if(par[0].isAssignableFrom(ServiceServerContext.class)) {
						return c.newInstance(new Object[]{this});
					}
				}
			}
		}
		if(!hasempty)
			throw new IllegalArgumentException("The injector source " + sourcecl + " does not have a proper constructor");

		return sourcecl.newInstance();
	}


	public Object allocateOutput(Class<Object> oc, ResponseFormat rf) throws Exception {
		return null;
	}

	public void outputCompleted(Object output) throws Exception {
	}

	public Writer getResponseWriter(ResponseFormat format, String callname) throws Exception {
		switch(format){
			default:
				throw new IllegalStateException("Unknown response format: " + format);

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
