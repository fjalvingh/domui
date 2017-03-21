package to.etc.webapp.query;

import java.sql.*;

/**
 * Thrown when a statement aborts with a query timeout exception, as caused when
 * {@link Statement#setQueryTimeout(int)} is used.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 11, 2011
 */
public class QQueryTimeoutException extends QDbException {
	public QQueryTimeoutException() {
		super("query.timeout");
	}

	public QQueryTimeoutException(Throwable x) {
		super(x, "query.timeout");
	}
}
