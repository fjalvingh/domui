package to.etc.domui.dom.header;

import to.etc.domui.dom.*;
import to.etc.domui.dom.html.*;

final public class CssContributor extends HeaderContributor {
	private String m_path;

	public CssContributor(String path) {
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
		CssContributor other = (CssContributor) obj;
		if(m_path == null) {
			if(other.m_path != null)
				return false;
		} else if(!m_path.equals(other.m_path))
			return false;
		return true;
	}

	@Override
	public void contribute(HtmlFullRenderer r) throws Exception {
		r.renderLoadCSS(m_path);
	}

	@Override
	public void contribute(OptimalDeltaRenderer r) throws Exception {
		r.renderLoadCSS(m_path);
	}
}
