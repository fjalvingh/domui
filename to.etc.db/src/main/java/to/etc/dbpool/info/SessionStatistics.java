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

import javax.annotation.*;

/**
 * Collects statistics inside a http session, for debugging purposes. This stores most of the data for the
 * last m_maxRequest server requests.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 12, 2010
 */
final public class SessionStatistics {
	final private int m_maxRequests;

	private int m_idCounter;

	/** All cached entries. Once stored here they are immutable; only the list itself changes and must be duplicated before return. */
	final private List<SessionStatisticsEntry> m_entryList;

	public SessionStatistics(int maxRequests) {
		m_maxRequests = maxRequests;
		m_entryList = new ArrayList<SessionStatisticsEntry>(maxRequests + 1);
	}

	public synchronized List<SessionStatisticsEntry> getRequests() {
		return new ArrayList<SessionStatisticsEntry>(m_entryList);
	}

	private synchronized int nextID() {
		return m_idCounter++;
	}

	/**
	 * Merge the request data. After the merge all data stored is immutable and accessable by the debug page(s).
	 * @param collector
	 * @param thisrequest
	 */
	public void addRequestInfo(String requestId, PerformanceCollector collector, StatisticsCollectorBase thisrequest) {
		if(thisrequest.getNAnything() == 0) // Do not store sillyness
			return;

		PerformanceStore ps = new PerformanceStore();
		ps.merge(collector);
		int requestNumber = nextID();
		SessionStatisticsEntry sse = new SessionStatisticsEntry(thisrequest, requestId, requestNumber, ps);

		//-- Store in table as last entry; release oldest entry if list is too big
		synchronized(this) {
			if(m_entryList.size() >= m_maxRequests)
				m_entryList.remove(0); // Remove oldest entry
			m_entryList.add(sse);
		}
	}

	@Nullable
	public SessionStatisticsEntry getEntry(String requestId) {
		for(SessionStatisticsEntry e: m_entryList) {
			if(requestId.equals(e.getRequestId()))
				return e;
		}
		return null;
	}
}
