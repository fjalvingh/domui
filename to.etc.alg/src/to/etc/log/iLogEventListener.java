package to.etc.log;

/**
 * This interface must be implemented by log event listeners.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public interface iLogEventListener {
	/**
	 * Called when an event is received that this instance was interested in.
	 * @param lr			the logrecord
	 * @throws Exception	whatever went wrong
	 */
	public void logEvent(LogRecord lr) throws Exception;

	/**
	 * Must return T if this writer is interested in events of that category.
	 * @param c	The category
	 * @return	T if interested.
	 */
	public boolean isInterestedIn(Category c);
}
