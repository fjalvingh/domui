package my.domui.app.ui.pages.management;

import my.domui.app.core.authentication.Rights;
import my.domui.app.core.db.DbUser;
import my.domui.app.ui.pages.base.AbstractListPage;
import to.etc.domui.annotations.UIRights;
import to.etc.domui.state.UIGoto;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-18.
 */
@UIRights(Rights.ADMIN)
public class UserListPage extends AbstractListPage<DbUser> {
	public UserListPage() {
		super(DbUser.class);
		setOptionEmptySearch(true);
	}

	@Override public void createContent() throws Exception {
		super.createContent();
	}

	@Override protected void onNew() {
		UIGoto.moveSub(UserEditPage.class, "id", "NEW");
	}

	@Override protected void onSelect(DbUser selectedRow) {
		UIGoto.moveSub(UserEditPage.class, "id", selectedRow.getId());
	}
}
