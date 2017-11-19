package to.etc.domui.login;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-11-17.
 */
public class ImpersonationFailedException extends RuntimeException {
	public ImpersonationFailedException(String message) {
		super(message);
	}
}
