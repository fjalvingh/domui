package to.etc.dbpool.info;

import java.util.*;

/**
 * A single request's data as stored in the session statistics.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 12, 2010
 */
public class SessionStatisticsEntry {
	/** Unique identifier to allow finding it. */
	final private int m_id;

	/** Collection timestamp (System.currentTimeMillis()) */
	final private long m_ts;

	final private InfoCollectorBase m_request;

	private PerformanceStore m_store;

	public SessionStatisticsEntry(InfoCollectorBase request, int id, PerformanceStore ps) {
		m_request = request;
		m_id = id;
		m_ts = System.currentTimeMillis();
		m_store = ps;
	}

	public InfoCollectorBase getRequest() {
		return m_request;
	}

	public int getId() {
		return m_id;
	}

	public Date getTS() {
		return new Date(m_ts);
	}

	public PerformanceStore getStore() {
		return m_store;
	}
}
