package to.etc.server.misc;

/**
 * @author Frits Jalvingh
 */
public class ErrorException extends ActionException {
	/**
	 * Constructor
	 */
	public ErrorException() {
		super("Something went wrong but I have no idea what ;-)");
	}

	public ErrorException(String s) {
		super(s);
	}
}
