package to.etc.domui.pages.generic;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class BasicPage<T> extends UrlPage {
	private Class<T> m_baseClass;

	private String m_pageTitle;

	public BasicPage(Class<T> baseClass) {
		m_baseClass = baseClass;
	}

	public BasicPage(Class<T> baseClass, String txt) {
		m_baseClass = baseClass;
		m_pageTitle = txt;
	}

	public Class<T> getBaseClass() {
		return m_baseClass;
	}

	@Override
	public void createContent() throws Exception {
		addPageHeaders();
		add(new VerticalSpacer(5)); // add little space between title bar and other components
		addPageTitleBar();
	}

	public String getPageTitle() {
		return m_pageTitle;
	}

	protected void addPageHeaders() throws Exception {
	}

	/**
	 * Override to add custom page title bar.
	 */
	protected void addPageTitleBar() {}

	public void clearGlobalMessages() {
		IErrorFence fence = DomUtil.getMessageFence(this);
		fence.clearGlobalMessages(this, null);
	}
}
