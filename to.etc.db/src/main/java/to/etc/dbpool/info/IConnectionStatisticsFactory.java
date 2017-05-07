package to.etc.dbpool.info;

import to.etc.dbpool.*;

import javax.annotation.*;
import java.util.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 3/21/16.
 */
@DefaultNonNull
public interface IConnectionStatisticsFactory {
	void startConnectionStatistics(ConnectionProxy proxy) throws Exception;

	List<DbMetric> finishConnectionStatistics(ConnectionProxy proxy) throws Exception;
}
