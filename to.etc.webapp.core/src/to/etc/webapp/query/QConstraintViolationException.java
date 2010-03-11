package to.etc.webapp.query;

public class QConstraintViolationException extends QDbException {
	public QConstraintViolationException() {
		super("constraint.violation");
	}

	public QConstraintViolationException(Throwable x) {
		super(x, "constraint.violation");
	}
}
