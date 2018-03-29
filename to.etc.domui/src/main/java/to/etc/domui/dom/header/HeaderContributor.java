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
package to.etc.domui.dom.header;

import to.etc.domui.dom.IContributorRenderer;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A header contributor can be registered by nodes to cause something to
 * be generated at the time that the page HEAD is rendered. A header
 * contributor typically contains things like Javascript modules to load or
 * stylesheets to use. The actual contribution to the header is done at rendering
 * time, so the content can be dynamically determined.
 * Each header contributor must implement full equality comparison semantics in such
 * a way that when a header contributor for the same contribution is added it can be
 * dropped. This is needed for instance when adding Javascript modules; if 15 components
 * all need the same .js file it needs to be added only once, not 15 times...
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
abstract public class HeaderContributor {
	private static final String GOOGLE_CHARTS = "googlecharts";

	static public final Comparator<HeaderContributorEntry> C_ENTRY = new Comparator<HeaderContributorEntry>() {
		@Override
		public int compare(HeaderContributorEntry a, HeaderContributorEntry b) {
			return a.getOrder() - b.getOrder();
		}
	};

	static private Map<String, HeaderContributor> m_jsMap = new HashMap<String, HeaderContributor>();

	abstract public void contribute(IContributorRenderer r) throws Exception;

	@Override
	abstract public int hashCode();

	@Override
	abstract public boolean equals(final Object obj);

	@Nonnull
	static synchronized public HeaderContributor loadJavascript(final String name) {
		HeaderContributor c = m_jsMap.get(name);
		if(c == null) {
			c = new JavascriptContributor(name);
			m_jsMap.put(name, c);
		}
		return c;
	}

	@Nonnull
	static synchronized public HeaderContributor loadJavaScriptlet(final String name) {
		HeaderContributor c = m_jsMap.get(name);
		if(c == null) {
			c = new JavaScriptletContributor(name);
			m_jsMap.put(name, c);
		}
		return c;
	}

	static synchronized public HeaderContributor loadStylesheet(final String name) {
		HeaderContributor c = m_jsMap.get(name);
		if(c == null) {
			c = new CssContributor(name);
			m_jsMap.put(name, c);
		}
		return c;
	}

	static synchronized public HeaderContributor loadThemedJavasciptContributor(final String name) {
		HeaderContributor c = m_jsMap.get(name);
		if(c == null) {
			c = new ThemedJavascriptContributor(name);
			m_jsMap.put(name, c);
		}
		return c;
	}

	/**
	 * Add a header contributor to add the Google Analytics blurb to every page.
	 * @param uacode
	 * @return
	 */
	static synchronized public HeaderContributor loadGoogleAnalytics(final String uacode) {
		HeaderContributor c = m_jsMap.get(uacode);
		if(c == null) {
			StringBuilder blurb = new StringBuilder();
			blurb.append("\n\nvar _gaq = _gaq || [];\n");
			blurb.append("_gaq.push(['_setAccount', '" + uacode + "']);\n");
			blurb.append("_gaq.push(['_trackPageview']);\n");

			blurb.append("(function() {\n");
			blurb.append("var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;\n");
			blurb.append("ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';\n");
			blurb.append("var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);\n");
			blurb.append("})();\n");

			c = HeaderContributor.loadJavaScriptlet(blurb.toString());
			m_jsMap.put(uacode, c);
		}
		return c;
	}

	public static synchronized HeaderContributor loadGoogleCharts() {
		HeaderContributor hc = m_jsMap.get(GOOGLE_CHARTS);
		if (hc == null) {
			hc = new GoogleChartsContributor();
			m_jsMap.put(GOOGLE_CHARTS, hc);
		}
		return hc;
	}
}
