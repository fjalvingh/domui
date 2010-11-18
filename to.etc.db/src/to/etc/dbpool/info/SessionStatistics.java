package to.etc.dbpool.info;

import java.util.*;

/**
 * Collects statistics inside a http session, for debugging purposes. This stores most of the data for the
 * last m_maxRequest server requests.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 12, 2010
 */
public class SessionStatistics {
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
	public void addRequestInfo(PerformanceCollector collector, InfoCollectorBase thisrequest) {
		if(thisrequest.getNConnectionAllocations() == 0) // Do not store sillyness
			return;

		PerformanceStore ps = new PerformanceStore();
		ps.merge(collector);
		SessionStatisticsEntry sse = new SessionStatisticsEntry(thisrequest, nextID(), ps);

		//-- Store in table as last entry; release oldest entry if list is too big
		synchronized(this) {
			if(m_entryList.size() >= m_maxRequests)
				m_entryList.remove(0); // Remove oldest entry
			m_entryList.add(sse);
		}
	}
}
