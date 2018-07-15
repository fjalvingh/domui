package to.etc.domui.util.importers;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-4-18.
 */
public class ImportValueException extends RuntimeException {
	public ImportValueException(Throwable cause, String message) {
		super(message, cause);
	}

	public ImportValueException(String message) {
		super(message);
	}
}
