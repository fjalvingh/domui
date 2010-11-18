package to.etc.dbpool.info;

import java.util.*;

public class GlobalPerformanceStore {
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

	public void addRequestInfo(InfoCollectorBase icb) {
		//-- Do not register pages that do nothing with the db.
		if(icb.getNConnectionAllocations() == 0)
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
