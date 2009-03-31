package to.etc.domui.dom.header;

import to.etc.domui.dom.FullHtmlRenderer;

/**
 * Contributes a specific .js file from the webapp to the page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
public class JavascriptContributor extends HeaderContributor {
	private String		m_path;

	public JavascriptContributor(String path) {
		m_path = path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_path == null) ? 0 : m_path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		JavascriptContributor other = (JavascriptContributor) obj;
		if(m_path == null) {
			if(other.m_path != null)
				return false;
		}
		else if(!m_path.equals(other.m_path))
			return false;
		return true;
	}

	@Override
	public void contribute(FullHtmlRenderer r) throws Exception {
		r.renderLoadJavascript(m_path);
	}

}
