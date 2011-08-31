package to.etc.dbreplay;

import java.sql.*;

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

	private Connection m_dbc;

	public ReplayExecutor(DbReplay r, int index) {
		m_r = r;
		m_index = index;
	}

	/**
	 * Force termination asap.
	 */
	public synchronized void terminate() {
		m_terminate = true;
		notifyAll();
		interrupt();
	}

	private synchronized boolean isTerminated() {
		return m_terminate;
	}


	@Override
	public void run() {
		try {
			m_dbc = m_r.getPool().getUnpooledDataSource().getConnection();

			m_r.executorReady(this);

			executeLoop();
		} catch(InterruptedException x) {
			; // Ignore.
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
			if(!isTerminated())
				System.out.println(m_index + ": terminated");
		}
	}

	private void executeLoop() throws Exception {
		for(;;) {
			if(isTerminated())
				return;
			Thread.sleep(5000);

		}
	}
}
