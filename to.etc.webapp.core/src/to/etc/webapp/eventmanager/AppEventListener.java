package to.etc.webapp.eventmanager;

/**
 * A listener for a specific type of event.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 12, 2006
 */
public interface AppEventListener<T extends AppEventBase> {
	public void handleEvent(T obj) throws Exception;
}
