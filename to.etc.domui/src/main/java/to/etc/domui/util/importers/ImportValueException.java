package to.etc.domui.util.importers;

import to.etc.util.MessageException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-4-18.
 */
public class ImportValueException extends MessageException {
	public ImportValueException(Throwable cause, String message) {
		super(cause, message);
	}

	public ImportValueException(String message) {
		super(message);
	}
}
