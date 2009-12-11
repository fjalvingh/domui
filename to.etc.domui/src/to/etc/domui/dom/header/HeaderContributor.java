package to.etc.domui.dom.header;

import java.util.*;

import to.etc.domui.dom.*;
import to.etc.domui.dom.html.*;

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
	static public final Comparator<HeaderContributorEntry> C_ENTRY = new Comparator<HeaderContributorEntry>() {
		@Override
		public int compare(HeaderContributorEntry a, HeaderContributorEntry b) {
			return a.getOrder() - b.getOrder();
		}
	};

	static private Map<String, HeaderContributor> m_jsMap = new HashMap<String, HeaderContributor>();

	abstract public void contribute(HtmlFullRenderer r) throws Exception;

	abstract public void contribute(OptimalDeltaRenderer r) throws Exception;

	@Override
	abstract public int hashCode();

	@Override
	abstract public boolean equals(final Object obj);

	static synchronized public HeaderContributor loadJavascript(final String name) {
		HeaderContributor c = m_jsMap.get(name);
		if(c == null) {
			c = new JavascriptContributor(name);
			m_jsMap.put(name, c);
		}
		return c;
	}

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
}
