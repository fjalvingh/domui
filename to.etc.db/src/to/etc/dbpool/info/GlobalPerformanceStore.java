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

/**
 * This stores global statistics for database queries all through the pool for
 * session based requests.
 *
 * FIXME This seems nonsense, as it only summarizes session statistics while global should
 * mean batch and session.
 */
final public class GlobalPerformanceStore {
	static private final String REQ_EXEC_TIME = "request-exec-time";

	static private final String REQ_CONNALLOCS = "request-connection-allocations";

	final private PerformanceStore m_store = new PerformanceCollector();

	public GlobalPerformanceStore() {
		//-- Register the other performance classes for requests.
		m_store.define(REQ_EXEC_TIME, "REQ: slowest requests", true, 20);
		m_store.define(REQ_CONNALLOCS, "REQ: largest #of connection allocations", true, 20);
	}

	public PerformanceStore getStore() {
		return m_store;
	}

	/**
	 * Returns (a copy of) all currently defined performance lists. The copy's (accessible data) is threadsafe.
	 * @return
	 */
	public List<PerfList> getLists() {
		List<PerfList> dupl;
		synchronized(m_store) {
			dupl = m_store.getLists();
		}
		Collections.sort(dupl, new Comparator<PerfList>() {
			public int compare(PerfList a, PerfList b) {
				return a.getKey().compareTo(b.getKey());
			}
		});
		return dupl;
	}

	public PerfList getList(String name) {
		synchronized(m_store) {
			return m_store.getList(name);
		}
	}

	public List<PerfItem> getItems(String listKey) {
		synchronized(m_store) {
			return m_store.getItems(listKey);
		}
	}

	public void addRequestInfo(StatisticsCollectorBase icb) {
		//-- Do not register pages that do nothing with the db.
		if(icb.getNAnything() == 0)
			return;
		synchronized(getStore()) {
			m_store.addItem(REQ_EXEC_TIME, icb.getIdent(), icb.getDuration(), null, icb);
			m_store.addItem(REQ_CONNALLOCS, icb.getIdent(), icb.getNConnectionAllocations(), null, icb);
		}
	}

	public void clear() {
		synchronized(getStore()) {
			getStore().clear();
		}
	}
}
