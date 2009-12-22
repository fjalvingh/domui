package to.etc.webapp.query;


/**
 * Represents a <i>selection</i> of data elements from a database. This differs from
 * a QCriteria in that it collects not one persistent class instance per row but multiple
 * items per row, and each item can either be a persistent class or some property or
 * calculated value (max, min, count et al).
 *
 * <p>Even though this type has a generic type parameter representing the base object
 * being queried, the list() method for this object will return a List<Object[]> always.</p>
 *
 * <p>QSelection queries return an array of items for each row, and each element
 * of the array is typed depending on it's source. In addition, QSelection queries
 * expose the ability to handle grouping. QSelection criteria behave as and should
 * be seen as SQL queries in an OO wrapping.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
public class QSelection<T> extends QCriteriaQueryBase<T> {
	private QSelection(Class<T> clz) {
		super(clz);
	}

	/**
	 * Create a selection query based on the specified persistent class (public API).
	 * @param <T>	The base type being queried
	 * @param root	The class representing the base type being queried, thanks to the brilliant Java Generics implementation.
	 * @return
	 */
	static public <T> QSelection<T>	create(Class<T> root) {
		return new QSelection<T>(root);
	}

	public void	visit(QNodeVisitor v) throws Exception {
		v.visitSelection(this);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Object selectors.									*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addColumn(QSelectionItem item, String alias) {
		super.addColumn(item, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPropertySelection(QSelectionFunction f, String prop, String alias) {
		super.addPropertySelection(f, prop, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> avg(String property, String alias) {
		return (QSelection<T>) super.avg(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> avg(String property) {
		return (QSelection<T>) super.avg(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> count(String property, String alias) {
		return (QSelection<T>) super.count(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> count(String property) {
		return (QSelection<T>) super.count(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> countDistinct(String property, String alias) {
		return (QSelection<T>) super.countDistinct(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> countDistinct(String property) {
		return (QSelection<T>) super.countDistinct(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> max(String property, String alias) {
		return (QSelection<T>) super.max(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> max(String property) {
		return (QSelection<T>) super.max(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> min(String property, String alias) {
		return (QSelection<T>) super.min(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> min(String property) {
		return (QSelection<T>) super.min(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> selectProperty(String property, String alias) {
		return (QSelection<T>) super.selectProperty(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> selectProperty(String property) {
		return (QSelection<T>) super.selectProperty(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> sum(String property, String alias) {
		return (QSelection<T>) super.sum(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QSelection<T> sum(String property) {
		return (QSelection<T>) super.sum(property);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Overrides to force return type needed for chaining	*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#add(to.etc.webapp.query.QOperatorNode)
	 */
	@Override
	public QSelection<T> add(final QOperatorNode r) {
		return (QSelection<T>) super.add(r);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#add(to.etc.webapp.query.QOrder)
	 */
	@Override
	public QSelection<T> add(final QOrder r) {
		return (QSelection<T>) super.add(r);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ascending(java.lang.String)
	 */
	@Override
	public QSelection<T> ascending(final String property) {
		return (QSelection<T>) super.ascending(property);
	}
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#between(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public QSelection<T> between(final String property, final Object a, final Object b) {
		return (QSelection<T>) super.between(property, a, b);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#descending(java.lang.String)
	 */
	@Override
	public QSelection<T> descending(final String property) {
		return (QSelection<T>) super.descending(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#eq(java.lang.String, double)
	 */
	@Override
	public QSelection<T> eq(final String property, final double value) {
		return (QSelection<T>) super.eq(property, value);
	}
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#eq(java.lang.String, long)
	 */
	@Override
	public QSelection<T> eq(final String property, final long value) {
		return (QSelection<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#eq(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> eq(final String property, final Object value) {
		return (QSelection<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ge(java.lang.String, double)
	 */
	@Override
	public QSelection<T> ge(final String property, final double value) {
		return (QSelection<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ge(java.lang.String, long)
	 */
	@Override
	public QSelection<T> ge(final String property, final long value) {
		return (QSelection<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ge(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> ge(final String property, final Object value) {
		return (QSelection<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#gt(java.lang.String, double)
	 */
	@Override
	public QSelection<T> gt(final String property, final double value) {
		return (QSelection<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#gt(java.lang.String, long)
	 */
	@Override
	public QSelection<T> gt(final String property, final long value) {
		return (QSelection<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#gt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> gt(final String property, final Object value) {
		return (QSelection<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ilike(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> ilike(final String property, final Object value) {
		return (QSelection<T>) super.ilike(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#isnotnull(java.lang.String)
	 */
	@Override
	public QSelection<T> isnotnull(final String property) {
		return (QSelection<T>) super.isnotnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#isnull(java.lang.String)
	 */
	@Override
	public QSelection<T> isnull(final String property) {
		return (QSelection<T>) super.isnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#le(java.lang.String, double)
	 */
	@Override
	public QSelection<T> le(final String property, final double value) {
		return (QSelection<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#le(java.lang.String, long)
	 */
	@Override
	public QSelection<T> le(final String property, final long value) {
		return (QSelection<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#le(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> le(final String property, final Object value) {
		return (QSelection<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#like(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> like(final String property, final Object value) {
		return (QSelection<T>) super.like(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#lt(java.lang.String, double)
	 */
	@Override
	public QSelection<T> lt(final String property, final double value) {
		return (QSelection<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#lt(java.lang.String, long)
	 */
	@Override
	public QSelection<T> lt(final String property, final long value) {
		return (QSelection<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#lt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> lt(final String property, final Object value) {
		return (QSelection<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ne(java.lang.String, double)
	 */
	@Override
	public QSelection<T> ne(final String property, final double value) {
		return (QSelection<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ne(java.lang.String, long)
	 */
	@Override
	public QSelection<T> ne(final String property, final long value) {
		return (QSelection<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ne(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> ne(final String property, final Object value) {
		return (QSelection<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#or(to.etc.webapp.query.QOperatorNode[])
	 */
	@Override
	public QSelection<T> or(final QOperatorNode... a) {
		return (QSelection<T>) super.or(a);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#sqlCondition(java.lang.String)
	 */
	@Override
	public QSelection<T> sqlCondition(final String sql) {
		return (QSelection<T>) super.sqlCondition(sql);
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
