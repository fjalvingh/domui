package to.etc.webapp.query;

import java.util.*;

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
public class QSelection<T> extends QRestrictionsBase {
	final private Class<T>	m_root;
	private final List<QSelectionColumn> m_itemList = Collections.EMPTY_LIST;

	private QSelection(Class<T> clz) {
		m_root = clz;
	}

	/**
	 * Create a selection query based on the specified persistent class.
	 * @param <T>
	 * @param root
	 * @return
	 */
	static public <T> QSelection<T>	create(Class<T> root) {
		return new QSelection(root);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Object selectors.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Select a property value from the base property in the result set.
	 * @param property
	 * @return
	 */
	public QSelection<T>	selectProperty(String property) {


		return this;
	}




	/*--------------------------------------------------------------*/
	/*	CODING:	Overrides to force return type needed for chaining	*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#add(to.etc.webapp.query.QOperatorNode)
	 */
	@Override
	public QSelection<T> add(final QOperatorNode r) {
		return (QSelection<T>) super.add(r);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#add(to.etc.webapp.query.QOrder)
	 */
	@Override
	public QSelection<T> add(final QOrder r) {
		return (QSelection<T>) super.add(r);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ascending(java.lang.String)
	 */
	@Override
	public QSelection<T> ascending(final String property) {
		return (QSelection<T>) super.ascending(property);
	}
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#between(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public QSelection<T> between(final String property, final Object a, final Object b) {
		return (QSelection<T>) super.between(property, a, b);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#descending(java.lang.String)
	 */
	@Override
	public QSelection<T> descending(final String property) {
		return (QSelection<T>) super.descending(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#eq(java.lang.String, double)
	 */
	@Override
	public QSelection<T> eq(final String property, final double value) {
		return (QSelection<T>) super.eq(property, value);
	}
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#eq(java.lang.String, long)
	 */
	@Override
	public QSelection<T> eq(final String property, final long value) {
		return (QSelection<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#eq(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> eq(final String property, final Object value) {
		return (QSelection<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ge(java.lang.String, double)
	 */
	@Override
	public QSelection<T> ge(final String property, final double value) {
		return (QSelection<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ge(java.lang.String, long)
	 */
	@Override
	public QSelection<T> ge(final String property, final long value) {
		return (QSelection<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ge(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> ge(final String property, final Object value) {
		return (QSelection<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#gt(java.lang.String, double)
	 */
	@Override
	public QSelection<T> gt(final String property, final double value) {
		return (QSelection<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#gt(java.lang.String, long)
	 */
	@Override
	public QSelection<T> gt(final String property, final long value) {
		return (QSelection<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#gt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> gt(final String property, final Object value) {
		return (QSelection<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ilike(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> ilike(final String property, final Object value) {
		return (QSelection<T>) super.ilike(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#isnotnull(java.lang.String)
	 */
	@Override
	public QSelection<T> isnotnull(final String property) {
		return (QSelection<T>) super.isnotnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#isnull(java.lang.String)
	 */
	@Override
	public QSelection<T> isnull(final String property) {
		return (QSelection<T>) super.isnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#le(java.lang.String, double)
	 */
	@Override
	public QSelection<T> le(final String property, final double value) {
		return (QSelection<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#le(java.lang.String, long)
	 */
	@Override
	public QSelection<T> le(final String property, final long value) {
		return (QSelection<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#le(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> le(final String property, final Object value) {
		return (QSelection<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#like(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> like(final String property, final Object value) {
		return (QSelection<T>) super.like(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#lt(java.lang.String, double)
	 */
	@Override
	public QSelection<T> lt(final String property, final double value) {
		return (QSelection<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#lt(java.lang.String, long)
	 */
	@Override
	public QSelection<T> lt(final String property, final long value) {
		return (QSelection<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#lt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> lt(final String property, final Object value) {
		return (QSelection<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ne(java.lang.String, double)
	 */
	@Override
	public QSelection<T> ne(final String property, final double value) {
		return (QSelection<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ne(java.lang.String, long)
	 */
	@Override
	public QSelection<T> ne(final String property, final long value) {
		return (QSelection<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ne(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection<T> ne(final String property, final Object value) {
		return (QSelection<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#or(to.etc.webapp.query.QOperatorNode[])
	 */
	@Override
	public QSelection<T> or(final QOperatorNode... a) {
		return (QSelection<T>) super.or(a);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#sqlCondition(java.lang.String)
	 */
	@Override
	public QSelection<T> sqlCondition(final String sql) {
		return (QSelection<T>) super.sqlCondition(sql);
	}
}
