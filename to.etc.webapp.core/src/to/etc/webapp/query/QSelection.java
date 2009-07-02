package to.etc.webapp.query;

import java.util.*;

/**
 * Represents a selection of data elements from a database. This differs from
 * a QCriteria in that it collects not one item per row but multiple items per
 * row, and each item can either be a persistent class or some property or
 * calculated value (max, min, count et al).
 * QSelection queries return an array of items for each row, and each element
 * of the array is typed depending on it's source. In addition, QSelection queries
 * expose the ability to handle grouping. QSelection criteria behave as and should
 * be seen as SQL queries in an OO wrapping.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 21, 2009
 */
public class QSelection extends QRestrictionsBase {
	private final List<QSelectedItem>	m_itemList = Collections.EMPTY_LIST;

	@Override
	public QSelection add(final QOperatorNode r) {
		return (QSelection)super.add(r);
	}

	@Override
	public QSelection add(final QOrder r) {
		return (QSelection)super.add(r);
	}

	@Override
	public QSelection ascending(final String property) {
		return (QSelection)super.ascending(property);
	}

	@Override
	public QSelection between(final String property, final Object a, final Object b) {
		return (QSelection)super.between(property, a, b);
	}

	@Override
	public QSelection descending(final String property) {
		return (QSelection)super.descending(property);
	}

	@Override
	public QSelection eq(final String property, final double value) {
		return (QSelection)super.eq(property, value);
	}

	@Override
	public QSelection eq(final String property, final long value) {
		return (QSelection)super.eq(property, value);
	}

	@Override
	public QSelection eq(final String property, final Object value) {
		return (QSelection)super.eq(property, value);
	}

	@Override
	public QSelection ge(final String property, final double value) {
		return (QSelection)super.ge(property, value);
	}

	@Override
	public QSelection ge(final String property, final long value) {
		return (QSelection)super.ge(property, value);
	}

	@Override
	public QSelection ge(final String property, final Object value) {
		return (QSelection)super.ge(property, value);
	}

	@Override
	public QSelection gt(final String property, final double value) {
		return (QSelection)super.gt(property, value);
	}

	@Override
	public QSelection gt(final String property, final long value) {
		return (QSelection)super.gt(property, value);
	}

	@Override
	public QSelection gt(final String property, final Object value) {
		return (QSelection)super.gt(property, value);
	}

	@Override
	public QSelection ilike(final String property, final Object value) {
		return (QSelection)super.ilike(property, value);
	}

	@Override
	public QSelection isnotnull(final String property) {
		return (QSelection)super.isnotnull(property);
	}

	@Override
	public QSelection isnull(final String property) {
		return (QSelection)super.isnull(property);
	}

	@Override
	public QSelection le(final String property, final double value) {
		return (QSelection)super.le(property, value);
	}

	@Override
	public QSelection le(final String property, final long value) {
		return (QSelection)super.le(property, value);
	}

	@Override
	public QSelection le(final String property, final Object value) {
		return (QSelection)super.le(property, value);
	}

	@Override
	public QSelection like(final String property, final Object value) {
		return (QSelection)super.like(property, value);
	}

	@Override
	public QSelection lt(final String property, final double value) {
		return (QSelection)super.lt(property, value);
	}

	@Override
	public QSelection lt(final String property, final long value) {
		return (QSelection)super.lt(property, value);
	}

	@Override
	public QSelection lt(final String property, final Object value) {
		return (QSelection)super.lt(property, value);
	}

	@Override
	public QSelection ne(final String property, final double value) {
		return (QSelection)super.ne(property, value);
	}

	@Override
	public QSelection ne(final String property, final long value) {
		return (QSelection)super.ne(property, value);
	}

	@Override
	public QSelection ne(final String property, final Object value) {
		return (QSelection)super.ne(property, value);
	}

	@Override
	public QSelection or(final QOperatorNode... a) {
		return (QSelection)super.or(a);
	}

	@Override
	public QSelection sqlCondition(final String sql) {
		return (QSelection)super.sqlCondition(sql);
	}
}
