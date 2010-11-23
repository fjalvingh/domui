package to.etc.telnet;

/**
 * Event handler when you're interested in Telnet events.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * @version 1.0
 */
public interface ITelnetClientEvent {
	void sessionClosed(TelnetSession ts);
}
