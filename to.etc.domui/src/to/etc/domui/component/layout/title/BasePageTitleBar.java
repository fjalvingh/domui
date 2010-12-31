package to.etc.domui.component.layout.title;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public abstract class BasePageTitleBar extends Div {
	private String m_title;

	private boolean m_showAsModified;

	public BasePageTitleBar() {}

	public BasePageTitleBar(final String title) {
		m_title = title;
	}

	/**
	 * Return the title that is used by this bar. If no user title is set this returns the
	 * calculated title (from annotations and metadata).
	 * @return
	 */
	public String getPageTitle() {
		if(m_title != null) {
			return m_title;
		}
		return DomUtil.calcPageTitle(getPage().getBody().getClass());
	}

	public boolean isShowAsModified() {
		return m_showAsModified;
	}

	public void setPageTitle(String ttl) {
		m_title = ttl;
	}

	public void setShowAsModified(boolean showAsModified) {
		m_showAsModified = showAsModified;
	}
}
