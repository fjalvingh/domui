package to.etc.security;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-1-19.
 */
public class KeyFormatException extends Exception {
	public KeyFormatException(String message) {
		super(message);
	}

	public KeyFormatException(Throwable cause, String message) {
		super(message, cause);
	}
}
