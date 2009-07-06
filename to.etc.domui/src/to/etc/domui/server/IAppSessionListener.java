package to.etc.domui.server;

import to.etc.domui.state.*;

public interface IAppSessionListener {
	public void sessionCreated(DomApplication app, AppSession ses);

	public void sessionDestroyed(DomApplication app, AppSession ses);
}
