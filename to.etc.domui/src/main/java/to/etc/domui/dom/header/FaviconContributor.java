package to.etc.domui.dom.header;

import to.etc.domui.dom.*;
import to.etc.domui.dom.html.*;

final public class FaviconContributor extends HeaderContributor {
	private String m_favicon;

	public FaviconContributor(String favicon) {
		m_favicon = favicon;
	}

	@Override
	public void contribute(IContributorRenderer r) throws Exception {
		if(r instanceof OptimalDeltaRenderer)
			return;

		IBrowserOutput o = r.o();

		o.tag("link");
		o.attr("rel", "shortcut icon");
		o.attr("href", r.ctx().getRelativePath(m_favicon));
		o.endtag();
		o.dec();
		//o.closetag("link");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_favicon == null) ? 0 : m_favicon.hashCode());
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
		FaviconContributor other = (FaviconContributor) obj;
		if(m_favicon == null) {
			return other.m_favicon == null;
		} else
			return m_favicon.equals(other.m_favicon);
	}
}
