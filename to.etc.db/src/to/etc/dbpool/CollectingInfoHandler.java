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
		m_listener.queryStatementExecuted(sp, executeDuration, fetchDuration, rsp.m_rowCount);
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

	/*--------------------------------------------------------------*/
	/*	CODING:	The execute() statemnts.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.dbpool.IInfoHandler#executeStart(to.etc.dbpool.StatementProxy)
	 */
	@Override
	public void executeStart(StatementProxy sp) {}

	@Override
	public void executeEnd(StatementProxy sp, SQLException error, Boolean result) {}

	/*--------------------------------------------------------------*/
	/*	CODING:	Batched command sets.								*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.dbpool.IInfoHandler#addBatch(to.etc.dbpool.StatementProxy, java.lang.String)
	 */
	@Override
	public void addBatch(StatementProxy sp, String sql) {}

	@Override
	public void executeBatchStart(StatementProxy sp) {}

	@Override
	public void executeBatchEnd(StatementProxy sp, SQLException error, int[] rc) {}

}
