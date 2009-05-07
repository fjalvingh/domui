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
	static public final	QBinaryNode	eq(String property, Object value) {
		return new QBinaryNode(QOperation.EQ, new QPropertyNode(property), createValueNode(value));
	}
	static public final	QBinaryNode	ne(String property, Object value) {
		return new QBinaryNode(QOperation.NE, new QPropertyNode(property), createValueNode(value));
	}
	static public final	QBinaryNode	gt(String property, Object value) {
		return new QBinaryNode(QOperation.GT, new QPropertyNode(property), createValueNode(value));
	}
	static public final	QBinaryNode	lt(String property, Object value) {
		return new QBinaryNode(QOperation.LT, new QPropertyNode(property), createValueNode(value));
	}
	static public final	QBinaryNode	ge(String property, Object value) {
		return new QBinaryNode(QOperation.GE, new QPropertyNode(property), createValueNode(value));
	}
	static public final	QBinaryNode	le(String property, Object value) {
		return new QBinaryNode(QOperation.LE, new QPropertyNode(property), createValueNode(value));
	}
	static public final	QBinaryNode	like(String property, Object value) {
		return new QBinaryNode(QOperation.LIKE, new QPropertyNode(property), createValueNode(value));
	}
	static public final	QBetweenNode	between(String property, Object a, Object b) {
		return new QBetweenNode(QOperation.BETWEEN, new QPropertyNode(property), createValueNode(a), createValueNode(b));
	}
	static public final	QBinaryNode	ilike(String property, Object value) {
		return new QBinaryNode(QOperation.ILIKE, new QPropertyNode(property), createValueNode(value));
	}
	static public final QUnaryNode	isnull(String property) {
		return new QUnaryNode(QOperation.ISNULL, new QPropertyNode(property));
	}
	static public final QUnaryNode	isnotnull(String property) {
		return new QUnaryNode(QOperation.ISNOTNULL, new QPropertyNode(property));
	}
	static public final QUnaryNode	sqlCondition(String sql) {
		return new QUnaryNode(QOperation.SQL, new QLiteral(sql));
	}
	static private QOperatorNode	createValueNode(Object value) {
		if(value instanceof QOperatorNode)
			return (QOperatorNode) value;
		return new QLiteral(value);
	}
}
