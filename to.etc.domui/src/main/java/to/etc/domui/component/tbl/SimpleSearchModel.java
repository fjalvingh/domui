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

import java.util.*;

import javax.annotation.*;

import org.slf4j.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

/**
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2008
 */
public class SimpleSearchModel<T> extends TableListModelBase<T> implements IKeyedTableModel<T>, ITruncateableDataModel, ISortableTableModel, IShelvedListener {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleSearchModel.class);

	/**
	 * Functor interface to create some abstract query result.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on May 23, 2010
	 */
	public interface IQuery<T> {
		List<T> query(QDataContext dc, String sortOn, int maxrows) throws Exception;
	}

	/** Thingy to get a database session from, if needed, */
	@Nullable
	final private QDataContextFactory m_sessionSource;

	@Nullable
	final private NodeBase m_contextSourceNode;

	@Nullable
	final private IQueryHandler<T> m_queryHandler;

	@Nullable
	final private IQuery<T> m_queryFunctor;

	/** Generalized search query. */
	@Nullable
	private QCriteria<T> m_query;

	@Nullable
	private List<T> m_workResult;

	private boolean[] m_workRefreshed;

	private boolean m_truncated;

	/** If we sort on property name this is the property name to sort on. */
	@Nullable
	private String m_sort;

	/**
	 * If we sort using sortComparator this is used to identify used comparator - to prevent multiple queries when order is already set to same.
	 */
	@Nullable
	private String m_sortComparatorKey;

	/**
	 * Sort using provided sort comparator. This is an alternative to sorting on sort column or criteria.
	 * This causes more data to fetch than usual querying -> because in-memory sorting. See ITableModel#IN_MEMORY_FILTER_OR_SORT_MAX_SIZE
	 */
	@Nullable
	private Comparator<T> m_sortComparator;

	/** If sorting, this is T if the sort should be descending. */
	private boolean m_desc;

	private boolean m_refreshAfterShelve;

	/** The max. #of rows to return before truncating. */
	private int m_maxRowCount;

	public final static class SortHelper<T> implements ISortHelper<T> {
		private final String m_columnName;

		public SortHelper(String columnName) {
			m_columnName = columnName;
		}

		@Override
		public <M extends ITableModel<T>> void adjustSort(@Nonnull M model, boolean descending) throws Exception {
			SimpleSearchModel<T> ssm = (SimpleSearchModel<T>) model;
			QCriteria<T> sq = ssm.getCriteria();
			if(descending)
				sq.descending(m_columnName);
			else
				sq.ascending(m_columnName);
			ssm.setCriteria(sq);
		}
	}

	/**
	 * Implementation of ISortHelper that can be used when sort is specified by sort column comparator.
	 * @param <T>
	 */
	@DefaultNonNull
	public final static class ByComparatorSortHelper<T> implements ISortHelper<T>{

		private final String m_columnKey;

		private final Comparator<T> m_comparator;

		/**
		 * Specify column key and comparator.
		 * @param columnKey
		 * @param comparator
		 */
		public ByComparatorSortHelper(String columnKey, Comparator<T> comparator){
			m_columnKey = columnKey;
			m_comparator = comparator;
		}

		@Override
		public <M extends ITableModel<T>> void adjustSort(M model, boolean descending) throws Exception {
			SimpleSearchModel<T> smm = (SimpleSearchModel<T>) model;
			smm.sortOn(m_comparator, m_columnKey, descending);
		}
	}

	/**
	 * EXPERIMENTAL INTERFACE
	 * @param contextSourceNode
	 * @param qc
	 */
	public SimpleSearchModel(@Nonnull NodeBase contextSourceNode, @Nonnull QCriteria<T> qc) {
		m_query = qc;
		m_contextSourceNode = contextSourceNode;
		m_queryFunctor = null;
		m_sessionSource = null;
		m_queryHandler = null;
	}

	/**
	 * Use {@link SimpleSearchModel#SimpleSearchModel(IQueryHandler, QCriteria) instead!
	 * @param ss
	 * @param qc
	 */
	public SimpleSearchModel(@Nonnull QDataContextFactory ss, @Nonnull QCriteria<T> qc) {
		m_query = qc;
		m_sessionSource = ss;
		m_queryFunctor = null;
		m_queryHandler = null;
		m_contextSourceNode = null;
	}

	public SimpleSearchModel(@Nonnull IQueryHandler<T> ss, @Nonnull QCriteria<T> qc) {
		m_query = qc;
		m_queryHandler = ss;
		m_queryFunctor = null;
		m_contextSourceNode = null;
		m_sessionSource = null;
	}

	public SimpleSearchModel(@Nonnull QDataContextFactory f, @Nonnull IQuery<T> q) {
		m_sessionSource = f;
		m_queryFunctor = q;
		m_contextSourceNode = null;
		m_query = null;
		m_queryHandler = null;
	}

	public SimpleSearchModel(@Nonnull NodeBase contextSource, @Nonnull IQuery<T> q) {
		m_contextSourceNode = contextSource;
		m_queryFunctor = q;
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
	/**
	 *
	 * @param refreshAfterShelve
	 */
	public void setRefreshAfterShelve(boolean refreshAfterShelve) {
		m_refreshAfterShelve = refreshAfterShelve;
	}

	public boolean isRefreshAfterShelve() {
		return m_refreshAfterShelve;
	}

	/**
	 * Return the current result row limit. When &lt;= 0 the result will have a  default
	 * limit.
	 * @return
	 */
	public int getMaxRowCount() {
		return m_maxRowCount;
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
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	private QDataContext getQueryContext() throws Exception {
		if(m_sessionSource != null) {
			return m_sessionSource.getDataContext(); // Create/get session
		} else if(m_contextSourceNode != null) {
			return m_contextSourceNode.getSharedContext();
		}
		throw new IllegalStateException("No sessionSource and no contextSourceNode present - I do not know how to allocate a QDataContext");
	}


	final private void execQuery() throws Exception {
		long ts = System.nanoTime();
		QDataContext dc = null;

		int queryLimit = getMaxRowCount() > 0 ? getMaxRowCount() : ITableModel.DEFAULT_MAX_SIZE;
		QCriteria<T> query = m_query;
		if (null != query && query.getLimit() > 0) {
			queryLimit = query.getLimit();
		}
		queryLimit++; // Increment by 1: if that amount is returned we know we have overflowed.
		int resultLimit = queryLimit;

		Comparator<T> sortComparator = m_sortComparator;
		if(null != sortComparator) {
			//custom sort requires more data fetch for in-memory sort
			if (queryLimit <= ITableModel.IN_MEMORY_FILTER_OR_SORT_MAX_SIZE) {
				queryLimit = ITableModel.IN_MEMORY_FILTER_OR_SORT_MAX_SIZE + 1;
			}
		}
		try {
			IQuery<T> queryFunctor = m_queryFunctor;
			if(queryFunctor != null) {
				dc = getQueryContext(); // Allocate data context
				m_workResult = queryFunctor.query(dc, m_sort, queryLimit);
			} else if(m_query != null) {
				QCriteria<T> qc = m_query; // Get the base query,
				int oldLimit = qc.getLimit();
				qc.limit(queryLimit);
				handleQuerySorting(qc);

				if(m_queryHandler != null) {
					m_workResult = m_queryHandler.query(qc);
				} else {
					dc = getQueryContext(); // Allocate data context if needed.
					m_workResult = dc.query(qc);
				}
				qc.limit(oldLimit);
			} else
				throw new IllegalStateException("No query and no query functor- no idea how to create the result..");
		} finally {
			try {
				if(dc != null)
					dc.close();
			} catch(Exception x) {}
		}
		if (null != sortComparator){
			if (getWorkResult().size() == queryLimit){
				//more results than expected, we can't do in-memory sorting, for now we report into sys err, see later about reporting in UI as well
				System.err.println("Unable to do proper in-memory sorting since query fetch more than " + (queryLimit - 1) + " rows!");
			}
			if (m_desc) {
				getWorkResult().sort(Collections.reverseOrder(sortComparator));
			}else{
				getWorkResult().sort(sortComparator);
			}
			if (getWorkResult().size() > resultLimit) {
				m_workResult = getWorkResult().subList(0, resultLimit + 1);
			}
		}
		if(getWorkResult().size() >= resultLimit) {
			getWorkResult().remove(getWorkResult().size() - 1);
			m_truncated = true;
		} else
			m_truncated = false;

		if(LOG.isDebugEnabled()) {
			ts = System.nanoTime() - ts;
			LOG.debug("db: persistence framework query and materialize took " + StringTool.strNanoTime(ts));
		}
	}

	protected void handleQuerySorting(QCriteria<T> qc) {
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

	@Nullable
	private List<QOrder> m_criteriaSortOrder;

	/**
	 * Return a criteria for this search which can then be manipulated for sorting. Warning: the
	 * current implementation allows changing the root's order by ONLY(!). Once altered the
	 * new criteria must be set using {@link #setCriteria(QCriteria)}.
	 *
	 * FIXME This urgently needs to duplicate the query instead of messing around with the original!
	 * @return
	 */
	@Nonnull
	public QCriteria<T> getCriteria() {
		QCriteria<T> query = m_query;
		if(null == query)
			throw new IllegalStateException("This model is not using a QCriteria query.");
		m_criteriaSortOrder = new ArrayList<>(query.getOrder());		// Store the current query.
		query.getOrder().clear();
		return query;
	}

	public void setCriteria(@Nonnull QCriteria<T> query) {
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
			setSortComparator(null, null);
		}
		clear();
		try {
			fireModelChanged();
		} catch(Exception x) {
			throw WrappedException.wrap(x);						// 8-(
		}
	}

	private void setSortComparator(@Nullable Comparator<T> sortComparator, @Nullable String sortComparatorKey) {
		m_sortComparator = sortComparator;
		m_sortComparatorKey = sortComparatorKey;
	}

	@Override
	public boolean isTruncated() {
		return m_truncated;
	}

	protected void initResult() throws Exception {
		if(m_workResult == null)
			execQuery();
	}

	@Nonnull
	@Override
	protected List<T> getList() throws Exception {
		initResult();
		return getWorkResult();
	}

	@Nonnull
	private List<T> getWorkResult() {
		if(null != m_workResult)
			return m_workResult;
		throw new IllegalStateException("workResult not initialized");
	}

	@Override
	@SuppressWarnings("deprecation")
	@Nonnull
	public List<T> getItems(int start, int end) throws Exception {
		initResult();
		if(start < 0)
			start = 0;
		if(end > getRows())
			end = getRows();
		if(end <= start)
			return Collections.EMPTY_LIST;
		if(isRefreshAfterShelve()) {
			//-- Make sure a refreshed map is present,
			if(m_workRefreshed == null)
				m_workRefreshed = new boolean[getWorkResult().size()];
			QDataContext qs = null;
			for(int i = start; i < end; i++) {
				if(!m_workRefreshed[i]) {
					if(qs == null)
						qs = getQueryContext();
					qs.refresh(getWorkResult().get(i));
					m_workRefreshed[i] = true;
				}
			}
		}

		return getWorkResult().subList(start, end);
	}

	@Override
	public T findRowObject(String key) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public String getRowKey(int row) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	public void clear() {
		m_workResult = null;
		m_workRefreshed = null;
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
		setSortComparator(null, null);
		QCriteria<T> query = m_query;
		if (null != query){
			query.getOrder().clear();
		}
		fireModelChanged();
	}

	/**
	 * When called, this does a re-query using the specified sortComparator.
	 */
	public void sortOn(Comparator<T> sortComparator, String sortComparatorKey, boolean descending) throws Exception {
		if(DomUtil.isEqual(sortComparatorKey, m_sortComparatorKey) && descending == m_desc) // Nothing changed, get lost.
			return;
		clear();
		m_desc = descending;
		m_sort = null;
		QCriteria<T> query = m_query;
		if (null != query){
			query.getOrder().clear();
		}
		setSortComparator(sortComparator, sortComparatorKey);
		fireModelChanged();
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
	 *
	 * @see to.etc.domui.util.IShelvedListener#onShelve()
	 */
	@Override
	public void onShelve() throws Exception {
		LOG.debug("Shelving the model");
		clear();
	}

	@Override
	public void onUnshelve() throws Exception {}


	@Override
	public void refresh() {
		clear();
	}
}
