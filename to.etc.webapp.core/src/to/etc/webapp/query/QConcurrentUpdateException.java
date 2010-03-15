package to.etc.webapp.query;

/**
 * This will be thrown when a concurrent update happens. It gets thrown form "TCN" exceptions and
 * Hibernate versioning exceptions.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 11, 2010
 */
public class QConcurrentUpdateException extends QDbException {
	public QConcurrentUpdateException() {
		super("concurrent.update");
	}
}
