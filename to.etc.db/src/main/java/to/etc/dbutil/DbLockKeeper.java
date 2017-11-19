/*
 * DomUI Java User Interface - shared code
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
package to.etc.dbutil;

import java.sql.*;
import java.util.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;
import javax.sql.*;

import to.etc.dbpool.*;

@ThreadSafe
public final class DbLockKeeper {

	private DataSource m_dataSource;

	private final static DbLockKeeper M_INSTANCE = new DbLockKeeper();

	private static final String TABLENAME = "SYS_SERVER_LOCKS";

	@GuardedBy("this")
	private static final Map<LockThreadKey, Lock> M_MAINTAINED_LOCKS = new HashMap<LockThreadKey, Lock>();

	public synchronized static DbLockKeeper getInstance() {
		if(M_INSTANCE.m_dataSource == null) {
			throw new RuntimeException("DbLockKeeper not yet initialized");
		}
		return M_INSTANCE;
	}

	private DbLockKeeper() {}


	/**
	 * Initializes the DbLockKeeper. Creates the required tables and sets the datasource. Should be called before the first use of this class.
	 * @param ds the datasource used to create the connections.
	 */
	public synchronized static void init(DataSource ds) {
		if(M_INSTANCE.m_dataSource != null) {
			throw new RuntimeException("DbLockKeeper is already initialized.");
		}
		M_INSTANCE.m_dataSource = ds;
		PreparedStatement ps = null;
		Connection dbc = null;
		try {
			dbc = ds.getConnection();
			dbc.setAutoCommit(false);
			ps = dbc.prepareStatement("create table " + TABLENAME + " ( LOCK_NAME varchar(60) not null primary key)");
			ps.executeUpdate();
			dbc.commit();
		} catch(Exception x) {
			//Exception ignored, Table is always created, fails when already present.
		} finally {
			try {
				if(ps != null)
					ps.close();
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}

	}

	/**
	 * Method should be used to create a lock. It can be used to make sure that certain processes won't run at the same time
	 * on multiple servers. The method won't finish until lock is given.
	 *
	 * IMPORTANT
	 * The lock must be released after execution of the code.
	 *
	 * @param lockName the name of the lock
	 * @throws Exception
	 */
	public LockHandle lock(final String lockName) throws Exception {
		LockThreadKey key = new LockThreadKey(lockName, Thread.currentThread());
		Lock lock;
		synchronized(this) {
			lock = M_MAINTAINED_LOCKS.get(key);
			if(lock != null) {
				return new LockHandle(lock);
			}
		}

		Connection dbc = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			dbc = m_dataSource.getConnection();
			PoolManager.setLongLiving(dbc);
			dbc.setAutoCommit(false);
			insertLock(lockName, dbc);

			ps = dbc.prepareStatement("select lock_name from " + TABLENAME + " where lock_name = '" + lockName + "' for update");
			rs = ps.executeQuery();
			if(!rs.next()) {
				throw new Exception("Lock with name: " + lockName + " not acquired");
			}
			lock = new Lock(this, lockName, dbc);
			LockHandle lh = new LockHandle(lock);
			synchronized(this) {
				M_MAINTAINED_LOCKS.put(key, lock);
			}
			dbc = null; // Release ownership.
			return lh;
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

	/**
	 * Get a lock, but do not wait for it- if the lock is taken the code
	 * returns immediately, returning a null lock handle.
	 *
	 * @param lockName
	 * @return
	 * @throws Exception
	 */
	@Nullable
	public LockHandle lockNowait(final String lockName) throws Exception {
		LockThreadKey key = new LockThreadKey(lockName, Thread.currentThread());
		Lock lock;

		//-- Recursive call into same lock? Then return a new lock handle incrementing the use count.
		synchronized(this) {
			lock = M_MAINTAINED_LOCKS.get(key);
		}
		if(lock != null) {
			return new LockHandle(lock);
		}

		Connection dbc = m_dataSource.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			dbc.setAutoCommit(false);
			PoolManager.setLongLiving(dbc);
			insertLock(lockName, dbc);

			ps = dbc.prepareStatement("select lock_name from " + TABLENAME + " where lock_name = '" + lockName + "' for update nowait");
			rs = ps.executeQuery();
			if(!rs.next()) {
				return null;
			}
			lock = new Lock(this, lockName, dbc);
			LockHandle lh = new LockHandle(lock);
			synchronized(this) {
				M_MAINTAINED_LOCKS.put(key, lock);
			}
			dbc = null; // Release ownership.
			return lh;
		} catch(SQLException sx) {
//			System.out.println("Errcode=" + sx.getErrorCode() + ", state=" + sx.getSQLState());
			String msg = sx.getMessage().toLowerCase();
			if(msg.contains("NOWAIT") || msg.contains("ora-00054") || msg.contains("lock"))
				return null;
			throw sx;
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


	private synchronized void releaseLock(String lockName) {
		LockThreadKey key = new LockThreadKey(lockName, Thread.currentThread());
		Lock lock = M_MAINTAINED_LOCKS.remove(key);
		if(lock == null || !lock.isClosed()) {
			throw new IllegalStateException("Lock with name:" + lockName + " has already been closed");
		}
	}

	/**
	 * Tries to insert the lock in the database. Ignores exceptions.
	 * @param lockName the name of the used lock
	 * @param dbc Connection to use
	 */
	private void insertLock(final String lockName, final Connection dbc) {
		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement("insert into " + TABLENAME + " (lock_name) values('" + lockName + "')");
			ps.executeUpdate();
			dbc.commit();
		} catch(Exception e) {
			//Exception ignored, LockName is always inserted, fails when already present.
		} finally {
			if(ps != null)
				try {
					ps.close();
				} catch(SQLException e) {}

			//-- Postgresql needs rollback or all other statements will fail.
			try {
				dbc.rollback();
			} catch(Exception x) {}
		}
	}

	/**
	 * Class to function as a key in the maintained locks map of the outer class.
	 */
	private static final class LockThreadKey {
		private String m_lockName;

		private Thread m_thread;

		public LockThreadKey(String lockName, Thread thread) {
			m_lockName = lockName;
			m_thread = thread;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((m_lockName == null) ? 0 : m_lockName.hashCode());
			result = prime * result + ((m_thread == null) ? 0 : m_thread.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			LockThreadKey other = (LockThreadKey) obj;
			if(m_lockName == null) {
				if(other.m_lockName != null)
					return false;
			} else if(!m_lockName.equals(other.m_lockName))
				return false;
			if(m_thread == null) {
				return other.m_thread == null;
			} else
				return m_thread.equals(other.m_thread);
		}

	}

	/**
	 * Class keeps an lock on the database. Only handles to this lock will be
	 *  distibuted to classes that require a database lock. When all handle are
	 *  released the lock is also released.
	 */
	private static final class Lock {
		private Connection m_lockedConnection;

		@GuardedBy("m_keeper")
		private int m_lockCounter;

		private String m_lockName;

		private DbLockKeeper m_keeper;

		public Lock(DbLockKeeper keeper, String lockName, Connection lockedConnection) {
			m_lockedConnection = lockedConnection;
			m_lockName = lockName;
			m_keeper = keeper;
		}

		public boolean isClosed() {
			return m_lockedConnection == null;
		}

		@SuppressWarnings("synthetic-access")
		public void release() {
			synchronized(m_keeper) {
				m_lockCounter--;
				if(m_lockCounter == 0) {
					try {
						m_lockedConnection.rollback();
					} catch(SQLException x) {
						//-- jal 20110821 Should only occur on real database trouble - log it but ignore, there is nothing we can do.
						x.printStackTrace();
					} finally {
						try {
							//-- jal 20110821 symmetry: should move to releaseLock method.
							m_lockedConnection.close();
						} catch(Exception x) {}
						m_lockedConnection = null;
					}
					m_keeper.releaseLock(m_lockName);
				}
			}
		}

		void increaseCounter() {
			synchronized(m_keeper) {
				m_lockCounter++;
			}
		}

	}

	/**
	 * Handle for a specific lock. Multiple handles can be distributed for a single lock.
	 * This will only be the case when a lock is asked for the same thread multiple times.
	 */
	public static final class LockHandle {
		private Lock m_lock;

		private boolean m_released;

		public LockHandle(Lock lock) {
			m_lock = lock;
			lock.increaseCounter();
		}

		/**
		 * If this handle is the last/only handle for a lock the lock is released.
		 * @throws Exception when exception with releasing the lock occurs.
		 */
		public void release() {
			if(m_released) // jal 20110821 Explicitly allow mutiple releases- better than not releasing at all.
				return;
			m_lock.release();
			m_released = true;
		}
	}
}
