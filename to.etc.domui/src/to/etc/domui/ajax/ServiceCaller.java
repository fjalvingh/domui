package to.etc.domui.ajax;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import to.etc.server.ajax.*;
import to.etc.server.ajax.renderer.*;
import to.etc.server.ajax.renderer.json.*;
import to.etc.server.ajax.renderer.xml.*;
import to.etc.util.*;
import to.etc.xml.*;

/**
 * A generic class that allows for Java services (methods on a service class)
 * to be called using several calling sequences and entries.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 16, 2006
 */
public class ServiceCaller {
	static private final Logger		LOG = Logger.getLogger(ServiceCaller.class.getName());

	static private boolean[]		PARAMONE = {true};

	/** Maps keys to resolved handler info thingies, for speed. */
	private final Map<String, ServiceClassDefinition> m_classDefMap = new HashMap<String, ServiceClassDefinition>();

	private final XmlRegistry 		m_xmlRegistry = new XmlRegistry();

	private final JSONRegistry 		m_JSONRegistry = new JSONRegistry();

	private ResponseFormat 			m_defaultFormat = ResponseFormat.XML;

	public ServiceCaller() {
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Class name resolver.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Tries to find the class specified on the system, using the default paths
	 * if necessary. If a class is found it is not yet checked for compliance.
	 * @param basename
	 * @return
	 */
	private Class<?> findClass(final String basename) {
		ClassLoader	ldr	= getClass().getClassLoader();
		try {
			return ldr.loadClass(basename);
		} catch(Exception x) {
		}
		return null;
	}

	private ServiceClassDefinition getServiceClassDefinition(final String basename) throws Exception {
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
	 * Returns a service class definition for the class name specified. The classname
	 * can be a complete classname. If it is not it is located by scanning the default
	 * package list.
	 * @param name	The dotted package.classname, or a single classname.
	 */
	ServiceClassDefinition resolveHandler(final String name) throws Exception {
		String basename = name.replace('/', '.');							// Unslash name
		ServiceClassDefinition hi = getServiceClassDefinition(basename);
		hi.initialize();
		return hi;
	}

	/**
	 * Resolves a package.class.methodname into a handler reference.
	 * @param byname
	 * @return
	 */
	private ServiceMethodDefinition findHandlerMethod(final ServiceCallerCallback cb, final String rurl) throws Exception {
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
			if(! hasAnyRole(cb, roles))
				throw new ServiceExecException(hi.getHandlerClass(), "handler class is not allowed for the user's roles");
		}

		//-- 2. Resolve the method
		ServiceMethodDefinition mi = hi.getMethod(mn);
		roles = mi.getRoles();
		if(roles.length > 0) {
			if(! hasAnyRole(cb, roles))
				throw new ServiceExecException(hi.getHandlerClass(), "handler method is not allowed for the user's roles");
		}
		return mi;
	}

	private boolean hasAnyRole(final ServiceCallerCallback cb, final String[] roles) throws Exception {
		for(String s : roles) {
			if(cb.hasRight(s))
				return true;
		}
		return false;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Service Object allocation and injection.			*/
	/*--------------------------------------------------------------*/
	/**
	 * Allocates a handler for the request to execute, if needed. If the
	 * call is a static call this returns null, else it returns a fully
	 * initialized (injected) copy of the handler class.
	 * @param mi
	 * @return
	 */
	private Object allocateHandler(final ServiceCallerCallback cb, final ServiceMethodDefinition mi) throws Exception {
		if(mi.isStatic())
			return null;
		return cb.createHandlerClass(mi.getServiceClassDefinition().getHandlerClass());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple (one-method) call handling.					*/
	/*--------------------------------------------------------------*/
	/**
	 * This executes a single call. Both "return value" and "parameter 1 is output" calls
	 * are supported.
	 */
	public void	executeSingleCall(final ServiceCallerCallback cb, final String callsign, ResponseFormat formatoverride) throws Exception {
		ServiceMethodDefinition mi	= findHandlerMethod(cb, callsign);	// Decode into some method
		Object handler = allocateHandler(cb, mi);						// We always need a handler instance,

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
			Object result = executeMethod(cb, mi, handler);		// Use RequestContext (this) as source for parameters
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
		executeMethod(cb, mi, handler, oo);
		cb.outputCompleted(oo);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Call parameter provisioning and method call code.	*/
	/*--------------------------------------------------------------*/

	private class MethodCallHelper {
		private final ServiceMethodDefinition	m_methodDef;
		private final Object[]					m_param;
		private final Class<?>[]				m_formals;

		public MethodCallHelper(final ServiceMethodDefinition mi) {
			m_methodDef = mi;
			m_formals	= mi.getMethod().getParameterTypes();
			m_param		= new Object[m_formals.length];
		}

		public Object invoke(final Object handler) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

	}

	/**
	 * This takes whatever source (either the request context or a JSON/XML parameter map) and
	 * assigns parameters to the call, then issues the call. The result is returned.
	 *
	 * @param mi
	 * @param handler
	 * @param sourceobject
	 * @return
	 * @throws Exception
	 */
	private Object executeMethod(final ServiceCallerCallback cb, final ServiceMethodDefinition mi, final Object handler) throws Exception {
		Object result;
		long ts = System.nanoTime();
		StringBuilder sb = new StringBuilder();
		sb.append("SVC: call ");
		sb.append(mi.toString());
		try {
			MethodCallHelper ch	= new MethodCallHelper(mi);
//			ch.assignParameters();
			result = ch.invoke(handler);					// Quick invoke, does the parameter setting.
			sb.append(": okay, result=");
			if(result == null)
				sb.append("null");
			else
				sb.append(result.getClass().getName());
		} catch(InvocationTargetException ix) {
			Throwable x = ix.getCause();
			sb.append(": exception ");
			sb.append(x.toString());

			//-- Create a service exception from this
			throw new ServiceExecException(x, mi.getMethod(), x.getMessage());
		} finally {
			ts = System.nanoTime() - ts;
			sb.append(" (");
			sb.append(StringTool.strNanoTime(ts));
			sb.append(")");
			LOG.info(sb.toString());
		}
		return result;
	}

	/**
	 * Calls the object when it has it's own object parameter.
	 *
	 * @param mi
	 * @param handler
	 * @param sourceobject
	 * @return
	 * @throws Exception
	 */
	private void executeMethod(final ServiceCallerCallback ctx, final ServiceMethodDefinition mi, final Object handler, final Object output) throws Exception {
		long ts = System.nanoTime();
		StringBuilder sb = new StringBuilder();
		sb.append("SVC: call ");
		sb.append(mi.toString());
		try {
//			List<ParameterInjectorSet>	list = getMethodInjectorCache().getInjectorSet("B", sourceList, mi.getMethod(), PARAMONE);
			MethodCallHelper ch	= new MethodCallHelper(mi);
//			ch.calculateParameters(ctx);				// Calculate all parameters but p0
//			ch.setParameter(0, output);					// Set parameter 0
			ch.invoke(handler);							// Invoke and release
			sb.append(": okay (result written to output param)");
		} catch(InvocationTargetException ix) {
			Throwable x = ix.getCause();
			sb.append(": exception ");
			sb.append(x.toString());

			//-- Create a service exception from this
			throw new ServiceExecException(x, mi.getMethod(), x.getMessage());
		} finally {
			ts = System.nanoTime() - ts;
			sb.append(" (");
			sb.append(StringTool.strNanoTime(ts));
			sb.append(")");
			LOG.info(sb.toString());
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Response rendering.									*/
	/*--------------------------------------------------------------*/

	private void renderResponseObject(final Writer ow, final ResponseFormat rf, final Object result) throws Exception {
		XmlWriter xw = new XmlWriter(ow);
		switch(rf){
			default:
				XmlRenderer xr = new XmlRenderer(getXmlRegistry(), xw);
				xr.render(result);
				break;

			case JSON:
				JSONRenderer jr = new JSONRenderer(getJSONRegistry(), xw, true);
				jr.render(result);
				break;
		}
	}

	/**
	 * This allocates an output class instance by wrapping some provided writer if the
	 * class passed is acceptible.
	 *
	 * @param ctx
	 * @param oc
	 * @param rf
	 * @return
	 * @throws Exception
	 */
	private <T> T	allocateOutput(final ServiceCallerCallback ctx, final Class<T> oc, final ResponseFormat rf) throws Exception {
		if(oc.isAssignableFrom(StructuredWriter.class)) {		// Parameter is structured writer?
			/*
			 * Get the default writer from the context, then wrap either a JSON or XML writer around it.
			 */
			Writer	ow	= ctx.getResponseWriter(rf, "undefined");
			switch(rf) {
				default:
					throw new ServiceException("Unknown response format "+rf);
				case JSON:
					JSONRenderer jr = new JSONRenderer(getJSONRegistry(), new IndentWriter(ow), true);
					return (T)new JSONStructuredWriter(jr);
				case XML:
					XmlRenderer xr = new XmlRenderer(getXmlRegistry(), new XmlWriter(ow));
					return (T)new XMLStructuredWriter(xr);
			}
		}

		if(oc.isAssignableFrom(Writer.class)) {				// Generic writer?
			return (T)ctx.getResponseWriter(rf, "undefined");	// Just return it
		}
		throw new ServiceException("The output class '"+oc.toString()+"' is not acceptable.");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Bulk call handling - JSON							*/
	/*--------------------------------------------------------------*/

//	static private class BulkSourceGetter implements InjectorSourceRetriever {
//		private final InjectorSourceRetriever	m_parent;
//
//		private Map<Object, Object>		m_current;
//
//		BulkSourceGetter(final InjectorSourceRetriever r) {
//			m_parent = r;
//		}
//		public Object getInjectorSource(final Class<?> sourcecl) throws Exception {
//			if(sourcecl == Map.class)
//				return m_current;
//			return m_parent.getInjectorSource(sourcecl);
//		}
//		public void setCurrent(final Map<Object, Object> current) {
//			m_current = current;
//		}
//	}
//
//	/**
//	 * Reads and executes a JSON bulk request. The JSON structure is an array of
//	 * objects. Each object has the following keys:
//	 * <dl>
//	 * 	<dt>method: string</dt>
//	 * 	<dd>The full class and method name of the thing to call. The classname and method name are
//	 * 		separated by a dot.</dd>
//	 *	<dt>parameters: object</dt>
//	 *	<dd>The call's parameters, as an object where each key will get evaluated as a parameter.</dd>
//	 *	<dt>id: string or number</dt>
//	 *	<dd>When present the response will echo this ID</dd>
//	 *	<dt>cancelonerror: boolean<dt>
//	 *	<dd>When present and true, the bulk handler will cancel the rest of the calls if one call fails.</dd>
//	 * </dl>
//	 * @param json
//	 * @throws Exception
//	 */
//	public void executeBulkJSON(final ServiceCallerCallback cb, final String json) throws Exception {
//		LOG.info("SVC: JSON bulk call: " + json);
//		Object jsonds = JSONParser.parseJSON(json);
//		if(! (jsonds instanceof List))
//			throw new ServiceException("The bulk call JSON data must be an array");
//		List<Object> reslist = new ArrayList<Object>();
//		boolean cancelled = false;
//		int ix = 0;
//		BulkSourceGetter	bsg = new BulkSourceGetter(cb);		// Thingy to collect parameters for each call
//		List<Class<? extends Object>>	sourceList = new ArrayList<Class<? extends Object>>(getSourceClassesList());
//		sourceList.add(Map.class);								// Append map type containing JSON parameters
//
//		for(Object o : (List) jsonds) {
//			//-- This should be a Map containing the command names. Execute each and append the result to the result list for later rendering
//			if(!(o instanceof Map))
//				throw new ServiceException("The bulk call's list member type of item# " + ix + " is not a JSON object");
//			if(cancelled)
//				reslist.add(new HashMap<Object, Object>());
//			else {
//				Object res = executeSingleJSON(cb, sourceList, (Map) o, ix, bsg);
//				reslist.add(res);
//			}
//			ix++;
//		}
//
////		//-- Render back the result.
//		Writer	ow	= cb.getResponseWriter(ResponseFormat.JSON, "bulk");
//		renderResponseObject(ow, ResponseFormat.JSON, reslist);
//	}
//
//	/**
//	 * Tries to execute a single JSON-specified call.
//	 * @param callmap
//	 * @return
//	 * @throws Exception
//	 */
//	private Object executeSingleJSON(final ServiceCallerCallback cb, final List<Class<? extends Object>> sourceList, final Map<Object, Object> callmap, final int index, final BulkSourceGetter isr) throws Exception {
//		String name = (String) callmap.get("method");
//		if(name == null)
//			throw new ServiceException("Missing 'method' property in list item #" + index); // Fatal.
//		Object o = callmap.get("id");
//		String id = o == null ? null : o.toString();
//		o = callmap.get("cancelonerror");
//		boolean cancel = false;
//		if(o != null && StringTool.dbGetBool((String) o))
//			cancel = true;
//		o = callmap.get("parameters");
//		if(o != null && !(o instanceof Map))
//			throw new ServiceException("The 'parameters' item is not a Map in list item #" + index); // Fatal.
//		Map<Object, Object> parameters = o == null ? new HashMap<Object, Object>() : (Map) o;
//		isr.setCurrent(parameters);
//		ServiceMethodDefinition mi = findHandlerMethod(cb, name); 		// Find the appropriate method to call, and check permissions.
//		Object handler = allocateHandler(isr, sourceList, mi);			// We always need a handler instance,
//		Object	result = executeMethod(isr, sourceList, mi, handler);
//		Map<Object,Object> resmap = new HashMap<Object, Object>();
//		if(id != null)
//			resmap.put("id", id);
//		resmap.put("result", result);
//		return resmap;
//	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Dumb code.											*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 */
	public XmlRegistry getXmlRegistry() {
		return m_xmlRegistry;
	}
	public ResponseFormat getDefaultFormat() {
		return m_defaultFormat;
	}
	public void setDefaultResponseFormat(final ResponseFormat rf) {
		m_defaultFormat = rf;
	}
	public JSONRegistry getJSONRegistry() {
		return m_JSONRegistry;
	}
}
