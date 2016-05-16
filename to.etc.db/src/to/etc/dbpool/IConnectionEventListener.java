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

import javax.annotation.*;

/**
 * This listener gets notified of all kinds of events that take place on a connection. It
 * is mainly used to collect statistics on database use.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 27, 2011
 */
interface IConnectionEventListener {
	/**
	 * Called when a prepare is started. This starts the prepare clock for the statement.
	 */
	void prepareStatement(@Nonnull StatementProxy sp);

	/**
	 * Called when the prepare call has finished. Ends the prepare clock, and posts the "statement prepared" event.
	 * @param sp
	 */
	void prepareStatementEnd(@Nonnull StatementProxy sp);

	/**
	 * Started a statement query.
	 * @param sp
	 */
	void executeQueryStart(@Nonnull StatementProxy sp, @Nonnull ResultSetProxy rsp);

	void executeQueryEnd(@Nonnull StatementProxy sp, @Nullable SQLException wx, @Nonnull ResultSetProxy rs);

	void executePreparedQueryStart(StatementProxy sp, @Nonnull ResultSetProxy rsp);

	void executePreparedQueryEnd(@Nonnull StatementProxy sp, @Nullable SQLException wx, @Nonnull ResultSetProxy rs);


	/**
	 * Generic close result set.
	 * @param sp
	 * @param rsp
	 */
	void resultSetClosed(@Nonnull StatementProxy sp, @Nonnull ResultSetProxy rsp);

	void executeUpdateStart(StatementProxy sp);

	void executeUpdateEnd(StatementProxy sp, SQLException error, int rowcount);

	void executeStart(StatementProxy sp);

	void executeEnd(StatementProxy sp, SQLException error, Boolean result);


	void addBatch(StatementProxy sp, String sql);

	void executeBatchStart(StatementProxy sp);

	void executeBatchEnd(StatementProxy sp, SQLException error, int[] rc);


	void executePreparedUpdateStart(StatementProxy sp);

	void executePreparedUpdateEnd(StatementProxy sp, SQLException error, int rowcount);

	void connectionAllocated(ConnectionProxy dbc);

	void connectionClosed(ConnectionProxy proxy);
}
