package to.etc.webapp.query;

public class QDuplicateKeyException extends QDbException {
	public QDuplicateKeyException() {
		super("duplicate.key");
	}

	public QDuplicateKeyException(Throwable x) {
		super(x, "duplicate.key");
	}
}
