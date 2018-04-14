package to.etc.dbpool.info;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.dbpool.ConnectionProxy;

import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 3/21/16.
 */
@NonNullByDefault
public interface IConnectionStatisticsFactory {
	void startConnectionStatistics(ConnectionProxy proxy) throws Exception;

	List<DbMetric> finishConnectionStatistics(ConnectionProxy proxy) throws Exception;
}
