package to.etc.util;

/**
 * An exception base for exceptions that are meant to be shown as user messages.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 8-5-19.
 */
public class MessageException extends RuntimeException {
	protected MessageException() {}

	public MessageException(String message) {
		super(message);
	}

	public MessageException(Throwable cause, String message) {
		super(message, cause);
	}

	public MessageException(Throwable cause) {
		super(cause);
	}
}
