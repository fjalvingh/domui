package to.etc.webapp.query;

import java.util.*;

import to.etc.webapp.*;
import to.etc.webapp.annotations.*;

/**
 * Base class representing most of the query structure, just not the public interface part.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 21, 2009
 */
public class QCriteriaQueryBase<T> extends QRestrictor<T> {
	/** If this is a selection query instead of an object instance query, this will contain the selected items. */
	private List<QSelectionColumn> m_itemList = Collections.EMPTY_LIST;

	private int m_limit = -1;

	private int m_start = 0;

	/** The restrictions (where clause) in effect. */
	private QOperatorNode m_restrictions;

	private List<QOrder> m_order = Collections.EMPTY_LIST;

	/** Query options */
	private Map<String, Object> m_optionMap;

	protected QCriteriaQueryBase(Class<T> clz) {
		super(clz, QOperation.AND);
	}

	protected QCriteriaQueryBase(ICriteriaTableDef<T> meta) {
		super(meta, QOperation.AND);
	}

	//	/** jal 20100122 Copy constructor needs to do FULL DEEP COPY of the data since it is no longer immutable!!
	//	 * Copy constructor.
	//	 * @param q
	//	 */
	//	public QCriteriaQueryBase(QCriteriaQueryBase<T> q) {
	//		super(q.getBaseClass());
	//		m_order = new ArrayList<QOrder>(q.m_order);
	//		m_limit = q.m_limit;
	//		m_start = q.m_start;
	//		if(q.m_restrictions != null) {
	//			if(q.m_restrictions.getOperation() == QOperation.AND) {
	//				m_restrictions = new QMultiNode(QOperation.AND);
	//				for(QOperatorNode qn : ((QMultiNode) q.m_restrictions).getChildren())
	//					((QMultiNode) m_restrictions).add(qn);
	//			} else {
	//				m_restrictions = q.m_restrictions;
	//			}
	//		}
	//	}

	@Override
	public QOperatorNode getRestrictions() {
		return m_restrictions;
	}

	@Override
	public void setRestrictions(QOperatorNode restrictions) {
		m_restrictions = restrictions;
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
	protected void addPropertySelection(QSelectionFunction f, @GProperty String prop, String alias) {
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
	protected QCriteriaQueryBase<T> selectProperty(@GProperty final String property) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, null);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QCriteriaQueryBase<T> selectProperty(@GProperty final String property, String alias) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, alias);
		return this;
	}

	/**
	 * Select the max of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QCriteriaQueryBase<T> max(@GProperty final String property) {
		addPropertySelection(QSelectionFunction.MAX, property, null);
		return this;
	}

	/**
	 * Select the max of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QCriteriaQueryBase<T> max(@GProperty final String property, String alias) {
		addPropertySelection(QSelectionFunction.MAX, property, alias);
		return this;
	}

	/**
	 * Select the minimal value of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QCriteriaQueryBase<T> min(@GProperty final String property) {
		addPropertySelection(QSelectionFunction.MIN, property, null);
		return this;
	}

	/**
	 * Select the minimal value of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QCriteriaQueryBase<T> min(@GProperty final String property, String alias) {
		addPropertySelection(QSelectionFunction.MIN, property, alias);
		return this;
	}

	/**
	 * Select the average value of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QCriteriaQueryBase<T> avg(@GProperty final String property) {
		addPropertySelection(QSelectionFunction.AVG, property, null);
		return this;
	}

	/**
	 * Select the average value of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QCriteriaQueryBase<T> avg(@GProperty final String property, String alias) {
		addPropertySelection(QSelectionFunction.AVG, property, alias);
		return this;
	}

	/**
	 * Select the sum of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QCriteriaQueryBase<T> sum(@GProperty final String property) {
		addPropertySelection(QSelectionFunction.SUM, property, null);
		return this;
	}

	/**
	 * Select the sum of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QCriteriaQueryBase<T> sum(@GProperty final String property, String alias) {
		addPropertySelection(QSelectionFunction.SUM, property, alias);
		return this;
	}

	/**
	 * Select a count over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QCriteriaQueryBase<T> count(@GProperty final String property) {
		addPropertySelection(QSelectionFunction.COUNT, property, null);
		return this;
	}

	/**
	 * Select a count over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QCriteriaQueryBase<T> count(@GProperty final String property, String alias) {
		addPropertySelection(QSelectionFunction.COUNT, property, alias);
		return this;
	}

	/**
	 * Select a count of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	protected QCriteriaQueryBase<T> countDistinct(@GProperty final String property) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, null);
		return this;
	}

	/**
	 * Select a count of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	protected QCriteriaQueryBase<T> countDistinct(@GProperty final String property, String alias) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, alias);
		return this;
	}

	/**
	 * Add an order clause to the list of sort items.
	 * @param r
	 * @return
	 */
	public QCriteriaQueryBase<T> add(QOrder r) {
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
	public QCriteriaQueryBase<T> ascending(@GProperty final String property) {
		add(QOrder.ascending(property));
		return this;
	}

	/**
	 * Add a property to do a descending sort on.
	 * @param property
	 * @return
	 */
	public QCriteriaQueryBase<T> descending(@GProperty final String property) {
		add(QOrder.descending(property));
		return this;
	}

	/**
	 * Limit the #of rows to the specified count.
	 * @param limit
	 * @return
	 */
	public QCriteriaQueryBase<T> limit(int limit) {
		m_limit = limit;
		return this;
	}

	/**
	 * Start returning rows at the specified index in the result set (0-based).
	 * @param start
	 * @return
	 */
	public QCriteriaQueryBase<T> start(int start) {
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
	 * Returns the order-by list.
	 * @return
	 */
	final public List<QOrder> getOrder() {
		return m_order;
	}


}
