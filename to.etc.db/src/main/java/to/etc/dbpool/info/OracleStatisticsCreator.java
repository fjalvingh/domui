package to.etc.dbpool.info;

import java.sql.*;
import java.util.*;

import javax.annotation.*;

import to.etc.dbpool.*;

/**
 * This Oracle-only thing enables per-session tracing of database performance data.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2/2/16.
 */
@DefaultNonNull
final public class OracleStatisticsCreator {
	static final private String KEY = "OraStatCtx";

	/** When set this switches on session trace for all connections. This is hideously expensive so use in extreme cases only. */
	static private volatile boolean m_enableSessionTrace;

	static private boolean m_allStatistics = System.getProperty("db.extended") != null;

	static {
		m_enableSessionTrace = System.getProperty("db.trace") != null;
	}

	final private Map<ConnectionProxy, Map<MetricsDefinition, DbMetric>> m_storeMap = new HashMap<>();

	static public OracleStatisticsCreator	get(ConnectionProxy px) {
		return px.getPool().getOrCreateAttribute(KEY, () -> new OracleStatisticsCreator());
	}

	public void enableConnectionStatistics(ConnectionProxy px, String sessionID) throws Exception {
		try(Statement statement = px.createStatement()) {
			statement.execute("begin dbms_session.set_identifier('"+ sessionID +"'); end;");			// Set session ID
			try {
				try {
					statement.execute("begin dbms_monitor.client_id_stat_enable('" + sessionID + "'); end;");    // Enable statistics gathering
				} catch(Exception x) {
					if(! x.getMessage().contains("ORA-13861")) {			// Statistics collection already enabled -> stale, so remove and retry...
						throw x;
					}

					//-- Disable them, cleaning out the old statistics
					statement.execute("begin dbms_monitor.client_id_stat_disable('" + sessionID + "'); end;");    // Disable and remove stats

					//-- Then try again
					statement.execute("begin dbms_monitor.client_id_stat_enable('" + sessionID + "'); end;");    // Enable statistics gathering
				}

				if(m_allStatistics) {
					Map<MetricsDefinition, DbMetric> map = loadExtendedStatistics(px);
					synchronized(m_storeMap) {
						m_storeMap.put(px, map);
					}
				}

				if(m_enableSessionTrace)
					statement.execute("begin dbms_monitor.client_id_trace_enable(client_id => '" + sessionID + "', waits => true, binds => false); end;");
			} catch(Exception x) {
				System.err.println("dbpool: " + x);
			}
		}
	}

	public List<DbMetric> disableConnectionStatistics(ConnectionProxy px, String sessionID) throws Exception {
		Map<MetricsDefinition, DbMetric> map = new HashMap<>();
		try(PreparedStatement ps = px.prepareStatement("select stat_name, value from v$client_stats where client_identifier = ?")) {
			ps.setString(1, sessionID);
			try(ResultSet rs = ps.executeQuery()) {
				while(rs.next()) {
					DbMetric m = MetricsDefinition.createByName(rs.getString(1), rs.getDouble(2));
					map.put(m.getDefinition(), m);
				}
			}
		}

		try(Statement st = px.createStatement()) {
			try {
				st.execute("begin dbms_monitor.client_id_stat_disable('" + sessionID + "'); end;");    // Disable and remove stats
				if(m_enableSessionTrace)
					st.execute("begin dbms_monitor.client_id_trace_disable(client_id => '" + sessionID + "'); end;");
			} catch(Exception x) {
				System.err.println("dbpool: " + x);
			}
		}

		if(m_allStatistics) {
			Map<MetricsDefinition, DbMetric> newMap = loadExtendedStatistics(px);
			if(newMap.size() > 0) {
				Map<MetricsDefinition, DbMetric> oldMap;
				synchronized(m_storeMap) {
					oldMap = m_storeMap.remove(px);
				}
				if(oldMap != null) {
					mergeMaps(map, oldMap, newMap);
				}
			}
		}

		return new ArrayList<>(map.values());
	}

	/**
	 * This collects all metrics present in newMap, and subtracts whatever the value was in
	 * oldMap.
	 *
	 * @param map
	 * @param oldMap
	 * @param newMap
	 */
	private void mergeMaps(Map<MetricsDefinition, DbMetric> map, Map<MetricsDefinition, DbMetric> oldMap, Map<MetricsDefinition, DbMetric> newMap) {
		newMap.forEach((name, metric) -> {
			DbMetric old = oldMap.get(name);
			if(old != null) {
				double value = metric.getValue() - old.getValue();
				if(value > 0) {
					metric.setValue(value);					// Set delta
					if(! map.containsKey(metric.getDefinition()))
						map.put(metric.getDefinition(), metric);
				}
			}
		});
	}

	/**
	 * Load the data from v$sesstat.
	 *
	 * @param cx
	 * @return
	 */
	private Map<MetricsDefinition, DbMetric> loadExtendedStatistics(ConnectionProxy cx) {
		Map<MetricsDefinition, DbMetric> map = new HashMap<>();
		try(Statement st = cx.createStatement()) {
			try(ResultSet rs = st.executeQuery("select value, name"
				+ " from v$sesstat s join v$statname n on s.statistic# = n.statistic# where sid=sys_context('USERENV', 'SID')"
				+ " and value > 0")) {
				while(rs.next()) {
					double value = rs.getDouble(1);
					String name = rs.getString(2);
					DbMetric m = MetricsDefinition.createByName(name, value);
					map.put(m.getDefinition(), m);
				}
			}
		} catch(Exception x) {
			System.err.println("dbpool: " + x);
		}
		return map;
	}

	/**
	 * QD Enable session trace (tkprof file generation) for all session connections.
	 * @param on
	 */
	static public void enableSessionTrace(boolean on) {
		m_enableSessionTrace = on;
	}
}
