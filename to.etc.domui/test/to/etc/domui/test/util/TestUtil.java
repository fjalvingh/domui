package to.etc.domui.test.util;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

public class TestUtil {
	static private AppSession m_session;

	static private DomApplication m_application;

	static public synchronized DomApplication getApplication() {
		if(m_application == null) {
			m_application = new DomApplication() {
				@Override
				public Class< ? extends UrlPage> getRootPage() {
					return null;
				}
			};
			DomApplication.internalSetCurrent(m_application);
		}
		return m_application;
	}

	static public AppSession getAppSession() {
		getApplication();
		if(m_session == null) {
			m_session = new AppSession();
		}
		return m_session;
	}

	/**
	 * Create a page structure valid for testing pps.
	 * @param pg
	 * @return
	 */
	static private Page initPage(UrlPage pg, PageParameters pp) throws Exception {
		getApplication();
		Page p = new Page(pg);
		WindowSession ws = new WindowSession(getAppSession());
		ConversationContext cc = new ConversationContext();
		ws.acceptNewConversation(cc);
		p.internalInitialize(pp, cc);
		cc.internalRegisterPage(p, pp);
		return p;
	}

	static public Page createPage(Class< ? extends UrlPage> clz, PageParameters pp) throws Exception {
		if(pp == null)
			pp = new PageParameters();
		UrlPage pg = clz.newInstance(); // Should have parameterless ctor
		Page p = initPage(pg, pp);
		return p;
	}

	static public Page createPage(Class< ? extends UrlPage> clz) throws Exception {
		return createPage(clz, null);
	}
}
