package to.etc.dbpool.stats;

import to.etc.dbpool.*;

public interface InfoCollector {
	/**
	 * Called when a prepare is started. This starts the clock for the statement.
	 * @param sql
	 */
	public void prepareStatement(String sql);

	/**
	 * Called when the prepare call has finished.
	 * @param sql
	 * @param sp
	 */
	public void prepareStatementEnd(String sql, StatementProxy sp);

	public void executeQueryStart(StatementProxy sp);

	public void executePreparedQueryStart(StatementProxy sp);

	public void executeError(StatementProxy sp, Exception x);

	public void executeQueryEnd(StatementProxy sp, ResultSetProxy rs);

	public void executeUpdateStart(StatementProxy sp);

	public void executePreparedUpdateStart(StatementProxy sp);

	public void executeUpdateEnd(StatementProxy sp, int rowcount);

	public void executeStart(StatementProxy sp);

	public void executeEnd(StatementProxy sp, Boolean result);

	public void incrementUpdateCount(int uc);

	public void executeBatchStart(StatementProxy sp);

	public void executeBatchEnd(StatementProxy sp, int[] rc);

	//-- resultset calls
	public void incrementRowCount(ResultSetProxy rp);

	public void addBatch(String sql);


}
