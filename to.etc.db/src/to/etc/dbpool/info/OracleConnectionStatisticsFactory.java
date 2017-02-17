package to.etc.dbpool.info;

import to.etc.dbpool.*;

import java.util.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 3/21/16.
 */
public class OracleConnectionStatisticsFactory implements IConnectionStatisticsFactory {
	@Override public List<DbMetric> finishConnectionStatistics(ConnectionProxy proxy) throws Exception {
		OracleStatisticsCreator c = OracleStatisticsCreator.get(proxy);
		List<DbMetric> list = c.disableConnectionStatistics(proxy, "pxy" + proxy.getId());
		return list;
	}

	@Override public void startConnectionStatistics(ConnectionProxy proxy) throws Exception {
		OracleStatisticsCreator c = OracleStatisticsCreator.get(proxy);
		c.enableConnectionStatistics(proxy, "pxy" + proxy.getId());
	}
}
