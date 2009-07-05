package to.etc.webapp.query;

import java.util.*;

/**
 * Base class representing the "where" part generation common to most use classes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 21, 2009
 */
public class QRestrictionsBase {
	private int m_limit = -1;

	private int m_start = 0;

	private List<QOperatorNode> m_restrictionList = Collections.EMPTY_LIST;

	private List<QOrder> m_order = Collections.EMPTY_LIST;

	QRestrictionsBase() {}

	/**
	 * Copy constructor.
	 * @param q
	 */
	public QRestrictionsBase(QRestrictionsBase q) {
		m_restrictionList = new ArrayList<QOperatorNode>(q.m_restrictionList);
		m_order = new ArrayList<QOrder>(q.m_order);
		m_limit = q.m_limit;
		m_start = q.m_start;
	}

	/**
	 * Add a new restriction to the list of restrictions on the data.
	 * @param r
	 * @return
	 */
	public QRestrictionsBase add(QOperatorNode r) {
		if(m_restrictionList == Collections.EMPTY_LIST)
			m_restrictionList = new ArrayList<QOperatorNode>();
		m_restrictionList.add(r);
		return this;
	}

	/**
	 * Add an order item to the list of sort items.
	 * @param r
	 * @return
	 */
	public QRestrictionsBase add(QOrder r) {
		if(m_order == Collections.EMPTY_LIST)
			m_order = new ArrayList<QOrder>();
		m_order.add(r);
		return this;
	}

	/**
	 * Add a property to do an ascending sort on.
	 * @param property
	 * @return
	 */
	public QRestrictionsBase ascending(String property) {
		add(QOrder.ascending(property));
		return this;
	}

	/**
	 * Add a property to do a descending sort on.
	 * @param property
	 * @return
	 */
	public QRestrictionsBase descending(String property) {
		add(QOrder.descending(property));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase eq(String property, Object value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase eq(String property, long value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase eq(String property, double value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase ne(String property, Object value) {
		add(QRestriction.ne(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase ne(String property, long value) {
		add(QRestriction.ne(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase ne(String property, double value) {
		add(QRestriction.ne(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase gt(String property, Object value) {
		add(QRestriction.gt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase gt(String property, long value) {
		add(QRestriction.gt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase gt(String property, double value) {
		add(QRestriction.gt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase lt(String property, Object value) {
		add(QRestriction.lt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase lt(String property, long value) {
		add(QRestriction.lt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase lt(String property, double value) {
		add(QRestriction.lt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase ge(String property, Object value) {
		add(QRestriction.ge(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase ge(String property, long value) {
		add(QRestriction.ge(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase ge(String property, double value) {
		add(QRestriction.ge(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase le(String property, Object value) {
		add(QRestriction.le(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase le(String property, long value) {
		add(QRestriction.le(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase le(String property, double value) {
		add(QRestriction.le(property, value));
		return this;
	}

	/**
	 * Do a 'like' comparison. The wildcard marks here are always %; a literal % is to
	 * be presented as \%. The comparison is case-dependent.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase like(String property, Object value) {
		add(QRestriction.like(property, value));
		return this;
	}

	/**
	 * Compare the value of a property with two literal bounds.
	 * @param property
	 * @param a
	 * @param b
	 * @return
	 */
	public QRestrictionsBase between(String property, Object a, Object b) {
		add(QRestriction.between(property, a, b));
		return this;
	}

	/**
	 * Do a case-independent 'like' comparison. The wildcard marks here are always %; a literal % is to
	 * be presented as \%. The comparison is case-independent.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase ilike(String property, Object value) {
		add(QRestriction.ilike(property, value));
		return this;
	}

	/**
	 * Add a set of OR nodes to the set.
	 * @param a
	 * @return
	 */
	public QRestrictionsBase or(QOperatorNode... a) {
		add(QRestriction.or(a));
		return this;
	}

	/**
	 * Add the restriction that the property specified must be null.
	 * @param property
	 * @return
	 */
	public QRestrictionsBase isnull(String property) {
		add(QRestriction.isnull(property));
		return this;
	}

	/**
	 * Add the restriction that the property specified must be not-null.
	 *
	 * @param property
	 * @return
	 */
	public QRestrictionsBase isnotnull(String property) {
		add(QRestriction.isnotnull(property));
		return this;
	}

	/**
	 * Add a restriction specified in bare SQL. This is implementation-dependent.
	 * @param sql
	 * @return
	 */
	public QRestrictionsBase sqlCondition(String sql) {
		add(QRestriction.sqlCondition(sql));
		return this;
	}

	/**
	 * Limit the #of rows to the specified count.
	 * @param limit
	 * @return
	 */
	public QRestrictionsBase limit(int limit) {
		m_limit = limit;
		return this;
	}

	/**
	 * Start returning rows at the specified index in the result set (0-based).
	 * @param start
	 * @return
	 */
	public QRestrictionsBase start(int start) {
		m_start = start;
		return this;
	}

	/**
	 * Returns the limit.
	 * @return
	 */
	final public int getLimit() {
		return m_limit;
	}

	/**
	 * Returns the start index set.
	 * @return
	 */
	final public int getStart() {
		return m_start;
	}

	/**
	 * Return all restrictions added to this set; these represent the "where" clause of a query.
	 * @return
	 */
	final public QOperatorNode getRestrictions() {
		if(m_restrictionList.size() == 0)
			return null;
		if(m_restrictionList.size() == 1)
			return m_restrictionList.get(0); // Return the single restriction.
		return new QMultiNode(QOperation.AND, m_restrictionList); // Return an AND of all restrictions
	}

	/**
	 * Returns the #of restrictions added to this set!? Useless??
	 * @return
	 */
	final public boolean hasRestrictions() {
		return m_restrictionList.size() > 0;
	}

	/**
	 * Returns the order-by list.
	 * @return
	 */
	final public List<QOrder> getOrder() {
		return m_order;
	}
}
