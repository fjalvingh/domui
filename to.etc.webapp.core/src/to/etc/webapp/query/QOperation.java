package to.etc.webapp.query;

/**
 * AN enum representing all operations.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
public enum QOperation {
	AND, OR, EQ, NE, LE, GE, LT, GT, ILIKE, LIKE, BETWEEN,

	PROP, PARAM, LITERAL, ORDER, ISNULL, ISNOTNULL, SQL,

	EXISTS_SUBQUERY,

	SELECTION_SUBQUERY
}
