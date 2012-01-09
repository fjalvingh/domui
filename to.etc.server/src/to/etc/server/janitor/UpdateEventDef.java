package to.etc.server.janitor;

import java.sql.*;

/**
 * A definition class for an update event. Simple interface to
 * handle, post and register update events thru a single class
 * defining the event.
 *
 * @author jal
 * Created on Feb 21, 2005
 */
public class UpdateEventDef {
	/** The source to post events to */
	private UpdateSource	m_us;

	/** The event name for the event. */
	private String			m_evname;

	public UpdateEventDef(UpdateSource us, String evname) {
		m_evname = evname;
		m_us = us;
	}

	/**
	 * Adds (another) handler for the event. When called this will
	 * start the event janitor task.
	 * @param l
	 */
	public void addHandler(UpdateListener l) {
		m_us.addHandler(m_evname, l);
	}

	/**
	 * Should be USED only by a post call.
	 * @param i1
	 * @param i2
	 * @param i3
	 * @param s1
	 * @param s2
	 * @throws SQLException
	 */
	protected void postEvent(long i1, long i2, long i3, String s1, String s2) throws SQLException {
		m_us.postUpdate(m_evname, s1, s2, i1, i2, i3);
	}
}
