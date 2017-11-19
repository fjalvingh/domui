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
 * This polled task provider checks for tasks to execute in the sys_pending_operations table. It
 * handles all polling chores and properly handles all order requirements for pending operations. Operations
 * here are checked only every 5 minutes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 4, 2009
 */
public class PendingOperationTaskProvider implements IPollQueueTaskProvider {
	static private PendingOperationTaskProvider m_instance = new PendingOperationTaskProvider();

	private PollingWorkerQueue m_executor;

	private DataSource m_ds;

	private String m_serverID;

	/** The single-thread usage baton. */
	private boolean m_inUse;

	private int m_lastSelectedIndex;

	private long m_tsNextCheck;

	private long m_tsNextCleanup;

	private List<IPendingOperationListener> m_listeners = Collections.EMPTY_LIST;

	//	private PendingOperationTaskProvider(final DataSource ds, final String serverID) {
	//		m_ds = ds;
	//		m_serverID = serverID;
	//	}
	private PendingOperationTaskProvider() {}

	/**
	 * Initializes this thing, and adds it to the worker queue handler.
	 * @param serverID
	 */
	static public void initialize(final DataSource ds, final String serverID) {
		m_instance.internalInitialize(ds, serverID);
	}

	private synchronized void internalInitialize(final DataSource ds, final String serverID) {
		if(m_executor != null)
			throw new IllegalStateException("Attempt to re-initialize");
		m_serverID = serverID;
		m_ds = ds;
		PollingWorkerQueue.getInstance().registerProvider(this);
		m_executor = PollingWorkerQueue.getInstance();
	}

	static public PendingOperationTaskProvider getInstance() {
		return m_instance;
	}

	public synchronized void addListener(IPendingOperationListener l) {
		m_listeners = new ArrayList<IPendingOperationListener>(m_listeners);
		m_listeners.add(l);
	}

	public synchronized void removeListener(IPendingOperationListener l) {
		m_listeners = new ArrayList<IPendingOperationListener>(m_listeners);
		m_listeners.remove(l);
	}

	synchronized List<IPendingOperationListener> getListeners() {
		return m_listeners;
	}

	@Override
	public void initializeOnRegistration(final PollingWorkerQueue pwq) throws Exception {
		m_executor = pwq;

		//-- Release any "old" state (server has died)
		Connection dbc = m_ds.getConnection();
		PreparedStatement ps = null;
		try {
			//-- All jobs executing on me while I died need a reschedule.
			ps = dbc
				.prepareStatement("update sys_pending_operations set spo_executing_server=null,spo_state='RTRY',spo_retries=spo_retries+1,spo_lasterror='Server has died' where spo_executing_server=?");
			ps.setString(1, m_serverID);
			ps.executeUpdate();
			ps.close();
			dbc.commit();

			//-- We just rebooted, so reallow BOOT type.
			ps = dbc.prepareStatement("update sys_pending_operations set spo_executing_server=null,spo_state='RTRY',spo_retries=spo_retries+1 where spo_state='BOOT'");
			ps.executeUpdate();
			ps.close();
			dbc.commit();

			//-- Make sure no RTRY instances have spo_executing_server set to anything.
			ps = dbc.prepareStatement("update sys_pending_operations set spo_executing_server=null where spo_executing_server is not null and spo_state='RTRY'");
			int rc = ps.executeUpdate();
			if(rc != 0)
				System.out.println("pwq: ERROR: found " + rc + " pending operations in RTRY state with executing_server set to non-null!???");
			ps.close();
			dbc.commit();

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

	/**
	 * This checks for a new pending operation to execute. Only one thread can use this
	 * operation at a time. The first thread that enters this method obtains the baton
	 * and is allowed to continue. Other threads entering see that the baton is used and
	 * exit immediately, without a task.
	 *
	 * @see to.etc.webapp.pendingoperations.IPollQueueTaskProvider#getRunnableTask()
	 */

	@Override
	public Runnable getRunnableTask() throws Exception {
		long cts = System.currentTimeMillis();
		boolean cleanup = false;
		synchronized(this) {
			if(m_inUse || cts < m_tsNextCheck) { // Not yet time to check again?
			//				System.out.println("potp: no need to scan for PendingOperation");
				return null;
			}
			m_inUse = true;

			if(cts >= m_tsNextCleanup) {
				cleanup = true;
				m_tsNextCleanup = 4 * 60 * 60 * 1000; // Cleanup every 4 hours
			}
		}
		if(cleanup)
			cleanupDatabase();

		//-- We own this now. Handle the thread;
		try {
			//			System.out.println("potp: scanning for next task...");
			Runnable task = findBestTask();
			//			System.out.println("potp: got task="+task);
			if(task != null) {
				m_executor.checkProvider(this); // Notify, allowing another thread to check for task actions too.
			}
			return task;
		} finally {
			synchronized(this) { // Make sure the baton is released all the time
				m_inUse = false;
			}
		}
	}

	/**
	 * Checks whether it's time to cleanup the database.
	 */
	private void cleanupDatabase() {
		Connection dbc = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			dbc = m_ds.getConnection();
			Timestamp ts1 = new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // 1 day ago
			Timestamp ts2 = new Timestamp(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000); // 7 days ago

			ps = dbc.prepareStatement("delete from sys_pending_operations" + " where (spo_date_created < ?)" // older than ts2 removed unconditionally
				+ " or (spo_date_created < ? and spo_state = 'DONE')" // Completed thingies removed within a day.
			);
			ps.setTimestamp(1, ts2);
			ps.setTimestamp(2, ts1);
			ps.executeUpdate();
			dbc.commit();
		} catch(Exception x) {
			System.err.println("PendingOperations: EXCEPTION while cleaning up the database: " + x);
			x.printStackTrace();
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
					dbc.rollback();
			} catch(Exception x) {}
			try {
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Opens sys_pending_operations in LOCK mode, then allocates the next-task(set)-to-run from it.
	 * @return
	 */
	private Runnable findBestTask() throws Exception {
		Connection dbc = m_ds.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		//		long ts	= System.nanoTime();
		try {
			Date now = new Date();
			dbc.setAutoCommit(false); // Make very certain stuff's not commited.
			ps = dbc.prepareStatement("select " + PendingOperation.FIELDS + " from sys_pending_operations" + " where spo_executing_server is null" // Only if not already executing
				+ " and (spo_must_execute_on_server is null or spo_must_execute_on_server=?)" // Free or for this server
				+ " and spo_state='RTRY'" // Not failed
				+ " and (spo_date_next_try is null or spo_date_next_try <= ?)" // Time to try next has been exceeded
				+ " order by spo_id" + " for update" // FORCE LOCK while scanning/editing
			);
			ps.setString(1, m_serverID);
			ps.setTimestamp(2, new Timestamp(now.getTime()));
			rs = ps.executeQuery();
			List<PendingOperation> ack = new ArrayList<PendingOperation>();
			while(rs.next()) {
				PendingOperation po = new PendingOperation();
				po.initFromRS(rs); // Get all fields.
				ack.add(po);
			}
			rs.close();
			//			System.out.println("potp: pending task scan resulted in "+ack.size()+" pending operations to consider");

			//-- FIXME Integrate the code below with the select instead of reading everything in memory.
			//-- Find the first BEST task to execute, starting at the "last index",
			int todo = ack.size();
			List<PendingOperation> resultlist = null;
			for(;;) {
				if(todo-- <= 0) {
					//-- Could not allocate job- exit and try again in x minutes.
					synchronized(this) {
						m_tsNextCheck = System.currentTimeMillis() + 1 * 60 * 1000; // Every 5 minutes if queue is empty
					}
					dbc.commit();
					return null;
				}
				m_lastSelectedIndex++;
				if(m_lastSelectedIndex >= ack.size())
					m_lastSelectedIndex = 0;
				PendingOperation po = ack.get(m_lastSelectedIndex);
				if(po.getOrderGroup() == null) { // Not a group-> always claimable
					resultlist = new ArrayList<PendingOperation>();
					resultlist.add(po);
					break;
				}

				//-- Load all other members in the group, and check if they are runnable/complete
				List<PendingOperation> grouplist = loadGroup(dbc, po);
				if(grouplist != null && grouplist.size() > 0) {
					resultlist = grouplist;
					break;
				}

				//-- Thingy is invalid- continue.
			}

			//-- We have a thing to run. Mark it and all other members of the group as EXECUTING, then return it;
			markTasksExecuting(dbc, resultlist); // Mark all of these as EXECUTING,
			dbc.commit();

			//-- Return a grouplist runnable for this thing.
			return new PendingOperationTask(this, resultlist);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				dbc.rollback();
			} catch(Exception x) {}
			try {
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
			//			ts	= System.nanoTime() - ts;
			//			System.out.println("PendingOperationProvider: scan took "+StringTool.strNanoTime(ts));
		}
	}

	/**
	 * Marks all of the tasks specified as "locked by server", and mark the 1st one as "EXECUTING".
	 * @param dbc
	 * @param polist
	 * @throws SQLException
	 */
	private void markTasksExecuting(final Connection dbc, final List<PendingOperation> polist) throws SQLException {
		int ix = 0;
		for(PendingOperation po : polist) {
			po.setState(PendingOperationState.EXEC); 			// All of these are EXECUTING -> they are to be done by this server(!)
			po.setExecutesOnServerID(m_serverID); 				// So, claim them as owned by THIS server
			if(ix++ == 0) {
				//-- Only the first one we'll execute immediately, so mark that one as started.
				po.setLastExecutionStart(new Date());
				po.setLastExecutionEnd(null);
				po.setRetries(po.getRetries() + 1);				// Increment runcount
				po.setLastError(null);							// Clear error while running,
			}
			po.save(dbc);
		}
	}

	/**
	 * Loads a group, and checks to see if it's executable. This is the case if all members of the group
	 * can be executed or are retryable, and if the first group member to execute has met it's contained time.
	 *
	 * @param dbc
	 * @param inpo
	 * @return The list, of which the 1st member is valid, or null if the group cannot run.
	 * @throws SQLException
	 */
	@Nullable
	private List<PendingOperation> loadGroup(final Connection dbc, final PendingOperation inpo) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc.prepareStatement("select " + PendingOperation.FIELDS + " from sys_pending_operations" + " where spo_order_groupname=?" // defines group
				+ " and spo_state in ('RTRY','EXEC','FATL','BOOT') " //
				+ " order by spo_order_timestamp, spo_order_sub" // defines order
				+ " for update");

			ps.setString(1, inpo.getOrderGroup());
			rs = ps.executeQuery();
			List<PendingOperation> res = new ArrayList<PendingOperation>();
			while(rs.next()) {
				PendingOperation po = new PendingOperation();
				po.initFromRS(rs);
				res.add(po);
			}

			/*
			 * Loop through all members, and
			 */
			//-- The first member in this list must be executable at this time, or the group is invalid.
			for(;;) {
				if(res.size() == 0)
					return null;
				PendingOperation op = res.get(0);
				if(op.getState() == PendingOperationState.EXEC)				// If already executing somewhere else-> exit.
					return null;

				if(op.getState() == PendingOperationState.BOOT || op.getState() == PendingOperationState.FATL) {
					/*
					 * These types can hold up the whole group, so make sure that this is what's needed by asking the provider...
					 */
					IPendingOperationExecutor pox = findExecutor(op);
					if(null == pox)											// We should find one, but do not abort at this level
						return null;
					if(!(pox instanceof IPendingOperationExecutor2))		// No way to know if skipping the failed one is allowed?
						return null;
					IPendingOperationExecutor2 px2 = (IPendingOperationExecutor2) pox;
					if(!px2.isSkipFailedAllowed(op))						// We're not allowed to run this group -> skip it.
						return null;

					//-- We may skip this member and continue. To prevent reloading this same member over and over again delete it now.
					op.delete(dbc);											// Delete this (will be committed by mainloop)
					res.remove(0);											// Remove it from the list and retry all of this with the next member in the group
					continue;
				}
				if(op.getMustExecuteOnServerID() != null && !op.getMustExecuteOnServerID().equals(m_serverID))
					return null;
				if(op.getExecutesOnServerID() != null)
					return null;
				if(op.getNextTryTime() != null && op.getNextTryTime().getTime() < new Date().getTime())
					return null;

				return res;
			}
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

	Connection allocateConnection() throws SQLException {
		Connection dbc = m_ds.getConnection();
		dbc.setAutoCommit(false);
		return dbc;
	}

	DataSource getDataSource() {
		return m_ds;
	}

	String getServerID() {
		return m_serverID;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Pending operation types.							*/
	/*--------------------------------------------------------------*/
	/** Map of ops_type to executor for that type. */
	private final Map<String, IPendingOperationExecutor> m_typeMap = new HashMap<String, IPendingOperationExecutor>();

	/**
	 * Register a pending operation type and it's executor.
	 * @param type
	 * @param pox
	 */
	public void registerPendingOperationType(final String type, final IPendingOperationExecutor pox) {
		if(null != m_typeMap.put(type.toLowerCase(), pox))
			throw new IllegalStateException("Duplicate PendingOperation.type=" + type);
	}

	/**
	 * Find an executor for a given pendingOperation type.
	 * @param po
	 * @return
	 */
	public IPendingOperationExecutor findExecutor(final PendingOperation po) {
		return m_typeMap.get(po.getType().toLowerCase());
		//
		//		if("SOAP".equals(po.getType())) {
		//			return new PendingFullSOAPCallExecutor(po, ls);
		//		}
		//		return null;
	}

	/**
	 * Store a PendingOperation in the table, or die.
	 * @param po
	 * @param sis
	 */
	public void saveOperation(final PendingOperation po, final StringInputStream sis) throws Exception {
		Connection dbc = allocateConnection();
		try {
			po.setSourceServerID(m_serverID);
			dbc.setAutoCommit(false);
			po.save(dbc);
			po.saveStream(dbc, sis);
			dbc.commit();

			synchronized(this) {
				m_tsNextCheck = 0;
				notify();
			}
		} finally {
			try {
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Store a PendingOperation in the table, or die.
	 * @param po
	 * @param sis
	 */
	public void saveOperation(final PendingOperation po, final Serializable object) throws Exception {
		Connection dbc = allocateConnection();
		try {
			po.setSourceServerID(m_serverID);
			if(DeveloperOptions.getBool("domui.developer", false))
				po.setMustExecuteOnServerID(m_serverID);
			dbc.setAutoCommit(false);
			po.save(dbc);
			po.setSerializedObject(object);
			po.saveSerialized(dbc);
			dbc.commit();
			synchronized(this) {
				m_tsNextCheck = 0;
				notify();
			}
		} finally {
			try {
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}
	}

	/*--------------------------------------------------------------------------*/
	/*	CODING:	Pending operation external identifier and operation	progress	*/
	/*--------------------------------------------------------------------------*/

	/**
	 * Updates the current progress of a given pending operation.
	 * @param po
	 * @throws SQLException
	 */
	protected void updateProgress(final PendingOperation po) throws SQLException {
		Connection dbc = allocateConnection();
		try {
			po.save(dbc);
			dbc.commit();
		} finally {
			FileTool.closeAll(dbc);
		}
	}

	/**
	 * Gets current progress of a scheduled or running calculation of a given scenario (externalId)
	 * @param externalId
	 * @return
	 * @throws SQLException
	 */
	public PendingJobProgressInfo getCurrentProgress(final String externalId) throws SQLException {
		Connection dbc = allocateConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc.prepareStatement("select progress_path, progress_percentage from sys_pending_operations" + " where spo_xident=?" + " and (spo_state = ? or spo_state = ?)");//

			ps.setString(1, externalId);
			ps.setString(2, PendingOperationState.RTRY.name());
			ps.setString(3, PendingOperationState.EXEC.name());

			rs = ps.executeQuery();

			if(rs.next()) {
				PendingJobProgressInfo pi = new PendingJobProgressInfo(rs.getString(1), rs.getInt(2));
				return pi;
			}
			return null;
		} finally {
			FileTool.closeAll(rs, ps, dbc);
		}
	}

	/**
	 * Checks to see if there already is scheduled or running calculation of a given scenario (externalId).
	 * @param externalId
	 * @return
	 * @throws SQLException
	 */
	public boolean isBusyWithJob(final String externalId) throws SQLException {
		return (getCurrentProgress(externalId) != null);
	}

}
