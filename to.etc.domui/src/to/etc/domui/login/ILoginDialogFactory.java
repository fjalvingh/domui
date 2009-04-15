package to.etc.domui.login;

import to.etc.domui.trouble.*;

public interface ILoginDialogFactory {
	String		getLoginRURL(NotLoggedInException x);
}
