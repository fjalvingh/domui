package to.etc.domui.trouble;

/**
 * Thrown when output to the server's client failed with an IO Exception.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 24, 2013
 */
final public class ClientDisconnectedException extends RuntimeException {
	public ClientDisconnectedException(Throwable cause) {
		super(cause);
	}
}
