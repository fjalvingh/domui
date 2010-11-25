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
package to.etc.dbpool.info;

import to.etc.dbpool.*;

public interface InfoCollector {
	/**
	 * Called when a prepare is started. This starts the clock for the statement.
	 * @param sql
	 */
	void prepareStatement(String sql);

	/**
	 * Called when the prepare call has finished.
	 * @param sql
	 * @param sp
	 */
	void prepareStatementEnd(String sql, StatementProxy sp);

	void executeQueryStart(StatementProxy sp);

	void executePreparedQueryStart(StatementProxy sp);

	void executeError(StatementProxy sp, Exception x);

	void executeQueryEnd(StatementProxy sp, ResultSetProxy rs);

	void executeUpdateStart(StatementProxy sp);

	void executePreparedUpdateStart(StatementProxy sp);

	void executeUpdateEnd(StatementProxy sp, int rowcount);

	void executeStart(StatementProxy sp);

	void executeEnd(StatementProxy sp, Boolean result);

	void incrementUpdateCount(int uc);

	void executeBatchStart(StatementProxy sp);

	void executeBatchEnd(StatementProxy sp, int[] rc);

//	//-- resultset calls
//	void incrementRowCount(ResultSetProxy rp);

	/**
	 * The result set was closed, and row fetch count and row fetch duration is available.
	 * @param rp
	 */
	void resultSetClosed(ResultSetProxy rp);

	void addBatch(String sql);

	void connectionAllocated();

	void finish();


}
