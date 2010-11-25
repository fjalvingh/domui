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

public class DummyCollector implements InfoCollector {
	static public final DummyCollector INSTANCE = new DummyCollector();

	private DummyCollector() {}

	public void executeBatchEnd(final StatementProxy sp, final int[] rc) {}

	public void executeBatchStart(final StatementProxy sp) {}

	public void executeEnd(final StatementProxy sp, final Boolean result) {}

	public void executeError(final StatementProxy sp, final Exception x) {}

	public void executePreparedQueryStart(final StatementProxy sp) {}

	public void executePreparedUpdateStart(final StatementProxy sp) {}

	public void executeQueryEnd(final StatementProxy sp, final ResultSetProxy rs) {}

	public void executeQueryStart(final StatementProxy sp) {}

	public void executeStart(final StatementProxy sp) {}

	public void executeUpdateEnd(final StatementProxy sp, final int rowcount) {}

	public void executeUpdateStart(final StatementProxy sp) {}

	public void incrementUpdateCount(final int uc) {}

	public void prepareStatement(final String sql) {}

	public void prepareStatementEnd(final String sql, final StatementProxy sp) {}

	public void resultSetClosed(ResultSetProxy rp) {}

	public void addBatch(final String sql) {}

	public void connectionAllocated() {}

	public void finish() {}
}
