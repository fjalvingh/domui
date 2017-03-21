/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.webapp.pendingoperations;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import javax.annotation.*;
import javax.sql.*;

import to.etc.util.*;

/**
 * Represents a pending operation in the pending operation queue.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 4, 2009
 */
public class PendingOperation {
	static final String[] FLDAR = {"spo_xident", "spo_issuing_server", "spo_date_created", "spo_must_execute_on_server", "spo_executing_server", "spo_last_execute_started",
		"spo_last_execute_completed", "spo_state", "spo_retries", "spo_date_next_try", "spo_order_groupname", "spo_order_timestamp", "spo_order_sub", "spo_type", "spo_arg1", "spo_arg2",
		"spo_lasterror", "spo_errorlog", "spo_userid", "spo_description", "spo_submitsource", "progress_path", "progress_percentage"};

	static final String FIELDS = "spo_id,spo_xident,spo_issuing_server,spo_date_created,spo_must_execute_on_server,spo_executing_server"
		+ ",spo_last_execute_started,spo_last_execute_completed,spo_state,spo_retries,spo_date_next_try,spo_order_groupname,spo_order_timestamp"
		+ ",spo_order_sub,spo_type,spo_arg1,spo_arg2,spo_lasterror, spo_errorlog, spo_userid,spo_description,spo_submitsource,progress_path,progress_percentage";

	private long m_id = -1;

	private String m_xid;

	private String m_sourceServerID;

	private Date m_creationTime;

	private String m_mustExecuteOnServerID;

	private String m_executesOnServerID;

	private Date m_lastExecutionStart;

	private Date m_lastExecutionEnd;

	private int m_retries;

	private Date m_nextTryTime;

	private String m_orderGroup;

	private Date m_orderTime;

	private int m_orderIndex;

	private String m_type;

	private String m_arg1;

	private Properties m_properties;

	private String m_lastError;

	private String m_errorLog;

	private PendingOperationState m_state = PendingOperationState.RTRY;

	private String m_userID;

	private String m_description;

	private String m_submitsource;

	private Object m_serializedObject;

	private DataSource m_dataSource;

	static private String m_updateSQL, m_insertSQL;

	private String m_progressPath;

	private int m_progressPercentage = 0;

	public long getId() {
		return m_id;
	}

	public void setId(final long id) {
		m_id = id;
	}

	public String getXid() {
		return m_xid;
	}

	public void setXid(final String xid) {
		m_xid = xid;
	}

	public String getSourceServerID() {
		return m_sourceServerID;
	}

	public void setSourceServerID(final String sourceServerID) {
		m_sourceServerID = sourceServerID;
	}

	public Date getCreationTime() {
		return m_creationTime;
	}

	public void setCreationTime(final Date creationTime) {
		m_creationTime = creationTime;
	}

	public String getMustExecuteOnServerID() {
		return m_mustExecuteOnServerID;
	}

	public void setMustExecuteOnServerID(final String mustExecuteOnServerID) {
		m_mustExecuteOnServerID = mustExecuteOnServerID;
	}

	public String getExecutesOnServerID() {
		return m_executesOnServerID;
	}

	public void setExecutesOnServerID(final String executesOnServerID) {
		m_executesOnServerID = executesOnServerID;
	}

	public Date getLastExecutionStart() {
		return m_lastExecutionStart;
	}

	public void setLastExecutionStart(final Date lastExecutionStart) {
		m_lastExecutionStart = lastExecutionStart;
	}

	public Date getLastExecutionEnd() {
		return m_lastExecutionEnd;
	}

	public void setLastExecutionEnd(final Date lastExecutionEnd) {
		m_lastExecutionEnd = lastExecutionEnd;
	}

	public int getRetries() {
		return m_retries;
	}

	public void setRetries(final int retries) {
		m_retries = retries;
	}

	public Date getNextTryTime() {
		return m_nextTryTime;
	}

	public void setNextTryTime(final Date nextTryTime) {
		m_nextTryTime = nextTryTime;
	}

	public String getOrderGroup() {
		return m_orderGroup;
	}

	public void setOrderGroup(final String orderGroup) {
		m_orderGroup = orderGroup;
	}

	public Date getOrderTime() {
		return m_orderTime;
	}

	public void setOrderTime(final Date orderTime) {
		m_orderTime = orderTime;
	}

	public int getOrderIndex() {
		return m_orderIndex;
	}

	public void setOrderIndex(final int orderIndex) {
		m_orderIndex = orderIndex;
	}

	public String getType() {
		return m_type;
	}

	public void setType(final String type) {
		m_type = type;
	}

	public String getArg1() {
		return m_arg1;
	}

	public void setArg1(final String arg1) {
		m_arg1 = arg1;
	}

	public String getProperty(final String name) {
		return m_properties == null ? null : m_properties.getProperty(name);
	}

	public void setProperty(final String name, final String value) {
		if(value == null) {
			if(m_properties != null) {
				m_properties.remove(name);
				if(m_properties.size() == 0)
					m_properties = null;
			}
		} else {
			if(m_properties == null)
				m_properties = new Properties();
			m_properties.setProperty(name, value);
		}
	}

	public String getLastError() {
		return m_lastError;
	}

	public void setLastError(final String lastError) {
		m_lastError = lastError;
	}

	public String getErrorLog() {
		return m_errorLog;
	}

	public void setErrorLog(final String errorLog) {
		m_errorLog = errorLog;
	}

	public PendingOperationState getState() {
		return m_state;
	}

	public void setState(final PendingOperationState state) {
		m_state = state;
	}

	/**
	 * The UserID to be used to execute this command. This determines the ROS_ID during execution of the call.
	 * @return
	 */
	public String getUserID() {
		return m_userID;
	}

	public void setUserID(final String userID) {
		m_userID = userID;
	}

	/**
	 * A short user-readable description of the call.
	 * @return
	 */
	public String getDescription() {
		return m_description;
	}

	public void setDescription(final String description) {
		m_description = description;
	}

	/**
	 * The dotted path describing the module and location that issued this call.
	 * @return
	 */
	public String getSubmitsource() {
		return m_submitsource;
	}

	public void setSubmitsource(final String submitsource) {
		m_submitsource = submitsource;
	}

	/**
	 * The path of actions accounted for in progress of operation execution.
	 * @return
	 */
	public String getProgressPath() {
		return m_progressPath;
	}

	public void setProgressPath(String progressPath) {
		m_progressPath = progressPath;
	}

	/**
	 * Current percentage of task progress.
	 * @return
	 */
	public int getProgressPercentage() {
		return m_progressPercentage;
	}

	public void setProgressPercentage(int progressPercentage) {
		m_progressPercentage = progressPercentage;
	}

	public Object getSerializedObject() throws SQLException {
		if(m_serializedObject == null) {
			Connection dbc = getDS().getConnection();
			try {
				loadSerialized(dbc);
			} finally {
				try {
					dbc.close();
				} catch(Exception x) {}
			}
		}
		return m_serializedObject;
	}

	public void setSerializedObject(final Object serializedObject) {
		m_serializedObject = serializedObject;
	}

	/**
	 * Returns a lazily-loaded inputstream from the serialized blob.
	 * @return
	 * @throws SQLException
	 */
	public InputStream getSerializedStream() throws SQLException {
		//-- Get a stream representing the LOB data
		Connection dbc = getDS().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		InputStream is = null;
		try {
			ps = dbc.prepareStatement("select spo_serialized from sys_pending_operations where spo_id=?");
			ps.setLong(1, m_id);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("sys_pending_operations.id=" + m_id + " is not found");
			Blob b = rs.getBlob(1);
			if(b == null)
				return null;
			try {
				is = b.getBinaryStream();
				if(is == null)
					return null;
				InputStream res = new WrappedDatabaseInputStream(dbc, ps, rs, is);
				dbc = null;
				ps = null;
				rs = null;
				is = null;
				return res;
			} catch(Exception x) {
				throw new PendingOperationSerializationException("Failed to deserialize object from pendingOperation=" + m_id + ": " + x, x);
			}
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
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

	public void initFromRS(final ResultSet rs) throws SQLException {
		int f = 1;
		m_id = rs.getLong(f++);
		m_xid = rs.getString(f++);
		m_sourceServerID = rs.getString(f++);
		m_creationTime = rs.getTimestamp(f++);
		m_mustExecuteOnServerID = rs.getString(f++);
		m_executesOnServerID = rs.getString(f++);
		m_lastExecutionStart = rs.getTimestamp(f++);
		m_lastExecutionEnd = rs.getTimestamp(f++);
		m_state = PendingOperationState.valueOf(rs.getString(f++));
		m_retries = rs.getInt(f++);
		m_nextTryTime = rs.getTimestamp(f++);
		m_orderGroup = rs.getString(f++);
		m_orderTime = rs.getTimestamp(f++);
		m_orderIndex = rs.getInt(f++);
		m_type = rs.getString(f++);
		m_arg1 = rs.getString(f++);
		String pro = rs.getString(f++);
		if(pro == null)
			m_properties = null;
		else {
			m_properties = new Properties(); // Loop thru hoops to load from a string...
			try {
				ByteArrayInputStream baos = new ByteArrayInputStream(pro.getBytes("iso-8859-1"));
				m_properties.load(baos);
			} catch(IOException x) {
				x.printStackTrace();
			}
		}

		m_lastError = rs.getString(f++);
		m_errorLog = rs.getString(f++);
		m_userID = rs.getString(f++);
		m_description = rs.getString(f++);
		m_submitsource = rs.getString(f++);
	}

	public void delete(@Nonnull Connection dbc) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement("delete from sys_pending_operations where spo_id=?");
			ps.setLong(1, getId());
			ps.executeUpdate();
			setId(-1);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Saves the content of the record. Does not save the serialized LOB.
	 * @param dbc
	 * @throws SQLException
	 */
	public void save(final Connection dbc) throws SQLException {
		PreparedStatement ps = null;
		try {
			if(m_id == -1) {
				CallableStatement cs = dbc.prepareCall(m_insertSQL);
				ps = cs;
			} else
				ps = dbc.prepareStatement(m_updateSQL);

			//-- For any state other than EXEC make sure "executing_on" is empty !IMPORTANT
			if(m_state != PendingOperationState.EXEC)
				m_executesOnServerID = null;

			//-- Set all fields, in order,
			int f = 1;
			ps.setString(f++, m_xid);
			ps.setString(f++, m_sourceServerID);
			ps.setTimestamp(f++, m_creationTime == null ? null : new Timestamp(m_creationTime.getTime()));
			ps.setString(f++, m_mustExecuteOnServerID);
			ps.setString(f++, m_executesOnServerID);
			ps.setTimestamp(f++, m_lastExecutionStart == null ? null : new Timestamp(m_lastExecutionStart.getTime()));
			ps.setTimestamp(f++, m_lastExecutionEnd == null ? null : new Timestamp(m_lastExecutionEnd.getTime()));
			ps.setString(f++, m_state.name());
			ps.setInt(f++, m_retries);
			ps.setTimestamp(f++, m_nextTryTime == null ? null : new Timestamp(m_nextTryTime.getTime()));
			ps.setString(f++, m_orderGroup);
			ps.setTimestamp(f++, m_orderTime == null ? null : new Timestamp(m_orderTime.getTime()));
			ps.setInt(f++, m_orderIndex);
			ps.setString(f++, m_type);
			ps.setString(f++, m_arg1);

			String pro = null;
			if(m_properties != null && m_properties.size() != 0) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
				try {
					m_properties.store(baos, "-");

					//-- Convert bytes back to a $ (what an ass-backward interface 8-/)
					pro = new String(baos.toByteArray(), "iso-8859-1");
				} catch(IOException x) {
					x.printStackTrace(); // Impossible: should never happen.
				}
				if(pro == null || pro.length() > 4000)
					throw new IllegalStateException("Properties string is too long for PendingOperation. or it is null!?");
			}

			ps.setString(f++, pro);
			ps.setString(f++, m_lastError);
			ps.setString(f++, m_errorLog);

			ps.setString(f++, m_userID);
			ps.setString(f++, m_description);
			ps.setString(f++, m_submitsource);
			ps.setString(f++, m_progressPath);
			ps.setInt(f++, m_progressPercentage);

			if(m_id != -1)
				ps.setLong(f++, m_id); // Assign PK for update,
			else {
				CallableStatement cs = (CallableStatement) ps;
				cs.registerOutParameter(f, Types.NUMERIC);
			}

			int rc = ps.executeUpdate();
			if(rc != 1)
				throw new SQLException("Update count incorrect: " + rc);
			if(m_id == -1) {
				CallableStatement cs = (CallableStatement) ps;
				m_id = cs.getLong(f); // Get assigned ID
			}
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	private DataSource getDS() {
		if(m_dataSource == null)
			throw new IllegalStateException("No datasource known.");
		return m_dataSource;
	}

	void setDS(final DataSource ds) {
		m_dataSource = ds;
	}

	private Object loadSerialized(final Connection dbc) throws SQLException {
		if(m_serializedObject != null)
			return m_serializedObject;
		if(m_id == -1)
			throw new IllegalStateException("Cannot load a LOB from a record that is not stored");

		//-- Get a stream representing the LOB data
		PreparedStatement ps = null;
		ResultSet rs = null;
		InputStream is = null;
		try {
			ps = dbc.prepareStatement("select spo_serialized from sys_pending_operations where spo_id=?");
			ps.setLong(1, m_id);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("sys_pending_operations.id=" + m_id + " is not found");
			Blob b = rs.getBlob(1);
			if(b == null)
				return null;
			try {
				is = b.getBinaryStream();
				if(is == null)
					return null;

				//-- Deserialize
				ObjectInputStream ois = new ObjectInputStream(is);
				return m_serializedObject = ois.readObject();
			} catch(Exception x) {
				throw new PendingOperationSerializationException("Failed to deserialize object from pendingOperation=" + m_id + ": " + x, x);
			}
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
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

	public void saveStream(final Connection dbc, final InputStream is) throws SQLException, IOException {
		dbc.setAutoCommit(false);
		if(m_id == -1)
			throw new IllegalStateException("Cannot store a LOB in a record that is not stored");
		PreparedStatement ps = null;
		ResultSet rs = null;
		OutputStream os = null;
		try {
			ps = dbc.prepareStatement("update sys_pending_operations set spo_serialized=" + (is == null ? "null" : "empty_blob()") + " where spo_id=?");
			ps.setLong(1, m_id);
			int rc = ps.executeUpdate();
			if(rc != 1)
				throw new SQLException("Update count <> 1: " + rc);
			if(is == null)
				return;
			ps.close();

			//-- Must stream data there,
			ps = dbc.prepareStatement("select spo_serialized from sys_pending_operations where spo_id=? for update of spo_serialized");
			ps.setLong(1, m_id);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("Cannot re-find sys_pending_operations.id=" + m_id);
			Blob tb = rs.getBlob(1);
			os = (OutputStream) ClassUtil.callObjectMethod(tb, "getBinaryOutputStream", new Class< ? >[0]);
			if(null == os)
				throw new PendingOperationSerializationException("Failed to serialize object from pendingOperation=" + m_id + ": cannot get blob output stream");

			try {
				FileTool.copyFile(os, is);
				os.close();
			} catch(Exception x) {
				throw new PendingOperationSerializationException("Failed to serialize object from pendingOperation=" + m_id + ": " + x, x);
			}
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
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

	void saveSerialized(final Connection dbc) throws SQLException {
		dbc.setAutoCommit(false);
		if(m_id == -1)
			throw new IllegalStateException("Cannot store a LOB in a record that is not stored");
		PreparedStatement ps = null;
		ResultSet rs = null;
		OutputStream os = null;
		try {
			ps = dbc.prepareStatement("update sys_pending_operations set spo_serialized=" + (m_serializedObject == null ? "null" : "empty_blob()") + " where spo_id=?");
			ps.setLong(1, m_id);
			int rc = ps.executeUpdate();
			if(rc != 1)
				throw new SQLException("Update count <> 1: " + rc);
			if(m_serializedObject == null)
				return;
			ps.close();

			//-- Must stream data there,
			ps = dbc.prepareStatement("select spo_serialized from sys_pending_operations where spo_id=? for update of spo_serialized");
			ps.setLong(1, m_id);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("Cannot re-find sys_pending_operations.id=" + m_id);
			Blob tb = rs.getBlob(1);
			os = (OutputStream) ClassUtil.callObjectMethod(tb, "getBinaryOutputStream", new Class< ? >[0]);
			try {
				ObjectOutputStream oos = new ObjectOutputStream(os);
				oos.writeObject(m_serializedObject);
				oos.close();
			} catch(Exception x) {
				throw new PendingOperationSerializationException("Failed to serialize object from pendingOperation=" + m_id + ": " + x, x);
			}
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
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

	static {
		StringBuilder sb = new StringBuilder(512);
		sb.append("begin ");
		StringTool.createInsertStatement(sb, "sys_pending_operations", "spo_id", "sys_spo_seq.nextval", FLDAR);
		sb.append(" returning spo_id into ?; end;");
		m_insertSQL = sb.toString();

		sb.setLength(0);
		StringTool.createUpdateStatement(sb, "sys_pending_operations", "spo_id", FLDAR);
		m_updateSQL = sb.toString();
	}

	public void setError(final PendingOperationState rtry, final String string) {
		m_state = rtry;
		m_lastError = StringTool.strTrunc(string, 250);
	}
}
