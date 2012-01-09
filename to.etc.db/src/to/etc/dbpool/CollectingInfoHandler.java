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

import javax.annotation.*;

import to.etc.dbpool.info.*;

/**
 * Class that actually collects all statistics. It accepts "primitive" events
 * from the db layer and translates them to statistic events.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 28, 2011
 */
class CollectingInfoHandler implements IInfoHandler {
	final private IStatisticsListener m_listener;

	public CollectingInfoHandler(@Nonnull IStatisticsListener listener) {
		m_listener = listener;
	}

	@Nonnull
	public IStatisticsListener getListener() {
		return m_listener;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Preparing/creating statements.						*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.dbpool.IInfoHandler#prepareStatement(to.etc.dbpool.StatementProxy)
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
	 * @see to.etc.dbpool.IInfoHandler#executeQueryStart(to.etc.dbpool.StatementProxy, to.etc.dbpool.ResultSetProxy)
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
	 * @see to.etc.dbpool.IInfoHandler#resultSetClosed(to.etc.dbpool.StatementProxy, to.etc.dbpool.ResultSetProxy)
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
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Generic update statement.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.dbpool.IInfoHandler#executeUpdateStart(to.etc.dbpool.StatementProxy)
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
	 * @see to.etc.dbpool.IInfoHandler#executeStart(to.etc.dbpool.StatementProxy)
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
	/**
	 * These are not currently measured.
	 * @see to.etc.dbpool.IInfoHandler#addBatch(to.etc.dbpool.StatementProxy, java.lang.String)
	 */
	@Override
	public void addBatch(StatementProxy sp, String sql) {}

	@Override
	public void executeBatchStart(StatementProxy sp) {
		sp.m_tsStart = System.nanoTime();
	}

	@Override
	public void executeBatchEnd(StatementProxy sp, SQLException error, int[] rc) {
		long executeDuration = System.nanoTime() - sp.m_tsStart;
		m_listener.executeBatchExecuted(sp, executeDuration, rc);
	}
}
