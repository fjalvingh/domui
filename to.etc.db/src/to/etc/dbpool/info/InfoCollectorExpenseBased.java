package to.etc.dbpool.info;

import java.util.*;

import to.etc.dbpool.*;

/**
 * This collects per-statement execution time and collects statement counts.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 3, 2010
 */
public class InfoCollectorExpenseBased extends InfoCollectorBase implements InfoCollector {
	private final List<IPerformanceCollector> m_collectorList = new ArrayList<IPerformanceCollector>();

	private StmtType m_type;

	//	/** Duration of the entire request in nano's */
	//	private long m_requestDuration;

	/** Timestamp of the last START of a call. */
	private long m_lastTS;

	/** The stmt count for the last execute. */
	private StmtCount m_lastCount;

	private boolean m_collectSQL;

	private Map<String, StmtCount> m_sqlMap = new HashMap<String, StmtCount>();

	private List<StmtCount> m_batchSet;

	final private String m_fullRequestURL;

	static final public class StmtCount {
		final private String m_sql;

		private int m_executions;

		private long m_rows;

		private long m_totalFetchDuration;

		/** Total time spent executing all these queries. */
		private long m_totalExecuteNS;

		//    	public Set<String>	tmpset = new HashSet<String>();
		StmtCount(String sql) {
			m_sql = sql;
		}

		public String getSQL() {
			return m_sql;
		}

		void incExecutions() {
			m_executions++;
		}

		public int getExecutions() {
			return m_executions;
		}

		public long getRows() {
			return m_rows;
		}

		void incRows(int rowcount) {
			m_rows += rowcount;
		}

		public long getTotalExecuteNS() {
			return m_totalExecuteNS;
		}

		void incTotalExecuteNS(long dt) {
			m_totalExecuteNS += dt;
		}

		/**
		 * Fetch duration summed for all executes, in nano's.
		 * @return
		 */
		public long getTotalFetchDuration() {
			return m_totalFetchDuration;
		}

		void addFetchDuration(long d) {
			m_totalFetchDuration += d;
		}
	}

	public InfoCollectorExpenseBased(final String ident, String queryString, boolean collectSQL) {
		super(ident);
		m_collectSQL = collectSQL;
		if(queryString == null || queryString.length() == 0)
			m_fullRequestURL = ident;
		else
			m_fullRequestURL = ident + "?" + queryString;
	}

	public void addPerformanceCollector(IPerformanceCollector pc) {
		m_collectorList.add(pc);
	}

	/**
	 * Called when the collector is removed from the set. It should complete all data so that retrieval can be done.
	 * @see to.etc.dbpool.info.InfoCollector#finish()
	 */
	public void finish() {
		if(m_collectSQL) {
			for(IPerformanceCollector pc: m_collectorList)
				pc.saveCounters(m_fullRequestURL, getCounters());
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Per-statement counters maintenance....				*/
	/*--------------------------------------------------------------*/
	/**
	 * Add/locate the counter structure for the specified SQL statement.
	 * @param sql
	 * @return
	 */
	private StmtCount findCounter(final String sql) {
		StmtCount c = m_sqlMap.get(sql);
		if(c == null) {
			c = new StmtCount(sql);
			m_sqlMap.put(sql, c);
		}
		return c;
	}

	/**
	 * Add the stmt to the map and increment it's executioncount.
	 * @param p
	 */
	private StmtCount addExecution(final StatementProxy p) {
		if(!m_collectSQL)
			return null;
		StmtCount c = findCounter(p.getSQL());
		c.incExecutions();
		m_lastCount = c;
		return c;
	}

	public Map<String, StmtCount> getStatementMap() {
		return m_sqlMap;
	}

	public List<StmtCount>	getCounters() {
		return new ArrayList<StmtCount>(m_sqlMap.values());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Statement expense accounting...						*/
	/*--------------------------------------------------------------*/

	/**
	 * Called when a statement execute has run. This is only the part where
	 * a result set is returned; it does not include fetch of the result set.
	 */
	private void postExecuteDuration(StatementProxy sp, long dt, StmtType type, StmtCount counter) {
		if(!m_collectSQL)
			return;
		if(m_lastCount != null)
			m_lastCount.incTotalExecuteNS(dt);

		//-- Push this into the performance collectors.
		for(IPerformanceCollector pc : m_collectorList)
			pc.postExecuteDuration(m_fullRequestURL, sp, dt, type, counter);
	}

	public <T extends IPerformanceCollector> T findCollector(Class<T> clz) {
		for(IPerformanceCollector pc : m_collectorList) {
			if(pc.getClass() == clz)
				return (T) pc;
		}
		return null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	InfoCollector interface implementation.				*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.dbpool.info.InfoCollector#connectionAllocated()
	 */
	public void connectionAllocated() {
		m_nConnectionAllocations++;
	}

	public void executeError(final StatementProxy sp, final Exception x) {
		m_nErrors++;
	}

	public void resultSetClosed(ResultSetProxy rp) {
		m_nRows += rp.internalGetRowCount();
		if(!m_collectSQL)
			return;

		StmtCount c = findCounter(rp.getSQL());
		c.incRows(rp.internalGetRowCount());
		long fd = rp.internalGetFetchDuration();
		c.addFetchDuration(fd);
		m_totalFetchDuration += fd;
	}

	public void incrementUpdateCount(final int uc) {
		if(uc > 0)
			m_nUpdatedRows += uc;
	}

	public void prepareStatement(final String sql) {
		m_lastTS = System.nanoTime();
		m_type = StmtType.T_PREPARED;
		m_nPrepares++;
		if(m_collectSQL) {
			findCounter(sql);
		}
	}

	public void prepareStatementEnd(final String sql, final StatementProxy sp) {
		if(m_type != StmtType.T_PREPARED)
			throw new IllegalStateException("Bad type: " + m_type);
		long dt = System.nanoTime() - m_lastTS;
		m_prepareDuration += dt;
		m_type = null;
	}

	public void executePreparedQueryStart(final StatementProxy sp) {
		m_lastTS = System.nanoTime();
		m_type = StmtType.T_PREPQ;
		m_nPreparedQueries++;
		addExecution(sp);
	}

	public void executeQueryStart(final StatementProxy sp) {
		m_nStatementQueries++;
		m_lastTS = System.nanoTime();
		m_type = StmtType.T_QSTMT;
		addExecution(sp);
	}

	public void executeQueryEnd(final StatementProxy sp, final ResultSetProxy rs) {
		long dt = System.nanoTime() - m_lastTS;
		switch(m_type){
			default:
				throw new IllegalStateException("Bad type.");
			case T_PREPQ:
				m_preparedQueryDuration += dt;
				break;

			case T_QSTMT:
				m_statementQueryDuration += dt;
				break;
		}
		postExecuteDuration(sp, dt, m_type, findCounter(sp.getSQL()));
	}

	public void executePreparedUpdateStart(final StatementProxy sp) {
		m_nPreparedUpdates++;
		m_lastTS = System.nanoTime();
		m_type = StmtType.T_PREPUPD;
		addExecution(sp);
	}

	public void executeUpdateStart(final StatementProxy sp) {
		m_nStatementUpdates++;
		m_lastTS = System.nanoTime();
		m_type = StmtType.T_USTMT;
		addExecution(sp);
	}

	public void executeUpdateEnd(final StatementProxy sp, final int rowcount) {
		if(rowcount > 0) {
			m_nUpdatedRows += rowcount;
			if(null != m_lastCount)
				m_lastCount.incRows(rowcount);
		}
		long dt = System.nanoTime() - m_lastTS;
		switch(m_type){
			default:
				throw new IllegalStateException("Bad type.");
			case T_PREPUPD:
				m_preparedUpdateDuration += dt;
				break;
			case T_USTMT:
				m_statementUpdateDuration += dt;
				break;
		}
		postExecuteDuration(sp, dt, m_type, findCounter(sp.getSQL()));
	}

	public void executeStart(final StatementProxy sp) {
		m_nExecutes++;
		m_lastTS = System.nanoTime();
		m_type = StmtType.T_EXECUTES;
		addExecution(sp);
	}

	public void executeEnd(final StatementProxy sp, final Boolean result) {
		long dt = System.nanoTime() - m_lastTS;
		switch(m_type){
			default:
				throw new IllegalStateException("Bad type.");
			case T_EXECUTES:
				m_executeDuration += dt;
				break;
		}
		postExecuteDuration(sp, dt, m_type, findCounter(sp.getSQL()));
	}

	public void executeBatchEnd(final StatementProxy sp, final int[] rc) {}

	public void executeBatchStart(final StatementProxy sp) {
		if(m_batchSet != null && m_batchSet.size() > 0) {
			for(StmtCount c : m_batchSet)
				c.incExecutions();
			m_batchSet.clear();
		}
	}

	public void addBatch(final String sql) {
		if(!m_collectSQL)
			return;

		StmtCount c = findCounter(sql);
		if(m_batchSet == null)
			m_batchSet = new ArrayList<StmtCount>();
		m_batchSet.add(c);
	}

	/*--------------------------------------------------------------*/
	/* CODING: Statistics retrieval.                                */
	/*--------------------------------------------------------------*/
	public void reportSimple() {
		if(getNConnectionAllocations() == 0)
			return;
		long rd = System.nanoTime() - getStartTS();
		System.out.println("S: " + getIdent() + ":" + DbPoolUtil.strNanoTime(rd) + " #conn=" + getNConnectionAllocations() + " #q=" + getTotalQueries() + " #u=" + getTotalUpdates()
			+ " #qrow=" + getNRows() + " #urow=" + getNUpdatedRows() + " #errs=" + getNErrors());
	}
}
