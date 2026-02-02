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

	final private StatisticsCollectorBase m_request;

	private final String m_requestId;

	private PerformanceStore m_store;

	public SessionStatisticsEntry(StatisticsCollectorBase request, String requestId, int id, PerformanceStore ps) {
		m_requestId = requestId;
		m_request = request;
		m_id = id;
		m_ts = System.currentTimeMillis();
		m_store = ps;
	}

	public String getRequestId() {
		return m_requestId;
	}

	public StatisticsCollectorBase getRequest() {
		return m_request;
	}

	public List<DbMetric> getMetrics() {
		return m_request.getMetrics();
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
