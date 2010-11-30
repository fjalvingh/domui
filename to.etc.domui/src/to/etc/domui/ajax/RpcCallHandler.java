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

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import org.slf4j.*;

import to.etc.domui.annotations.*;
import to.etc.util.*;
import to.etc.webapp.ajax.renderer.*;
import to.etc.webapp.ajax.renderer.json.*;
import to.etc.webapp.ajax.renderer.xml.*;
import to.etc.xml.*;

/**
 * A generic class that allows for Java services (methods on a service class)
 * to be called using several calling sequences and entries.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 16, 2006
 */
public class RpcCallHandler {
	static private final Logger LOG = LoggerFactory.getLogger(RpcCallHandler.class);

	//	static private boolean[]		PARAMONE = {true};
	//
	/** Maps keys to resolved handler info thingies, for speed. */
	private final Map<String, RpcClassDefinition> m_classDefMap = new HashMap<String, RpcClassDefinition>();

	private final XmlRegistry m_xmlRegistry = new XmlRegistry();

	private final JSONRegistry m_JSONRegistry = new JSONRegistry();

	private ResponseFormat m_defaultFormat = ResponseFormat.XML;

	public RpcCallHandler() {}


	/*--------------------------------------------------------------*/
	/*	CODING:	Class name resolver.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Tries to find the class specified on the system, using the default paths
	 * if necessary. If a class is found it is not yet checked for compliance.
	 * @param basename
	 * @return
	 */
	private Class< ? > findClass(final String basename) {
		ClassLoader ldr = getClass().getClassLoader();
		try {
			return ldr.loadClass(basename);
		} catch(Exception x) {}
		return null;
	}

	private RpcClassDefinition getServiceClassDefinition(final String basename) throws Exception {
		RpcClassDefinition hi = null;
		synchronized(this) {
			hi = m_classDefMap.get(basename);
			if(hi != null)
				return hi;

			//-- Not cached yet. Get a ref
			Class< ? > cl = findClass(basename);
			if(cl == null)
				throw new RpcException("Unknown class '" + basename + "'");

			//-- Make sure this is annotated as a handler (security)
			AjaxHandler am = cl.getAnnotation(AjaxHandler.class);
			if(am == null)
				throw new RpcException("The class '" + cl.getCanonicalName() + "' is not annotated as an @AjaxHandler");

			hi = new RpcClassDefinition(cl);
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
	@Nonnull
	RpcClassDefinition resolveHandler(final String name) throws Exception {
		String basename = name.replace('/', '.'); // Unslash name
		RpcClassDefinition hi = getServiceClassDefinition(basename);
		hi.initialize();
		return hi;
	}

	/**
	 * Resolves a package.class.methodname into a handler reference.
	 * @param byname
	 * @return
	 */
	private RpcMethodDefinition findHandlerMethod(final IRpcCallContext cb, final String rurl) throws Exception {
		int pos = rurl.lastIndexOf('.'); // Get separator between classname and method name
		if(pos == -1)
			throw new RpcException("Invalid call: need [package].[class].[method] like to.etc.test.AClass.getThingy");
		String cn = rurl.substring(0, pos); // Class part,
		String mn = rurl.substring(pos + 1); // Method part.
		//        String  callstring = "call "+cn+"."+mn+": "+getRequest().getQueryString();
		//		System.out.println(callstring);

		//-- Resolve the URL into a handler class to execute,
		RpcClassDefinition hi = resolveHandler(cn);
//		if(hi == null)
//			throw new RpcException("Unknown AJAX service class '" + cn + "'");

		//-- 1. Constraints on the handler itself: security
		String[] roles = hi.getRoles();
		if(roles.length > 0) {
			if(!hasAnyRole(cb, roles))
				throw new RpcException(hi.getHandlerClass() + ": handler class is not allowed for the user's roles");
		}

		//-- 2. Resolve the method
		RpcMethodDefinition mi = hi.getMethod(mn);
		roles = mi.getRoles();
		if(roles.length > 0) {
			if(!hasAnyRole(cb, roles))
				throw new RpcException(mi.getMethod().toString() + ": handler method is not allowed for the user's roles");
		}
		return mi;
	}

	private boolean hasAnyRole(final IRpcCallContext cb, final String[] roles) throws Exception {
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
	private Object allocateHandler(final IRpcCallContext cb, final RpcMethodDefinition mi) throws Exception {
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
	public void executeSingleCall(final IRpcCallContext cb, final IParameterProvider pv, final String callsign, ResponseFormat formatoverride) throws Exception {
		RpcMethodDefinition mi = findHandlerMethod(cb, callsign); // Decode into some method
		Object handler = allocateHandler(cb, mi); // We always need a handler instance,

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
			Object result = executeMethod(cb, mi, handler, pv, null); // Use RequestContext (this) as source for parameters
			Writer ow = cb.getResponseWriter(formatoverride, mi.getMethod().getName());
			renderResponseObject(ow, formatoverride, result);
			return;
		}

		/*
		 * This method has an "output" first parameter which it will use to generate
		 * output. First we ask the context if it knows how to provide for a result; if
		 * not we make one ourselves using a JSON or XML output thingy.
		 */
		Object oo = cb.allocateOutput(mi.getOutputClass(), formatoverride);
		if(oo == null)
			oo = allocateOutput(cb, mi.getOutputClass(), formatoverride); // Try to allocate something myself

		/*
		 * The output class is assigned - call the method by hand.
		 */
		executeMethod(cb, mi, handler, pv, oo);
		cb.outputCompleted(oo);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Call parameter provisioning and method call code.	*/
	/*--------------------------------------------------------------*/


	static public <T extends Annotation> T findAnnotation(final Annotation[] annar, final Class<T> clz) {
		for(Annotation ann : annar) {
			if(ann.annotationType() == clz)
				return (T) ann;
		}
		return null;
	}

	/**
	 * This takes whatever source (either the request context or a JSON/XML parameter map) and
	 * assigns parameters to the call, then issues the call. The result is returned.
	 *
	 * @param mi
	 * @param handler
	 * @return
	 * @throws Exception
	 */
	private Object executeMethod(final IRpcCallContext cb, final RpcMethodDefinition mi, final Object handler, final IParameterProvider papro, final Object output) throws Exception {
		Object result;
		long ts = System.nanoTime();
		StringBuilder sb = new StringBuilder();
		sb.append("SVC: call ");
		sb.append(mi.toString());
		try {
			Class< ? >[] formals = mi.getMethod().getParameterTypes();
			Object[] args = new Object[formals.length];
			Annotation[][] argannar = mi.getMethod().getParameterAnnotations();
			int oix = 0; // Output parametert output
			int ix = 0; // Actual parameter
			if(output != null) {
				//-- The thingy is void and generates it's own output somehow
				args[0] = output;
				oix = 1; // Skip 1st parameter in assignment from input
			}
			while(oix < formals.length) {
				AjaxParam apm = findAnnotation(argannar[oix], AjaxParam.class);
				if(apm == null)
					throw new RpcException("Parameter " + oix + " of method " + mi.getMethod() + " is missing an @AjaxParam annotation.");
				args[oix] = papro.findParameterValue(formals[oix], argannar[oix], ix, apm);
				if(args[oix] == IParameterProvider.NO_VALUE)
					throw new RpcException("Parameter " + oix + " of method " + mi.getMethod() + " has no value.");
				oix++;
			}
			result = mi.getMethod().invoke(handler, args);
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
			throw new RpcException(x, x.getMessage());
		} finally {
			ts = System.nanoTime() - ts;
			sb.append(" (");
			sb.append(StringTool.strNanoTime(ts));
			sb.append(")");
			LOG.info(sb.toString());
		}
		return result;
	}

	//	/**
	//	 * Calls the object when it has it's own object parameter.
	//	 *
	//	 * @param mi
	//	 * @param handler
	//	 * @param sourceobject
	//	 * @return
	//	 * @throws Exception
	//	 */
	//	private void executeMethod(final IRpcCallContext ctx, final RpcMethodDefinition mi, final Object handler, final Object output) throws Exception {
	//		long ts = System.nanoTime();
	//		StringBuilder sb = new StringBuilder();
	//		sb.append("SVC: call ");
	//		sb.append(mi.toString());
	//		try {
	////			List<ParameterInjectorSet>	list = getMethodInjectorCache().getInjectorSet("B", sourceList, mi.getMethod(), PARAMONE);
	//
	//
	////			ch.calculateParameters(ctx);				// Calculate all parameters but p0
	////			ch.setParameter(0, output);					// Set parameter 0
	//			ch.invoke(handler);							// Invoke and release
	//			sb.append(": okay (result written to output param)");
	//		} catch(InvocationTargetException ix) {
	//			Throwable x = ix.getCause();
	//			sb.append(": exception ");
	//			sb.append(x.toString());
	//
	//			//-- Create a service exception from this
	//			throw new ServiceExecException(x, mi.getMethod(), x.getMessage());
	//		} finally {
	//			ts = System.nanoTime() - ts;
	//			sb.append(" (");
	//			sb.append(StringTool.strNanoTime(ts));
	//			sb.append(")");
	//			LOG.info(sb.toString());
	//		}
	//	}

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
	private <T> T allocateOutput(final IRpcCallContext ctx, final Class<T> oc, final ResponseFormat rf) throws Exception {
		if(oc.isAssignableFrom(StructuredWriter.class)) { // Parameter is structured writer?
			/*
			 * Get the default writer from the context, then wrap either a JSON or XML writer around it.
			 */
			Writer ow = ctx.getResponseWriter(rf, "undefined");
			switch(rf){
				default:
					throw new RpcException("Unknown response format " + rf);
				case JSON:
					JSONRenderer jr = new JSONRenderer(getJSONRegistry(), new IndentWriter(ow), true);
					return (T) new JSONStructuredWriter(jr);
				case XML:
					XmlRenderer xr = new XmlRenderer(getXmlRegistry(), new XmlWriter(ow));
					return (T) new XMLStructuredWriter(xr);
			}
		}

		if(oc.isAssignableFrom(Writer.class)) { // Generic writer?
			return (T) ctx.getResponseWriter(rf, "undefined"); // Just return it
		}
		throw new RpcException("The output class '" + oc.toString() + "' is not acceptable.");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Bulk call handling - JSON							*/
	/*--------------------------------------------------------------*/

	/**
	 * Reads and executes a JSON bulk request. The JSON structure is an array of
	 * objects. Each object has the following keys:
	 * <dl>
	 * 	<dt>method: string</dt>
	 * 	<dd>The full class and method name of the thing to call. The classname and method name are
	 * 		separated by a dot.</dd>
	 *	<dt>parameters: object</dt>
	 *	<dd>The call's parameters, as an object where each key will get evaluated as a parameter.</dd>
	 *	<dt>id: string or number</dt>
	 *	<dd>When present the response will echo this ID</dd>
	 *	<dt>cancelonerror: boolean<dt>
	 *	<dd>When present and true, the bulk handler will cancel the rest of the calls if one call fails.</dd>
	 * </dl>
	 * @param json
	 * @throws Exception
	 */
	public void executeBulkJSON(final IRpcCallContext cb, final String json) throws Exception {
		LOG.info("SVC: JSON bulk call: " + json);
		Object jsonds = JSONParser.parseJSON(json);
		if(!(jsonds instanceof List< ? >))
			throw new RpcException("The bulk call JSON data must be an array");
		List<Object> reslist = new ArrayList<Object>();
		boolean cancelled = false;
		int ix = 0;
		//		BulkSourceGetter	bsg = new BulkSourceGetter(cb);		// Thingy to collect parameters for each call
		//		List<Class<? extends Object>>	sourceList = new ArrayList<Class<? extends Object>>(getSourceClassesList());
		//		sourceList.add(Map.class);								// Append map type containing JSON parameters

		for(Object o : (List< ? >) jsonds) {
			//-- This should be a Map containing the command names. Execute each and append the result to the result list for later rendering
			if(!(o instanceof Map< ? , ? >))
				throw new RpcException("The bulk call's list member type of item# " + ix + " is not a JSON object");
			if(cancelled)
				reslist.add(new HashMap<Object, Object>());
			else {
				Object res = executeSingleJSON(cb, (Map<Object, Object>) o, ix);
				reslist.add(res);
			}
			ix++;
		}

		//		//-- Render back the result.
		Writer ow = cb.getResponseWriter(ResponseFormat.JSON, "bulk");
		renderResponseObject(ow, ResponseFormat.JSON, reslist);
	}

	/**
	 * Tries to execute a single JSON-specified call.
	 * @param callmap
	 * @return
	 * @throws Exception
	 */
	private Object executeSingleJSON(final IRpcCallContext cb, final Map<Object, Object> callmap, final int index) throws Exception {
		String name = (String) callmap.get("method");
		if(name == null)
			throw new RpcException("Missing 'method' property in list item #" + index); // Fatal.
		Object o = callmap.get("id");
		String id = o == null ? null : o.toString();
		o = callmap.get("cancelonerror");
		//		boolean cancel = false;
		//		if(o != null && StringTool.dbGetBool((String) o))
		//			cancel = true;
		o = callmap.get("parameters");
		if(o != null && !(o instanceof Map< ? , ? >))
			throw new RpcException("The 'parameters' item is not a Map in list item #" + index); // Fatal.
		Map<Object, Object> parameters = o == null ? new HashMap<Object, Object>() : (Map<Object, Object>) o;
		RpcMethodDefinition mi = findHandlerMethod(cb, name); // Find the appropriate method to call, and check permissions.
		Object handler = allocateHandler(cb, mi); // We always need a handler instance,
		JsonParameterProvider pp = new JsonParameterProvider(parameters);
		Object result = executeMethod(cb, mi, handler, pp, null);
		Map<Object, Object> resmap = new HashMap<Object, Object>();
		if(id != null)
			resmap.put("id", id);
		resmap.put("result", result);
		return resmap;
	}

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
