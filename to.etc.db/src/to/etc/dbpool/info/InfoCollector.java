package to.etc.dbpool.info;

import to.etc.dbpool.*;

public interface InfoCollector {
	/**
	 * Called when a prepare is started. This starts the clock for the statement.
	 * @param sql
	 */
	void prepareStatement(String sql);

	/**
	 * Called when the prepare call has finished.
	 * @param sql
	 * @param sp
	 */
	void prepareStatementEnd(String sql, StatementProxy sp);

	void executeQueryStart(StatementProxy sp);

	void executePreparedQueryStart(StatementProxy sp);

	void executeError(StatementProxy sp, Exception x);

	void executeQueryEnd(StatementProxy sp, ResultSetProxy rs);

	void executeUpdateStart(StatementProxy sp);

	void executePreparedUpdateStart(StatementProxy sp);

	void executeUpdateEnd(StatementProxy sp, int rowcount);

	void executeStart(StatementProxy sp);

	void executeEnd(StatementProxy sp, Boolean result);

	void incrementUpdateCount(int uc);

	void executeBatchStart(StatementProxy sp);

	void executeBatchEnd(StatementProxy sp, int[] rc);

//	//-- resultset calls
//	void incrementRowCount(ResultSetProxy rp);

	/**
	 * The result set was closed, and row fetch count and row fetch duration is available.
	 * @param rp
	 */
	void resultSetClosed(ResultSetProxy rp);

	void addBatch(String sql);

	void connectionAllocated();

	void finish();


}
