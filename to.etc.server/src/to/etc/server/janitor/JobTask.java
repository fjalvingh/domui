package to.etc.server.janitor;

import java.sql.*;

import javax.sql.*;

import to.etc.server.syslogger.*;

/**
 * This is the janitor task called for a job.
 *
 * Created on Apr 7, 2003
 * @author jal
 */
public class JobTask extends JanitorTask {
	private Job			m_job;

	private DataSource	m_dbconn;

	private Connection	m_dbc;

	public JobTask(Job j) {
		m_job = j;
	}

	/**
	 * Returns the default connection.
	 * @return
	 * @throws SQLException
	 */
	public Connection db() throws SQLException {
		if(m_dbc == null)
			m_dbc = m_dbconn.getConnection();
		return m_dbc;
	}

	/**
	 * This gets called by the janitor once the time has come to
	 * execute this job. It starts to ensure that this IS the instance
	 * that needs to execute the job; if so the job record is flagged.
	 */
	@Override
	public void run() {
		try {
			if(!mayRun())
				return; // Check if THIS instance needs to schedule this

			//-- When here the job is accepted so initialize all else.
			initAndRun();
		} catch(Exception x) {
			Panicker.logUnexpected(x, "In scheduling job with ID=" + m_job.getID());
		} finally {
			try {
				if(m_dbc != null)
					m_dbc.close();
			} catch(Exception x) {}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Check for running state.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Tries to set the running state to X (executing) atomically.
	 */
	private boolean mayRun() throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = db().prepareStatement("select c.jstate from nema_jobs c where jid=?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new IllegalStateException("The job " + m_job + " can no longer be located in the database.");

			String state = rs.getString(1);
			if(!state.startsWith("R"))
				return false; // Not in RUNNABLE state- be done.
			rs.updateString(1, "X"); // Force to EXECUTING state
			rs.updateRow(); // Force update,
			rs.close();
			ps.close();
			rs = null;
			ps = null;
			db().commit();

			return true; // Job accepted for execution.
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}


	/**
	 * Called to initialize, run and complete the status of the job. This
	 * creates the job's running environment (the logfile stream) and calls
	 * the job's entrypoint. After execution the job's state is set to OK or
	 * FAILED and the job stream gets flushed, writing the last data to the
	 * database.
	 * @throws Exception
	 */
	private void initAndRun() throws Exception {

	}
}
