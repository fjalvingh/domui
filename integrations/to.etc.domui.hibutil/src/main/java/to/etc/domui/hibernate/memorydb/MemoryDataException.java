package to.etc.domui.hibernate.memorydb;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-09-19.
 */
public class MemoryDataException extends RuntimeException {
	public MemoryDataException(String message) {
		super(message);
	}

	public MemoryDataException(Throwable cause, String message) {
		super(message, cause);
	}

	public MemoryDataException(Throwable cause) {
		super(cause);
	}
}
