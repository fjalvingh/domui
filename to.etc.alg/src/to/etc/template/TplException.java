package to.etc.template;


/**
 * Thrown when the small template mechanism encounters an error.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TplException extends Exception {
	/**
	 * Constructor
	 */
	public TplException() {
		super("Template exception");
	}

	public TplException(String s) {
		super("Small Template Error: " + s);
	}
}
