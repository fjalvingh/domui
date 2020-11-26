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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.dom.IBrowserOutput;
import to.etc.domui.dom.PrettyXmlOutputWriter;
import to.etc.util.DeveloperOptions;
import to.etc.util.StringTool;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Main handler for DomUI page requests. This handles all requests that target or come
 * from a DomUI page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
final public class ApplicationRequestHandler implements IFilterRequestHandler {
	static final public Logger LOG = LoggerFactory.getLogger(ApplicationRequestHandler.class);

	@NonNull
	private final DomApplication m_application;

	@NonNull
	private OopsFrameRenderer m_oopsRenderer = new OopsFrameRenderer();

	@NonNull
	private ResponseCommandWriter m_commandWriter = new ResponseCommandWriter();

	private static boolean m_logPerf = DeveloperOptions.getBool("domui.logtime", false);

	ApplicationRequestHandler(@NonNull final DomApplication application) {
		m_application = application;
	}

	/**
	 * Accept .obit, the defined DomUI extension (.ui by default) and the empty URL if a home page is set in {@link DomApplication}.
	 */
	private boolean accepts(@NonNull IRequestContext ctx) {
		return m_application.getUrlExtension().equals(ctx.getExtension())
				|| ctx.getExtension().equals("obit")
				|| (m_application.getRootPage() != null && ctx.getPageName() == null && ! m_application.isIgnoredUrlPrefix(ctx.getPageParameters().getInputPath()))
				;
	}

	@Override
	public boolean handleRequest(@NonNull final RequestContextImpl ctx) throws Exception {
		if(! accepts(ctx))
			return false;

		PageRequestHandler ph = new PageRequestHandler(m_application, this, m_commandWriter, ctx);
		ph.executeRequest();
		return true;
	}

	/**
	 * Sends a redirect as a 304 MOVED command. This should be done for all full-requests.
	 */
	static public void generateHttpRedirect(RequestContextImpl ctx, String to, String rsn) throws Exception {
		to = appendPersistedParameters(to, ctx);
		if(! to.startsWith("/") && ! to.startsWith("http")) {
			to = "/" + ctx.getRequestResponse().getWebappContext() + to;
		}

		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter("text/html; charset=UTF-8", "utf-8"));
		out.writeRaw("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" + "<html><head><script language=\"javascript\"><!--\n"
			+ "location.replace(" + StringTool.strToJavascriptString(to, true) + " + location.hash);\n" + "--></script>\n" + "</head><body>" + rsn + "</body></html>\n");
	}

	/**
	 * Generate an AJAX redirect command. Should be used by all COMMAND actions.
	 */
	static public void generateAjaxRedirect(RequestContextImpl ctx, String url) throws Exception {
		if(! url.startsWith("/") && ! url.startsWith("http")) {
			url = "/" + ctx.getRequestResponse().getWebappContext() + url;
		}
		if(LOG.isInfoEnabled())
			LOG.info("redirecting to " + url);
		url = appendPersistedParameters(url, ctx);

		IBrowserOutput out = new PrettyXmlOutputWriter(ctx.getOutputWriter("text/xml; charset=UTF-8", "utf-8"));
		out.tag("redirect");
		out.attr("url", url);
		out.endAndCloseXmltag();
	}

	private static String appendPersistedParameters(String url, RequestContextImpl ctx) {
		Set<String> nameSet = ctx.getApplication().getPersistentParameterSet();
		if(nameSet.size() == 0)
			return url;
		Map<String, String> map = ctx.getPersistedParameterMap();
		StringBuilder sb = new StringBuilder(url);
		boolean first = ! url.contains("?");
		for(Entry<String, String> entry : map.entrySet()) {
			if(first) {
				sb.append('?');
				first = false;
			} else {
				sb.append('&');
			}
			StringTool.encodeURLEncoded(sb, entry.getKey());
			sb.append('=');
			StringTool.encodeURLEncoded(sb, entry.getValue());
		}
		return sb.toString();
	}

	/**
	 * Return the page renderer to use to render an Oops (Exception) page.
	 */
	public OopsFrameRenderer getOopsRenderer() {
		return m_oopsRenderer;
	}
}
