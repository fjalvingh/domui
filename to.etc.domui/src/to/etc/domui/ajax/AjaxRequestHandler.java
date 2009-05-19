package to.etc.domui.ajax;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import to.etc.domui.server.*;
import to.etc.iocular.*;
import to.etc.iocular.container.*;
import to.etc.iocular.def.*;
import to.etc.server.ajax.*;
import to.etc.server.ajax.renderer.json.*;
import to.etc.server.ajax.renderer.xml.*;
import to.etc.server.injector.*;
import to.etc.server.servicer.*;

/**
 * This handles .ajax requests.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 14, 2009
 */
public class AjaxRequestHandler implements FilterRequestHandler {
	static private final Logger		LOG = Logger.getLogger(AjaxRequestHandler.class.getName());
	static private final String		CONT_KEY = "ajax.ioc";

	private final DomApplication m_application;

	private IInstanceBuilder	m_instanceBuilder;

	private List<IRequestInterceptor> m_interceptorList = new ArrayList<IRequestInterceptor>();

	private Container			m_applicationContainer;

	private ContainerDefinition	m_sessionContainerDef;

	private ContainerDefinition	m_requestContainerDef;

	public AjaxRequestHandler(final DomApplication domApplication) {
		m_application = domApplication;
	}

	public DomApplication getApplication() {
		return m_application;
	}

	public synchronized void addInterceptor(final IRequestInterceptor r) {
		List<IRequestInterceptor> l = new ArrayList<IRequestInterceptor>(m_interceptorList);
		l.add(r);
		m_interceptorList = l;
	}

	public synchronized List<IRequestInterceptor> getInterceptorList() {
		return m_interceptorList;
	}

	public IInstanceBuilder getInstanceBuilder() {
		return m_instanceBuilder;
	}

	public Container getApplicationContainer() {
		return m_applicationContainer;
	}

	public void setApplicationContainer(final Container applicationContainer) {
		m_applicationContainer = applicationContainer;
	}

	public ContainerDefinition getSessionContainerDef() {
		return m_sessionContainerDef;
	}

	public void setSessionContainerDef(final ContainerDefinition sessionContainerDef) {
		m_sessionContainerDef = sessionContainerDef;
	}

	public ContainerDefinition getRequestContainerDef() {
		return m_requestContainerDef;
	}

	public void setRequestContainerDef(final ContainerDefinition requestContainerDef) {
		m_requestContainerDef = requestContainerDef;
	}
	public ResponseFormat getDefaultFormat() {
		return m_defaultFormat;
	}
	public void setDefaultFormat(ResponseFormat rf) {
		m_defaultFormat = rf;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Handler allocation and injection.					*/
	/*--------------------------------------------------------------*/
	/**
	 * UNSTABLE INTERFACE - must move to separate class (interface).
	 * @return
	 */
	private Container	getRequestContainer(RequestContextImpl ci) {
		//-- If we have a request thing get it,
		Object v = ci.getAttribute(CONT_KEY);
		if(v != null)
			return (Container) v;

		Container	dad;
		if(getSessionContainerDef() == null)
			dad = getApplicationContainer();
		else {
			//-- Obtain/create the session container
			dad	= (Container)ci.getSession().getAttribute(CONT_KEY);
			if(dad == null) {
				//-- Create the session container
				dad	= new BasicContainer(getSessionContainerDef(), getApplicationContainer());
				ci.getSession().setAttribute(CONT_KEY, dad);
				dad.start();
				//-- FIXME Needs destruction listener.
			}
		}
		
		//-- Make a basic container, then store
		BasicContainer	rq	= new BasicContainer(getRequestContainerDef(), dad);
		ci.setAttribute("arq.bc", rq);
		rq.start();
		return rq;
	}

	private <T>	T	makeCallClass(Class<T> clz, AjaxRequestContext ctx) throws Exception {
		Container	bc	= getRequestContainer(ctx.getRctx());
		bc.setParameter(ctx.getRctx());
		return bc.getObject(clz);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	FilterRequestHandler implementation.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Actual execution by delegating to the context.
	 * @see to.etc.domui.server.FilterRequestHandler#handleRequest(to.etc.domui.server.RequestContextImpl)
	 */
	public void handleRequest(final RequestContextImpl ctx) throws Exception {
		AjaxRequestContext	ax	= new AjaxRequestContext(this, ctx);
		ax.execute();
	}

	static private boolean[]	PARAMONE = {true};

	/** Maps keys to resolved handler info thingies, for speed. */
	private final Map<String, ServiceClassDefinition> m_classDefMap = new HashMap<String, ServiceClassDefinition>();

	private final XmlRegistry m_xmlRegistry = new XmlRegistry();

	private final JSONRegistry m_JSONRegistry = new JSONRegistry();

	private ResponseFormat m_defaultFormat = ResponseFormat.XML;

	/*--------------------------------------------------------------*/
	/*	CODING:	Decode the class to use and get class reference.	*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns a service class definition for the class name specified. The classname
	 * can be a complete classname. If it is not it is located by scanning the default
	 * package list.
	 * @param name	The dotted package.classname, or a single classname.
	 */
	private ServiceClassDefinition resolveHandler(String name) throws Exception {
		//-- Replace all / in the name by dots,
		String basename = name.replace('/', '.');
		ServiceClassDefinition hi = getServiceClassDefinition(basename);
		hi.initialize();
		return hi;
	}

	/**
	 * Tries to find the class specified on the system, using the default paths
	 * if necessary. If a class is found it is not yet checked for compliance.
	 * @param basename
	 * @return
	 */
	private Class<?> findClass(String basename) {
		ClassLoader	ldr	= getClass().getClassLoader();
		try {
			return ldr.loadClass(basename);
		} catch(Exception x) {
		}
		return null;
	}

	private ServiceClassDefinition getServiceClassDefinition(String basename) throws Exception {
		ServiceClassDefinition hi = null;
		synchronized(this) {
			hi = m_classDefMap.get(basename);
			if(hi != null)
				return hi;

			//-- Not cached yet. Get a ref
			Class<?> cl = findClass(basename);
			if(cl == null)
				throw new UnknownServiceClassException(basename);
			
			//-- Make sure this is annotated as a handler (security)
			AjaxHandler	am = cl.getAnnotation(AjaxHandler.class);
			if(am == null)
				throw new ServiceException("The class '"+cl.getCanonicalName()+"' is not annotated as an @AjaxHandler");

			hi = new ServiceClassDefinition(cl);
			m_classDefMap.put(basename, hi);
			m_classDefMap.put(cl.getCanonicalName(), hi); // Register actual name too; if basename == actual this just replaces the earlier put
			return hi;
		}
	}

	/**
	 * Resolves a package.class.methodname into a handler reference.
	 * @param byname
	 * @return
	 */
	private ServiceMethodDefinition findHandlerMethod(String rurl) throws Exception {
		int pos = rurl.lastIndexOf('.'); // Get separator between classname and method name
		if(pos == -1)
			throw new ServiceException("Invalid call: need [package].[class].[method] like to.etc.test.AClass.getThingy");
		String cn = rurl.substring(0, pos); // Class part,
		String mn = rurl.substring(pos + 1); // Method part.
		//        String  callstring = "call "+cn+"."+mn+": "+getRequest().getQueryString();
		//		System.out.println(callstring);

		//-- Resolve the URL into a handler class to execute,
		ServiceClassDefinition hi = resolveHandler(cn);
		if(hi == null)
			throw new UnknownServiceClassException(cn);

		//-- 1. Constraints on the handler itself: security
		String[] roles = hi.getRoles();
		if(roles.length > 0) {
			if(! hasAnyRole(roles))
				throw new ServiceExecException(hi.getHandlerClass(), "handler class is not allowed for the user's roles");
		}

		//-- 2. Resolve the method
		ServiceMethodDefinition mi = hi.getMethod(mn);
		roles = mi.getRoles();
		if(roles.length > 0) {
			if(! hasAnyRole(roles))
				throw new ServiceExecException(hi.getHandlerClass(), "handler method is not allowed for the user's roles");
		}
		return mi;
	}

	private boolean hasAnyRole(String[] roles) throws Exception {
		for(String s : roles) {
			if(cb.getAuthenticator().userHasRole(s))
				return true;
		}
		return false;
	}

	
	
	
	
	/*--------------------------------------------------------------*/
	/*	CODING:	Simple (one-method) call handling.					*/
	/*--------------------------------------------------------------*/
	/**
	 * This executes a single call. Both "return value" and "parameter 1 is output" calls
	 * are supported.
	 */
	public void	executeSingleCall(AjaxRequestContext ctx, String callsign, ResponseFormat formatoverride) throws Exception {
		ServiceMethodDefinition mi	= findHandlerMethod(callsign);		// Decode into some method
		Class<?>	clz	= mi.getServiceClassDefinition().getHandlerClass();
		Object		handler	= makeCallClass(clz, ctx);					// Ask whatever IOC container for the instance, fully injected

		//-- Render the result in the specified format.
		if(formatoverride == null || formatoverride == ResponseFormat.UNDEFINED) {
			formatoverride = mi.getResponseFormat();
			if(formatoverride == null || formatoverride == ResponseFormat.UNDEFINED) {
				formatoverride = getDefaultFormat();
			}
		}

		/*
		 * If this method has a return method we will call the method then render the
		 * result using one of the renderers.
		 */
		if(mi.getOutputClass() == null) {
			Object result = executeMethod(cb, sourceList, mi, handler);		// Use RequestContext (this) as source for parameters
			Writer	ow	= cb.getResponseWriter(formatoverride, mi.getMethod().getName());
			renderResponseObject(ow, formatoverride, result);
			return;
		}

		/*
		 * This method has an "output" first parameter which it will use to generate
		 * output. First we ask the context if it knows how to provide for a result; if
		 * not we make one ourselves using a JSON or XML output thingy.
		 */
		Object	oo	= cb.allocateOutput(mi.getOutputClass(), formatoverride);
		if(oo == null)
			oo	= allocateOutput(cb, mi.getOutputClass(), formatoverride);	// Try to allocate something myself

		/*
		 * The output class is assigned - call the method by hand.
		 */
		executeMethod(cb, sourceList, mi, handler, oo);
		cb.outputCompleted(oo);
	}




}
