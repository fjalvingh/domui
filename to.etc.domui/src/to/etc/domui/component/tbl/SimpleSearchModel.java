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
public class SimpleSearchModel<T> extends TableListModelBase<T> implements IKeyedTableModel<T>, ITruncateableDataModel, IProgrammableSortableModel, IShelvedListener {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleSearchModel.class);

	/**
	 * Functor interface to create some abstract query result.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on May 23, 2010
	 */
	public static interface IQuery<T> {
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
	final private QCriteria<T> m_query;

	@Nullable
	private List<T> m_workResult;

	private boolean[] m_workRefreshed;

	private boolean m_truncated;

	/** If we sort on property name this is the property name to sort on. */
	@Nullable
	private String m_sort;

	/** If we sort using a helper this contains that helper. */
	private ISortHelper m_sortHelper;

	/** If sorting, this is T if the sort should be descending. */
	private boolean m_desc;

	private boolean m_refreshAfterShelve;

	/** The max. #of rows to return before truncating. */
	private int m_maxRowCount;

	/** The criteria to use when the sort spec is to be adapted during execution of a sort helper. Null if not executing a sort helper. */
	private QCriteria< ? > m_sortCriteria;

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
			return QContextManager.getContext(m_contextSourceNode.getPage());
		}
		throw new IllegalStateException("No sessionSource and no contextSourceNode present - I do not know how to allocate a QDataContext");
	}


	final private void execQuery() throws Exception {
		long ts = System.nanoTime();
		QDataContext dc = null;

		int limit = getMaxRowCount() > 0 ? getMaxRowCount() : ITableModel.DEFAULT_MAX_SIZE;
		limit++; // Increment by 1: if that amount is returned we know we have overflowed.

		try {
			IQuery<T> queryFunctor = m_queryFunctor;
			if(queryFunctor != null) {
				if(m_sortHelper != null)
					throw new IllegalStateException("Implementation restriction: you cannot (currently) use an ISortHelper when using an IQuery functor to actually do the query.");
				dc = getQueryContext(); // Allocate data context
				m_workResult = queryFunctor.query(dc, m_sort, limit);
			} else if(m_query != null) {
				QCriteria<T> qc = m_query; // Get the base query,
				if(qc.getLimit() <= 0)
					qc.limit(limit);
				handleQuerySorting(qc);

				if(m_queryHandler != null) {
					m_workResult = m_queryHandler.query(qc);
				} else {
					dc = getQueryContext(); // Allocate data context if needed.
					m_workResult = dc.query(qc);
				}
			} else
				throw new IllegalStateException("No query and no query functor- no idea how to create the result..");
		} finally {
			try {
				if(dc != null)
					dc.close();
			} catch(Exception x) {}
		}

		if(getWorkResult().size() >= limit) {
			getWorkResult().remove(getWorkResult().size() - 1);
			m_truncated = true;
		} else
			m_truncated = false;

		if(LOG.isDebugEnabled()) {
			ts = System.nanoTime() - ts;
			LOG.debug("db: persistence framework query and materialize took " + StringTool.strNanoTime(ts));
		}
	}

	//	protected void execQueryOLD() throws Exception {
	//		long ts = System.nanoTime();
	//		QCriteria<T> qc = m_query; // Get the base query,
	//		if(qc.getLimit() <= 0)
	//			qc.limit(ITableModel.DEFAULT_MAX_SIZE + 1);
	//		if(m_sort != null) { // Are we sorting?
	//			qc.getOrder().clear(); // FIXME Need to duplicate.
	//			if(m_desc)
	//				qc.descending(m_sort);
	//			else
	//				qc.ascending(m_sort);
	//		}
	//		if(m_sessionSource != null) {
	//			QDataContext qs = m_sessionSource.getDataContext(); // Create/get session
	//			m_workResult = qs.query(qc); // Execute the query.
	//		} else if(m_queryHandler != null) {
	//			m_workResult = m_queryHandler.query(qc);
	//		} else if(m_contextSourceNode != null) {
	//			QDataContext dc = QContextManager.getContext(m_contextSourceNode.getPage());
	//			m_workResult = dc.query(qc); // Execute the query.
	//			dc.close();
	//		} else if(m_queryFunctor != null) {
	//
	//
	//		} else
	//			throw new IllegalStateException("No QueryHandler nor SessionSource set- don't know how to do the query");
	//
	//		if(m_workResult.size() > ITableModel.DEFAULT_MAX_SIZE) {
	//			m_workResult.remove(m_workResult.size() - 1);
	//			m_truncated = true;
	//		} else
	//			m_truncated = false;
	//		if(LOG.isDebugEnabled()) {
	//			ts = System.nanoTime() - ts;
	//			LOG.debug("db: persistence framework query and materialize took " + StringTool.strNanoTime(ts));
	//		}
	//	}

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

		//-- Do we have a sort helper method here?
		if(m_sortHelper != null) {
			qc.getOrder().clear(); // FIXME Need to duplicate.
			try {
				m_sortCriteria = qc; // Only allow sort criteria access when sorting.
				m_sortHelper.adjustSort(this, m_desc);
			} finally {
				m_sortCriteria = null;
			}
			return;
		}

		//-- We're not sorting.
	}

	@Nonnull
	public QCriteria< ? > getSortCriteria() {
		if(m_sortCriteria == null)
			throw new IllegalStateException("Sort criteria can be accessed during sort helper execution ONLY.");
		return m_sortCriteria;
	}

	public boolean isTruncated() {
		return m_truncated;
	}

	@Override
	public int getTruncatedCount() {
		return isTruncated() ? ITableModel.DEFAULT_MAX_SIZE : 0;
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

	/**
	 * @see to.etc.domui.component.tbl.ITableModel#getRowKey(int)
	 */
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
		//		initResult();			20080730 jal lazily init,
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
	/*	CODING:	IProgrammableSortableModel impl.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Set a sorter to sort the result.
	 * @see to.etc.domui.component.tbl.IProgrammableSortableModel#sortOn(to.etc.domui.component.tbl.ISortHelper, boolean)
	 */
	@Override
	public void sortOn(ISortHelper helper, boolean descending) throws Exception {
		if(m_sort == null && m_sortHelper == helper && m_desc == descending)
			return;

		clear();
		m_sort = null;
		m_sortHelper = helper;
		m_desc = descending;
		fireModelChanged();
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
