package to.etc.webapp.qsql;

/**
 * An exception thrown when a database query is somehow incorrect (grammar, column references).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 26, 2010
 */
public class QQuerySyntaxException extends RuntimeException {
	public QQuerySyntaxException(Throwable x, String arg0) {
		super(arg0, x);
	}

	public QQuerySyntaxException(String arg0) {
		super(arg0);
	}
}