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

import javax.annotation.*;

import to.etc.util.*;

/**
 * This is a runnable which tries to execute one or more PendingOperations. This only executes multiple
 * PendingOperations if these operations belong to the same group. In that case it handles group state
 * transitions in the database with all of the group records locked, to prevent multiserver trouble.
 * If any of the PendingOperations fails the failed one is marked as such and scheduled for retry; all
 * others in the group are released without being processed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 5, 2009
 */
final public class PendingOperationTask implements Runnable, ILogSink {
	private final PendingOperationTaskProvider m_provider;

	private final List<PendingOperation> m_list;


	public PendingOperationTask(final PendingOperationTaskProvider provider, final List<PendingOperation> list) {
		m_provider = provider;
		m_list = list;
	}

	/**
	 * Loop through all pendingOperations and execute them one by one. Aborts as soon as an operation fails.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		int lsz = m_list.size() + 1;
		try {
			while(m_list.size() > 0) {
				Set<PendingOperation> upset = new HashSet<PendingOperation>();

				if(lsz == m_list.size())
					throw new IllegalStateException("LOOP DETECT");
				lsz = m_list.size();
				PendingOperation op = m_list.remove(0);			// Get the 1st thing to do, and remove it from the worklist.
				upset.add(op);
				runOperation(op); 								// Run this thing and mark it's state after execution.

				//-- Check some things and update the finished operation
				if(op.getState() == PendingOperationState.EXEC)
					throw new IllegalStateException("Still in state EXEC after run");
				if(op.getLastExecutionEnd() == null)
					op.setLastExecutionEnd(new java.util.Date());
				op.setExecutesOnServerID(null);					// Finished execution

				if(op.getErrorLog() != null) {
					op.setErrorLog(op.getErrorLog() + "\n" + m_logWriter.getBuffer().toString());
				} else
					op.setErrorLog(m_logWriter.getBuffer().toString());

				//-- If the last operation failed we cannot continue with the rest of the group either, unless it is specifically allowed by the provider...
				PendingOperation deleteme = null;
				if(op.getState() != PendingOperationState.DONE) {				// Op failed?
					boolean fail = true;

					//-- Ask the provider if we may continue regardless of this.
					try {
						IPendingOperationExecutor pox = m_provider.findExecutor(op);
						if(null != pox) {										// We should find one, but do not abort at this level
							if(pox instanceof IPendingOperationExecutor2) {		// No way to know if skipping the failed one is allowed?
								IPendingOperationExecutor2 px2 = (IPendingOperationExecutor2) pox;
								if(px2.isSkipFailedAllowed(op)) {				// We're not allowed to run this group -> skip it.
									fail = false;
									deleteme = op;
								}
							}
						}
					} catch(Exception x) {
						//-- Ignore: treat any exception here as fail.
						x.printStackTrace();
					}

					if(fail) {
						//-- We need to FAIL the group, preventing execution. Unmark all other group operations, and set them to the same status and time as the failed one
						for(PendingOperation opo : m_list) {
							opo.setState(op.getState());
							opo.setNextTryTime(op.getNextTryTime());
							opo.setExecutesOnServerID(null);
							upset.add(opo);
						}

						//-- Update the DB
						handleDatabaseUpdate(upset, null);						// Update everything
						return;
					}
				}

				//-- We can continue, provided there's something to do ;-) We need to update the finished thing, and claim the next thing if there is one in one atomic database op.
				PendingOperation nextop = null;
				if(m_list.size() > 0) {
					nextop = m_list.get(0);

					//-- Mark the next operation as executing.
					nextop.setLastExecutionStart(new java.util.Date());			// Will start now,
					nextop.setLastExecutionEnd(null);							// Has not terminated yet
					nextop.setState(PendingOperationState.EXEC);				// Is executing
					nextop.setLastError(null);									// No error message when re-executing
					if(!m_provider.getServerID().equals(nextop.getExecutesOnServerID()))
						throw new IllegalStateException("Next pendingOp not allocated to run on THIS server!?");
					upset.add(nextop);
				}

				handleDatabaseUpdate(upset, deleteme); 							// Handle database chores,
			}
		} catch(Exception x) {
			x.printStackTrace(); // UNEXPECTED EXCEPTION!?
		}
	}

	/**
	 * Atomically updates the database status for the last job that executed AND, if present, the next job in the group
	 * to execute.
	 * @param finishedop
	 * @param nextop
	 * @throws Exception
	 */
	private void handleDatabaseUpdate(@Nonnull Set<PendingOperation> updateset, @Nullable PendingOperation deleteme) throws Exception {
		if(updateset.size() == 0)
			return;

		Connection dbc = m_provider.allocateConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//-- Lock records to change
			StringBuilder sb = new StringBuilder(128);
			sb.append("select * from sys_pending_operations where spo_id in (");
			int ct = 0;
			for(PendingOperation po : updateset) {
				if(ct++ > 0)
					sb.append(",");
				sb.append(po.getId());
			}
			sb.append(") for update");
			ps = dbc.prepareStatement(sb.toString());
			rs = ps.executeQuery();
			rs.close();
			if(deleteme != null) {
				deleteme.delete(dbc);
			}
			updateset.remove(deleteme);

			for(PendingOperation po : updateset) {
				po.save(dbc);
			}
			dbc.commit();
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
				dbc.close();
			} catch(Exception x) {}
		}
	}

	/** This will contain the written log from the task. */
	private StringWriter m_logWriter;

	/** This is the wrapper for easy output to the logging writer. */
	private PrintWriter m_errorWriter;

	@Override
	public void exception(final @Nonnull Throwable t, final @Nonnull String msg) {
		m_errorWriter.println("[exception] " + msg);
		t.printStackTrace(m_errorWriter);
		System.err.println("[pending task exception] " + msg);
	}

	@Override
	public void log(final @Nonnull String msg) {
		m_errorWriter.println("[log] " + msg);
	}

	/**
	 * Fully executes the specified operation using it's assigned factory.
	 * @param po
	 */
	private void runOperation(final PendingOperation po) {
		//-- 1. Allocate an output channel for logging and errors,
		if(m_logWriter == null) {
			m_logWriter = new StringWriter(8192);
		} else {
			m_logWriter.getBuffer().setLength(0);
		}
		m_errorWriter = new PrintWriter(m_logWriter);
		po.setDS(m_provider.getDataSource());

		//-- 2. Allocate the appropriate executor from the pendingOperation record.
		po.setState(PendingOperationState.EXEC);
		Throwable errx = null;
		try {
			m_errorWriter.println("Execute #" + po.getRetries() + " starting, at " + po.getLastExecutionStart() + " on server " + po.getExecutesOnServerID());
			IPendingOperationExecutor pox = m_provider.findExecutor(po);
			if(pox == null) {
				String msg = "Internal error: cannot find an executor for operation=" + po.getId() + ", type=" + po.getType() + ", arg1=" + po.getArg1();
				m_errorWriter.println(msg);
				po.setLastError(StringTool.strTrunc(msg, 250));
				po.setState(PendingOperationState.BOOT); 		// Missing factory only retryable after system restart
				po.setExecutesOnServerID(null);
				return;
			}

			//-- Run all BEFORE listeners.
			for(IPendingOperationListener pol : m_provider.getListeners())
				pol.beforeOperation(po);

			//-- Prepare to execute. Clear some flags in the PendingOperations thingy so I can see what gets altered.
			po.setNextTryTime(null);
			pox.executePendingOperation(po, this);

			//-- Handle the result, if needed.
			if(po.getState() == PendingOperationState.EXEC) {
				//-- State not changed.... Assume DONE in this case.
				m_errorWriter.println("*warning: the pending operation did not clear the run state - assuming the execution was succesful");
				po.setState(PendingOperationState.DONE);
			} else if(po.getState() == PendingOperationState.RTRY) {
				//-- Retry needed: is a valid retry date set?
				if(po.getNextTryTime() == null) {
					po.setNextTryTime(new java.util.Date(System.currentTimeMillis() + waitTimeFor(po.getRetries())));
				}
			}

		} catch(Exception inx) {
			Throwable x = inx instanceof WrappedException ? inx.getCause() : inx;
			errx = x;

			//-- All exceptions here are unexpected..
			if(po.getState() == PendingOperationState.EXEC) 		// By default exceptions are fatal, unless the task itself set an alternative.
				po.setState(PendingOperationState.FATL);
			if(po.getState() == PendingOperationState.RTRY)
				po.setNextTryTime(new java.util.Date(System.currentTimeMillis() + waitTimeFor(po.getRetries())));

			//-- Report the trouble,
			m_errorWriter.println("Unexpected EXCEPTION: " + x);
			x.printStackTrace(m_errorWriter);
			m_errorWriter.println("Execution completed UNSUCCESFULLY with state=" + po.getState());
			po.setLastError(StringTool.strTrunc(x.toString(), 250));
		} finally {
			//-- Call all exit listeners
			for(IPendingOperationListener pol : m_provider.getListeners()) {
				try {
					pol.afterOperation(po, errx);
				} catch(Exception x) {
					m_errorWriter.println("Listener " + pol + " failed with " + x);
					x.printStackTrace(m_errorWriter);
				}
			}
		}
	}

	private long waitTimeFor(final int runtimes) {
		if(runtimes < 4)
			return 60000; // Retry every minute
		if(runtimes < 8)
			return 10 * 60 * 1000; // 4..7 retries: every 10 minutes
		if(runtimes < 12)
			return 60 * 60 * 1000; // 8..11 retries every hour
		if(runtimes < 20)
			return 24l * 60 * 60 * 1000l; // once a day.
		return Long.MAX_VALUE; // Never again.
	}

}
