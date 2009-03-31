package to.etc.domui.state;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

/**
 * A class which allows access to the page's context and related information. This
 * is experimental. The PageContext is the root for all navigational information,
 * and interfaces the pages and the server. This would usually be the task of the
 * RequestContext, but that's an interface and I want the primary accessor to be
 * in the same class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 22, 2008
 */
public class PageContext {
	static private ThreadLocal<RequestContextImpl>	m_current = new ThreadLocal<RequestContextImpl>();
	static private ThreadLocal<Page>				m_page	= new ThreadLocal<Page>();

	static public RequestContext	getRequestContext() {
		RequestContext	rc = m_current.get();
		if(rc == null)
			throw new IllegalStateException("No current request!");
		return rc;
	}

	/**
	 * Called when a new request is to be made current, or when the request has
	 * ended.
	 * @param rc
	 */
	static public void	internalSet(RequestContextImpl rc) {
		m_current.set(rc);
	}
	static public void	internalSet(Page pg) {
		m_page.set(pg);
	}
	static public Page	getCurrentPage() {
		Page	pg = m_page.get();
		if(pg == null)
			throw new IllegalStateException("No current page");
		return pg;
	}
	static public ConversationContext	getCurrentConversation() {
		return getCurrentPage().getConversation();
	}
}
