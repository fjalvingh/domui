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
package to.etc.dbpool;

import java.util.*;

/**
 * PIT copy of pool statistics.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 2, 2010
 */
final public class PoolStats {
	/** The current #of allocated and used unpooled connections. */
	final private int m_unpooledAllocated;

	final private int m_unpooledMaxAllocated;

	/**
	 * The current #of connections allocated for the POOL. This does NOT include
	 * the unpooled connections. The total #of connections used by the pool is
	 * the sum of this variable plus m_n_unpooled_inuse.
	 */
	private int m_pooledAllocated;

	/** The current #of connections used by the clients of the pool, */
	private int m_pooledUsed;

	/** The max. #of connections that was simultaneously used. */
	private int m_pooledMaxUsed;

	/** #of connection allocations (alloc/free) done. */
	private int m_totalAllocations;

	/** The #of times we had to wait for a pooled connection. */
	private int m_connectionWaitCount;

	/** The #of times we failed an allocation because all pooled connections were used. */
	private int m_poolFailureCount;

	/** The #of connections that were disconnected because they were assumed to be hanging. */
	private int m_expiredDisconnects;

	/** The #of statements CURRENTLY allocated by the pool */
	private int m_statementOpenCount;

	/** The #of statements MAX allocated by the pool */
	private int m_statementPeakCount;

	/** The #of resultsets opened by all statements in the pool */
	private long m_resultsetOpenCount;

	/** #of getConnection() calls to the database itself. */
	final private int m_totalDatabaseAllocations;

	/** The #of prepare statement actions executed. */
	private long m_statementTotalPrepareCount;

	/// The #of rows returned.
	@Deprecated
	private long m_n_rows;

	final private List<ConnectionProxy> m_hangingConnections;

	PoolStats(int nUnpooledInuse, int nPooledAllocated, int nPooledInuse, int maxUsed, int nConnallocations, int nConnectionwaits, int nConnectionfails, int nHangdisconnects, int nOpenStmt,
		int peakOpenStmt, long nOpenRs, long nExec, long nRows, List<ConnectionProxy> hang, int totaldb, int unpooledmax) {
		m_unpooledAllocated = nUnpooledInuse;
		m_pooledAllocated = nPooledAllocated;
		m_pooledUsed = nPooledInuse;
		m_pooledMaxUsed = maxUsed;
		m_totalAllocations = nConnallocations;
		m_connectionWaitCount = nConnectionwaits;
		m_poolFailureCount = nConnectionfails;
		m_expiredDisconnects = nHangdisconnects;
		m_statementOpenCount = nOpenStmt;
		m_statementPeakCount = peakOpenStmt;
		m_resultsetOpenCount = nOpenRs;
		m_statementTotalPrepareCount = nExec;
		m_n_rows = nRows;
		m_hangingConnections = hang;
		m_totalDatabaseAllocations = totaldb;
		m_unpooledMaxAllocated = unpooledmax;
	}

	public int getTotalDatabaseAllocations() {
		return m_totalDatabaseAllocations;
	}

	/**
	 * The current #of allocated and used unpooled connections.
	 * @return
	 */
	public int getUnpooledAllocated() {
		return m_unpooledAllocated;
	}

	public int getUnpooledMaxAllocated() {
		return m_unpooledMaxAllocated;
	}

	/**
	 * The current #of connections allocated for the POOL. This does NOT include
	 * the unpooled connections. The total #of connections used by the pool is
	 * the sum of this variable plus getUsedUnpooled().
	 * @return
	 */
	public int getPooledAllocated() {
		return m_pooledAllocated;
	}

	public int getPooledUsed() {
		return m_pooledUsed;
	}

	public int getPooledMaxUsed() {
		return m_pooledMaxUsed;
	}

	/**
	 * Returns the #of times that a connection was allocated from the pool (i.e. the #of
	 * calls to getConnection()).
	 *
	 * @return
	 */
	public int getTotalAllocations() {
		return m_totalAllocations;
	}

	public int getConnectionWaitCount() {
		return m_connectionWaitCount;
	}

	public int getPoolFailureCount() {
		return m_poolFailureCount;
	}

	/**
	 * Returns the #of times a connection was closed by the Janitor because it
	 * was allocated for way too long.
	 *
	 * @return
	 */
	public int getExpiredDisconnects() {
		return m_expiredDisconnects;
	}

	public int getStatementOpenCount() {
		return m_statementOpenCount;
	}

	public int getStatementPeakCount() {
		return m_statementPeakCount;
	}

	public long getResultsetOpenCount() {
		return m_resultsetOpenCount;
	}

	public long getStatementTotalPrepareCount() {
		return m_statementTotalPrepareCount;
	}

	public long getN_rows() {
		return m_n_rows;
	}

	public List<ConnectionProxy> getCurrentlyHangingConnections() {
		return m_hangingConnections;
	}
}
