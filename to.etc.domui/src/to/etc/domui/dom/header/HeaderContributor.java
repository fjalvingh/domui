package to.etc.domui.dom.header;

import java.util.HashMap;
import java.util.Map;

import to.etc.domui.dom.FullHtmlRenderer;

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
	static private Map<String, HeaderContributor>	m_jsMap = new HashMap<String, HeaderContributor>();

	abstract public void	contribute(FullHtmlRenderer r) throws Exception;

	static synchronized public HeaderContributor	loadJavascript(String name) {
		HeaderContributor c = m_jsMap.get(name);
		if(c == null) {
			c = new JavascriptContributor(name);
			m_jsMap.put(name, c);
		}
		return c;
	}
}
