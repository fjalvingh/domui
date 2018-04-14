/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * (R) this library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * (R) this library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with (R) this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.webapp.query;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.annotations.GProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class representing most of the query structure, just not the public interface part.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 21, 2009
 */
public class QCriteriaQueryBase<T, R extends QCriteriaQueryBase<T, R>> extends QRestrictor<T, R> {
	/** If (R) this is a selection query instead of an object instance query, (R) this will contain the selected items. */
	@NonNull
	private List<QSelectionColumn> m_itemList = Collections.EMPTY_LIST;

	private int m_limit = -1;

	private int m_start = 0;

	private int m_timeout = -1;

	@Nullable
	private String m_testId;

	/** The restrictions (where clause) in effect. */
	@Nullable
	private QOperatorNode m_restrictions;

	@NonNull
	private List<QOrder> m_order = Collections.EMPTY_LIST;

	/** Query options */
	@Nullable
	private Map<String, Object> m_optionMap;

	private Map<String, QFetchStrategy> m_fetchMap = Collections.EMPTY_MAP;

	protected QCriteriaQueryBase(@NonNull Class<T> clz) {
		super(clz, QOperation.AND);
	}

	protected QCriteriaQueryBase(@NonNull ICriteriaTableDef<T> meta) {
		super(meta, QOperation.AND);
	}

	//	/** jal 20100122 Copy constructor needs to do FULL DEEP COPY of the data since it is no longer immutable!!
	//	 * Copy constructor.
	//	 * @param q
	//	 */
	//	public QCriteriaQueryBase(R q) {
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
	 */
	@NonNull
	public List<QSelectionColumn> getColumnList() {
		return m_itemList;
	}

	/**
	 * Set an option for the query or some of it's listeners.
	 */
	public void setOption(@NonNull String name, @Nullable Object val) {
		Map<String, Object> optionMap = m_optionMap;
		if(optionMap == null)
			optionMap = m_optionMap = new HashMap<String, Object>();
		optionMap.put(name, val);
	}

	/**
	 * Returns T if the specified option is present.
	 */
	public boolean hasOption(@NonNull String name) {
		Map<String, Object> optionMap = m_optionMap;
		return optionMap != null && optionMap.containsKey(name);
	}

	/**
	 * Get the value stored for a given option.
	 */
	@Nullable
	public Object getOption(@NonNull String name) {
		Map<String, Object> optionMap = m_optionMap;
		return optionMap == null ? null : optionMap.get(name);
	}

	@NonNull
	public Map<String, QFetchStrategy> getFetchStrategies() {
		return m_fetchMap;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Object selectors.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a column selector to the selection list.
	 */
	protected void addColumn(@NonNull QSelectionItem item, @Nullable String alias) {
		QSelectionColumn col = new QSelectionColumn(item, alias);
		if(m_itemList.size() == 0) {
			m_itemList = new ArrayList<QSelectionColumn>();
		}
		m_itemList.add(col);
	}

	/**
	 * Add a simple property selector to the list.
	 */
	protected void addPropertySelection(@NonNull QSelectionFunction f, @NonNull @GProperty String prop, @Nullable String alias) {
		if(prop == null || prop.length() == 0)
			throw new ProgrammerErrorException("The property for a " + f + " selection cannot be null or empty");
		QPropertySelection ps = new QPropertySelection(f, prop);
		addColumn(ps, alias);
	}

	/**
	 * Add a simple property selector to the list.
	 */
	protected <V> void addPropertySelection(@NonNull QSelectionFunction f, @NonNull QField<T, V> property, @Nullable String alias) {
		String prop = property.getName();
		if(prop == null || prop.length() == 0)
			throw new ProgrammerErrorException("The property for a " + f + " selection cannot be null or empty");
		QPropertySelection ps = new QPropertySelection(f, prop);
		addColumn(ps, alias);
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public R selectProperty(@NonNull @GProperty final String property) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, null);
		return (R) this;
	}

	@NonNull
	public <V> R selectProperty(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, null);
		return (R) this;
	}

	/**
	 * Select a property value from the base property in the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public R selectProperty(@NonNull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, alias);
		return (R) this;
	}

	@NonNull
	public <V> R selectProperty(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.PROPERTY, property, alias);
		return (R) this;
	}

	/**
	 * Select the max of a property in the set. (R) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public R max(@NonNull @GProperty String property) {
		addPropertySelection(QSelectionFunction.MAX, property, null);
		return (R) this;
	}

	@NonNull
	public <V> R max(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.MAX, property, null);
		return (R) this;
	}

	/**
	 * Select the max of a property in the set. (R) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public R max(@NonNull @GProperty String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.MAX, property, alias);
		return (R) this;
	}

	@NonNull
	public <V> R max(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.MAX, property, alias);
		return (R) this;
	}

	/**
	 * Select the minimal value of a property in the set. (R) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public R min(@NonNull @GProperty final String property) {
		addPropertySelection(QSelectionFunction.MIN, property, null);
		return (R) this;
	}

	@NonNull
	public <V> R min(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.MIN, property, null);
		return (R) this;
	}

	/**
	 * Select the minimal value of a property in the set. (R) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public R min(@NonNull @GProperty String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.MIN, property, alias);
		return (R) this;
	}

	@NonNull
	public <V> R min(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.MIN, property, alias);
		return (R) this;
	}

	/**
	 * Select the average value of a property in the set. (R) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public R avg(@NonNull @GProperty String property) {
		addPropertySelection(QSelectionFunction.AVG, property, null);
		return (R) this;
	}

	@NonNull
	public <V> R avg(@NonNull @GProperty QField<T, V> property) {
		addPropertySelection(QSelectionFunction.AVG, property, null);
		return (R) this;
	}

	/**
	 * Select the average value of a property in the set. (R) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public R avg(@NonNull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.AVG, property, alias);
		return (R) this;
	}

	@NonNull
	public <V> R avg(@NonNull @GProperty QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.AVG, property, alias);
		return (R) this;
	}

	/**
	 * Select the sum of a property in the set. (R) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public R sum(@NonNull @GProperty String property) {
		addPropertySelection(QSelectionFunction.SUM, property, null);
		return (R) this;
	}

	@NonNull
	public <V> R sum(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.SUM, property, null);
		return (R) this;
	}

	/**
	 * Select the sum of a property in the set. (R) this will cause a group by.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public R sum(@NonNull @GProperty String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.SUM, property, alias);
		return (R) this;
	}

	@NonNull
	public <V> R sum(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.SUM, property, alias);
		return (R) this;
	}

	/**
	 * Select a count over the result set.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public R count(@NonNull @GProperty String property) {
		addPropertySelection(QSelectionFunction.COUNT, property, null);
		return (R) this;
	}

	@NonNull
	public <V> R count(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.COUNT, property, null);
		return (R) this;
	}

	/**
	 * Select a count over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public R count(@NonNull @GProperty String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.COUNT, property, alias);
		return (R) this;
	}

	@NonNull
	public <V> R count(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.COUNT, property, alias);
		return (R) this;
	}

	/**
	 * Select a count of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public R countDistinct(@NonNull @GProperty String property) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, null);
		return (R) this;
	}

	@NonNull
	public <V> R countDistinct(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, null);
		return (R) this;
	}

	/**
	 * Select a count of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 * @param alias			The alias for using the property in the restrictions clause.
	 */
	@NonNull
	public R countDistinct(@NonNull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, alias);
		return (R) this;
	}

	@NonNull
	public <V> R countDistinct(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.COUNT_DISTINCT, property, alias);
		return (R) this;
	}

	/**
	 * Select of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public R distinct(@NonNull @GProperty String property) {
		addPropertySelection(QSelectionFunction.DISTINCT, property, null);
		return (R) this;
	}

	@NonNull
	public <V> R distinct(@NonNull QField<T, V> property) {
		addPropertySelection(QSelectionFunction.DISTINCT, property, null);
		return (R) this;
	}

	/**
	 * Select of the distinct values over the result set.
	 * @param property		The property whose literal value is to be selected
	 */
	@NonNull
	public R distinct(@NonNull @GProperty final String property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.DISTINCT, property, alias);
		return (R) this;
	}

	@NonNull
	public <V> R distinct(@NonNull QField<T, V> property, @Nullable String alias) {
		addPropertySelection(QSelectionFunction.DISTINCT, property, alias);
		return (R) this;
	}

	/**
	 * Add an order clause to the list of sort items.
	 */
	@NonNull
	public R add(@NonNull QOrder r) {
		if(m_order == Collections.EMPTY_LIST)
			m_order = new ArrayList<>();
		m_order.add(r);
		return (R) this;
	}

	/**
	 * Add a property to do an ascending sort on.
	 */
	@NonNull
	public R ascending(@NonNull @GProperty String property) {
		add(QOrder.ascending(property));
		return (R) this;
	}

	@NonNull
	public <V> R ascending(@NonNull QField<T, V> property) {
		add(QOrder.ascending(property.getName()));
		return (R) this;
	}

	/**
	 * Add a property to do a descending sort on.
	 */
	@NonNull
	public R descending(@NonNull @GProperty String property) {
		add(QOrder.descending(property));
		return (R) this;
	}

	@NonNull
	public <V> R descending(@NonNull QField<T, V> property) {
		add(QOrder.descending(property.getName()));
		return (R) this;
	}


	/**
	 * Limit the #of rows to the specified count.
	 */
	@NonNull
	public R limit(int limit) {
		m_limit = limit;
		return (R) this;
	}

	/**
	 * Start returning rows at the specified index in the result set (0-based).
	 */
	@NonNull
	public R start(int start) {
		m_start = start;
		return (R) this;
	}

	/**
	 * Returns the limit.
	 */
	final public int getLimit() {
		return m_limit;
	}

	/**
	 * Returns the start index set.
	 */
	final public int getStart() {
		return m_start;
	}

	/**
	 * Returns the order-by list.
	 */
	@NonNull
	final public List<QOrder> getOrder() {
		return m_order;
	}

	public int getTimeout() {
		return m_timeout;
	}

	/**
	 * Set the query timeout, in seconds. (R) this only works if supported by the underlying query platform, which may
	 * impose it's own query timeout on queries regardless of (R) this setting. If implemented, the query throws a
	 * {@link QQueryTimeoutException} when the query is aborted because it ran too long. When unset the value defaults
	 * to -1 which means "use the default timeout"; 0 means "no timeout at all"; all others is the timeout in seconds.
	 */
	public void setTimeout(int timeout) {
		m_timeout = timeout;
	}

	/**
	 * Set a fetch strategy for a relation.
	 */
	@NonNull
	public R fetch(@NonNull @GProperty String property, @NonNull QFetchStrategy strategy) {
		if(m_fetchMap.size() == 0)
			m_fetchMap = new HashMap<>();
		m_fetchMap.put(property, strategy);
		return (R) this;
	}

	@NonNull
	public <V> R fetch(@NonNull QField<T, V> property, @NonNull QFetchStrategy strategy) {
		if(m_fetchMap.size() == 0)
			m_fetchMap = new HashMap<>();
		m_fetchMap.put(property.getName(), strategy);
		return (R) this;
	}


	@Nullable
	public String getTestId(){
		return m_testId;
	}

	public void setTestId(@Nullable String testId){
		m_testId = testId;
	}
}
