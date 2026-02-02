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

import to.etc.dbpool.info.*;

/**
 * Listener for statistic database events.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 28, 2011
 */
public interface IStatisticsListener {
	void connectionAllocated(ConnectionProxy proxy);

	/**
	 * A statement was prepared or created.
	 * @param sp
	 * @param prepareDuration
	 */
	void statementPrepared(StatementProxy sp, long prepareDuration);

	/**
	 * A query statement was executed.
	 * @param sp
	 * @param executeDuration
	 * @param fetchDuration
	 * @param rowCount
	 * @param prepared
	 */
	void queryStatementExecuted(StatementProxy sp, long executeDuration, long fetchDuration, int rowCount, boolean prepared);

	/**
	 * An update statement has been executed.
	 * @param sp
	 * @param updateDuration
	 * @param updatedrowcount
	 */
	void executeUpdateExecuted(StatementProxy sp, long updateDuration, int updatedrowcount);

	/**
	 * Executed an "execute" command.
	 * @param sp
	 * @param updateDuration
	 * @param result
	 */
	void executeExecuted(StatementProxy sp, long updateDuration, Boolean result);

	void executePreparedUpdateExecuted(StatementProxy sp, long updateDuration, int rowcount);

	void finish();

	void executeBatchExecuted(long executeDuration, int totalStatements, int totalRows, List<BatchEntry> list);

	void connectionClosed(ConnectionProxy proxy);
}
