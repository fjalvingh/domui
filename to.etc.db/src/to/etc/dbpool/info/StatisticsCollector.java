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

import to.etc.dbpool.*;

import java.util.*;

/**
 * This collects per-statement execution time and collects statement counts per statement.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 3, 2010
 */
final public class StatisticsCollector extends StatisticsCollectorBase implements IStatisticsListener {
	static private long m_nextId = System.currentTimeMillis();

	private final boolean m_collectOraclePerformanceData;

	private Map<String, StatementStatistics> m_sqlMap = new HashMap<String, StatementStatistics>();

	final private String m_fullRequestURL;

	private boolean m_disabled;

	//@Nullable
	//final private String m_sessionID;

	public StatisticsCollector(final String ident, String queryString, boolean collectOraclePerformanceData) {
		super(ident);
		m_collectOraclePerformanceData = collectOraclePerformanceData;
		//String snid = collectOraclePerformanceData ? nextID() : null;
		//m_sessionID = snid;
		if(queryString == null || queryString.length() == 0)
			m_fullRequestURL = ident;
		else
			m_fullRequestURL = ident + "?" + queryString;
	}

	//private static synchronized String nextID() {
	//	return "sn" + Long.toString(m_nextId++, 36);
	//}

	/**
	 */
	@Override
	public void finish() {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Per-statement counters maintenance....				*/
	/*--------------------------------------------------------------*/
	/**
	 * Add/locate the counter structure for the specified SQL statement.
	 * @param sql
	 * @return
	 */
	private StatementStatistics findCounter(final String sql) {
		StatementStatistics c = m_sqlMap.get(sql);
		if(c == null) {
			c = new StatementStatistics(sql);
			m_sqlMap.put(sql, c);
		}
		return c;
	}

	public Map<String, StatementStatistics> getStatementMap() {
		return m_sqlMap;
	}

	public List<StatementStatistics>	getCounters() {
		return new ArrayList<>(m_sqlMap.values());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IStatisticsListener interface implementation.		*/
	/*--------------------------------------------------------------*/
	@Override
	public void connectionAllocated(ConnectionProxy proxy) {
		m_nConnectionAllocations++;
		m_currentOpenConnections++;
		if(m_currentOpenConnections >= m_maxConcurrentConnections)
			m_maxConcurrentConnections = m_currentOpenConnections;

		if(m_collectOraclePerformanceData) {
			IConnectionStatisticsFactory collector = proxy.getPool().getConnectionStatisticsFactory();
			if(null != collector) {
				//m_disabled = true;									// Disable temp to get insight in what these calls themselves cost.
				try {
					collector.startConnectionStatistics(proxy);
				} catch(Exception x) {
					System.err.println("dbpool: db statistics init failed " + x);
				} finally {
					m_disabled = false;
				}
			}
		}
	}

	@Override
	public void connectionClosed(ConnectionProxy proxy) {
		if(m_currentOpenConnections > 0)
			m_currentOpenConnections--;

		if(! m_collectOraclePerformanceData)
			return;

		IConnectionStatisticsFactory collector = proxy.getPool().getConnectionStatisticsFactory();
		if(null == collector)
			return;
		//m_disabled = true;
		try {
			List<DbMetric> list = collector.finishConnectionStatistics(proxy);
			mergeMetrics(list);
		} catch(Exception x) {
			System.err.println("dbpool: oracle db statistics gather failed " + x);
		} finally {
			m_disabled = false;
		}
	}

	@Override
	public void statementPrepared(StatementProxy sp, long prepareDuration) {
		if(m_disabled)
			return;
		m_nPrepares++;
		m_prepareDuration += prepareDuration;
	}

	@Override
	public void queryStatementExecuted(StatementProxy sp, long executeDuration, long fetchDuration, int rowCount, boolean prepared) {
		if(m_disabled)
			return;
		if(prepared) {
			m_nPreparedQueries++;
			m_preparedQueryDuration += executeDuration;
		} else {
			m_nStatementQueries++;
			m_statementQueryDuration += executeDuration;
		}
		m_totalFetchDuration += fetchDuration;
		m_nRows += rowCount;

		StatementStatistics c = findCounter(sp.getSQL());
		c.incExecutions();
		c.incRows(rowCount);
		c.addTotalFetchDuration(fetchDuration);
		c.addTotalExecuteDuration(executeDuration);
	}

	@Override
	public void executePreparedUpdateExecuted(StatementProxy sp, long updateDuration, int rowcount) {
		if(m_disabled)
			return;
		m_nPreparedUpdates++;
		m_nUpdatedRows += rowcount;
		m_preparedUpdateDuration += updateDuration;
		StatementStatistics c = findCounter(sp.getSQL());
		c.incExecutions();
		c.incRows(rowcount);
		c.addTotalExecuteDuration(updateDuration);
	}

	@Override
	public void executeUpdateExecuted(StatementProxy sp, long updateDuration, int updatedrowcount) {
		if(m_disabled)
			return;
		m_nStatementUpdates++;
		m_nUpdatedRows += updatedrowcount;
		m_statementUpdateDuration += updateDuration;
		StatementStatistics c = findCounter(sp.getSQL());
		c.incExecutions();
		c.incRows(updatedrowcount);
		c.addTotalExecuteDuration(updateDuration);
	}

	@Override
	public void executeExecuted(StatementProxy sp, long updateDuration, Boolean result) {
		if(m_disabled)
			return;
		m_nExecutes++;
		m_executeDuration += updateDuration;
		StatementStatistics c = findCounter(sp.getSQL());
		c.incExecutions();
		c.addTotalExecuteDuration(updateDuration);
	}


	@Override public void executeBatchExecuted(long executeDuration, int totalStatements, int totalRows, List<BatchEntry> list) {
		if(m_disabled)
			return;
		m_nExecutes += totalStatements;
		m_nUpdatedRows += totalRows;

		/*
		 * We have no duration per-statement. The only way we can handle individual statements is by
		 * spreading the execution time over the statements. We assign by using rowcount.
		 */
		double totalToGive = executeDuration;
		double rowsLeft = totalRows;

		//-- Try to assign per-statement, if applicable
		for(BatchEntry be: list) {
			StatementStatistics c = findCounter(be.getStatement());
			c.addExecutions(be.getExecCount());
			c.incRows(be.getRowCount());

			//-- Calculate a duration.
			if(rowsLeft <= 0)
				rowsLeft = 1.0;
			double duration = (totalToGive / rowsLeft) * be.getRowCount();
			duration = Math.round(duration);

			rowsLeft -= be.getRowCount();
			totalToGive -= duration;

			c.addTotalExecuteDuration((long) duration);
		}
	}

	/*--------------------------------------------------------------*/
	/* CODING: Statistics retrieval.                                */
	/*--------------------------------------------------------------*/
	public void reportSimple() {
		if(getNAnything() == 0)
			return;
		long rd = System.nanoTime() - getStartTS();
		System.out.println("S: " + getIdent() + ":" + DbPoolUtil.strNanoTime(rd) + " #conn=" + getNConnectionAllocations() + " #q=" + getTotalQueries() + " #u=" + getTotalUpdates()
 + " #qrow=" + getNRows() + " #urow="
			+ getNUpdatedRows() + " #errs=" + getNErrors() + ", #any=" + getNAnything());
	}
}
