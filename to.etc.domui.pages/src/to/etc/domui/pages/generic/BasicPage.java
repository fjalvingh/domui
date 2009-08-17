package to.etc.domui.pages.generic;

import java.util.*;

import to.etc.domui.dom.html.*;

public class BasicPage<T> extends UrlPage {
	private Class<T> m_baseClass;

	private String m_pageTitle;

	static private List<IGenericPageModifier> m_pageModifierList = new ArrayList<IGenericPageModifier>();

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
		add(new Div()); // add little space between title bar and other components
		addPageTitleBar();
	}

	public String getPageTitle() {
		return m_pageTitle;
	}

	protected void addPageHeaders() throws Exception {
		addPageHeaders(this);
	}

	protected void addPageHeaders(NodeContainer c) throws Exception {
		for(IGenericPageModifier m : getModifierList()) {
			m.addPageHeader(c, this);
		}
	}

	/**
	 * Override to add custom page title bar.
	 */
	protected void addPageTitleBar() {}

	public static synchronized void addModifier(IGenericPageModifier m) {
		m_pageModifierList = new ArrayList<IGenericPageModifier>(m_pageModifierList);
		m_pageModifierList.add(m);
	}

	public static synchronized List<IGenericPageModifier> getModifierList() {
		return m_pageModifierList;
	}
}
