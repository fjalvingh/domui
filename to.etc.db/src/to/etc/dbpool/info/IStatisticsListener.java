package to.etc.dbpool.info;

import to.etc.dbpool.*;

/**
 * Listener for statistic database events.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 28, 2011
 */
public interface IStatisticsListener {
	/**
	 * A statement was prepared or created.
	 * @param sp
	 * @param prepareDuration
	 */
	void statementPrepared(StatementProxy sp, long prepareDuration);

	/**
	 * A query statement was executed.
	 * @param sp
	 * @param executeDuration
	 * @param fetchDuration
	 * @param rowCount
	 * @param prepared
	 */
	void queryStatementExecuted(StatementProxy sp, long executeDuration, long fetchDuration, int rowCount, boolean prepared);

	/**
	 * An update statement has been executed.
	 * @param sp
	 * @param updateDuration
	 * @param updatedrowcount
	 */
	void executeUpdateExecuted(StatementProxy sp, long updateDuration, int updatedrowcount);

	/**
	 * Executed an "execute" command.
	 * @param sp
	 * @param updateDuration
	 * @param result
	 */
	void executeExecuted(StatementProxy sp, long updateDuration, Boolean result);

	void executePreparedUpdateExecuted(StatementProxy sp, long updateDuration, int rowcount);

	void executeBatchExecuted(StatementProxy sp, long executeDuration, int[] rc);
}
