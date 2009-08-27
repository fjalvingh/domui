package to.etc.dbpool.stats;

import java.util.*;

import to.etc.dbpool.*;

/**
 * Instance of a single run of thread-related data. This structure gets created
 * every time data collection for a thread starts. All of the data collected will
 * be maintained in this structure *if* available. Since this structure is only
 * ever accessed by a single thread we need not synchronize access.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 22, 2007
 */
public class ThreadData implements InfoCollector {
	static private final byte T_PREPARED = 1;

	static private final byte T_PREPQ = 2;

	static private final byte T_PREPUPD = 3;

	static private final byte T_QSTMT = 4;

	static private final byte T_USTMT = 5;

	static private final byte T_EXECUTES = 6;

	private final String m_ident;

	private int m_nestingCount;

	private byte m_type;

	/** The time that this request started. */
	private long m_ts_started;

	/** Duration of the entire request in nano's */
	private long m_requestDuration;

	/** Timestamp of the last START of a call. */
	private long m_lastTS;

	/**
	 * The #of times a connection was allocated during this request.
	 */
	private int m_nConnectionAllocations;

	/**
	 * The #of times a statement was PREPARED (preparedStatement)
	 */
	private int m_nPrepares;

	/**
	 * The total time spent in preparing the statements above.
	 */
	private long m_prepareDuration;

	/**
	 * #of queries issued using prepared statements.
	 */
	private int m_nPreparedQueries;

	/**
	 * #of updates issued using prepared statements.
	 */
	private int m_nPreparedUpdates;

	/**
	 * #of unprepared updates (using a statement).
	 */
	private int m_nStatementUpdates;

	private long m_nUpdatedRows;

	private long m_preparedQueryDuration;

	private long m_preparedUpdateDuration;

	private long m_statementQueryDuration;

	private long m_statementUpdateDuration;

	private int m_nExecutes;

	private long m_executeDuration;

	private int m_nErrors;

	/** Number of rows retrieved, */
	private int m_nRows;

	private long m_rowRetrievalTime;

	/**
	 * The #of statements passed
	 */
	private int m_nStatementQueries;

	private final boolean m_collectSQL = true;

	private Map<String, Counters> m_sqlMap = Collections.EMPTY_MAP;

	private List<Counters> m_batchSet;

	static public class Counters {
		public String sql;

		public long executions;

		public long rows;

		//    	public Set<String>	tmpset = new HashSet<String>();
		Counters() {}
	}

	public ThreadData(final String ident) {
		m_ident = ident;
		m_nestingCount = 1;
		m_ts_started = System.nanoTime();
	}

	private Counters findCounter(final String sql) {
		Counters c = m_sqlMap.get(sql);
		if(c == null) {
			c = new Counters();
			c.sql = sql;
			if(m_sqlMap == Collections.EMPTY_MAP)
				m_sqlMap = new HashMap<String, Counters>();
			m_sqlMap.put(sql, c);
		}
		return c;
	}

	private void addMap(final StatementProxy p) {
		if(!m_collectSQL)
			return;
		Counters c = findCounter(p.getSQL());
		c.executions++;
		//    	if(p.getSQL().contains("this_.CAN_ID") && p.getSQL().contains("from CON_CONTRACTANTEN this_ where this_.CST_ID=?")) {
		//    		try {
		//    			throw new Exception("X");
		//    		} catch(Exception x) {
		//    			String here = DbPoolUtil.strStacktrace(x);
		//    			c.tmpset.add(here);
		//    		}
		//    	}
	}

	public Map<String, Counters> getStatementMap() {
		return m_sqlMap;
	}

	public String getIdent() {
		return m_ident;
	}

	public void increment() {
		if(m_nestingCount == 0)
			throw new IllegalStateException("Use of discarded statistics");
		m_nestingCount++;
	}

	public boolean decrement() {
		if(m_nestingCount <= 0)
			throw new IllegalStateException("Use of discarded statistics");
		boolean last = --m_nestingCount == 0;
		if(!last)
			return false;
		m_requestDuration = System.nanoTime() - m_ts_started;
		return true;
	}

	public void connectionAllocated() {
		m_nConnectionAllocations++;
	}

	public void prepareStatement(final String sql) {
		m_lastTS = System.nanoTime();
		m_type = T_PREPARED;
		m_nPrepares++;
		if(m_collectSQL)
			findCounter(sql);
	}

	public void prepareStatementEnd(final String sql, final StatementProxy sp) {
		if(m_type != T_PREPARED)
			throw new IllegalStateException("Bad type");
		long dt = System.nanoTime() - m_lastTS;
		m_prepareDuration += dt;
		m_type = 0;
	}

	public void executePreparedQueryStart(final StatementProxy sp) {
		m_lastTS = System.nanoTime();
		m_type = T_PREPQ;
		m_nPreparedQueries++;
		addMap(sp);
	}

	public void executeQueryEnd(final StatementProxy sp, final ResultSetProxy rs) {
		long dt = System.nanoTime() - m_lastTS;
		switch(m_type){
			default:
				throw new IllegalStateException("Bad type.");
			case T_PREPQ:
				m_preparedQueryDuration += dt;
				break;

			case T_QSTMT:
				m_statementQueryDuration += dt;
				break;
		}
	}

	public void executePreparedUpdateStart(final StatementProxy sp) {
		m_nPreparedUpdates++;
		m_lastTS = System.nanoTime();
		m_type = T_PREPUPD;
		addMap(sp);
	}

	public void executeUpdateStart(final StatementProxy sp) {
		m_nStatementUpdates++;
		m_lastTS = System.nanoTime();
		m_type = T_USTMT;
		addMap(sp);
	}


	public void executeUpdateEnd(final StatementProxy sp, final int rowcount) {
		if(rowcount > 0)
			m_nUpdatedRows += rowcount;
		long dt = System.nanoTime() - m_lastTS;
		switch(m_type){
			default:
				throw new IllegalStateException("Bad type.");
			case T_PREPUPD:
				m_preparedUpdateDuration += dt;
				break;
			case T_USTMT:
				m_statementUpdateDuration += dt;
				break;
		}
	}

	public void executeError(final StatementProxy sp, final Exception x) {
		m_nErrors++;
	}

	public void executeQueryStart(final StatementProxy sp) {
		m_nStatementQueries++;
		m_lastTS = System.nanoTime();
		m_type = T_QSTMT;
		addMap(sp);
	}

	public void executeStart(final StatementProxy sp) {
		m_nExecutes++;
		m_lastTS = System.nanoTime();
		m_type = T_EXECUTES;
		addMap(sp);
	}

	public void executeEnd(final StatementProxy sp, final Boolean result) {
		long dt = System.nanoTime() - m_lastTS;
		switch(m_type){
			default:
				throw new IllegalStateException("Bad type.");
			case T_EXECUTES:
				m_executeDuration += dt;
				break;
		}
	}

	public void incrementUpdateCount(final int uc) {
		if(uc > 0)
			m_nUpdatedRows += uc;
	}

	public void executeBatchEnd(final StatementProxy sp, final int[] rc) {}

	public void executeBatchStart(final StatementProxy sp) {
		if(m_batchSet != null && m_batchSet.size() > 0) {
			for(Counters c : m_batchSet)
				c.executions++;
			m_batchSet.clear();
		}
	}

	public void addBatch(final String sql) {
		if(!m_collectSQL)
			return;

		Counters c = findCounter(sql);
		if(m_batchSet == null)
			m_batchSet = new ArrayList<Counters>();
		m_batchSet.add(c);
	}

	public void incrementRowCount(final ResultSetProxy rp) {
		m_nRows++;
	}

	/*--------------------------------------------------------------*/
	/* CODING: Statistics retrieval.                                */
	/*--------------------------------------------------------------*/

	public long getRequestDuration() {
		return m_requestDuration;
	}

	public int getNAllocatedConnections() {
		return m_nConnectionAllocations;
	}

	public int getNestingCount() {
		return m_nestingCount;
	}

	public int getNConnectionAllocations() {
		return m_nConnectionAllocations;
	}

	public int getNPrepares() {
		return m_nPrepares;
	}

	public long getPrepareDuration() {
		return m_prepareDuration;
	}

	public int getNPreparedQueries() {
		return m_nPreparedQueries;
	}

	public int getNPreparedUpdates() {
		return m_nPreparedUpdates;
	}

	public int getNStatementUpdates() {
		return m_nStatementUpdates;
	}

	public long getNUpdatedRows() {
		return m_nUpdatedRows;
	}

	public long getPreparedQueryDuration() {
		return m_preparedQueryDuration;
	}

	public long getPreparedUpdateDuration() {
		return m_preparedUpdateDuration;
	}

	public long getStatementQueryDuration() {
		return m_statementQueryDuration;
	}

	public long getStatementUpdateDuration() {
		return m_statementUpdateDuration;
	}

	public int getNExecutes() {
		return m_nExecutes;
	}

	public long getExecuteDuration() {
		return m_executeDuration;
	}

	public int getNErrors() {
		return m_nErrors;
	}

	public int getNStatementQueries() {
		return m_nStatementQueries;
	}

	public int getTotalQueries() {
		return m_nExecutes + m_nStatementQueries + m_nPreparedQueries;
	}

	public int getTotalUpdates() {
		return m_nPreparedUpdates + m_nStatementUpdates;
	}

	public int getTotalDBRequests() {
		return getTotalQueries() + getTotalUpdates();
	}

	public int getNRows() {
		return m_nRows;
	}
}
