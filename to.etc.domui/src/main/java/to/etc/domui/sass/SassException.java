package to.etc.domui.sass;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1-3-19.
 */
public class SassException extends RuntimeException {
	public SassException(String message) {
		super(message);
	}

	public SassException(String message, Throwable cause) {
		super(message, cause);
	}
}
