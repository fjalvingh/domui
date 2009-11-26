package to.etc.server.janitor;

import java.sql.*;


/**
 * This contains the data for the job to schedule.
 *
 * Created on Apr 4, 2003
 * @author jal
 */
public class Job {
	static private final int	MAX_INTPARM	= 4;

	static private final int	MAX_STRPARM	= 4;

	private int					m_jid;

	/** The class name that will handle the job for us */
	private String				m_classname;

	/** The requested start date/t ime for the job */
	private java.util.Date		m_start;

	/** The int parameters for the job (0..4) */
	private int[]				m_intparm_ar;

	/** The string parameters for the job (0..4) */
	private String[]			m_strparm_ar;

	/** The scheduler scheduling this dude */
	private JobScheduler		m_js;

	/**
	 * The Job Unique Key identifier. This is a key that CAN be used
	 * to prevent multiple jobs for the same task to be stored. If a
	 * JUK is present then the scheduler will check to see if a job
	 * with the same JUK is present in any state that would allow it
	 * to be scheduler later on. If so the scheduling of this new
	 * job is NOT permitted, and the call will throw an exception.
	 */
	private String				m_juk;

	protected Job() {

	}

	public Job(Class cl) {
		m_classname = cl.getName();
	}

	public Job(String classname) {
		m_classname = classname;
	}

	public void setStartTime(java.util.Date d) {
		m_start = d;
	}

	public java.util.Date getStartTime() {
		return m_start;
	}

	public String getClassName() {
		return m_classname;
	}

	protected void setScheduler(JobScheduler js) {
		m_js = js;
	}

	/**
	 * Returns the scheduler instance that scheduled this job.
	 * @return
	 */
	public JobScheduler getScheduler() {
		return m_js;
	}

	public int getID() {
		return m_jid;
	}

	/**
	 * Sets the nth integer parameter for the job to execute.
	 * @param ix
	 * @param val
	 */
	public void setParm(int ix, int val) {
		if(m_intparm_ar == null)
			m_intparm_ar = new int[MAX_INTPARM];
		if(ix < 0 || ix >= MAX_INTPARM)
			throw new IllegalArgumentException("Job int parameter number must be 0 <= x < " + MAX_INTPARM);
		m_intparm_ar[ix] = val;
	}

	/**
	 * Sets the nth stringinteger parameter for the job to execute.
	 * @param ix
	 * @param val
	 */
	public void setParm(int ix, String val) {
		if(m_strparm_ar == null)
			m_strparm_ar = new String[MAX_STRPARM];
		if(ix < 0 || ix >= MAX_STRPARM)
			throw new IllegalArgumentException("Job string parameter number must be 0 <= x < " + MAX_STRPARM);
		m_strparm_ar[ix] = val;
	}

	public void setJUK(String juk) {
		if(juk != null && juk.length() >= 20)
			throw new IllegalArgumentException("The Job Unique Key cannot exceed 20 chars in length");
		m_juk = juk;
	}

	public String getJUK() {
		return m_juk;
	}


	protected void initFromRS(ResultSet rs) throws SQLException {
		m_classname = rs.getString("jclassname");
		m_jid = rs.getInt("jid");


	}


}
