/*
 * DomUI Java User Interface - shared code
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
package to.etc.dbpool.info;

import java.util.*;

import to.etc.dbpool.*;

/**
 * This collects per-statement execution time and collects statement counts.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 3, 2010
 */
public class InfoCollectorExpenseBased extends InfoCollectorBase implements IStatisticsListener {
	private final List<IPerformanceCollector> m_collectorList = new ArrayList<IPerformanceCollector>();

	/** The stmt count for the last execute. */
	private StmtCount m_lastCount;

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

		public long getTotalExecuteDuration() {
			return m_totalExecuteNS;
		}

		void addTotalExecuteDuration(long dt) {
			m_totalExecuteNS += dt;
		}

		/**
		 * Fetch duration summed for all executes, in nano's.
		 * @return
		 */
		public long getTotalFetchDuration() {
			return m_totalFetchDuration;
		}

		void addTotalFetchDuration(long d) {
			m_totalFetchDuration += d;
		}
	}

	public InfoCollectorExpenseBased(final String ident, String queryString) {
		super(ident);
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
		for(IPerformanceCollector pc : m_collectorList)
			pc.saveCounters(m_fullRequestURL, getCounters());
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
	//	private void postExecuteDuration(StatementProxy sp, long totalexectime, StmtCount counter) {
	//		//
	//		//		//-- Push this into the performance collectors.
	//		//		for(IPerformanceCollector pc : m_collectorList)
	//		//			pc.postExecuteDuration(m_fullRequestURL, sp, counter.getTotalExecuteNS(), counter);
	//	}

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

	void executeError(final StatementProxy sp, final Exception x) {
		m_nErrors++;
	}

	@Override
	public void statementPrepared(StatementProxy sp, long prepareDuration) {
		m_nPrepares++;
		m_prepareDuration += prepareDuration;
	}

	@Override
	public void queryStatementExecuted(StatementProxy sp, long executeDuration, long fetchDuration, int rowCount, boolean prepared) {
		if(prepared) {
			m_nPreparedQueries++;
			m_preparedQueryDuration += executeDuration;
		} else {
			m_nStatementQueries++;
			m_statementQueryDuration += executeDuration;
		}
		m_totalFetchDuration += fetchDuration;
		m_nRows += rowCount;

		StmtCount c = findCounter(sp.getSQL());
		c.incExecutions();
		c.incRows(rowCount);
		c.addTotalFetchDuration(fetchDuration);
		c.addTotalExecuteDuration(executeDuration);
	}

	@Override
	public void executePreparedUpdateExecuted(StatementProxy sp, long updateDuration, int rowcount) {
		m_nPreparedUpdates++;
		m_nUpdatedRows += rowcount;
		m_preparedUpdateDuration += updateDuration;
		StmtCount c = findCounter(sp.getSQL());
		c.incExecutions();
		c.incRows(rowcount);
		c.addTotalExecuteDuration(updateDuration);
	}

	@Override
	public void executeUpdateExecuted(StatementProxy sp, long updateDuration, int updatedrowcount) {
		m_nStatementUpdates++;
		m_nUpdatedRows += updatedrowcount;
		m_statementUpdateDuration += updateDuration;
		StmtCount c = findCounter(sp.getSQL());
		c.incExecutions();
		c.incRows(updatedrowcount);
		c.addTotalExecuteDuration(updateDuration);
		//		postExecuteDuration(sp, updateDuration, c);
	}

	@Override
	public void executeExecuted(StatementProxy sp, long updateDuration, Boolean result) {
		m_nExecutes++;
		m_executeDuration += updateDuration;
		StmtCount c = findCounter(sp.getSQL());
		c.incExecutions();
		c.addTotalExecuteDuration(updateDuration);
		//		postExecuteDuration(sp, updateDuration, c);
	}

	/**
	 * FIXME Needs explicit handling.
	 * @see to.etc.dbpool.info.IStatisticsListener#executeBatchExecuted(to.etc.dbpool.StatementProxy, long, int[])
	 */
	@Override
	public void executeBatchExecuted(StatementProxy sp, long executeDuration, int[] rc) {
		m_nExecutes += rc == null ? 1 : rc.length;
		m_executeDuration += executeDuration;
	}

	//------ remove after here --------
	//	public void executeBatchStart(final StatementProxy sp) {
	//		if(m_batchSet != null && m_batchSet.size() > 0) {
	//			for(StmtCount c : m_batchSet)
	//				c.incExecutions();
	//			m_batchSet.clear();
	//		}
	//	}

	//	public void addBatch(final String sql) {
	//		StmtCount c = findCounter(sql);
	//		if(m_batchSet == null)
	//			m_batchSet = new ArrayList<StmtCount>();
	//		m_batchSet.add(c);
	//	}

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
