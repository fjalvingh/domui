package to.etc.webapp.query;

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
	private final Class<T>			m_baseClass;

	private QCriteria(final Class<T> b) {
		m_baseClass = b;
	}

	/**
	 * Copy constructor.
	 * @param q
	 */
	protected QCriteria(final QCriteria<T> q) {
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
	static public <U> QCriteria<U>	create(final Class<U> clz) {
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
	public void		visit(final QNodeVisitor v) throws Exception {
		v.visitCriteria(this);
	}

	@Override
	public QCriteria<T> add(final QOperatorNode r) {
		return (QCriteria<T>)super.add(r);
	}

	@Override
	public QCriteria<T> add(final QOrder r) {
		return (QCriteria<T>)super.add(r);
	}

	@Override
	public QCriteria<T> ascending(final String property) {
		return (QCriteria<T>)super.ascending(property);
	}

	@Override
	public QCriteria<T> between(final String property, final Object a, final Object b) {
		return (QCriteria<T>)super.between(property, a, b);
	}

	@Override
	public QCriteria<T> descending(final String property) {
		return (QCriteria<T>)super.descending(property);
	}

	@Override
	public QCriteria<T> eq(final String property, final double value) {
		return (QCriteria<T>)super.eq(property, value);
	}

	@Override
	public QCriteria<T> eq(final String property, final long value) {
		return (QCriteria<T>)super.eq(property, value);
	}

	@Override
	public QCriteria<T> eq(final String property, final Object value) {
		return (QCriteria<T>)super.eq(property, value);
	}

	@Override
	public QCriteria<T> ge(final String property, final double value) {
		return (QCriteria<T>)super.ge(property, value);
	}

	@Override
	public QCriteria<T> ge(final String property, final long value) {
		return (QCriteria<T>)super.ge(property, value);
	}

	@Override
	public QCriteria<T> ge(final String property, final Object value) {
		return (QCriteria<T>)super.ge(property, value);
	}

	@Override
	public QCriteria<T> gt(final String property, final double value) {
		return (QCriteria<T>)super.gt(property, value);
	}

	@Override
	public QCriteria<T> gt(final String property, final long value) {
		return (QCriteria<T>)super.gt(property, value);
	}

	@Override
	public QCriteria<T> gt(final String property, final Object value) {
		return (QCriteria<T>)super.gt(property, value);
	}

	@Override
	public QCriteria<T> ilike(final String property, final Object value) {
		return (QCriteria<T>)super.ilike(property, value);
	}

	@Override
	public QCriteria<T> isnotnull(final String property) {
		return (QCriteria<T>)super.isnotnull(property);
	}

	@Override
	public QCriteria<T> isnull(final String property) {
		return (QCriteria<T>)super.isnull(property);
	}

	@Override
	public QCriteria<T> le(final String property, final double value) {
		return (QCriteria<T>)super.le(property, value);
	}

	@Override
	public QCriteria<T> le(final String property, final long value) {
		return (QCriteria<T>)super.le(property, value);
	}

	@Override
	public QCriteria<T> le(final String property, final Object value) {
		return (QCriteria<T>)super.le(property, value);
	}

	@Override
	public QCriteria<T> like(final String property, final Object value) {
		return (QCriteria<T>)super.like(property, value);
	}

	@Override
	public QCriteria<T> lt(final String property, final double value) {
		return (QCriteria<T>)super.lt(property, value);
	}

	@Override
	public QCriteria<T> lt(final String property, final long value) {
		return (QCriteria<T>)super.lt(property, value);
	}

	@Override
	public QCriteria<T> lt(final String property, final Object value) {
		return (QCriteria<T>)super.lt(property, value);
	}

	@Override
	public QCriteria<T> ne(final String property, final double value) {
		return (QCriteria<T>)super.ne(property, value);
	}

	@Override
	public QCriteria<T> ne(final String property, final long value) {
		return (QCriteria<T>)super.ne(property, value);
	}

	@Override
	public QCriteria<T> ne(final String property, final Object value) {
		return (QCriteria<T>)super.ne(property, value);
	}

	@Override
	public QCriteria<T> or(final QOperatorNode... a) {
		return (QCriteria<T>)super.or(a);
	}

	@Override
	public QCriteria<T> sqlCondition(final String sql) {
		return (QCriteria<T>)super.sqlCondition(sql);
	}
}
