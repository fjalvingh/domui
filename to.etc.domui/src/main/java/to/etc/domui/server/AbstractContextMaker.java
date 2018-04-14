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

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.state.UIContext;
import to.etc.net.HttpCallException;
import to.etc.webapp.nls.NlsContext;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

abstract public class AbstractContextMaker implements IContextMaker {
	private static final String LOCALE_PARAM = "___locale";

	@Override
	abstract public void handleRequest(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws Exception;

	private static class Pair {
		@NonNull
		final private Pattern m_pattern;

		@NonNull
		final private String m_version;

		public Pair(@NonNull Pattern pattern, @NonNull String version) {
			m_pattern = pattern;
			m_version = version;
		}

		public boolean matches(@NonNull String url) {
			return m_pattern.matcher(url).matches();
		}

		@NonNull
		public String getVersion() {
			return m_version;
		}
	}

	/**
	 * Maps URL patterns to a default IE emulation mode to send as a header.
	 */
	private List<Pair> m_ieEmulationList = new ArrayList<Pair>();

	public AbstractContextMaker(ConfigParameters pp) throws UnavailableException {
		decodeParameters(pp);
	}

	private void decodeParameters(ConfigParameters pp) throws UnavailableException {
		String emu = pp.getString("ie-emulation");
		if(emu != null) {
			String[] patar = emu.split("\\s");				// pat:mode pat:mode
			for(String patmode : patar) {
				patmode = patmode.trim();
				if(patmode.length() == 0)
					continue;
				int pos = patmode.lastIndexOf(':');
				if(pos == -1)
					throw new UnavailableException("Missing ':' in ie-emulation parameter in web.xml");
				String pat = patmode.substring(0, pos);

				Pattern par;
				try {
					par = Pattern.compile(pat);
				} catch(Exception x) {
					throw new IllegalArgumentException("Invalid pattern '" + pat + "' in web.xml ie-emulation: " + x);
				}

				String xv = patmode.substring(pos + 1).trim();
				if(xv.length() != 0)
					m_ieEmulationList.add(new Pair(par, xv));
			}
		}
	}

	private Locale decodeLocale(@NonNull HttpServerRequestResponse rr, @NonNull final RequestContextImpl ctx) throws Exception {
		String forceloc = rr.getParameter(LOCALE_PARAM);
		if(null != forceloc) {
			String[] args = forceloc.split("_");
			if(args.length >= 1) {
				String lang = args[0];
				String country = args.length >= 2 ? args[1] : lang.toUpperCase();

				Locale loc = new Locale(lang, country);
				rr.getRequest().getSession().setAttribute(LOCALE_PARAM, loc);
				return loc;
			}
		}
		HttpSession ses = rr.getRequest().getSession(false);
		if(null != ses) {
			Locale loc = (Locale) ses.getAttribute(LOCALE_PARAM);
			if(null != loc)
				return loc;
		}

		return ctx.getApplication().getRequestLocale(rr.getRequest());
	}

	public void execute(@NonNull HttpServerRequestResponse requestResponse, @NonNull final RequestContextImpl ctx, FilterChain chain) throws Exception {
		//-- 201012 jal Set the locale for this request
		Locale loc = decodeLocale(requestResponse, ctx);
		NlsContext.setLocale(loc);

		List<IRequestInterceptor> il = ctx.getApplication().getInterceptorList();
		Exception xx = null;
		try {
			UIContext.internalSet(ctx);
			callInterceptorsBegin(il, ctx);

			boolean handled = ctx.getApplication().callRequestHandler(ctx);
			if(handled) {
				ctx.flush();
			} else {
				//-- Non-DomUI request.
				handleDoFilter(chain, requestResponse.getRequest(), requestResponse.getResponse());
			}

			//requestResponse.getResponse().addHeader("X-UA-Compatible", "IE=edge");	// 20110329 jal Force to highest supported mode for DomUI code.
			//requestResponse.getResponse().addHeader("X-XSS-Protection", "0");		// 20130124 jal Disable IE XSS filter, to prevent the idiot thing from seeing the CID as a piece of script 8-(
		} catch(HttpCallException x) {
			requestResponse.getResponse().sendError(x.getCode(), x.getMessage());
		} catch(Exception xxx) {
			xx = xxx;
			throw xxx;
		} finally {
			callInterceptorsAfter(il, ctx, xx);
			ctx.internalOnRequestFinished();
			try {
				ctx.discard();
			} catch(Exception x) {
				x.printStackTrace();
			}
			UIContext.internalClear();
		}
	}

	private void handleDoFilter(@NonNull FilterChain chain, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws ServletException, IOException {
		if(m_ieEmulationList.size() == 0) {
			chain.doFilter(request, response);
			return;
		}

		//-- Find whatever matches.
		String xv = null;
		String url = request.getRequestURI();
		for(Pair p : m_ieEmulationList) {
			if(p.matches(url)) {
				xv = p.getVersion();
				break;
			}
		}

		//-- No pattern available -> just use as-is, do not send any header.
		if(xv == null) {
			chain.doFilter(request, response);
			return;
		}

		WrappedHttpServetResponse wsr = new WrappedHttpServetResponse(url, response, xv);
		chain.doFilter(request, wsr);
		wsr.flushBuffer();
	}


	static public void callInterceptorsBegin(final List<IRequestInterceptor> il, final RequestContextImpl ctx) throws Exception {
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

	static public void callInterceptorsAfter(final List<IRequestInterceptor> il, final RequestContextImpl ctx, final Exception x) throws Exception {
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
