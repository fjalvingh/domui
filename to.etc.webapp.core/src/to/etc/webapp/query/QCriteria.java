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
public class QCriteria<T> extends QCriteriaQueryBase<T> {
	private QCriteria(final Class<T> b) {
		super(b);
	}

	/**
	 * Copy constructor.
	 * @param q
	 */
	protected QCriteria(final QCriteria<T> q) {
		super(q);
	}

	/**
	 * Create a QCriteria to select a set of the specified class. When used on it's own without
	 * added criteria this selects all possible items.
	 * @param <U>
	 * @param clz
	 * @return
	 */
	static public <U> QCriteria<U> create(final Class<U> clz) {
		return new QCriteria<U>(clz);
	}

	/**
	 * Create a duplicate of this Criteria.
	 * @return
	 */
	public QCriteria<T> dup() {
		return new QCriteria<T>(this);
	}

	/**
	 * Visit everything in this QCriteria.
	 * @param v
	 * @throws Exception
	 */
	public void visit(final QNodeVisitor v) throws Exception {
		v.visitCriteria(this);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#add(to.etc.webapp.query.QOperatorNode)
	 */
	@Override
	public QCriteria<T> add(final QOperatorNode r) {
		return (QCriteria<T>) super.add(r);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#add(to.etc.webapp.query.QOrder)
	 */
	@Override
	public QCriteria<T> add(final QOrder r) {
		return (QCriteria<T>) super.add(r);
	}
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ascending(java.lang.String)
	 */
	@Override
	public QCriteria<T> ascending(final String property) {
		return (QCriteria<T>) super.ascending(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#between(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public QCriteria<T> between(final String property, final Object a, final Object b) {
		return (QCriteria<T>) super.between(property, a, b);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#descending(java.lang.String)
	 */
	@Override
	public QCriteria<T> descending(final String property) {
		return (QCriteria<T>) super.descending(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#eq(java.lang.String, double)
	 */
	@Override
	public QCriteria<T> eq(final String property, final double value) {
		return (QCriteria<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#eq(java.lang.String, long)
	 */
	@Override
	public QCriteria<T> eq(final String property, final long value) {
		return (QCriteria<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#eq(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> eq(final String property, final Object value) {
		return (QCriteria<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ge(java.lang.String, double)
	 */
	@Override
	public QCriteria<T> ge(final String property, final double value) {
		return (QCriteria<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ge(java.lang.String, long)
	 */
	@Override
	public QCriteria<T> ge(final String property, final long value) {
		return (QCriteria<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ge(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> ge(final String property, final Object value) {
		return (QCriteria<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#gt(java.lang.String, double)
	 */
	@Override
	public QCriteria<T> gt(final String property, final double value) {
		return (QCriteria<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#gt(java.lang.String, long)
	 */
	@Override
	public QCriteria<T> gt(final String property, final long value) {
		return (QCriteria<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#gt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> gt(final String property, final Object value) {
		return (QCriteria<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ilike(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> ilike(final String property, final Object value) {
		return (QCriteria<T>) super.ilike(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#isnotnull(java.lang.String)
	 */
	@Override
	public QCriteria<T> isnotnull(final String property) {
		return (QCriteria<T>) super.isnotnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#isnull(java.lang.String)
	 */
	@Override
	public QCriteria<T> isnull(final String property) {
		return (QCriteria<T>) super.isnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#le(java.lang.String, double)
	 */
	@Override
	public QCriteria<T> le(final String property, final double value) {
		return (QCriteria<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#le(java.lang.String, long)
	 */
	@Override
	public QCriteria<T> le(final String property, final long value) {
		return (QCriteria<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#le(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> le(final String property, final Object value) {
		return (QCriteria<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#like(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> like(final String property, final Object value) {
		return (QCriteria<T>) super.like(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#lt(java.lang.String, double)
	 */
	@Override
	public QCriteria<T> lt(final String property, final double value) {
		return (QCriteria<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#lt(java.lang.String, long)
	 */
	@Override
	public QCriteria<T> lt(final String property, final long value) {
		return (QCriteria<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#lt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> lt(final String property, final Object value) {
		return (QCriteria<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ne(java.lang.String, double)
	 */
	@Override
	public QCriteria<T> ne(final String property, final double value) {
		return (QCriteria<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ne(java.lang.String, long)
	 */
	@Override
	public QCriteria<T> ne(final String property, final long value) {
		return (QCriteria<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ne(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> ne(final String property, final Object value) {
		return (QCriteria<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#or(to.etc.webapp.query.QOperatorNode[])
	 */
	@Override
	public QCriteria<T> or(final QOperatorNode... a) {
		return (QCriteria<T>) super.or(a);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#sqlCondition(java.lang.String)
	 */
	@Override
	public QCriteria<T> sqlCondition(final String sql) {
		return (QCriteria<T>) super.sqlCondition(sql);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#limit(int)
	 */
	@Override
	public QCriteria<T> limit(final int limit) {
		return (QCriteria<T>) super.limit(limit);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#start(int)
	 */
	@Override
	public QCriteria<T> start(final int start) {
		return (QCriteria<T>) super.start(start);
	}

	@Override
	public String toString() {
		QQueryRenderer	r	= new QQueryRenderer();
		try {
			visit(r);
		} catch(Exception x) {
			x.printStackTrace();
			return "Invalid query: "+x;
		}
		return r.toString();
	}
}
