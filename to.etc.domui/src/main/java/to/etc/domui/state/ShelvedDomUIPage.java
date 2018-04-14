package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.layout.IBreadCrumbTitler;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.util.DomUtil;
import to.etc.util.StringTool;

final public class ShelvedDomUIPage implements IShelvedEntry {
	@NonNull
	private final WindowSession m_session;

	@NonNull
	private final Page m_page;

	public ShelvedDomUIPage(@NonNull WindowSession ws, @NonNull Page page) {
		m_page = page;
		m_session = ws;
	}

	@NonNull
	@Override
	public String getName() {
		UrlPage body = getPage().getBody();
		if(body instanceof IBreadCrumbTitler) {
			return ((IBreadCrumbTitler) body).getBreadcrumbName();
		} else {
			String pageTitle = body.getPageTitle();
			if(pageTitle != null && !StringTool.isBlank(pageTitle)) {
				return pageTitle;
			} else {
				String name = body.getClass().getName();
				return name.substring(name.lastIndexOf('.') + 1);
			}
		}
	}

	@Override
	@Nullable
	public String getTitle() {
		if(getPage().getBody() instanceof IBreadCrumbTitler) {
			IBreadCrumbTitler body = (IBreadCrumbTitler) getPage().getBody();
			return body.getBreadcrumbTitle();
		}
		return null;
	}

	@NonNull
	public Page getPage() {
		return m_page;
	}

	@Override
	public void activate(@NonNull RequestContextImpl ctx, boolean ajax) throws Exception {
		/*
		 * jal 20100224 The old page is destroyed and we're now running in the "new" page's context! Since
		 * unshelve calls user code - which can access that context using PageContext.getXXX calls- we must
		 * make sure it is correct even though the request was for another page and is almost dying.
		 */
		UIContext.internalSet(getPage());
		getPage().internalUnshelve();
		m_session.generateRedirect(ctx, getPage(), ajax);
	}

	@Override
	public void discard() {
		m_session.discardPage(getPage());
	}

	@Override
	public String getURL() {
		return DomUtil.createPageURL(getPage().getBody().getClass(), getPage().getPageParameters());
	}

	@Override
	public String toString() {
		ConversationContext cc = getPage().internalGetConversation();
		return getPage() + " in " + cc + ": " + (cc == null ? "(null conv)" : cc.getState());
	}

	@Override
	public boolean isClose() {
		return false;
	}
}
