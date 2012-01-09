package to.etc.server.injector;

import java.lang.annotation.*;

import javax.servlet.http.*;

import to.etc.server.ajax.*;
import to.etc.server.misc.*;
import to.etc.server.servlet.*;

/**
 * Need a generic configurable IOC mechanism.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 23, 2009
 */
@Deprecated
public class RequestRetrieverProvider extends AnnotatedRetrieverProvider {
	@Override
	protected Retriever accepts(final Class sourcecl, final Class targetcl, final String name, final Annotation[] ann, final AjaxParam ap) {
		//    	System.out.println("Trying to find '"+name+"', type="+targetcl.toString()+", from source="+sourcecl.toString());
		if(RequestContext.class.isAssignableFrom(sourcecl) && name.equals("remoteUser")) {
			return new AnnotatedRetriever(String.class, name, ap) {
				@Override
				protected Object retrieveValuePrimitive(final Object source) throws Exception {
					if(!(source instanceof RequestContext)) {
						throw new IllegalStateException("?? RequestRetriever thingy called with source=" + source.getClass().toString());
					}
					RequestContext ctx = (RequestContext) source;
					return ctx.getRequest().getRemoteUser();
				}

				@Override
				public void releaseObject(final Object o) {
				}

				public String getDisplayName() {
					return "${request." + name + "}";
				}
			};
		}
		if(targetcl.isAssignableFrom(StupidServletStandardRoleRetriever.class)) {
			if(HttpServletRequest.class.isAssignableFrom(sourcecl)) {
				return new AnnotatedRetriever(String.class, name, ap) {
					@Override
					protected Object retrieveValuePrimitive(final Object source) throws Exception {
						if(!(source instanceof HttpServletRequest))
							throw new IllegalStateException("?? RequestRetriever thingy called with source=" + source.getClass().toString());
						HttpServletRequest rq = (HttpServletRequest) source;
						return new StupidServletStandardRoleRetrieverImpl(rq);
					}

					@Override
					public void releaseObject(final Object o) {
					}

					public String getDisplayName() {
						return "${roleRetriever." + name + "}";
					}

					@Override
					public Class getType() {
						return StupidServletStandardRoleRetriever.class;
					}
				};
			}
			if(RequestContext.class.isAssignableFrom(sourcecl)) {
				return new AnnotatedRetriever(String.class, name, ap) {
					@Override
					protected Object retrieveValuePrimitive(final Object source) throws Exception {
						if(!(source instanceof RequestContext)) {
							throw new IllegalStateException("?? RequestRetriever thingy called with source=" + source.getClass().toString());
						}
						RequestContext ctx = (RequestContext) source;
						return new StupidServletStandardRoleRetrieverImpl(ctx.getRequest());
					}

					@Override
					public void releaseObject(final Object o) {
					}

					public String getDisplayName() {
						return "${roleRetriever." + name + "}";
					}

					@Override
					public Class getType() {
						return StupidServletStandardRoleRetriever.class;
					}
				};
			}
		}

		return null;
	}
}
