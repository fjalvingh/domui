package to.etc.server.janitor;

import java.sql.*;


/**
 * This is the NEMA asynchronous job scheduler. This accepts jobs from the
 * database and schedules them on request. The jobs run in the NEMA context
 * and report their progress in the database.
 * 
 * The scheduler gets kicked by the nema update handler when a new job request
 * is issued, and atomically accepts the job for running.
 * 
 * Each job runs in it's own thread, and will report it's state on a regular
 * basis. To run the jobs the scheduler uses the Janitor's job queue to handle
 * the running tasks.
 * 
 * 
 * Created on Apr 4, 2003
 * @author jal
 */
public class JobScheduler {
	static public final byte	jsRUNNABLE	= 0;

	static public final byte	jsRUNNING	= 1;

	static public final byte	jsABORTED	= 2;

	/** The pool ID for the job tables. */
	private String				m_poolid;

	/**
	 * Returns the pool ID for this-monitor.
	 * @return
	 */
	public String getPoolID() {
		return m_poolid;
	}

	/**
	 * Posts the specified job for scheduling.
	 * @param dbc	The database pool for the job request
	 * @param j		The job data for the job to schedule.
	 * @return 		The job ID as defined in the database. 
	 */
	public int schedule(Connection dbc, Job j) throws Exception {
		//-- 1. Is there another job in COMPLETE state with the same JUK?
		if(j.getJUK() != null)
			checkJUK(dbc, j);

		return -1;
	}


	/**
	 * Check to see if a job with the same JUK exists. If so then storing the
	 * job is prohimited. 
	 * @param dbc
	 * @param j
	 * @throws Exception
	 */
	private void checkJUK(Connection dbc, Job j) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc.prepareStatement("select jid from nema_jobs where jstate ");
			rs = ps.executeQuery();

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


	/*--------------------------------------------------------------*/
	/*	CODING:	Event-driven job scheduler.							*/
	/*--------------------------------------------------------------*/


	public void updateJob(Connection dbc, int jid) {

	}


	/**
	 * Called when the system starts, this scans the job table, removed crud,
	 * and adds all jobs to do to the janitor's job queue.
	 * @param dbc
	 */
	//	public void	bootTimeInit(Connection dbc)
	//	{
	//		PreparedStatement ps = null;
	//		ResultSet rs = null;
	//		try
	//		{
	//			ps	= dbc.prepareStatement(
	//				"select jclassname,jid from nema_jobs where jstate='R'"
	//			);
	//			rs	= ps.executeQuery();
	//			while(rs.next())
	//			{
	//				Job	job = new Job(rs.getString(1));
	//				job.initFromRS(rs);
	//				
	//				//-- Schedule
	//				scheduleJob(job);
	//			}
	//			
	//		}
	//		catch(Exception x)
	//		{
	//			Panicker.logUnexpected(x, "In initializing the job monitor.");
	//		}
	//		finally
	//		{
	//			try { if(rs != null) rs.close(); } catch(Exception x){}
	//			try { if(ps != null) ps.close(); } catch(Exception x){}
	//		}
	//	}


	/**
	 * Adds the job to the janitor's job tables. 
	 * @param job
	 * @throws Exception
	 */
	//	private void	scheduleJob(Job job) throws Exception
	//	{
	//		job.setScheduler(this);
	//		
	//		NemaBroker.getJanitor().addTask(1000, true, job.getClassName(), new JobTask(job));				
	//	}


}
