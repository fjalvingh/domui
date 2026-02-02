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

import java.sql.*;

/**
 * This is the /dev/null or "ignore-all" info handler. It discards all info calls.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 28, 2011
 */
final class DummyConnectionEventListener implements IConnectionEventListener {
	static public final DummyConnectionEventListener INSTANCE = new DummyConnectionEventListener();

	private DummyConnectionEventListener() {}

	@Override
	public void connectionAllocated(ConnectionProxy dbc) {}

	@Override
	public void connectionClosed(ConnectionProxy proxy) {}

	@Override
	public void prepareStatement(StatementProxy sp) {}

	@Override
	public void prepareStatementEnd(StatementProxy sp) {}

	/*--------------------------------------------------------------*/
	/*	CODING:	Execute raw queries (unprepared)					*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see IConnectionEventListener#executeQueryStart(to.etc.dbpool.StatementProxy, to.etc.dbpool.ResultSetProxy)
	 */
	@Override
	public void executeQueryStart(StatementProxy sp, ResultSetProxy rsp) {}

	@Override
	public void executeQueryEnd(StatementProxy sp, SQLException x, ResultSetProxy rs) {}

	@Override
	public void resultSetClosed(StatementProxy sp, ResultSetProxy rsp) {}

	/*--------------------------------------------------------------*/
	/*	CODING:	Generic update statement.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see IConnectionEventListener#executeUpdateStart(to.etc.dbpool.StatementProxy)
	 */
	@Override
	public void executeUpdateStart(StatementProxy sp) {
	}

	@Override
	public void executeUpdateEnd(StatementProxy sp, SQLException sx, int rowcount) {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	The execute() statemnts.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see IConnectionEventListener#executeStart(to.etc.dbpool.StatementProxy)
	 */
	@Override
	public void executeStart(StatementProxy sp) {
	}

	@Override
	public void executeEnd(StatementProxy sp, SQLException error, Boolean result) {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Batched command sets.								*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see IConnectionEventListener#addBatch(to.etc.dbpool.StatementProxy, java.lang.String)
	 */
	@Override
	public void addBatch(StatementProxy sp, String sql) {}

	@Override
	public void executeBatchStart(StatementProxy sp) {}

	@Override
	public void executeBatchEnd(StatementProxy sp, SQLException error, int[] rc) {}

	@Override
	public void executePreparedQueryStart(StatementProxy sp, ResultSetProxy rsp) {}

	@Override
	public void executePreparedQueryEnd(StatementProxy sp, SQLException wx, ResultSetProxy rs) {}

	@Override
	public void executePreparedUpdateStart(StatementProxy sp) {}

	@Override
	public void executePreparedUpdateEnd(StatementProxy sp, SQLException error, int rowcount) {}
}
