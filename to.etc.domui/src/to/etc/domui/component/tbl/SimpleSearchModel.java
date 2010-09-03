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

	@Nullable
	private String m_sort;

	private boolean m_desc;

	private boolean m_refreshAfterShelve;

	/** The max. #of rows to return before truncating. */
	private int m_maxRowCount;

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
	@Nullable
	private QDataContext getQueryContext() throws Exception {
		if(m_sessionSource != null) {
			return m_sessionSource.getDataContext(); // Create/get session
		} else if(m_contextSourceNode != null) {
			return QContextManager.getContext(m_contextSourceNode.getPage());
		} else
			return null;
	}

	final private void execQuery() throws Exception {
		long ts = System.nanoTime();
		QDataContext dc = null;

		int limit = getMaxRowCount() > 0 ? getMaxRowCount() : ITableModel.DEFAULT_MAX_SIZE;
		limit++; // Increment by 1: if that amount is returned we know we have overflowed.

		try {
			dc = getQueryContext(); // Allocate data context if needed.

			if(m_queryFunctor != null) {
				m_workResult = m_queryFunctor.query(dc, m_sort, limit);
			} else if(m_query != null) {
				QCriteria<T> qc = m_query; // Get the base query,
				if(qc.getLimit() <= 0)
					qc.limit(limit);
				if(m_sort != null) { // Are we sorting?
					qc.getOrder().clear(); // FIXME Need to duplicate.
					if(m_desc)
						qc.descending(m_sort);
					else
						qc.ascending(m_sort);
				}

				if(m_queryHandler != null) {
					m_workResult = m_queryHandler.query(qc);
				} else {
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

		if(m_workResult.size() >= limit) {
			m_workResult.remove(m_workResult.size() - 1);
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
		return m_workResult;
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
				m_workRefreshed = new boolean[m_workResult.size()];
			QDataContext qs = null;
			for(int i = start; i < end; i++) {
				if(!m_workRefreshed[i]) {
					if(qs == null)
						qs = m_sessionSource.getDataContext(); // Create/get session
					qs.refresh(m_workResult.get(i));
					m_workRefreshed[i] = true;
				}
			}
		}

		return m_workResult.subList(start, end);
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
