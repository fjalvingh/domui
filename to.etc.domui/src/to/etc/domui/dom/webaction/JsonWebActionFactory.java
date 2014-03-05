package to.etc.domui.dom.webaction;

import java.io.*;
import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.json.*;
import to.etc.util.*;

/**
 * This action factory tries to find a method with the name specified in actionCode which accepts a single java class type
 * which will be marshalled from a "json" request parameter. The response is either void (in which case it is assumed
 * the call did any of it's work by manipulating data) or it is an Object which will be rendered as a JSON response inside
 * the request.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 18, 2013
 */
public class JsonWebActionFactory implements WebActionRegistry.IFactory {
	@Override
	@Nullable
	public IWebActionHandler createHandler(@Nonnull Class< ? extends NodeBase> node, @Nonnull String actionCode) {
		for(Method m: node.getMethods()) {
			if(m.getName().equals(actionCode)) {
				Class< ? >[] par = m.getParameterTypes();
				if(par.length == 1) {
					Class< ? > formal = par[0];
					if(IRequestContext.class.isAssignableFrom(formal))
						return null;

					//-- All else matches.
					Type paramType = m.getGenericParameterTypes()[0];	// Get full parameter declaration,
					return new JsonWebAction(m, formal, paramType);
				}
				return null;											// Quick exit if name is found but arguments do not match
			}
		}
		return null;
	}

	/**
	 * Render the response as a JSON object, by default.
	 * @param calledMethod
	 * @param ctx
	 * @param response
	 * @throws Exception
	 */
	static public void renderResponse(@Nonnull Method calledMethod, @Nonnull RequestContextImpl ctx, @Nullable Object response) throws Exception {
		Writer out = ctx.getRequestResponse().getOutputWriter("application/javascript; charset=UTF-8", "utf-8");
		try {
			JSON.render(out, response);
		} finally {
			FileTool.closeAll(out);
		}
	}
}

class JsonWebAction implements IWebActionHandler {
	@Nonnull
	final private Method m_method;

	@Nonnull
	final private Class< ? > m_formal;

	@Nonnull
	final private Type m_paramType;

	public JsonWebAction(@Nonnull Method method, @Nonnull Class< ? > formal, @Nonnull Type paramType) {
		m_method = method;
		m_formal = formal;
		m_paramType = paramType;
	}

	@Override
	public void handleWebAction(@Nonnull NodeBase node, @Nonnull RequestContextImpl context, boolean responseExpected) throws Exception {
		String json = context.getParameter("json");						// Get required json parameter
		if(null == json)
			throw new IllegalArgumentException("The request parameter 'json' is missing for web action method " + m_method);

		StringReader sr = new StringReader(json);
		Object decoded = JSON.decode(m_formal, m_paramType, sr);
		try {
			Object response = m_method.invoke(node, decoded);
			if(responseExpected) {
				if(m_method.getReturnType() != Void.TYPE) {
					JsonWebActionFactory.renderResponse(m_method, context, response);
				}
			}
		} catch(Exception x) {
			throw WrappedException.unwrap(x);
		}
	}
}
