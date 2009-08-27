package to.etc.dbpool.stats;

import to.etc.dbpool.*;

public class DummyCollector implements InfoCollector {
	static public final DummyCollector INSTANCE = new DummyCollector();

	private DummyCollector() {}

	public void executeBatchEnd(final StatementProxy sp, final int[] rc) {}

	public void executeBatchStart(final StatementProxy sp) {}

	public void executeEnd(final StatementProxy sp, final Boolean result) {}

	public void executeError(final StatementProxy sp, final Exception x) {}

	public void executePreparedQueryStart(final StatementProxy sp) {}

	public void executePreparedUpdateStart(final StatementProxy sp) {}

	public void executeQueryEnd(final StatementProxy sp, final ResultSetProxy rs) {}

	public void executeQueryStart(final StatementProxy sp) {}

	public void executeStart(final StatementProxy sp) {}

	public void executeUpdateEnd(final StatementProxy sp, final int rowcount) {}

	public void executeUpdateStart(final StatementProxy sp) {}

	public void incrementUpdateCount(final int uc) {}

	public void prepareStatement(final String sql) {}

	public void prepareStatementEnd(final String sql, final StatementProxy sp) {}

	public void incrementRowCount(final ResultSetProxy rp) {}

	public void addBatch(final String sql) {}
}
