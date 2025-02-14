package to.etc.domui.login;

import to.etc.webapp.eventmanager.AppEvent;
import to.etc.webapp.eventmanager.ChangeType;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-03-22.
 */
public class EvSessionLogout extends AppEvent {
	public EvSessionLogout(String key) {
		super(ChangeType.MODIFIED, key);
	}
}
