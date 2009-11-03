package to.etc.domui.dom.html;

/**
 * EXPERIMENTAL INTERFACE Components that "know" that they were changed by a user implement
 * this interface. It can be used to determine that data on a page changed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 3, 2009
 */
public interface IHasModifiedIndication {
	/**
	 * Returns the modified-by-user flag.
	 */
	boolean isModified();

	/**
	 * Set or clear the modified by user flag.
	 */
	void setModified(boolean as);
}
