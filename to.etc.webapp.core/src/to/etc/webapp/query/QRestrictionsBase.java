package to.etc.webapp.query;

import java.util.*;

import to.etc.webapp.*;

/**
 * Base class representing most of the query structure, just not the public interface part.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 21, 2009
 */
public class QRestrictionsBase<T> {
	/** The base class being queried in this selector. */
	private final Class<T> m_baseClass;

	/** If this is a selection query instead of an object instance query, this will contain the selected items. */
	private List<QSelectionColumn> m_itemList = Collections.EMPTY_LIST;

	private int m_limit = -1;

	private int m_start = 0;

	private List<QOperatorNode> m_restrictionList = Collections.EMPTY_LIST;

	private List<QOrder> m_order = Collections.EMPTY_LIST;

	/** Query options */
	private Map<String, Object> m_optionMap = null;

	protected QRestrictionsBase(Class<T> clz) {
		m_baseClass = clz;
	}

	/**
	 * Copy constructor.
	 * @param q
	 */
	public QRestrictionsBase(QRestrictionsBase<T> q) {
		m_baseClass = q.m_baseClass;
		m_restrictionList = new ArrayList<QOperatorNode>(q.m_restrictionList);
		m_order = new ArrayList<QOrder>(q.m_order);
		m_limit = q.m_limit;
		m_start = q.m_start;
	}

	/**
	 * Returns the persistent class being queried and returned.
	 * @return
	 */
	public Class<T> getBaseClass() {
		return m_baseClass;
	}

	/**
	 * Returns all selected columns.
	 * @return
	 */
	public List<QSelectionColumn> getColumnList() {
		return m_itemList;
	}

	/**
	 * Set an option for the query or some of it's listeners.
	 * @param name
	 * @param val
	 */
	public void setOption(String name, Object val) {
		if(m_optionMap == null)
			m_optionMap = new HashMap<String, Object>();
		m_optionMap.put(name, val);
	}

	/**
	 * Returns T if the specified option is present.
	 * @param name
	 * @return
	 */
	public boolean hasOption(String name) {
		return m_optionMap != null && m_optionMap.containsKey(name);
	}

	/**
	 * Get the value stored for a given option.
	 * @param name
	 * @return
	 */
	public Object getOption(String name) {
		return m_optionMap == null ? null : m_optionMap.get(name);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Object selectors.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a column selector to the selection list.
	 */
	protected void addColumn(QSelectionItem item, String alias) {
		QSelectionColumn col = new QSelectionColumn(item, alias);
		if(m_itemList.size() == 0) {
			m_itemList = new ArrayList<QSelectionColumn>();
		}
		m_itemList.add(col);
	}

	/**
	 * Add a simple property selector to the list.
	 * @param f
	 * @param prop
	 * @param alias
	 */
	protected void addPropertySelection(QSelectionFunction f, String prop, String alias) {
		if(prop == null || prop.length() == 0)
			throw new ProgrammerErrorException("The property for a " + f + " selection cannot be null or empty");
		QPropertySelection ps = new QPropertySelection(f, prop);
		addColumn(ps, alias);
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QRestrictionsBase<T> selectProperty(String property) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, null);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QRestrictionsBase<T> selectProperty(String property, String alias) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, alias);
		return this;
	}

	/**
	 * Select the max of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QRestrictionsBase<T> max(String property) {
		addPropertySelection(QSelectionFunction.MAX, property, null);
		return this;
	}

	/**
	 * Select the max of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QRestrictionsBase<T> max(String property, String alias) {
		addPropertySelection(QSelectionFunction.MAX, property, alias);
		return this;
	}

	/**
	 * Select the minimal value of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QRestrictionsBase<T> min(String property) {
		addPropertySelection(QSelectionFunction.MIN, property, null);
		return this;
	}

	/**
	 * Select the minimal value of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QRestrictionsBase<T> min(String property, String alias) {
		addPropertySelection(QSelectionFunction.MIN, property, alias);
		return this;
	}

	/**
	 * Select the average value of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QRestrictionsBase<T> avg(String property) {
		addPropertySelection(QSelectionFunction.AVG, property, null);
		return this;
	}

	/**
	 * Select the average value of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QRestrictionsBase<T> avg(String property, String alias) {
		addPropertySelection(QSelectionFunction.AVG, property, alias);
		return this;
	}

	/**
	 * Select the sum of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QRestrictionsBase<T> sum(String property) {
		addPropertySelection(QSelectionFunction.SUM, property, null);
		return this;
	}

	/**
	 * Select the sum of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QRestrictionsBase<T> sum(String property, String alias) {
		addPropertySelection(QSelectionFunction.SUM, property, alias);
		return this;
	}

	/**
	 * Select a count over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QRestrictionsBase<T> count(String property) {
		addPropertySelection(QSelectionFunction.COUNT, property, null);
		return this;
	}

	/**
	 * Select a count over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QRestrictionsBase<T> count(String property, String alias) {
		addPropertySelection(QSelectionFunction.COUNT, property, alias);
		return this;
	}

	/**
	 * Select a count of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QRestrictionsBase<T> countDistinct(String property) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, null);
		return this;
	}

	/**
	 * Select a count of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QRestrictionsBase<T> countDistinct(String property, String alias) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, alias);
		return this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Adding selection restrictions (where clause)		*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a new restriction to the list of restrictions on the data.
	 * @param r
	 * @return
	 */
	public QRestrictionsBase<T> add(QOperatorNode r) {
		if(m_restrictionList == Collections.EMPTY_LIST)
			m_restrictionList = new ArrayList<QOperatorNode>();
		m_restrictionList.add(r);
		return this;
	}

	/**
	 * Add an order clause to the list of sort items.
	 * @param r
	 * @return
	 */
	public QRestrictionsBase<T> add(QOrder r) {
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
	public QRestrictionsBase<T> ascending(String property) {
		add(QOrder.ascending(property));
		return this;
	}

	/**
	 * Add a property to do a descending sort on.
	 * @param property
	 * @return
	 */
	public QRestrictionsBase<T> descending(String property) {
		add(QOrder.descending(property));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase<T> eq(String property, Object value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase<T> eq(String property, long value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictionsBase<T> eq(String property, double value) {
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
	public QRestrictionsBase<T> ne(String property, Object value) {
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
	public QRestrictionsBase<T> ne(String property, long value) {
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
	public QRestrictionsBase<T> ne(String property, double value) {
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
	public QRestrictionsBase<T> gt(String property, Object value) {
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
	public QRestrictionsBase<T> gt(String property, long value) {
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
	public QRestrictionsBase<T> gt(String property, double value) {
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
	public QRestrictionsBase<T> lt(String property, Object value) {
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
	public QRestrictionsBase<T> lt(String property, long value) {
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
	public QRestrictionsBase<T> lt(String property, double value) {
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
	public QRestrictionsBase<T> ge(String property, Object value) {
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
	public QRestrictionsBase<T> ge(String property, long value) {
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
	public QRestrictionsBase<T> ge(String property, double value) {
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
	public QRestrictionsBase<T> le(String property, Object value) {
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
	public QRestrictionsBase<T> le(String property, long value) {
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
	public QRestrictionsBase<T> le(String property, double value) {
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
	public QRestrictionsBase<T> like(String property, Object value) {
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
	public QRestrictionsBase<T> between(String property, Object a, Object b) {
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
	public QRestrictionsBase<T> ilike(String property, Object value) {
		add(QRestriction.ilike(property, value));
		return this;
	}

	/**
	 * Add a set of OR nodes to the set.
	 * @param a
	 * @return
	 */
	public QRestrictionsBase<T> or(QOperatorNode... a) {
		add(QRestriction.or(a));
		return this;
	}

	/**
	 * Add the restriction that the property specified must be null.
	 * @param property
	 * @return
	 */
	public QRestrictionsBase<T> isnull(String property) {
		add(QRestriction.isnull(property));
		return this;
	}

	/**
	 * Add the restriction that the property specified must be not-null.
	 *
	 * @param property
	 * @return
	 */
	public QRestrictionsBase<T> isnotnull(String property) {
		add(QRestriction.isnotnull(property));
		return this;
	}

	/**
	 * Add a restriction specified in bare SQL. This is implementation-dependent.
	 * @param sql
	 * @return
	 */
	public QRestrictionsBase<T> sqlCondition(String sql) {
		add(QRestriction.sqlCondition(sql));
		return this;
	}

	/**
	 * Limit the #of rows to the specified count.
	 * @param limit
	 * @return
	 */
	public QRestrictionsBase<T> limit(int limit) {
		m_limit = limit;
		return this;
	}

	/**
	 * Start returning rows at the specified index in the result set (0-based).
	 * @param start
	 * @return
	 */
	public QRestrictionsBase<T> start(int start) {
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
