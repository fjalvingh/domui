package to.etc.template;


/**
 * Thrown when the small template mechanism encounters an error.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
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
