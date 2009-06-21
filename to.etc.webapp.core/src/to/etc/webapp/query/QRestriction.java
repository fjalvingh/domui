package to.etc.webapp.query;

/**
 * Factory for creating syntax tree nodes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class QRestriction {
	static public final	QMultiNode	and(QOperatorNode... list) {
		return new QMultiNode(QOperation.AND, list);
	}
	static public final	QMultiNode	or(QOperatorNode... list) {
		return new QMultiNode(QOperation.OR, list);
	}

	/**
	 * Equals a property to a value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	static public final	QPropertyComparison	eq(String property, Object value) {
		return new QPropertyComparison(QOperation.EQ, property, createValueNode(value));
	}
	static public final	QPropertyComparison	eq(String property, long value) {
		return new QPropertyComparison(QOperation.EQ, property, createValueNode(value));
	}
	static public final	QPropertyComparison	eq(String property, double value) {
		return new QPropertyComparison(QOperation.EQ, property, createValueNode(value));
	}

	static public final	QPropertyComparison	ne(String property, Object value) {
		return new QPropertyComparison(QOperation.NE, property, createValueNode(value));
	}
	static public final	QPropertyComparison	ne(String property, long value) {
		return new QPropertyComparison(QOperation.NE, property, createValueNode(value));
	}
	static public final	QPropertyComparison	ne(String property, double value) {
		return new QPropertyComparison(QOperation.NE, property, createValueNode(value));
	}

	static public final	QPropertyComparison	gt(String property, Object value) {
		return new QPropertyComparison(QOperation.GT, property, createValueNode(value));
	}
	static public final	QPropertyComparison	gt(String property, long value) {
		return new QPropertyComparison(QOperation.GT, property, createValueNode(value));
	}
	static public final	QPropertyComparison	gt(String property, double value) {
		return new QPropertyComparison(QOperation.GT, property, createValueNode(value));
	}

	static public final	QPropertyComparison	lt(String property, Object value) {
		return new QPropertyComparison(QOperation.LT, property, createValueNode(value));
	}
	static public final	QPropertyComparison	lt(String property, long value) {
		return new QPropertyComparison(QOperation.LT, property, createValueNode(value));
	}
	static public final	QPropertyComparison	lt(String property, double value) {
		return new QPropertyComparison(QOperation.LT, property, createValueNode(value));
	}

	static public final	QPropertyComparison	ge(String property, Object value) {
		return new QPropertyComparison(QOperation.GE, property, createValueNode(value));
	}
	static public final	QPropertyComparison	ge(String property, long value) {
		return new QPropertyComparison(QOperation.GE, property, createValueNode(value));
	}
	static public final	QPropertyComparison	ge(String property, double value) {
		return new QPropertyComparison(QOperation.GE, property, createValueNode(value));
	}

	static public final	QPropertyComparison	le(String property, Object value) {
		return new QPropertyComparison(QOperation.LE, property, createValueNode(value));
	}
	static public final	QPropertyComparison	le(String property, long value) {
		return new QPropertyComparison(QOperation.LE, property, createValueNode(value));
	}
	static public final	QPropertyComparison	le(String property, double value) {
		return new QPropertyComparison(QOperation.LE, property, createValueNode(value));
	}

	static public final	QPropertyComparison	like(String property, Object value) {
		return new QPropertyComparison(QOperation.LIKE, property, createValueNode(value));
	}
	static public final	QBetweenNode	between(String property, Object a, Object b) {
		return new QBetweenNode(QOperation.BETWEEN, property, createValueNode(a), createValueNode(b));
	}
	static public final	QBetweenNode	between(String property, long a, long b) {
		return new QBetweenNode(QOperation.BETWEEN, property, createValueNode(a), createValueNode(b));
	}
	static public final	QBetweenNode	between(String property, double a, double b) {
		return new QBetweenNode(QOperation.BETWEEN, property, createValueNode(a), createValueNode(b));
	}
	static public final	QPropertyComparison	ilike(String property, Object value) {
		return new QPropertyComparison(QOperation.ILIKE, property, createValueNode(value));
	}
	static public final QUnaryProperty	isnull(String property) {
		return new QUnaryProperty(QOperation.ISNULL, property);
	}
	static public final QUnaryProperty	isnotnull(String property) {
		return new QUnaryProperty(QOperation.ISNOTNULL, property);
	}
	static public final QUnaryNode	sqlCondition(String sql) {
		return new QUnaryNode(QOperation.SQL, new QLiteral(sql));
	}
	static private QOperatorNode	createValueNode(Object value) {
		if(value instanceof QOperatorNode)
			return (QOperatorNode) value;
		return new QLiteral(value);
	}
	static private QOperatorNode	createValueNode(long value) {
		return new QLiteral(Long.valueOf(value));
	}
	static private QOperatorNode	createValueNode(double value) {
		return new QLiteral(Double.valueOf(value));
	}
}
