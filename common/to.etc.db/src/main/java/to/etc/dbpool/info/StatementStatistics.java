package to.etc.dbpool.info;

/**
 * The statistics for a single SQL statement during the request/response cycle.
 */
final public class StatementStatistics {
	final private String m_sql;

	private int m_executions;

	private long m_rows;

	private long m_totalFetchDuration;

	/** Total time spent executing all these queries. */
	private long m_totalExecuteNS;

	StatementStatistics(String sql) {
		m_sql = sql;
	}

	public String getSQL() {
		return m_sql;
	}

	void incExecutions() {
		m_executions++;
	}

	void addExecutions(int count) {
		m_executions += count;
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
