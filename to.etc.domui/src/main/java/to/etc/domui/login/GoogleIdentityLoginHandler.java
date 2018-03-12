package to.etc.domui.login;

import to.etc.domui.dom.header.GoogleIdentificationContributor;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-3-18.
 */
final public class GoogleIdentityLoginHandler {
	private final UrlPage m_page;

	private final String m_clientID;

	private GoogleIdentityLoginHandler(UrlPage page, String id) {
		m_page = page;
		m_clientID = id;
	}

	/**
	 * Connect the authenticator to a login page.
	 */
	static GoogleIdentityLoginHandler create(UrlPage page, String clientID) {
		GoogleIdentityLoginHandler handler = new GoogleIdentityLoginHandler(page, clientID);
		page.getPage().addHeaderContributor(new GoogleIdentificationContributor(clientID), 100);
//		page.getPage().addHeaderContributor(HeaderContributor.loadJavascript("js/login.js"), 101);
		return handler;
	}

	public Div getLoginButton() {
		Div gg = new Div("g-signin2");
		gg.setSpecialAttribute("data-onsuccess", "WebUI.googleOnSignin");
		return gg;
	}
}
