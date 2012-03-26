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
package to.etc.domui.server;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import to.etc.domui.state.*;
import to.etc.domui.trouble.*;
import to.etc.webapp.nls.*;

abstract public class AbstractContextMaker implements IContextMaker {
	@Override
	abstract public boolean handleRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws Exception;

	private boolean m_ie8header;

	public AbstractContextMaker(ConfigParameters pp) {
		if("true".equals(pp.getString("ie8header")))
			m_ie8header = true;
	}

	public boolean execute(final RequestContextImpl ctx, FilterChain chain) throws Exception {
		//-- 201012 jal Set the locale for this request
		Locale loc = ctx.getApplication().getRequestLocale(ctx.getRequest());
		NlsContext.setLocale(loc);

		List<IRequestInterceptor> il = ctx.getApplication().getInterceptorList();
		Exception xx = null;
		IFilterRequestHandler rh = null;
		try {
			UIContext.internalSet(ctx);
			callInterceptorsBegin(il, ctx);
			rh = ctx.getApplication().findRequestHandler(ctx);
			if(rh == null) {
				//-- Non-DomUI request.
				handleDoFilter(chain, ctx.getRequest(), ctx.getResponse());
				return false;
			}
			ctx.getResponse().addHeader("X-UA-Compatible", "IE=edge"); // 20110329 jal Force to highest supported mode for DomUI code.
			rh.handleRequest(ctx);
			ctx.flush();
			return true;
		} catch(ThingyNotFoundException x) {
			ctx.getResponse().sendError(404, x.getMessage());
			return true;
		} catch(Exception xxx) {
			xx = xxx;
			throw xxx;
		} finally {
			callInterceptorsAfter(il, ctx, xx);
			ctx.onRequestFinished();
			try {
				ctx.discard();
			} catch(Exception x) {
				x.printStackTrace();
			}
			UIContext.internalClear();
		}
	}

	private void handleDoFilter(FilterChain chain, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!m_ie8header) {
			chain.doFilter(request, response);
			return;
		}

		String url = request.getRequestURI();
		int pos = url.lastIndexOf('.');
		String ext;
		if(pos == -1)
			ext = "";
		else
			ext = url.substring(pos + 1).toLowerCase();
		if(!isIeHeaderable(ext)) {
			chain.doFilter(request, response);
			return;
		}

		WrappedHttpServetResponse wsr = new WrappedHttpServetResponse(url, response);
		chain.doFilter(request, wsr);
		wsr.flushBuffer();
	}

	private boolean isIeHeaderable(String suf) {
		return "jsp".equals(suf) || "html".equals(suf) || "htm".equals(suf) || "js".equals(suf);
	}

	private void callInterceptorsBegin(final List<IRequestInterceptor> il, final RequestContextImpl ctx) throws Exception {
		int i;
		for(i = 0; i < il.size(); i++) {
			IRequestInterceptor ri = il.get(i);
			try {
				ri.before(ctx);
			} catch(Exception x) {
				DomApplication.LOG.error("Exception in RequestInterceptor.before()", x);

				//-- Call enders for all already-called thingies
				while(--i >= 0) {
					ri = il.get(i);
					try {
						ri.after(ctx, x);
					} catch(Exception xx) {
						DomApplication.LOG.error("Exception in RequestInterceptor.after() in wrapup", xx);
					}
				}
				throw x;
			}
		}
	}

	private void callInterceptorsAfter(final List<IRequestInterceptor> il, final RequestContextImpl ctx, final Exception x) throws Exception {
		Exception endx = null;

		for(int i = il.size(); --i >= 0;) {
			IRequestInterceptor ri = il.get(i);
			try {
				ri.after(ctx, x);
			} catch(Exception xx) {
				if(endx == null)
					endx = xx;
				DomApplication.LOG.error("Exception in RequestInterceptor.after()", xx);
			}
		}
		if(endx != null)
			throw endx;
	}
}
