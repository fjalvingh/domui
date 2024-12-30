package to.etc.domui.trouble;

import to.etc.util.MessageException;

/**
 * This replaces IllegalStateExceptions for places where we really do
 * not want a stack trace.
 */
public class UnexpectedErrorException extends MessageException {
	public UnexpectedErrorException(String message) {
		super(message);
	}

	public UnexpectedErrorException(Throwable cause, String message) {
		super(cause, message);
	}
}
