package to.etc.server.janitor;

import java.sql.*;
import java.util.*;

import to.etc.dbpool.*;
import to.etc.dbutil.*;

/**
 *
 *
 * @author jal
 * Created on Jan 23, 2005
 */
public class UpdateSource {
	private UpdateEventManager	m_em;

	private DbConnector			m_dbconn;

	private String				m_table		= "nema_updates";

	private String				m_sequence	= "nema_updates_sq";

	private long				m_upid		= -1;

	protected UpdateSource(UpdateEventManager em, DbConnector dbconn, String tbl, String seq) {
		m_em = em;
		m_dbconn = dbconn;
		if(tbl != null) {
			m_table = tbl;
			m_sequence = seq;
		}
	}

	@Override
	public int hashCode() {
		return m_dbconn.getID().hashCode() << 10 ^ m_table.hashCode();
	}

	@Override
	public boolean equals(Object b) {
		if(!(b instanceof UpdateSource))
			return false;
		UpdateSource e = (UpdateSource) b;
		return e.m_dbconn.getID().equalsIgnoreCase(m_dbconn.getID()) && e.m_table.equalsIgnoreCase(m_table);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Initialization.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Tries to create the table if it doesn't exist. Ignores all errors.
	 * @param dbc
	 */
	private void createTable(Connection dbc) {
		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement("create table " + m_table + "(" + " upid numeric(20,0) not null primary key," + " evcode varchar(40) not null," + " evs1   varchar(200) null,"
				+ " evs2   varchar(200) null," + " evi1   numeric(20,0) null," + " evi2   numeric(20,0) null," + " evi3   numeric(20,0) null" + ")");
			ps.executeUpdate();
		} catch(Exception x) {} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Called when this checker initializes. This retrieves the LAST update
	 * number currently used to initialize. Every update AFTER that number will
	 * be seen and called. This also registers the janitor task which calls the
	 * updater every time.
	 *
	 * @param dbc
	 * @throws Exception
	 */
	protected synchronized void init() throws Exception {
		if(m_upid != -1)
			return;

		Connection dbc = m_dbconn.makeConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			createTable(dbc);

			//-- Get the last update #
			ps = dbc.prepareStatement("select max(upid) from " + m_table);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new IllegalStateException("?? Cannot get max update number from DB=" + m_dbconn.getID() + " table=" + m_table);
			m_upid = rs.getLong(1);
			//			MSG.msg("Initialized, first NEW update will be "+(m_upid+1));
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Checker code.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Checks for updates on this checker, and handles them if found. To prevent
	 * multiple threads from handling updates this locks the instance.
	 */
	protected void checkUpdates(ArrayList al) throws Exception {
		Connection dbc = m_dbconn.makeConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			//			Nema.initConnection(dbc);
			ps = dbc.prepareStatement("select upid,evcode,evs1,evs2,evi1,evi2,evi3 from " + m_table + " where upid > ? order by upid");
			ps.setLong(1, m_upid);
			rs = ps.executeQuery();
			while(rs.next()) {
				m_upid = rs.getLong(1); // Update current update nr
				String ev = rs.getString(2);
				String s1 = rs.getString(3);
				String s2 = rs.getString(4);
				long i1 = rs.getLong(5);
				long i2 = rs.getLong(6);
				long i3 = rs.getLong(7);
				UpdateEvent e = new UpdateEvent(m_upid, ev, s1, s2, i1, i2, i3);
				al.add(e);
			}
		} finally {
			//			Nema.clearConnection();
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			//			MSG.msg("Last update seen has ID="+m_upid);
			try {
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}
	}

	@Override
	public String toString() {
		return m_dbconn.getID() + "/" + m_table;
	}


	public void addHandler(String evcode, UpdateListener ul) {
		m_em.registerHandler(evcode, ul);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Post an update generic code...						*/
	/*--------------------------------------------------------------*/
	public void postUpdate(String ev, String s1, String s2, long i1, long i2, long i3) throws SQLException {
		Connection dbc = m_dbconn.makeConnection();
		PreparedStatement ps = null;
		try {
			//-- Get a new sequence ID
			long id = GenericDB.getFullSequenceID(dbc, m_sequence);

			ps = dbc.prepareStatement("insert into " + m_table + "(upid,evcode,evs1,evs2,evi1,evi2,evi3) values(?,?,?,?,?,?,?)");
			ps.setLong(1, id);
			ps.setString(2, ev);
			ps.setString(3, s1);
			ps.setString(4, s2);
			ps.setLong(5, i1);
			ps.setLong(6, i2);
			ps.setLong(7, i3);
			ps.executeUpdate();
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}
	}
}
