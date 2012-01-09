package to.etc.server.injector;

import java.lang.annotation.*;

import to.etc.server.ajax.*;
import to.etc.server.misc.*;
import to.etc.server.servlet.*;

/**
 * This provides retrievers for names in a servlet context.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 11, 2006
 */
public class RequestParameterRetrieverProvider extends AnnotatedRetrieverProvider {
	@Override
	protected Retriever accepts(final Class sourcecl, final Class targetcl, final String name, Annotation[] ann, final AjaxParam ap) {
		if(RequestContext.class.isAssignableFrom(sourcecl)) {
			return new AnnotatedRetriever(String.class, name, ap) {
				@Override
				protected Object retrieveValuePrimitive(Object source) throws Exception {
					if(!(source instanceof RequestContext)) {
						throw new IllegalStateException("?? RequestContextRetriever thingy called with source=" + source.getClass().toString());
					}
					RequestContext ctx = (RequestContext) source;
					String[] pv = ctx.getRequest().getParameterValues(name);
					if(pv == null || pv.length == 0)
						return NO_VALUE;
					if(pv.length > 1)
						throw new ParameterException("The value for the injector parameter '" + name + "' must be a single request value").setParameterName(name);
					if(ap.json()) {
						//                    	System.out.println("ajax: json value for parameter '"+name+"' is "+pv[0]);
						//-- Convert the input from JSON to whatever object is needed,
						if(targetcl == Object.class) {
							//-- Generic assignment
							return JSONParser.parseJSON(pv[0]);
						} else {
							//-- Assign using a base class structure
							return JSONParser.parseJSON(pv[0], targetcl);
						}
					}

					return pv[0];
				}

				@Override
				public void releaseObject(Object o) {
				}

				public String getDisplayName() {
					return "${request." + name + "}";
				}
			};
		}
		return null;
	}
}
