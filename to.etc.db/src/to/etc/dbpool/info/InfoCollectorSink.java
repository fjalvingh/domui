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

import java.util.*;

import to.etc.dbpool.*;

public class InfoCollectorSink implements InfoCollector {
	private Map<String, InfoCollector> m_list = new HashMap<String, InfoCollector>();

	public void addCollector(String key, InfoCollector ic) {
		if(null != m_list.put(key, ic)) {
			System.out.println("POOLERR: Duplicate statistics collector with key=" + key);
		}
	}

	public InfoCollector removeCollector(String key) {
		return m_list.remove(key);
	}

	public void connectionAllocated() {
		for(InfoCollector ic : m_list.values())
			ic.connectionAllocated();
	}
	public void prepareStatement(String sql) {
		for(InfoCollector ic : m_list.values())
			ic.prepareStatement(sql);
	}

	public void prepareStatementEnd(String sql, StatementProxy sp) {
		for(InfoCollector ic : m_list.values())
			ic.prepareStatementEnd(sql, sp);
	}

	public void executeQueryStart(StatementProxy sp) {
		for(InfoCollector ic : m_list.values())
			ic.executeQueryStart(sp);
	}

	public void executePreparedQueryStart(StatementProxy sp) {
		for(InfoCollector ic : m_list.values())
			ic.executePreparedQueryStart(sp);
	}

	public void executeError(StatementProxy sp, Exception x) {
		for(InfoCollector ic : m_list.values())
			ic.executeError(sp, x);
	}

	public void executeQueryEnd(StatementProxy sp, ResultSetProxy rs) {
		for(InfoCollector ic : m_list.values())
			ic.executeQueryEnd(sp, rs);
	}

	public void executeUpdateStart(StatementProxy sp) {
		for(InfoCollector ic : m_list.values())
			ic.executeUpdateStart(sp);
	}

	public void executePreparedUpdateStart(StatementProxy sp) {
		for(InfoCollector ic : m_list.values())
			ic.executePreparedUpdateStart(sp);
	}

	public void executeUpdateEnd(StatementProxy sp, int rowcount) {
		for(InfoCollector ic : m_list.values())
			ic.executeUpdateEnd(sp, rowcount);
	}

	public void executeStart(StatementProxy sp) {
		for(InfoCollector ic : m_list.values())
			ic.executeStart(sp);
	}

	public void executeEnd(StatementProxy sp, Boolean result) {
		for(InfoCollector ic : m_list.values())
			ic.executeEnd(sp, result);
	}

	public void incrementUpdateCount(int uc) {
		for(InfoCollector ic : m_list.values())
			ic.incrementUpdateCount(uc);
	}

	public void executeBatchStart(StatementProxy sp) {
		for(InfoCollector ic : m_list.values())
			ic.executeBatchStart(sp);
	}

	public void executeBatchEnd(StatementProxy sp, int[] rc) {
		for(InfoCollector ic : m_list.values())
			ic.executeBatchEnd(sp, rc);
	}

//	public void incrementRowCount(ResultSetProxy rp) {
//		for(InfoCollector ic : m_list.values())
//			ic.incrementRowCount(rp);
//	}

	public void resultSetClosed(ResultSetProxy rp) {
		for(InfoCollector ic : m_list.values())
			ic.resultSetClosed(rp);
	}

	public void addBatch(String sql) {
		for(InfoCollector ic : m_list.values())
			ic.addBatch(sql);
	}

	public void finish() {
		for(InfoCollector ic : m_list.values())
			ic.finish();
	}
}
