package to.etc.webapp.query;

import java.util.*;

/**
 * Represents a <i>selection</i> of data elements from a database. This differs from
 * a QCriteria in that it collects not one persistent class instance per row but multiple
 * items per row, and each item can either be a persistent class or some property or
 * calculated value (max, min, count et al).
 *
 * QSelection queries return an array of items for each row, and each element
 * of the array is typed depending on it's source. In addition, QSelection queries
 * expose the ability to handle grouping. QSelection criteria behave as and should
 * be seen as SQL queries in an OO wrapping.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 21, 2009
 */
public class QSelection extends QRestrictionsBase {
	private final List<QSelectedItem> m_itemList = Collections.EMPTY_LIST;

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#add(to.etc.webapp.query.QOperatorNode)
	 */
	@Override
	public QSelection add(final QOperatorNode r) {
		return (QSelection) super.add(r);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#add(to.etc.webapp.query.QOrder)
	 */
	@Override
	public QSelection add(final QOrder r) {
		return (QSelection) super.add(r);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ascending(java.lang.String)
	 */
	@Override
	public QSelection ascending(final String property) {
		return (QSelection) super.ascending(property);
	}
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#between(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public QSelection between(final String property, final Object a, final Object b) {
		return (QSelection) super.between(property, a, b);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#descending(java.lang.String)
	 */
	@Override
	public QSelection descending(final String property) {
		return (QSelection) super.descending(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#eq(java.lang.String, double)
	 */
	@Override
	public QSelection eq(final String property, final double value) {
		return (QSelection) super.eq(property, value);
	}
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#eq(java.lang.String, long)
	 */
	@Override
	public QSelection eq(final String property, final long value) {
		return (QSelection) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#eq(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection eq(final String property, final Object value) {
		return (QSelection) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ge(java.lang.String, double)
	 */
	@Override
	public QSelection ge(final String property, final double value) {
		return (QSelection) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ge(java.lang.String, long)
	 */
	@Override
	public QSelection ge(final String property, final long value) {
		return (QSelection) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ge(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection ge(final String property, final Object value) {
		return (QSelection) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#gt(java.lang.String, double)
	 */
	@Override
	public QSelection gt(final String property, final double value) {
		return (QSelection) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#gt(java.lang.String, long)
	 */
	@Override
	public QSelection gt(final String property, final long value) {
		return (QSelection) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#gt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection gt(final String property, final Object value) {
		return (QSelection) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ilike(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection ilike(final String property, final Object value) {
		return (QSelection) super.ilike(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#isnotnull(java.lang.String)
	 */
	@Override
	public QSelection isnotnull(final String property) {
		return (QSelection) super.isnotnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#isnull(java.lang.String)
	 */
	@Override
	public QSelection isnull(final String property) {
		return (QSelection) super.isnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#le(java.lang.String, double)
	 */
	@Override
	public QSelection le(final String property, final double value) {
		return (QSelection) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#le(java.lang.String, long)
	 */
	@Override
	public QSelection le(final String property, final long value) {
		return (QSelection) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#le(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection le(final String property, final Object value) {
		return (QSelection) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#like(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection like(final String property, final Object value) {
		return (QSelection) super.like(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#lt(java.lang.String, double)
	 */
	@Override
	public QSelection lt(final String property, final double value) {
		return (QSelection) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#lt(java.lang.String, long)
	 */
	@Override
	public QSelection lt(final String property, final long value) {
		return (QSelection) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#lt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection lt(final String property, final Object value) {
		return (QSelection) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ne(java.lang.String, double)
	 */
	@Override
	public QSelection ne(final String property, final double value) {
		return (QSelection) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ne(java.lang.String, long)
	 */
	@Override
	public QSelection ne(final String property, final long value) {
		return (QSelection) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#ne(java.lang.String, java.lang.Object)
	 */
	@Override
	public QSelection ne(final String property, final Object value) {
		return (QSelection) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#or(to.etc.webapp.query.QOperatorNode[])
	 */
	@Override
	public QSelection or(final QOperatorNode... a) {
		return (QSelection) super.or(a);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QRestrictionsBase#sqlCondition(java.lang.String)
	 */
	@Override
	public QSelection sqlCondition(final String sql) {
		return (QSelection) super.sqlCondition(sql);
	}
}
