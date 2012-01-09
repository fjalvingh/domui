package to.etc.server.misc;

/**
 *	Root class for exceptions when a basic action has to be executed.
 *
 *	@author Frits Jalvingh
 */
public abstract class ActionException extends RuntimeException {
	/**
	 * Constructor
	 */
	public ActionException() {
		super("Action is required??");
	}

	public ActionException(String s) {
		super(s);
	}

	public ActionException(String s, Throwable rootcause) {
		super(s, rootcause);
	}
}
