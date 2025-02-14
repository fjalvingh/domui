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
package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.tbl.SimpleSearchModel.IQuery;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IShelvedListener;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;
import to.etc.webapp.query.IIdentifyable;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextFactory;
import to.etc.webapp.query.QOperatorNode;
import to.etc.webapp.query.QOrder;
import to.etc.webapp.query.QSelection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 3, 2019
 */
public class SimpleSearchModelLazy<T extends IIdentifyable<K>, K> extends TableModelBase<T> implements IModifyableTableModel<T>, IKeyedTableModel<T>, ITruncateableDataModel, ISortableTableModel, IShelvedListener {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleSearchModelLazy.class);

	/**
	 * Thingy to get a database session from, if needed,
	 */
	@Nullable final private QDataContextFactory m_sessionSource;

	@Nullable final private NodeBase m_contextSourceNode;

	@Nullable final private IQueryHandler<T> m_queryHandler;

	/**
	 * Generalized search query.
	 */
	@Nullable
	private QCriteria<T> m_query;

	@Nullable
	private List<K> m_idResultList;

	private boolean[] m_workRefreshed;

	private boolean m_truncated;

	/**
	 * If we sort on property name this is the property name to sort on.
	 */
	@Nullable
	private String m_sort;

	/**
	 * If we sort using sortComparator this is used to identify used comparator - to prevent multiple queries when order is already set to same.
	 */
	@Nullable
	private String m_sortComparatorKey;

	/**
	 * If sorting, this is T if the sort should be descending.
	 */
	private boolean m_desc;

	private boolean m_refreshAfterShelve;

	/**
	 * The max. #of rows to return before truncating.
	 */
	private int m_maxRowCount;

	/**
	 * When set, the thing calculates the actual row count if the initial query overflows.
	 */
	private boolean m_calculateActualRowCount;

	/**
	 * If available (determined by {@link #m_calculateActualRowCount}, the actual row count.
	 */
	@Nullable
	private Integer m_actualRowCount;

	@Nullable
	private List<QOrder> m_criteriaSortOrder;

	private final Map<K, T> m_byPkMap = new HashMap<>();

	public SimpleSearchModelLazy(@NonNull NodeBase contextSourceNode, @NonNull QCriteria<T> qc) {
		m_query = qc;
		m_contextSourceNode = contextSourceNode;
		m_sessionSource = null;
		m_queryHandler = null;
	}

	public SimpleSearchModelLazy(@NonNull QDataContextFactory ss, @NonNull QCriteria<T> qc) {
		m_query = qc;
		m_sessionSource = ss;
		m_queryHandler = null;
		m_contextSourceNode = null;
	}

	public SimpleSearchModelLazy(@NonNull IQueryHandler<T> ss, @NonNull QCriteria<T> qc) {
		m_query = qc;
		m_queryHandler = ss;
		m_contextSourceNode = null;
		m_sessionSource = null;
	}

	public SimpleSearchModelLazy(@NonNull QDataContextFactory f, @NonNull IQuery<T> q) {
		m_sessionSource = f;
		m_contextSourceNode = null;
		m_query = null;
		m_queryHandler = null;
	}

	public SimpleSearchModelLazy(@NonNull NodeBase contextSource, @NonNull IQuery<T> q) {
		m_contextSourceNode = contextSource;
		m_sessionSource = null;
		m_query = null;
		m_queryHandler = null;
	}

	public QCriteria<T> getQuery() {
		return m_query;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple getters and setters.							*/
	/*--------------------------------------------------------------*/
	public void setRefreshAfterShelve(boolean refreshAfterShelve) {
		m_refreshAfterShelve = refreshAfterShelve;
	}

	public boolean isRefreshAfterShelve() {
		return m_refreshAfterShelve;
	}

	/**
	 * Return the current result row limit. When &lt;= 0 the result will have a  default
	 * limit.
	 */
	public int getMaxRowCount() {
		return m_maxRowCount;
	}

	public boolean isCalculateActualRowCount() {
		return m_calculateActualRowCount;
	}

	public void setCalculateActualRowCount(boolean calculateActualRowCount) {
		m_calculateActualRowCount = calculateActualRowCount;
	}

	@Override
	@Nullable
	public Integer getActualRowCount() {
		return m_actualRowCount;
	}

	/**
	 * Set the current result row limit. When &lt;= 0 the result will have a  default
	 * limit.
	 */
	public void setMaxRowCount(int maxRowCount) {
		m_maxRowCount = maxRowCount;
	}

	/**
	 * Allocate and return a datacontext, if the query definition requires one.
	 */
	@NonNull
	private QDataContext getQueryContext() throws Exception {
		if(m_sessionSource != null) {
			return m_sessionSource.getDataContext(); // Create/get session
		} else if(m_contextSourceNode != null) {
			return m_contextSourceNode.getSharedContext();
		}
		throw new IllegalStateException("No sessionSource and no contextSourceNode present - I do not know how to allocate a QDataContext");
	}

	final private List<K> execQuery() throws Exception {
		long ts = System.nanoTime();
		QDataContext dc = null;

		int queryLimit = getMaxRowCount() > 0 ? getMaxRowCount() : ITableModel.DEFAULT_MAX_SIZE;
		QCriteria<T> query = m_query;
		if(query == null) {
			throw new IllegalStateException("No query and no query functor- no idea how to create the result..");
		}
		if(query.getLimit() > 0) {
			queryLimit = query.getLimit();
		}
		queryLimit++; 											// Increment by 1: if that amount is returned we know we have overflowed.
		int resultLimit = queryLimit;
		try {
			//-- Convert the query to a selection for just the IDs
			Class<T> baseClass = query.getBaseClass();
			if(null == baseClass)
				throw new IllegalStateException("I cannot query on metadata classes");

			QSelection<T> qsel = QSelection.create(baseClass);
			qsel.selectProperty("id");							// By definition
			String sort = m_sort;
			if (sort != null && !"id".equalsIgnoreCase(sort)) {
				qsel.selectProperty(sort);
			}
			QOperatorNode restrictions = query.getRestrictions();
			qsel.setRestrictions(restrictions == null ? null : restrictions.dup());
			qsel.limit(queryLimit);
			handleQuerySorting(qsel);

			List<Object[]> objList;
			if(m_queryHandler != null) {
				objList = m_queryHandler.query(qsel);
			} else {
				dc = getQueryContext();							// Allocate data context if needed.
				objList = dc.query(qsel);
			}

			if(objList.size() >= resultLimit) {
				objList.remove(objList.size() - 1);
				m_truncated = true;

				if(m_calculateActualRowCount) {
					//-- We must calculate a rowcount doing a count query 8-(
					m_actualRowCount = calculateActualRowCount(dc);
				}
			} else {
				m_truncated = false;
				m_actualRowCount = objList.size();				// We actually know the result count
			}
			m_byPkMap.clear();									// No records loaded
			List<K> idList = m_idResultList = new ArrayList<>(objList.size());
			for(Object[] objects : objList) {
				idList.add((K) objects[0]);
			}

			if(LOG.isDebugEnabled()) {
				ts = System.nanoTime() - ts;
				LOG.debug("db: persistence framework ID query took " + StringTool.strNanoTime(ts));
			}
			return idList;
		} finally {
			try {
				if(dc != null)
					dc.close();
			} catch(Exception x) {
			}
		}
	}


	@Nullable
	private Integer calculateActualRowCount(@Nullable QDataContext dc) throws Exception {
		QCriteria<T> query = Objects.requireNonNull(m_query);
		QSelection<T> sel = QSelection.create(query.getBaseClass());

		//-- Clone the restrictions.
		QOperatorNode restrictions = query.getRestrictions();
		if(null != restrictions) {
			sel.setRestrictions(restrictions.dup());
		}
		sel.count("id");
		IQueryHandler<T> queryHandler = m_queryHandler;
		Object[] objects;
		if(null != queryHandler) {
			List<Object[]> res = queryHandler.query(sel);
			if(res.size() != 1)
				return null;
			objects = res.get(0);
		} else if(null != dc) {
			objects = dc.queryOne(sel);
		} else
			throw new IllegalStateException("I have no idea how to query");
		if(null == objects)
			return null;
		Long l = (Long) objects[0];
		if(null == l)
			return null;
		return l.intValue();
	}

	private void handleQuerySorting(QSelection<T> qc) {
		//-- Handle the different sort forms. Are we sorting on property name?
		String sort = m_sort;
		if(sort != null) {
			qc.getOrder().clear(); // FIXME Need to duplicate.
			if(m_desc)
				qc.descending(sort);
			else
				qc.ascending(sort);
			return;
		}

		//-- We're not sorting.
	}

	/**
	 * Return a criteria for this search which can then be manipulated for sorting. Warning: the
	 * current implementation allows changing the root's order by ONLY(!). Once altered the
	 * new criteria must be set using {@link #setCriteria(QCriteria)}.
	 * <p>
	 * FIXME This urgently needs to duplicate the query instead of messing around with the original!
	 */
	@NonNull
	public QCriteria<T> getCriteria() {
		QCriteria<T> query = m_query;
		if(null == query)
			throw new IllegalStateException("This model is not using a QCriteria query.");
		m_criteriaSortOrder = new ArrayList<>(query.getOrder());        // Store the current query.
		query.getOrder().clear();
		return query;
	}

	public void setCriteria(@NonNull QCriteria<T> query) {
		//-- For now: if the new sort order in the query is the same as the previous one -> we do nothing to prevent duplicate queries
		List<QOrder> oldOrder = m_criteriaSortOrder;
		m_criteriaSortOrder = null;

		if(query == m_query && oldOrder != null && oldOrder.equals(query.getOrder())) {
			return;
		}

		//-- A new query and/or sort order has been set- send the event.
		m_query = query;
		if(query.getOrder().size() > 0) {
			m_sort = null;
			setSortComparator(null);
		}
		clear();
		try {
			fireModelChanged();
		} catch(Exception x) {
			throw WrappedException.wrap(x);                        // 8-(
		}
	}

	private void setSortComparator(@Nullable String sortComparatorKey) {
		m_sortComparatorKey = sortComparatorKey;
	}

	@Override
	public boolean isTruncated() {
		return m_truncated;
	}

	protected List<K> initResult() throws Exception {
		List<K> idResultList = m_idResultList;
		if(idResultList == null) {
			idResultList = execQuery();
		}
		return idResultList;
	}

	@Override public int getRows() throws Exception {
		return initResult().size();
	}

	@Override
	@NonNull
	public List<T> getItems(int start, int end) throws Exception {
		List<K> idList = initResult();
		if(start < 0)
			start = 0;
		if(end > getRows())
			end = getRows();
		if(end <= start)
			return Collections.EMPTY_LIST;

		boolean[] workRefreshed = null;
		if(isRefreshAfterShelve()) {
			//-- Make sure a refreshed map is present,
			workRefreshed = m_workRefreshed;
			if(workRefreshed == null) {
				workRefreshed = m_workRefreshed = new boolean[idList.size()];
			}
		}

		//-- Get the keys of all missing thingies
		List<K> loadList = new ArrayList<>(end - start);
		for(int index = start; index < end; index++) {
			K key = idList.get(index);
			T item = m_byPkMap.get(key);
			if(null == item) {
				loadList.add(key);
			}
		}

		//-- Load all thingies and move them to the map.
		if(loadList.size() > 0)
			loadInstances(loadList);

		List<T> result = new ArrayList<>(end - start);
		for(int index = start; index < end; index++) {
			K key = idList.get(index);
			T item = m_byPkMap.get(key);			// Must now be in the map
			if(null != item) {
				result.add(item);
				if(workRefreshed != null && ! workRefreshed[index]) {
					getQueryContext().refresh(item);
					workRefreshed[index] = true;
				}
			}
		}
		return result;
	}

	@Override protected T getItem(int ix) throws Exception {
		List<K> idList = initResult();
		if(ix < 0 || ix >= idList.size())
			throw new IllegalStateException("Index " + ix + " out of bounds: max = " + idList.size());
		K key = idList.get(ix);
		T item = m_byPkMap.get(key);
		if(null == item) {
			item = loadInstance(key);
			if(null == item)
				return null;
			m_byPkMap.put(key, item);

			if(isRefreshAfterShelve()) {
				//-- Make sure a refreshed map is present,
				boolean[] workRefreshed = m_workRefreshed;
				if(workRefreshed == null) {
					workRefreshed = m_workRefreshed = new boolean[idList.size()];
					if(! workRefreshed[ix]) {
						getQueryContext().refresh(item);
						workRefreshed[ix] = true;
					}
				}
			}
		}
		return item;
	}

	private void loadInstances(List<K> loadList) throws Exception {
		Class<T> baseClass = Objects.requireNonNull(Objects.requireNonNull(m_query).getBaseClass());
		QCriteria<T> query = QCriteria.create(baseClass).in("id", loadList);

		IQueryHandler<T> queryHandler = m_queryHandler;
		List<T> itemList = queryHandler == null ? getQueryContext().query(query) : queryHandler.query(query);
		for(T item : itemList) {
			m_byPkMap.put(item.getId(), item);
		}
	}

	@Nullable
	private T loadInstance(K pk) throws Exception {
		Class<T> baseClass = Objects.requireNonNull(Objects.requireNonNull(m_query).getBaseClass());
		IQueryHandler<T> queryHandler = m_queryHandler;
		if(null == queryHandler) {
			return getQueryContext().get(baseClass, pk);
		} else {
			List<T> res = queryHandler.query(QCriteria.create(baseClass).eq("id", pk));
			if(res.size() != 1)
				return null;
			return res.get(0);
		}
	}

	public void clear() {
		m_idResultList = null;
		m_byPkMap.clear();
		m_workRefreshed = null;
	}

	@Override public boolean delete(T val) throws Exception {
		K key = val.getId();
		List<K> idList = initResult();
		int index = idList.indexOf(key);
		if(index < 0)
			return false;
		delete(index);
		return true;
	}

	@Override public T delete(int index) throws Exception {
		T oldItem = getItem(index);
		Objects.requireNonNull(m_idResultList).remove(index);
		return oldItem;
	}

	@Override public void add(T row) throws Exception {
		add(getRows(), row);
	}

	@Override public void add(int index, T row) throws Exception {
		List<K> idList = initResult();
		if(index > idList.size())
			index = idList.size();
		idList.add(index, row.getId());
		m_byPkMap.put(row.getId(), row);
		fireAdded(index);
	}

	public void modified(int index) throws Exception {
		fireModified(index);
	}

	public void modified(@NonNull T val) throws Exception {
		List<K> idList = initResult();
		int ix = idList.indexOf(val);
		if(ix != -1)
			fireModified(ix);
	}

	public int indexOf(@NonNull T val) throws Exception {
		List<K> idList = initResult();
		return idList.indexOf(val.getId());
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	IKeyedTableModel implementation.							*/
	/*----------------------------------------------------------------------*/


	@Nullable
	@Override
	public T findRowObject(Object key) throws Exception {
		return m_byPkMap.get((K) key);
	}

	@Override
	public Object getRowKey(int row) throws Exception {
		if(row < 0 || row >= getRows()) {
			throw new IllegalStateException("The row number " + row + " must be >= 0 and < " + getRows());
		}
		K key = Objects.requireNonNull(m_idResultList).get(row);
		return key;
	}

	/**
	 * If the specified object is present in this model/should be present in this model
	 * then update it from the database, and send an update event to all listeners.
	 */
	@Override public void updateByKey(Object keyIn) throws Exception {
		K key = (K) keyIn;
		List<K> idList = Objects.requireNonNull(m_idResultList);

		QCriteria<T> query = m_query;
		if(null == query) {
			//-- Only works with QCriteria because we need a base type
			return;
		}
		Class<T> baseClass = query.getBaseClass();
		if(null == baseClass) {
			//-- If we have a generic base/programmed query we have no metadata
			return;
		}

		PropertyMetaModel<?> pk = MetaManager.findClassMeta(baseClass).getPrimaryKey();
		if(null == pk) {
			//-- Without a PK field we have no way to augment the criteria
			return;
		}
		QCriteria<T> newQuery = QCriteria.create(baseClass);
		QOperatorNode restrictions = query.getRestrictions();
		if(null != restrictions) {
			QOperatorNode copy = restrictions.dup();
			newQuery.setRestrictions(copy);
		}
		newQuery.eq(pk.getName(), key);					// Add a where id = value, so the query returns 0 or 1 result quickly (indexed by PK)

		//-- 1. Do a query with this primary key.
		T recordInstance;
		if(m_queryHandler != null) {
			List<T> result = m_queryHandler.query(newQuery);
			if(result.size() == 0) {
				recordInstance = null;
			} else if(result.size() == 1) {
				recordInstance = result.get(0);
			} else
				throw new IllegalStateException("The query-by-PK returned " + result.size() + " results!?\n- query is " + newQuery);
		} else {
			QDataContext dc = getQueryContext(); // Allocate data context if needed.
			recordInstance = dc.queryOne(newQuery);
		}

		//-- If recordInstance is null: remove it from this model if we're containing the thingy
		int itemIndex = idList.indexOf(key);
		if(null == recordInstance) {
			if(itemIndex < 0) {
				//-- It was not present, so we're happy and have nothing to do
				return;
			}
			m_byPkMap.remove(key);
			delete(itemIndex);
			return;
		}

		if(itemIndex < 0) {
			itemIndex = getRows();

			//-- We have a new one... Add it. FIXME: I cannot properly implement sort order easily
			if(itemIndex >= getMaxRowCount()) {						// Already too large -> do not add
				return;
			}
			idList.add(itemIndex, key);
			m_byPkMap.put(key, recordInstance);
			add(itemIndex, recordInstance);							// Add it as the last item
			return;
		}

		//-- The changed one is in the model currently -> refresh, then update
		T item = m_byPkMap.get(key);
		if(null == item) {
			item = loadInstance(key);
			m_byPkMap.put(key, item);
		}

		if(m_queryHandler == null && item != null) {
			getQueryContext().refresh(item);
		}
		modified(itemIndex);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	SortableTableModel implementation.					*/
	/*--------------------------------------------------------------*/
	/**
	 * When called this does a re-query using the specified sort property.
	 */
	@Override
	public void sortOn(String key, boolean descending) throws Exception {
		if(DomUtil.isEqual(key, m_sort) && descending == m_desc) // Nothing changed, get lost.
			return;
		clear();
		m_desc = descending;
		m_sort = key;
		setSortComparator(null);
		QCriteria<T> query = m_query;
		if(null != query) {
			query.getOrder().clear();
		}
		fireModelSorted();
	}

	@Override
	@Nullable
	public String getSortKey() {
		return m_sort;
	}

	@Override
	public boolean isSortDescending() {
		return m_desc;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IShelveListener implementation.						*/
	/*--------------------------------------------------------------*/

	/**
	 * When the component is shelved we discard all results. This causes a requery when
	 * unshelved (when accessed).
	 */
	@Override
	public void onShelve() throws Exception {
		LOG.debug("Shelving the model");
		clear();
	}

	@Override
	public void onUnshelve() throws Exception {
	}


	@Override
	public void refresh() {
		clear();
	}
}
