package to.etc.telnet;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-02-22.
 */
public class TelnetCommandException extends RuntimeException {
	public TelnetCommandException(String message) {
		super(message);
	}
}
