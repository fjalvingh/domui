package to.etc.webapp.query;

/**
 * Thrown for all cases where a record is not found but required. Please take heed: many methods
 * return null when a record is not found instead of throwing an exception.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 11, 2010
 */
public class QNotFoundException extends QDbException {
	public QNotFoundException() {
		super("record.not.found.simple");
	}

	public QNotFoundException(Throwable x) {
		super(x, "record.not.found.simple");
	}

	public QNotFoundException(String type, Object key) {
		super("recordNotFound", type, key);
	}

	public QNotFoundException(Class< ? > type, Object key) {
		super("recordNotFound", type.getName(), key);
	}
}
