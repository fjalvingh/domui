package to.etc.webapp.query;

import java.util.*;

/**
 * Represents the selection of a list of persistent entity classes from the database. A QCriteria
 * has a fixed type (the type of the class being selected) and maintains the list of conditions (criteria's)
 * that the selection must hold.
 * This is a concrete representation of something representing a query tree. To use a QCriteria in an actual
 * query you need a translator which translates the QCriteria tree into something for the target persistence
 * layer. Implementations of such a translator for Hibernate and SPF exist.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class QCriteria<T> extends QRestrictionsBase {
	private Class<T>			m_baseClass;

	private QCriteria(Class<T> b) {
		m_baseClass = b;
	}

	/**
	 * Copy constructor.
	 * @param q
	 */
	protected QCriteria(QCriteria<T> q) {
		super(q);
		m_baseClass = q.m_baseClass;
	}

	/**
	 * Create a QCriteria to select a set of the specified class. When used on it's own without
	 * added criteria this selects all possible items.
	 * @param <U>
	 * @param clz
	 * @return
	 */
	static public <U> QCriteria<U>	create(Class<U> clz) {
		return new QCriteria<U>(clz);
	}

	/**
	 * Returns the persistent class being queried and returned.
	 * @return
	 */
	public Class<T> getBaseClass() {
		return m_baseClass;
	}

	/**
	 * Create a duplicate of this Criteria.
	 * @return
	 */
	public QCriteria<T>		dup() {
		return new QCriteria<T>(this);
	}

	/**
	 * Visit everything in this QCriteria.
	 * @param v
	 * @throws Exception
	 */
	public void		visit(QNodeVisitor v) throws Exception {
		v.visitCriteria(this);
	}

	@Override
	public QCriteria<T> add(QOperatorNode r) {
		return (QCriteria<T>)super.add(r);
	}

	@Override
	public QCriteria<T> add(QOrder r) {
		return (QCriteria<T>)super.add(r);
	}

	@Override
	public QCriteria<T> ascending(String property) {
		return (QCriteria<T>)super.ascending(property);
	}

	@Override
	public QCriteria<T> between(String property, Object a, Object b) {
		return (QCriteria<T>)super.between(property, a, b);
	}

	@Override
	public QCriteria<T> descending(String property) {
		return (QCriteria<T>)super.descending(property);
	}

	@Override
	public QCriteria<T> eq(String property, double value) {
		return (QCriteria<T>)super.eq(property, value);
	}

	@Override
	public QCriteria<T> eq(String property, long value) {
		return (QCriteria<T>)super.eq(property, value);
	}

	@Override
	public QCriteria<T> eq(String property, Object value) {
		return (QCriteria<T>)super.eq(property, value);
	}

	@Override
	public QCriteria<T> ge(String property, double value) {
		return (QCriteria<T>)super.ge(property, value);
	}

	@Override
	public QCriteria<T> ge(String property, long value) {
		return (QCriteria<T>)super.ge(property, value);
	}

	@Override
	public QCriteria<T> ge(String property, Object value) {
		return (QCriteria<T>)super.ge(property, value);
	}

	@Override
	public QCriteria<T> gt(String property, double value) {
		return (QCriteria<T>)super.gt(property, value);
	}

	@Override
	public QCriteria<T> gt(String property, long value) {
		return (QCriteria<T>)super.gt(property, value);
	}

	@Override
	public QCriteria<T> gt(String property, Object value) {
		return (QCriteria<T>)super.gt(property, value);
	}

	@Override
	public QCriteria<T> ilike(String property, Object value) {
		return (QCriteria<T>)super.ilike(property, value);
	}

	@Override
	public QCriteria<T> isnotnull(String property) {
		return (QCriteria<T>)super.isnotnull(property);
	}

	@Override
	public QCriteria<T> isnull(String property) {
		return (QCriteria<T>)super.isnull(property);
	}

	@Override
	public QCriteria<T> le(String property, double value) {
		return (QCriteria<T>)super.le(property, value);
	}

	@Override
	public QCriteria<T> le(String property, long value) {
		return (QCriteria<T>)super.le(property, value);
	}

	@Override
	public QCriteria<T> le(String property, Object value) {
		return (QCriteria<T>)super.le(property, value);
	}

	@Override
	public QCriteria<T> like(String property, Object value) {
		return (QCriteria<T>)super.like(property, value);
	}

	@Override
	public QCriteria<T> lt(String property, double value) {
		return (QCriteria<T>)super.lt(property, value);
	}

	@Override
	public QCriteria<T> lt(String property, long value) {
		return (QCriteria<T>)super.lt(property, value);
	}

	@Override
	public QCriteria<T> lt(String property, Object value) {
		return (QCriteria<T>)super.lt(property, value);
	}

	@Override
	public QCriteria<T> ne(String property, double value) {
		return (QCriteria<T>)super.ne(property, value);
	}

	@Override
	public QCriteria<T> ne(String property, long value) {
		return (QCriteria<T>)super.ne(property, value);
	}

	@Override
	public QCriteria<T> ne(String property, Object value) {
		return (QCriteria<T>)super.ne(property, value);
	}

	@Override
	public QCriteria<T> or(QOperatorNode... a) {
		return (QCriteria<T>)super.or(a);
	}

	@Override
	public QCriteria<T> sqlCondition(String sql) {
		return (QCriteria<T>)super.sqlCondition(sql);
	}
}
