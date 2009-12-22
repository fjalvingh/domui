package to.etc.webapp.query;

import java.util.*;

/**
 * Represents the "where" part of a query, or a part of that "where" part, under construction.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2009
 */
public class QRestrictionBase<T> {
	/** The base class being queried in this selector. */
	private final Class<T> m_baseClass;

	private List<QOperatorNode> m_restrictionList = Collections.EMPTY_LIST;

	protected QRestrictionBase(Class<T> baseClass) {
		m_baseClass = baseClass;
	}

	protected QRestrictionBase(QRestrictionBase<T> q) {
		m_baseClass = q.m_baseClass;
		m_restrictionList = new ArrayList<QOperatorNode>(q.m_restrictionList);
	}

	/**
	 * Returns the persistent class being queried and returned.
	 * @return
	 */
	public Class<T> getBaseClass() {
		return m_baseClass;
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


	/*--------------------------------------------------------------*/
	/*	CODING:	Adding selection restrictions (where clause)		*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a new restriction to the list of restrictions on the data. This will do "and" collapsion: when the node added is an "and"
	 * it's nodes will be added directly to the list (because that already represents an and combinatory).
	 * @param r
	 * @return
	 */
	public QRestrictionBase<T> add(QOperatorNode r) {
		if(m_restrictionList == Collections.EMPTY_LIST)
			m_restrictionList = new ArrayList<QOperatorNode>();
		if(r.getOperation() == QOperation.AND) {
			//-- Collapse this node.
			for(QOperatorNode nb : ((QMultiNode) r).getChildren())
				m_restrictionList.add(nb);
			return this;
		}

		m_restrictionList.add(r);
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionBase<T> eq(String property, Object value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionBase<T> eq(String property, long value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionBase<T> eq(String property, double value) {
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
	public QRestrictionBase<T> ne(String property, Object value) {
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
	public QRestrictionBase<T> ne(String property, long value) {
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
	public QRestrictionBase<T> ne(String property, double value) {
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
	public QRestrictionBase<T> gt(String property, Object value) {
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
	public QRestrictionBase<T> gt(String property, long value) {
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
	public QRestrictionBase<T> gt(String property, double value) {
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
	public QRestrictionBase<T> lt(String property, Object value) {
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
	public QRestrictionBase<T> lt(String property, long value) {
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
	public QRestrictionBase<T> lt(String property, double value) {
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
	public QRestrictionBase<T> ge(String property, Object value) {
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
	public QRestrictionBase<T> ge(String property, long value) {
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
	public QRestrictionBase<T> ge(String property, double value) {
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
	public QRestrictionBase<T> le(String property, Object value) {
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
	public QRestrictionBase<T> le(String property, long value) {
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
	public QRestrictionBase<T> le(String property, double value) {
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
	public QRestrictionBase<T> like(String property, Object value) {
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
	public QRestrictionBase<T> between(String property, Object a, Object b) {
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
	public QRestrictionBase<T> ilike(String property, Object value) {
		add(QRestriction.ilike(property, value));
		return this;
	}

	/**
	 * Add a set of OR nodes to the set.
	 * @param a
	 * @return
	 */
	@Deprecated
	public QRestrictionBase<T> or(QOperatorNode a1, QOperatorNode a2, QOperatorNode... rest) {
		QOperatorNode[] ar = new QOperatorNode[rest.length + 2];
		ar[0] = a1;
		ar[1] = a2;
		System.arraycopy(rest, 0, ar, 2, rest.length);
		add(QRestriction.or(ar));
		return this;
	}

	/**
	 * Return a thingy that can be used to create "or" nodes;
	 * @return
	 */
	public QOr<T> or() {
		QMultiNode or = new QMultiNode(QOperation.OR);
		add(or);
		return new QOr<T>(this, or);
	}

	/**
	 * Add the restriction that the property specified must be null.
	 * @param property
	 * @return
	 */
	public QRestrictionBase<T> isnull(String property) {
		add(QRestriction.isnull(property));
		return this;
	}

	/**
	 * Add the restriction that the property specified must be not-null.
	 *
	 * @param property
	 * @return
	 */
	public QRestrictionBase<T> isnotnull(String property) {
		add(QRestriction.isnotnull(property));
		return this;
	}

	/**
	 * Add a restriction specified in bare SQL. This is implementation-dependent.
	 * @param sql
	 * @return
	 */
	public QRestrictionBase<T> sqlCondition(String sql) {
		add(QRestriction.sqlCondition(sql));
		return this;
	}
}
