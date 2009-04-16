package to.etc.domui.server;

import java.util.*;
import java.util.logging.*;

import to.etc.domui.state.*;
import to.etc.domui.trouble.*;

abstract public class AbstractContextMaker implements ContextMaker {
	public boolean	execute(final RequestContextImpl ctx) throws Exception {
		List<IRequestInterceptor>	il = ctx.getApplication().getInterceptorList();
		Exception	xx = null;
		try {
			callInterceptorsBegin(il, ctx);
			PageContext.internalSet(ctx);

			FilterRequestHandler	rh	= ctx.getApplication().findRequestHandler(ctx);
			if(rh == null)
				return false;
			rh.handleRequest(ctx);
			ctx.flush();
			return true;
		} catch(ThingyNotFoundException x) {
			ctx.getResponse().sendError(404, x.getMessage());
			return true;
		} catch(Exception xxx) {
			xx	= xxx;
			throw xxx;
		} finally {
			callInterceptorsAfter(il, ctx, xx);
			ctx.onRequestFinished();
			try { ctx.discard(); } catch(Exception x) {x.printStackTrace(); }
			PageContext.internalSet((RequestContextImpl)null);
		}
	}

	private void	callInterceptorsBegin(final List<IRequestInterceptor> il, final RequestContextImpl ctx) throws Exception {
		int	i;
		for(i = 0; i < il.size(); i++) {
			IRequestInterceptor	ri = il.get(i);
			try {
				ri.before(ctx);
			} catch(Exception x) {
				DomApplication.LOG.log(Level.SEVERE, "Exception in RequestInterceptor.before()", x);

				//-- Call enders for all already-called thingies
				while(--i >= 0) {
					ri = il.get(i);
					try {
						ri.after(ctx, x);
					} catch(Exception xx) {
						DomApplication.LOG.log(Level.SEVERE, "Exception in RequestInterceptor.after() in wrapup", xx);
					}
				}
				throw x;
			}
		}
	}

	private void	callInterceptorsAfter(final List<IRequestInterceptor> il, final RequestContextImpl ctx, final Exception x) throws Exception {
		Exception endx= null;

		for(int i = il.size(); --i >= 0;) {
			IRequestInterceptor	ri = il.get(i);
			try {
				ri.after(ctx, x);
			} catch(Exception xx) {
				if(endx == null)
					endx = xx;
				DomApplication.LOG.log(Level.SEVERE, "Exception in RequestInterceptor.after()", xx);
			}
		}
		if(endx != null)
			throw endx;
	}
}
