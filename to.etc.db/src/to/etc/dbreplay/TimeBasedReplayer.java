package to.etc.dbreplay;

import java.util.*;

import to.etc.dbpool.*;

public class TimeBasedReplayer implements IReplayer {
	private Map<Integer, ReplayExecutor> m_executorMap = new HashMap<Integer, ReplayExecutor>();

	private Set<Integer> m_ignoreSet = new HashSet<Integer>();

	/** The timestamp of the previous replay record. */
	private long m_lastReplayTime;

	private long m_lastRealTime;

	@Override
	public int decodeArgs(String option, String[] args, int argc) {
		return -1;
	}
	public void handleRecord(DbReplay r, ReplayRecord rr) throws Exception {
		long ct = System.currentTimeMillis();

		//-- Try to assign an executor.
		Integer cid = Integer.valueOf(rr.getConnectionId());
		if(rr.getType() == StatementProxy.ST_CLOSE) {
			m_ignoreSet.remove(cid); // If this was ignored - end that
			ReplayExecutor rx = m_executorMap.remove(cid); // Was an executor assigned to this connection?
			if(null != rx) {
				r.releaseExecutor(rx);
			}
			return;
		}

		//-- Skip boring actions
		if(rr.getType() == StatementProxy.ST_COMMIT || rr.getType() == StatementProxy.ST_ROLLBACK)
			return;

		//-- If we're ignored: increment ignored stmt count and exit
		if(m_ignoreSet.contains(cid)) {
			r.incConnSkips();
			return;
		}

		//-- Determine the time delta between this record and the previous one
		if(m_lastReplayTime == 0) {
			m_lastReplayTime = rr.getStatementTime();
			m_lastRealTime = ct;
		} else {
			long deltat = rr.getStatementTime() - m_lastReplayTime;
			if(deltat < 0)
				deltat = 0;
			m_lastReplayTime = rr.getStatementTime();

			if(deltat > r.getMaxStatementDelay())
				deltat = r.getMaxStatementDelay();

			if(deltat > 0) {
				if(deltat > 5000)
					System.out.println("       - long sleep of " + DbPoolUtil.strMillis(deltat));
				Thread.sleep(deltat);
			}
			m_lastRealTime = ct;
		}

		//-- Ok, we need an executor for this. Get or allocate;
		ReplayExecutor rx = m_executorMap.get(cid); // Is an executor already assigned to this connection?
		if(rx == null) {
			//-- Try to allocate an executor
			rx = r.allocateExecutor();
			if(null == rx) {
				//-- Nothing free... Add to ignore set, and increment error count
				m_ignoreSet.add(cid); // Ignore all related statements
				r.log("no free executor: " + cid);
				return;
			}

			//-- Assign executor
			m_executorMap.put(cid, rx);
		}

		if(r.isLogging())
			r.log("x: " + rr.getSummary());
		rx.queue(rr);
	}

}
