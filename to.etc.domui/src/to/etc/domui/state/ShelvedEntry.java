package to.etc.domui.state;

import to.etc.domui.dom.html.*;

/**
 * A single entry in the breadcrumb trail. This contains the shelved page instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 22, 2008
 */
public class ShelvedEntry {
	private Page				m_page;

	public ShelvedEntry(Page page) {
		m_page = page;
	}

	public Page getPage() {
		return m_page;
	}
}