package my.domui.app.ui.pages.management;

import my.domui.app.core.authentication.LoginAuthenticator;
import my.domui.app.core.authentication.Rights;
import my.domui.app.core.db.DbUser;
import my.domui.app.ui.pages.base.BasicPage;
import to.etc.domui.annotations.UIRights;
import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.state.UIGoto;
import to.etc.util.StringTool;

import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-18.
 */
@UIRights(Rights.ADMIN)
public class UserEditPage extends BasicPage {
	private DbUser m_user;

	private String m_password1;

	private String m_password2;


	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		FormBuilder fb = new FormBuilder(cp);
		fb.property(m_user, DbUser.pFULLNAME).control();
		fb.property(m_user, DbUser.pEMAIL).control();
		fb.property(m_user, DbUser.pPHONENUMBER).control();

		fb.property(this, "password1").label("New password").control();
		fb.property(this, "password2").label("Repeat password").control();


		ButtonBar bb = new ButtonBar();
		add(bb);
		bb.addButton("Save", FaIcon.faFloppyO, a -> save()).css("is-primary");
		bb.addBackButton();
	}

	private void save() throws Exception {
		//-- Password change?
		String p1 = getPassword1();
		String p2 = getPassword2();
		if(!StringTool.isBlank(p1) || ! StringTool.isBlank(p2)) {
			if(! Objects.equals(p1, p2)) {
				MsgBox.error(this, "Passwords are not equal");
				return;
			}

			//-- Check password complexity here - please improve this as this check is nonsense
			if(isValidPassword(p1)) {
				MsgBox.error(this, "The password is too simple; must be 6 characters with at least one number, and letters in different cases.");
				return;
			}

			//-- Ok, accept the password
			getUser().setPassword(LoginAuthenticator.getEncyptedPassword(p1));
		}
		getSharedContext().save(getUser());
		getSharedContext().commit();
		UIGoto.back();
	}

	private boolean isValidPassword(String pw) {
		int lc = 0;
		int uc = 0;
		int nu = 0;
		int lt = 0;
		for(int i = pw.length(); --i >= 0;) {
			char c = pw.charAt(i);
			if(Character.isDigit(c))
				nu++;
			else if(!Character.isLetter(c))
				lt++;
			else if(Character.isLowerCase(c))
				lc++;
			else if(Character.isUpperCase(c))
				uc++;
		}
		return lc > 0 && uc > 1 && nu > 1 && pw.length() >= 6;
	}

	@UIUrlParameter(name = "id")
	public DbUser getUser() {
		return m_user;
	}

	public void setUser(DbUser user) {
		m_user = user;
	}

	public String getPassword1() {
		return m_password1;
	}

	public void setPassword1(String password1) {
		m_password1 = password1;
	}

	public String getPassword2() {
		return m_password2;
	}

	public void setPassword2(String password2) {
		m_password2 = password2;
	}
}
