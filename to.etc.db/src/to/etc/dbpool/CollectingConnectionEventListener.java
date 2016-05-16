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
package to.etc.dbpool;

import java.sql.*;
import java.util.*;

import javax.annotation.*;

import to.etc.dbpool.info.*;

/**
 * This class listens for connection events and creates statistic events from
 * them, by calculating durations and translating the events to the statistic
 * event listener's events.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 28, 2011
 */
class CollectingConnectionEventListener implements IConnectionEventListener {
	final private IStatisticsListener m_listener;

	public CollectingConnectionEventListener(@Nonnull IStatisticsListener listener) {
		m_listener = listener;
	}

	@Nonnull
	public IStatisticsListener getListener() {
		return m_listener;
	}

	@Override
	public void connectionAllocated(ConnectionProxy proxy) {
		m_listener.connectionAllocated(proxy);
	}

	@Override
	public void connectionClosed(ConnectionProxy proxy) {
		m_listener.connectionClosed(proxy);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Preparing/creating statements.						*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see IConnectionEventListener#prepareStatement(to.etc.dbpool.StatementProxy)
	 */
	@Override
	public void prepareStatement(@Nonnull StatementProxy sp) {
		sp.m_tsStart = System.nanoTime();
	}

	@Override
	public void prepareStatementEnd(StatementProxy sp) {
		long duration = System.nanoTime() - sp.m_tsStart;
		m_listener.statementPrepared(sp, duration); // Pass it on.
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Execute raw queries (unprepared)					*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see IConnectionEventListener#executeQueryStart(to.etc.dbpool.StatementProxy, to.etc.dbpool.ResultSetProxy)
	 */
	@Override
	public void executeQueryStart(StatementProxy sp, ResultSetProxy rsp) {
		rsp.m_ts_allocated = System.nanoTime();
	}

	@Override
	public void executeQueryEnd(StatementProxy sp, SQLException error, ResultSetProxy rs) {
		rs.m_ts_executeEnd = System.nanoTime();
		rs.m_prepared = true;
	}

	/**
	 * Result set closed: pass all statistics to the listener.
	 * @see IConnectionEventListener#resultSetClosed(to.etc.dbpool.StatementProxy, to.etc.dbpool.ResultSetProxy)
	 */
	@Override
	public void resultSetClosed(StatementProxy sp, ResultSetProxy rsp) {
		long cts = System.nanoTime();
		long executeDuration = rsp.m_ts_executeEnd - rsp.m_ts_allocated; // #nanos in execute query
		long fetchDuration = cts - rsp.m_ts_executeEnd;
		m_listener.queryStatementExecuted(sp, executeDuration, fetchDuration, rsp.m_rowCount, rsp.m_prepared);
	}

	@Override
	public void executePreparedQueryStart(StatementProxy sp, ResultSetProxy rsp) {
		rsp.m_ts_allocated = System.nanoTime();
	}

	@Override
	public void executePreparedQueryEnd(StatementProxy sp, SQLException wx, ResultSetProxy rs) {
		rs.m_ts_executeEnd = System.nanoTime();
		rs.m_prepared = true;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Generic update statement.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see IConnectionEventListener#executeUpdateStart(to.etc.dbpool.StatementProxy)
	 */
	@Override
	public void executeUpdateStart(StatementProxy sp) {
		sp.m_tsStart = System.nanoTime();
	}

	@Override
	public void executeUpdateEnd(StatementProxy sp, SQLException sx, int rowcount) {
		long updateDuration = System.nanoTime() - sp.m_tsStart;
		m_listener.executeUpdateExecuted(sp, updateDuration, rowcount);
	}

	@Override
	public void executePreparedUpdateStart(StatementProxy sp) {
		sp.m_tsStart = System.nanoTime();
	}

	@Override
	public void executePreparedUpdateEnd(StatementProxy sp, SQLException error, int rowcount) {
		long updateDuration = System.nanoTime() - sp.m_tsStart;
		m_listener.executePreparedUpdateExecuted(sp, updateDuration, rowcount);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	The execute() statemnts.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see IConnectionEventListener#executeStart(to.etc.dbpool.StatementProxy)
	 */
	@Override
	public void executeStart(StatementProxy sp) {
		sp.m_tsStart = System.nanoTime();
	}

	@Override
	public void executeEnd(StatementProxy sp, SQLException error, Boolean result) {
		long updateDuration = System.nanoTime() - sp.m_tsStart;
		m_listener.executeExecuted(sp, updateDuration, result);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Batched command sets.								*/
	/*--------------------------------------------------------------*/
	private Map<StatementProxy, List<String>> m_batchMap = new HashMap<>();

	/**
	 * @see IConnectionEventListener#addBatch(to.etc.dbpool.StatementProxy, java.lang.String)
	 */
	@Override
	public void addBatch(StatementProxy sp, String sql) {
		List<String> l = m_batchMap.get(sp);
		if(null == l) {
			l = new ArrayList<>();
			m_batchMap.put(sp, l);
		}
		l.add(sql);
	}

	@Override
	public void executeBatchStart(StatementProxy sp) {
		sp.m_tsStart = System.nanoTime();
	}

	@Override
	public void executeBatchEnd(StatementProxy sp, SQLException error, int[] rc) {
		long executeDuration = System.nanoTime() - sp.m_tsStart;

		int totalStatements = 0;
		int totalRows = 0;
		Map<String, BatchEntry> batchMap = new HashMap<>();
		List<BatchEntry> list = new ArrayList<>();
		List<String> stl = m_batchMap.get(sp);
		if(null == stl)
			stl = Collections.EMPTY_LIST;

		if(null != rc) {
			//-- 1. Create a map of statements with their total execution count
			for(int i = 0; i < rc.length; i++) {
				String stmt = i < stl.size() ? stl.get(i) : "(unknown stmt)";
				BatchEntry be = batchMap.get(stmt);
				if(null == be) {
					be = new BatchEntry(stmt);
					batchMap.put(stmt, be);
					list.add(be);
				}
				int count = rc[i];
				if(count == Statement.SUCCESS_NO_INFO) {	// Fuck oracle
					totalStatements++;
					be.add(1);
					be.setNoRowCount();
					totalRows += 1;
				} else if(count >= 0) {
					totalStatements++;
					be.add(count);
					totalRows += count;
				}
			}

			Collections.sort(list, new Comparator<BatchEntry>() {
				@Override public int compare(BatchEntry a, BatchEntry b) {
					return b.getRowCount() - a.getRowCount();
				}
			});
		} else {
			totalStatements = stl.size();
		}
		m_batchMap.remove(sp);

		m_listener.executeBatchExecuted(executeDuration, totalStatements, totalRows, list);
	}
}
