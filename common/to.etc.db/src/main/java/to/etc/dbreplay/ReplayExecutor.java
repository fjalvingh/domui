package to.etc.dbreplay;

import java.sql.*;
import java.util.*;

import to.etc.dbpool.*;

/**
 * This is a worker thread class which handles commands sent to it. It has it's own
 * separate connection to handle statements on.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 31, 2011
 */
class ReplayExecutor extends Thread {
	private DbReplay m_r;

	private int m_index;

	private boolean m_terminate;

	/** T if this executor has terminated, protected by DbReplay instance. */
	private boolean m_terminated;

	private Connection m_dbc;

	private List<ReplayRecord> m_queueList = new ArrayList<ReplayRecord>();

	/** T if this executor is idling. Protected by IdleLock */
	private boolean m_idle;

	private Object m_idleLock;

	public ReplayExecutor(DbReplay r, int index, Object idleLock) {
		m_r = r;
		m_index = index;
		m_idleLock = idleLock;
	}

	/**
	 * Force termination asap.
	 */
	public synchronized void terminate() {
		m_terminate = true;
		notify();
		interrupt();
	}

	private synchronized boolean isTerminating() {
		return m_terminate;
	}

	public synchronized void queue(ReplayRecord q) {
		m_queueList.add(q);
		notify();
	}

	@Override
	public void run() {
		try {
			m_dbc = m_r.getPool().getUnpooledDataSource().getConnection();
			m_dbc.setAutoCommit(false);
			String rs;
			if((rs = m_r.getRunSchema()) != null) {
				sql("alter session set current_schema=" + rs);
			}
			m_r.executorReady(this);

			executeLoop();
		} catch(InterruptedException x) {
			// Ignore.
		} catch(Exception x) {
			System.out.println(m_index + ": terminated due to " + x);
			x.printStackTrace();
		} finally {
			try {
				if(m_dbc != null)
					m_dbc.rollback();
			} catch(Exception x) {}
			try {
				if(m_dbc != null)
					m_dbc.close();
			} catch(Exception x) {}
			m_r.executorStopped(this);
			if(!isTerminating())
				System.out.println(m_index + ": terminated");
		}
	}

	private void sql(String sql) throws Exception {
		PreparedStatement ps = null;
		try {
			ps = m_dbc.prepareStatement(sql);
			ps.executeUpdate();
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	private void executeLoop() throws Exception {
		for(;;) {
			ReplayRecord	rr = null;
			synchronized(this) {
				if(m_terminate) {
					boolean idle = isIdle();
					setIdle(true);
					if(idle)
						m_r.removeIdle(this);

					synchronized(m_r) {
						m_r.notifyAll(); // Stopping.
					}
					return;
				}

				if(m_queueList.size() > 0) {
					rr = m_queueList.remove(0);
					synchronized(m_r) {
						if(m_idle) { // Were we idling?
							m_r.removeIdle(this);
							m_idle = false;
						}
					}
				} else {
					//-- Nothing in the queue (anymore): mark me as idle if not already.
					synchronized(m_r) {
						if(!m_idle) { // Not already idle?
							m_r.addIdle(this);
							m_idle = true;
						}
					}
					wait(5000);
				}
			}
			if(null != rr)
				execute(rr);
		}
	}

	public boolean isTerminated() {
		synchronized(m_r) {
			return m_terminated;
		}
	}

	boolean isIdle() {
		synchronized(m_idleLock) {
			return m_idle;
		}
	}

	private void setIdle(boolean idle) {
		synchronized(m_idleLock) {
			m_idle = idle;
		}
	}

	private void execute(ReplayRecord rr) {
		if(rr.isUnexecutable())
			return;
		switch(rr.getType()){
			default:
				m_r.incIgnored();
				return;

			case StatementProxy.ST_QUERY:
				executeQueryStatement(rr);
				return;
		}
	}

	private void executeQueryStatement(ReplayRecord rr) {
		m_r.startExecution();
		int errs = 0;
		int rows = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
//			System.out.println("     #" + m_index + ": " + rr.getSql());
			ps = m_dbc.prepareStatement(rr.getSql());

			for(int i = 0; i < rr.getParamCount(); i++) {
				rr.assignParameter(ps, i);
			}
			rs = ps.executeQuery();
			while(rs.next()) {
				rows++;
			}
//			System.out.println("     #" + m_index + ": DONE, " + rows + " rows");

		} catch(Exception x) {
			if(m_r.isLogging())
				m_r.log(x.toString());
//			System.out.println(x.toString());
			errs++;
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			m_r.endExecution(1, 0, errs, rows);
		}
	}

}
