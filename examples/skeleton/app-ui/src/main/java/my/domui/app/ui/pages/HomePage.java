package my.domui.app.ui.pages;

import to.etc.domui.annotations.UIRights;
import to.etc.domui.component.misc.ALink;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;
import my.domui.app.ui.pages.management.UserListPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 11-2-18.
 */
@UIRights
public class HomePage extends UrlPage {
	@Override public void createContent() throws Exception {
		addLink(UserListPage.class, "Manage users");




	}

	private void addLink(Class<UserListPage> pg, String ttl) {
		Div d = new Div();
		add(d);
		ALink link = new ALink(pg);
		d.add(link);
		link.add(ttl);
	}
}
