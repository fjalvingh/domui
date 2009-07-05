package to.etc.domui.dom.header;

import to.etc.domui.dom.*;

/**
 * Javascript contributor which obtains the Javascript to use from the
 * current theme.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 3, 2008
 */
public class ThemedJavascriptContributor extends HeaderContributor {
	private String m_path;

	public ThemedJavascriptContributor(String path) {
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
		ThemedJavascriptContributor other = (ThemedJavascriptContributor) obj;
		if(m_path == null) {
			if(other.m_path != null)
				return false;
		} else if(!m_path.equals(other.m_path))
			return false;
		return true;
	}

	@Override
	public void contribute(FullHtmlRenderer r) throws Exception {
		r.renderLoadJavascript(m_path);
	}
}
