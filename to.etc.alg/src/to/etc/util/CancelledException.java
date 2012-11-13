package to.etc.util;

/**
 * Thrown by the progress reporter when cancel is pressed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 12, 2012
 */
public class CancelledException extends RuntimeException {
	public CancelledException() {
		super("The operation was cancelled by user request");
	}
}
