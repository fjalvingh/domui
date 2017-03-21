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
package to.etc.domui.testsupport;

import java.io.*;

import javax.annotation.*;

import to.etc.domui.dom.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

public class TUtilDomUI {
	static private volatile AppSession m_session;

	static private DomApplication m_application;

	static public synchronized void setApplication(DomApplication application) throws Exception {
		ConfigParameters cp = new ConfigParameters() {
			@Override
			public String getString(@Nonnull String name) {
				return null;
			}

			@Nonnull
			@Override
			public File getWebFileRoot() {
				return new File("/tmp"); // FIXME Howto?
			}
		};
		application.internalInitialize(cp, false);
		m_application = application;
	}

	static public synchronized DomApplication getApplication() throws Exception {
		if(m_application == null) {
			m_application = new DomApplication() {
				@Override
				public Class< ? extends UrlPage> getRootPage() {
					return null;
				}
			};
			ConfigParameters cp = new ConfigParameters() {
				@Nullable
				@Override
				public String getString(@Nonnull String name) {
					return null;
				}

				@Nonnull
				@Override
				public File getWebFileRoot() {
					return new File("/tmp"); // FIXME Howto?
				}
			};

			m_application.internalInitialize(cp, false);
		}
		return m_application;
	}

	static public AppSession getAppSession() throws Exception {
		getApplication();
		if(m_session == null) {
			m_session = new AppSession(getApplication());
		}
		return m_session;
	}

	static public BrowserVersion getBrowserVersion() {
		return getBrowserVersionFFox30();
	}

	static public BrowserVersion getBrowserVersionIE7() {
		return BrowserVersion.parseUserAgent("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)");
	}

	static public BrowserVersion getBrowserVersionIE8() {
		return BrowserVersion.parseUserAgent("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
	}

	static public BrowserVersion getBrowserVersionFFox30() {
		return BrowserVersion.parseUserAgent("Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.14) Gecko/2009090217 Ubuntu/9.04 (jaunty) Firefox/3.0.14");
	}

	static public BrowserVersion getBrowserVersionKonqueror() {
		return BrowserVersion.parseUserAgent("Mozilla/5.0 (compatible; Konqueror/3.5; Linux; en_US) KHTML/3.5.10 (like Gecko) (Debian)");
	}


	/**
	 * Create a page structure valid for testing pps. Do not make public: using UrlPage is unsafe before this
	 * is called, so a page must be fully created using one of these methods here.
	 * @param pg
	 * @return
	 */
	static private Page initPage(UrlPage pg, PageParameters pp) throws Exception {
		getApplication();
		if(pp == null)
			pp = new PageParameters();
		Page p = new Page(pg);
		WindowSession ws = new WindowSession(getAppSession());
		ConversationContext cc = new ConversationContext();
		ws.acceptNewConversation(cc);
		p.internalInitialize(pp, cc);
		cc.internalRegisterPage(p, pp);
		return p;
	}

	static public Page createPage(Class< ? extends UrlPage> clz, PageParameters pp) throws Exception {
		if(pp == null)
			pp = new PageParameters();
		UrlPage pg = clz.newInstance(); // Should have parameterless ctor
		Page p = initPage(pg, pp);
		return p;
	}

	static public Page createPage(Class< ? extends UrlPage> clz) throws Exception {
		return createPage(clz, null);
	}

	/**
	 * Create an empty, parameterless UrlPage page to use to add nodes to.
	 * @return
	 * @throws Exception
	 */
	static public UrlPage createBody() throws Exception {
		return createPage(UrlPage.class).getBody();
	}

	/**
	 * Create an empty UrlPage with page parameters.
	 * @param pp
	 * @return
	 * @throws Exception
	 */
	static public UrlPage createBody(PageParameters pp) throws Exception {
		return createPage(UrlPage.class, pp).getBody();
	}

	/**
	 * Create a page body from the specified class.
	 * @param <T>
	 * @param clz
	 * @param pp
	 * @return
	 * @throws Exception
	 */
	static public <T extends UrlPage> T createBody(Class<T> clz, PageParameters pp) throws Exception {
		return (T) createPage(clz, pp).getBody();
	}

	/**
	 * Create a page body from the specified class.
	 * @param <T>
	 * @param clz
	 * @return
	 * @throws Exception
	 */
	static public <T extends UrlPage> T createBody(Class<T> clz) throws Exception {
		return (T) createPage(clz, null).getBody();
	}


	static public HtmlFullRenderer getFullRenderer(IBrowserOutput o) throws Exception {
		BrowserVersion bv = getBrowserVersion();
		return TUtilDomUI.getApplication().findRendererFor(bv, o);
	}

	static public String getFullRenderText(Page pg) throws Exception {
		return getFullRenderText(getBrowserVersion(), pg);
	}

	static public String getFullRenderText(NodeBase nd) throws Exception {
		return getFullRenderText(getBrowserVersion(), nd.getPage());
	}

	static public String getFullRenderText(BrowserVersion bv, Page pg) throws Exception {
		StringWriter sw = new StringWriter();
		IBrowserOutput ro = new PrettyXmlOutputWriter(sw);
		HtmlFullRenderer hr = getFullRenderer(ro);

		IRequestContext ctx = new TestRequestContext();
		UIContext.internalSet(pg);
		UIContext.internalSet(ctx);

		pg.internalFullBuild(); // Cause full build
		hr.render(ctx, pg);
		pg.internalClearDeltaFully();
		return sw.getBuffer().toString();
	}


}
