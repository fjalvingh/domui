package to.etc.webapp.eventmanager;

/**
 * The type of listening operation requested; used to specify when
 * the listener is to be called after an event has occured.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 30, 2006
 */
public enum ListenerType {
	DELAYED, IMMEDIATELY, LOCALLY,
}
