package to.etc.domui.state;

import javax.annotation.*;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

final public class ShelvedDomUIPage implements IShelvedEntry {
	@Nonnull
	private final WindowSession m_session;

	@Nonnull
	private final Page m_page;

	public ShelvedDomUIPage(@Nonnull WindowSession ws, @Nonnull Page page) {
		m_page = page;
		m_session = ws;
	}

	@Nonnull
	@Override
	public String getName() {
		UrlPage body = getPage().getBody();
		if(body instanceof IBreadCrumbTitler) {
			return ((IBreadCrumbTitler) body).getBreadcrumbName();
		} else if(!DomUtil.isBlank(body.getTitle())) {
			return body.getTitle();
		} else {
			return body.getClass().getName();
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

	@Nonnull
	public Page getPage() {
		return m_page;
	}

	@Override
	public void activate(@Nonnull RequestContextImpl ctx, boolean ajax) throws Exception {
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
		return getPage() + " in " + getPage().internalGetConversation() + ": " + getPage().internalGetConversation().getState();
	}
}
