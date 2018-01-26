package to.etc.dbutil.reverse;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
public class ReverserException extends RuntimeException {
	public ReverserException(String message) {
		super(message);
	}

	public ReverserException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReverserException(Throwable cause) {
		super(cause);
	}

	public ReverserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
