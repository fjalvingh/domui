/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.webapp.query;

import to.etc.webapp.*;
import to.etc.webapp.annotations.*;

import javax.annotation.*;
import java.util.*;

/**
 * Base class representing most of the query structure, just not the public interface part.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 21, 2009
 */
public class QCriteriaQueryBase<T> extends QRestrictor<T> {
	/** If this is a selection query instead of an object instance query, this will contain the selected items. */
	@Nonnull
	private List<QSelectionColumn> m_itemList = Collections.EMPTY_LIST;

	private int m_limit = -1;

	private int m_start = 0;

	private int m_timeout = -1;

	@Nullable
	private String m_testId;

	/** The restrictions (where clause) in effect. */
	@Nullable
	private QOperatorNode m_restrictions;

	@Nonnull
	private List<QOrder> m_order = Collections.EMPTY_LIST;

	/** Query options */
	@Nullable
	private Map<String, Object> m_optionMap;

	private Map<String, QFetchStrategy> m_fetchMap = Collections.EMPTY_MAP;

	protected QCriteriaQueryBase(@Nonnull Class<T> clz) {
		super(clz, QOperation.AND);
	}

	protected QCriteriaQueryBase(@Nonnull ICriteriaTableDef<T> meta) {
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

	@Nullable
	@Override
	public QOperatorNode getRestrictions() {
		return m_restrictions;
	}

	@Override
	public void setRestrictions(@Nullable QOperatorNode restrictions) {
		m_restrictions = restrictions;
	}

	/**
	 * Returns all selected columns.
	 * @return
	 */
	@Nonnull
	public List<QSelectionColumn> getColumnList() {
		return m_itemList;
	}

	/**
	 * Set an option for the query or some of it's listeners.
	 * @param name
	 * @param val
	 */
	public void setOption(@Nonnull String name, @Nullable Object val) {
		Map<String, Object> optionMap = m_optionMap;
		if(optionMap == null)
			optionMap = m_optionMap = new HashMap<String, Object>();
		optionMap.put(name, val);
	}

	/**
	 * Returns T if the specified option is present.
	 * @param name
	 * @return
	 */
	public boolean hasOption(@Nonnull String name) {
		Map<String, Object> optionMap = m_optionMap;
		return optionMap != null && optionMap.containsKey(name);
	}

	/**
	 * Get the value stored for a given option.
	 * @param name
	 * @return
	 */
	@Nullable
	public Object getOption(@Nonnull String name) {
		Map<String, Object> optionMap = m_optionMap;
		return optionMap == null ? null : optionMap.get(name);
	}

	@Nonnull
	public Map<String, QFetchStrategy> getFetchStrategies() {
		return m_fetchMap;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Object selectors.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a column selector to the selection list.
	 */
	protected void addColumn(@Nonnull QSelectionItem item, @Nullable String alias) {
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
	protected void addPropertySelection(@Nonnull QSelectionFunction f, @Nonnull @GProperty String prop, @Nullable String alias) {
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
	@Nonnull
	protected QCriteriaQueryBase<T> selectProperty(@Nonnull @GProperty final String property) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, null);
		return this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> selectProperty(@Nonnull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, alias);
		return this;
	}

	/**
	 * Select the max of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> max(@Nonnull @GProperty final String property) {
		addPropertySelection(QSelectionFunction.MAX, property, null);
		return this;
	}

	/**
	 * Select the max of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> max(@Nonnull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.MAX, property, alias);
		return this;
	}

	/**
	 * Select the minimal value of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> min(@Nonnull @GProperty final String property) {
		addPropertySelection(QSelectionFunction.MIN, property, null);
		return this;
	}

	/**
	 * Select the minimal value of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> min(@Nonnull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.MIN, property, alias);
		return this;
	}

	/**
	 * Select the average value of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> avg(@Nonnull @GProperty final String property) {
		addPropertySelection(QSelectionFunction.AVG, property, null);
		return this;
	}

	/**
	 * Select the average value of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> avg(@Nonnull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.AVG, property, alias);
		return this;
	}

	/**
	 * Select the sum of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> sum(@Nonnull @GProperty final String property) {
		addPropertySelection(QSelectionFunction.SUM, property, null);
		return this;
	}

	/**
	 * Select the sum of a property in the set. This will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> sum(@Nonnull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.SUM, property, alias);
		return this;
	}

	/**
	 * Select a count over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> count(@Nonnull @GProperty final String property) {
		addPropertySelection(QSelectionFunction.COUNT, property, null);
		return this;
	}

	/**
	 * Select a count over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> count(@Nonnull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.COUNT, property, alias);
		return this;
	}

	/**
	 * Select a count of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> countDistinct(@Nonnull @GProperty final String property) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, null);
		return this;
	}

	/**
	 * Select a count of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> countDistinct(@Nonnull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, alias);
		return this;
	}

	/**
	 * Select of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> distinct(@Nonnull @GProperty final String property) {
		addPropertySelection(QSelectionFunction.DISTINCT, property, null);
		return this;
	}

	/**
	 * Select of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @return
	 */
	@Nonnull
	protected QCriteriaQueryBase<T> distinct(@Nonnull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.DISTINCT, property, alias);
		return this;
	}

	/**
	 * Add an order clause to the list of sort items.
	 * @param r
	 * @return
	 */
	@Nonnull
	public QCriteriaQueryBase<T> add(@Nonnull QOrder r) {
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
	@Nonnull
	public QCriteriaQueryBase<T> ascending(@Nonnull @GProperty final String property) {
		add(QOrder.ascending(property));
		return this;
	}

	/**
	 * Add a property to do a descending sort on.
	 * @param property
	 * @return
	 */
	@Nonnull
	public QCriteriaQueryBase<T> descending(@Nonnull @GProperty final String property) {
		add(QOrder.descending(property));
		return this;
	}

	/**
	 * Limit the #of rows to the specified count.
	 * @param limit
	 * @return
	 */
	@Nonnull
	public QCriteriaQueryBase<T> limit(int limit) {
		m_limit = limit;
		return this;
	}

	/**
	 * Start returning rows at the specified index in the result set (0-based).
	 * @param start
	 * @return
	 */
	@Nonnull
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
	@Nonnull
	final public List<QOrder> getOrder() {
		return m_order;
	}

	public int getTimeout() {
		return m_timeout;
	}

	/**
	 * Set the query timeout, in seconds. This only works if supported by the underlying query platform, which may
	 * impose it's own query timeout on queries regardless of this setting. If implemented, the query throws a
	 * {@link QQueryTimeoutException} when the query is aborted because it ran too long. When unset the value defaults
	 * to -1 which means "use the default timeout"; 0 means "no timeout at all"; all others is the timeout in seconds.
	 * @param timeout
	 */
	public void setTimeout(int timeout) {
		m_timeout = timeout;
	}

	/**
	 * Set a fetch strategy for a relation.
	 * @param property
	 * @param strategy
	 * @return
	 */
	@Nonnull
	public QCriteriaQueryBase<T> fetch(@Nonnull @GProperty String property, @Nonnull QFetchStrategy strategy) {
		if(m_fetchMap.size() == 0)
			m_fetchMap = new HashMap<String, QFetchStrategy>();
		m_fetchMap.put(property, strategy);
		return this;
	}

	@Nullable
	public String getTestId(){
		return m_testId;
	}

	public void setTestId(@Nullable String testId){
		m_testId = testId;
	}
}
