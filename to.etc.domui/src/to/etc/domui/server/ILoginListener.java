package to.etc.domui.server;

import to.etc.domui.login.*;

public interface ILoginListener {
	void		userLogin(IUser user);
	void		userLogout(IUser user);
}
