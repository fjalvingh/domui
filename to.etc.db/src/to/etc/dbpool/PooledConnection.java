package to.etc.dbpool;

import java.sql.*;
import java.util.*;

import to.etc.dbpool.stats.*;
import to.etc.dbutil.*;

/**
 *	This is the proxy class for a database connection. It implements Connection
 *  and links to both a real database connection AND a PoolEntry class. The
 *  poolentry contains the cached reference and all administrative data, and
 *  it is the thing that is saved. This proxy class will be made unusable as
 *  soon as close() is called. This prevents problems when a using class keeps
 *  a reference to a Connection when it's been closed and has been assigned to
 *  another class.
 *
 *	Holds a single pooled NEMA connection. A Nema pooled connection will be
 *  reclaimed by the NEMA context as soon as the context is left (i.e. when the
 *  request is completed). All resources associated with this connection are
 *  released also (ResultSets, Statements)..
 *
 *	A Nema pooled connection is usually OWNED by the NemaContext it has been
 *  obtained from. The connection is released by the NemaContext as soon as the
 *  NemaContext is released by the template server.
 *
 *	Associated with the pooled connection is an error status. If this connection
 *  has had too many errors it will close itself and allow a new connection to
 *  be made.
 *
 *	The connection keeps a table of all resources obtained from it (Statements,
 *  PreparedStatements and the like). As soon as the Connection gets closed all
 *  derived things get closed also. All resources are kept in a weak reference,
 *  so that if the object was already released it is not kept from garbage
 *  collection.
 *
 *	This connection will later be used to handle prepared statement caching.
 *
 *	$Header$
 */
public class PooledConnection implements Connection {
	/** The Pool Entry for this connection; it can NEVER be NULL!!! */
	protected ConnectionPoolEntry m_pe;

	/** The thread who owns this connection. Always filled-in */
	private Thread m_owner_thread;

	/** T if this is a Thread connection. */
	private boolean m_isthread;

	/** T if this connection is in AutoCommit mode. */
	private boolean m_autocommit;

	/** The reason why this connection was detached. Can be connCLOSED or connINVALIDATE or null if active. */
	protected String m_detach_reason;

	protected String m_detach_location;

	/** An owner object currently associated with this dude. */
	protected iDbConnectionOwnerInfo m_owner_info;

	/** The ID for this proxy */
	protected int m_id;

	/** If set this connection will ignore close requests. To close the connection one MUST call closeThreadConnections() on the PoolManager, or call closeForced(). */
	private boolean m_not_closable;

	/** If we're collecting usage statistics this is not null and refers to the collector. */
	private final InfoCollector m_collector;

	/**
	 *	Creates new connection. This is the only way to attach one to the PoolEntry.
	 */
	protected PooledConnection(final ConnectionPoolEntry pe, final int id) {
		m_pe = pe;
		m_autocommit = true;
		m_id = id;
		m_collector = pe.getPool().getManager().threadCollector();
	}

	protected void setOwnerThread(final Thread current) {
		synchronized(m_pe) {
			if(m_owner_thread != null && m_owner_thread != current)
				throw new IllegalStateException("!? Connection is owned by 2 threads!?");
			m_owner_thread = current;
		}
	}

	protected Thread getOwnerThread() {
		synchronized(m_pe) {
			return m_owner_thread;
		}
	}

	protected InfoCollector collector() {
		return m_collector;
	}

	protected void setThreadConnection() {
		synchronized(m_pe) {
			m_pe.setThreadCached(true);
			m_isthread = true;
		}
	}

	public boolean isThreadConnection() {
		synchronized(m_pe) {
			return m_isthread;
		}
	}

	public void setUncloseable(final boolean unclosable) {
		m_not_closable = unclosable;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("ConnectionProxy[");
		sb.append(Integer.toString(m_id));
		sb.append("] of ");
		sb.append(m_pe.toString());
		return sb.toString();
	}

	/**
	 *	Checks if this database connection is still open; if not it throws an
	 *  exception.
	 */
	private Connection check() {
		return m_pe.proxyCheck(this, true);
	}

	public ConnectionPoolEntry checkPE() {
		m_pe.proxyCheck(this, true);
		return m_pe;
	}


	/**
	 * This saves the NEMA context using this connection. When called the
	 * currently saved context MUST BE the same as the context passed OR the
	 * saved context must be null.
	 */
	public void setOwnerInfo(final iDbConnectionOwnerInfo oi) {
		synchronized(m_pe) {
			if(m_owner_info == oi)
				return; // Same context set -> okay.
			if(oi == null) // Context cleared -> okay.
				m_owner_info = null;
			else if(m_owner_info != null) {
				/*
				 * jal 20050802 Test removed. The navi framework uses forwards
				 * to generate the views. The default implementation of the forward
				 * (resin) handles this recursively, i.e. we run from a NemaContext,
				 * then we re-enter NEMA and we get another context. When this context
				 * allocates a connection it uses the same connection as the earlier
				 * copy, causing this error.
				 */
				//
				//
				//				String em	= "Connection used by 2 contexts!?!";
				//
				//				PoolManager.panic(em, em);
				//				throw new RuntimeException(em);
			}
			m_owner_info = oi;
		}
	}


	/**
	 * Returns the REAL database connection (the one obtained from the JDBC driver)
	 * for this proxy.
	 * @return	the connection
	 */
	public Connection getRealConnection() {
		return check();
	}


	public BaseDB getDbType() {
		return checkPE().m_pool.getDbType();
	}

	public String getPoolID() {
		return checkPE().m_pool.getID();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Resource management...								*/
	/*--------------------------------------------------------------*/
	private boolean m_closed;

	/**
	 *	Close releases all resources associated with this connection by closing
	 *  them, then it returns the connection to the pool. If the connection is
	 *  already closed then nothing happens (it is allowed).
	 */
	public void close() throws java.sql.SQLException {
		//		StringTool.dumpLocation("Closing connection normally using close, connection="+this+", not-closeable="+m_not_closable);
		if(!m_autocommit)
			setAutoCommit(true);
		if(!m_not_closable)
			closeForced();
	}

	/**
	 * Can be called for connections that ignore the normal close operation to
	 * force the proxy closed. The connection is returned to the pool proper.
	 *
	 * @throws SQLException
	 */
	public void closeForced() throws SQLException {
		//		StringTool.dumpLocation("Closing connection normally using close, connection="+this+", not-closeable="+m_not_closable);

		Thread ct = Thread.currentThread();
		if(m_owner_thread != ct)
			throw new IllegalStateException("Proxy closed by thread that's not owning it!");
		m_pe.proxyClosed(this);
		if(m_closed) // Already closed?
			return;
		m_closed = true; // No: drop the connection
		//		m_pe.getPool().getManager().removeThreadConnection(m_owner_thread, m_pe.getPool(), this);
	}

	//	/**
	//	 * Called from the thread cache handler to drop the connection when
	//	 * the thread connection cache is cleared. This merely closes the
	//	 * real connection but does not do a callback to drop the connection
	//	 * from the thread cache.
	//	 */
	//	public void	_closeConnection() throws SQLException
	//	{
	//		System.out.println(this+": _closeConnection called.");
	//		m_pe.proxyClosed(this);
	//		m_closed = true;					// No: drop the connection
	//	}
	//

	/**
	 * Called from the NEMA versions of the resources only, this removes the
	 * resource from the resource list because it was normally closed.
	 * @param o		the resource to remove.
	 */
	protected void removeResource(final Object o) {
		m_pe.proxyRemoveResource(this, o);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	resource allocating proxies.						*/
	/*--------------------------------------------------------------*/
	public java.sql.PreparedStatement prepareStatement(java.lang.String p1) throws java.sql.SQLException {
		return m_pe.proxyPrepareStatement(this, p1);
	}

	public java.sql.PreparedStatement prepareStatement(java.lang.String p1, int[] p2) throws java.sql.SQLException {
		return m_pe.proxyPrepareStatement(this, p1, p2);
	}

	public java.sql.PreparedStatement prepareStatement(java.lang.String p1, int p2) throws java.sql.SQLException {
		return m_pe.proxyPrepareStatement(this, p1, p2);
	}

	public java.sql.PreparedStatement prepareStatement(java.lang.String p1, int p2, int p3) throws java.sql.SQLException {
		return m_pe.proxyPrepareStatement(this, p1, p2, p3);
	}

	public java.sql.PreparedStatement prepareStatement(java.lang.String p1, int p2, int p3, int p4) throws java.sql.SQLException {
		return m_pe.proxyPrepareStatement(this, p1, p2, p3, p4);
	}

	public java.sql.CallableStatement prepareCall(String name, int a, int b, int c) throws SQLException {
		return m_pe.proxyPrepareCall(this, name, a, b, c);
	}

	public PreparedStatement prepareStatement(String a, String[] ar) throws SQLException {
		return m_pe.proxyPrepareStatement(this, a, ar);
	}

	public java.sql.Statement createStatement() throws java.sql.SQLException {
		return m_pe.proxyCreateStatement(this);
	}

	public java.sql.Statement createStatement(int p1, int p2) throws java.sql.SQLException {
		return m_pe.proxyCreateStatement(this, p1, p2);
	}

	public java.sql.Statement createStatement(int p1, int p2, int p3) throws java.sql.SQLException {
		return m_pe.proxyCreateStatement(this, p1, p2, p3);
	}

	public java.sql.CallableStatement prepareCall(java.lang.String p1, int p2, int p3) throws java.sql.SQLException {
		return m_pe.proxyPrepareCall(this, p1, p2, p3);
	}

	public java.sql.CallableStatement prepareCall(java.lang.String p1) throws java.sql.SQLException {
		return m_pe.proxyPrepareCall(this, p1);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Callthroughs to the original statement.				*/
	/*--------------------------------------------------------------*/
	public void setCatalog(final java.lang.String p1) throws java.sql.SQLException {
		check().setCatalog(p1);
	}

	public void rollback() throws java.sql.SQLException {
		check().rollback();
	}


	public void clearWarnings() throws java.sql.SQLException {
		check().clearWarnings();
	}

	@SuppressWarnings("unchecked")
	public java.util.Map getTypeMap() throws java.sql.SQLException {
		return check().getTypeMap();
	}

	public int getTransactionIsolation() throws java.sql.SQLException {
		return check().getTransactionIsolation();
	}

	public void setTransactionIsolation(final int p1) throws java.sql.SQLException {
		check().setTransactionIsolation(p1);
	}

	public synchronized boolean isClosed() throws java.sql.SQLException {
		synchronized(m_pe) {
			return m_detach_reason != null;
		}
	}

	public void setAutoCommit(final boolean p1) throws java.sql.SQLException {
		check().setAutoCommit(p1);
		m_autocommit = p1;
	}


	public void commit() throws java.sql.SQLException {
		check().commit();
	}

	public java.lang.String getCatalog() throws java.sql.SQLException {
		return check().getCatalog();
	}


	public boolean isReadOnly() throws java.sql.SQLException {
		return check().isReadOnly();
	}


	public java.sql.DatabaseMetaData getMetaData() throws java.sql.SQLException {
		return check().getMetaData();
	}


	public void setReadOnly(final boolean p1) throws java.sql.SQLException {
		check().setReadOnly(p1);
	}


	public boolean getAutoCommit() throws java.sql.SQLException {
		return check().getAutoCommit();
	}

	public java.lang.String nativeSQL(final java.lang.String p1) throws java.sql.SQLException {
		return check().nativeSQL(p1);
	}

	@SuppressWarnings("unchecked")
	public void setTypeMap(final java.util.Map p1) throws java.sql.SQLException {
		check().setTypeMap(p1);
	}


	public java.sql.SQLWarning getWarnings() throws java.sql.SQLException {
		return check().getWarnings();
	}

	public void releaseSavepoint(final java.sql.Savepoint sp) throws SQLException {
		check().releaseSavepoint(sp);
	}

	public void rollback(final java.sql.Savepoint sp) throws SQLException {
		check().rollback(sp);
	}

	public java.sql.Savepoint setSavepoint(final String name) throws SQLException {
		return check().setSavepoint(name);
	}

	public java.sql.Savepoint setSavepoint() throws SQLException {
		return check().setSavepoint();
	}

	public int getHoldability() throws SQLException {
		return check().getHoldability();
	}

	public void setHoldability(final int m) throws SQLException {
		check().setHoldability(m);
	}


	/*--------------- New JDK6 garbage ------------------*/

	public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
		return check().createArrayOf(arg0, arg1);
	}

	public Blob createBlob() throws SQLException {
		return check().createBlob();
	}

	public Clob createClob() throws SQLException {
		return check().createClob();
	}

	public NClob createNClob() throws SQLException {
		return check().createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return check().createSQLXML();
	}

	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		return check().createStruct(arg0, arg1);
	}

	public Properties getClientInfo() throws SQLException {
		return check().getClientInfo();
	}

	public String getClientInfo(String arg0) throws SQLException {
		return check().getClientInfo(arg0);
	}

	public boolean isValid(int arg0) throws SQLException {
		return check().isValid(arg0);
	}

	public boolean isWrapperFor(Class< ? > iface) throws SQLException {
		return iface.isAssignableFrom(getClass());
	}

	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		check().setClientInfo(arg0);
	}

	public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
		check().setClientInfo(arg0, arg1);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return (T) check();
	}
}
