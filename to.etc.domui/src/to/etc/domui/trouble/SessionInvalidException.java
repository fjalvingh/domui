package to.etc.domui.trouble;

/**
 * Thrown when code finds out the HttpSession is no (longer) valid, which can
 * be when we have more browser windows open and we log out in one of them.
 *
 * See bugzilla 6800.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2/6/16.
 */
final public class SessionInvalidException extends RuntimeException {
	public SessionInvalidException(String message) {
		super(message);
	}
}
