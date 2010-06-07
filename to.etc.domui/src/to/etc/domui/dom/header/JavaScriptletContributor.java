package to.etc.domui.dom.header;

import to.etc.domui.dom.*;
import to.etc.domui.dom.html.*;

final public class JavaScriptletContributor extends HeaderContributor {
	private final String m_javascript;

	JavaScriptletContributor(final String javascript) {
		m_javascript = javascript;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_javascript == null) ? 0 : m_javascript.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if(obj == null)
			return false;
		if(this == obj)
			return true;
		if(getClass() != obj.getClass())
			return false;
		JavaScriptletContributor other = (JavaScriptletContributor) obj;
		if(m_javascript == null) {
			if(other.m_javascript != null)
				return false;
		} else if(!m_javascript.equals(other.m_javascript))
			return false;
		return true;
	}

	/**
	 * Generate the specified scriptlet as a script tag.
	 * @see to.etc.domui.dom.header.HeaderContributor#contribute(to.etc.domui.dom.HtmlFullRenderer)
	 */
	@Override
	public void contribute(final HtmlFullRenderer r) throws Exception {
		r.o().tag("script");
		r.o().attr("language", "javascript");
		r.o().endtag();
		r.o().writeRaw("<!--"); // Embed JS in comment
		r.o().writeRaw(m_javascript);
		r.o().writeRaw("\n-->");
		r.o().closetag("script");
	}

	@Override
	public void contribute(OptimalDeltaRenderer r) throws Exception {
		r.o().writeRaw(m_javascript);
	}
}
