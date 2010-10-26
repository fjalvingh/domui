package to.etc.webapp.query;

/**
 * Thrown for all cases where a record is not found but required. Please take heed: many methods
 * return null when a record is not found instead of throwing an exception.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 11, 2010
 */
public class QNotFoundException extends QDbException {
	/**
	 * REMOVE AS SOON AS POSSIBLE - You are NOT ALLOWED to change the CODE for a KNOWN EXCEPTION!N!#^*&#!%#*^$%@*^$^@$#^@$#^*!@$13263
	 */
	@Deprecated
	public QNotFoundException() {
		super("record.not.found.simple");
	}

	/**
	 * REMOVE AS SOON AS POSSIBLE - You are NOT ALLOWED to change the CODE for a KNOWN EXCEPTION!N!#^*&#!%#*^$%@*^$^@$#^@$#^*!@$13263
	 */
	@Deprecated
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
