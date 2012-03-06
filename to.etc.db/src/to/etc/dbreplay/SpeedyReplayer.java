package to.etc.dbreplay;

import to.etc.dbpool.*;

/**
 * Replayer which tries to speed up statements by queueing them to different executors
 * round-robin style.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 12, 2011
 */
public class SpeedyReplayer implements IReplayer {
	/** How many statements to queue per wait period */
	private int m_perWait = 1;

	/** #of statements left to do this wait period */
	private int m_left;

	private long m_lastTs;

	@Override
	public int decodeArgs(String option, String[] args, int argc) {
		if("-perwait".equals(option)) {
			if(argc >= args.length)
				throw new IllegalArgumentException("Missing [count] for -perwait option (SpeedyReplayer)");
			m_perWait = Integer.parseInt(args[argc++]);
			if(m_perWait <= 0)
				throw new IllegalArgumentException("count for -perwait option must be > 0 (SpeedyReplayer)");
			return argc;
		}
		return -1;
	}

	@Override
	public void handleRecord(DbReplay r, ReplayRecord rr) throws Exception {
		//-- Stuff to ignore?
		switch(rr.getType()){
			default:
				return;

			case StatementProxy.ST_QUERY:
				break;
		}

		long cts = System.currentTimeMillis();
		long maxwait = r.getMaxStatementDelay();

		if(m_lastTs == 0) {
			//-- First one- just queue.
			m_lastTs = cts;
			m_left = m_perWait;
		} else {
			if(m_left <= 0) {
				//-- No more left. Wait remaining period.
				long dt = m_lastTs + maxwait - cts;
				if(dt > 0)
					Thread.sleep(dt);
				m_left = m_perWait;
				cts = System.currentTimeMillis();
				m_lastTs = cts;
			}
		}

		m_left--;
		r.queueIdle(rr);
	}

}
