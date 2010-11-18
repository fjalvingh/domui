package to.etc.dbpool.info;

import java.util.*;

import to.etc.dbpool.*;
import to.etc.dbpool.info.InfoCollectorExpenseBased.StmtCount;

/**
 * Collector for all kinds of performance-related data.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 4, 2010
 */
public interface IPerformanceCollector {
	void postExecuteDuration(String request, StatementProxy sp, long dt, StmtType type, StmtCount sc);

	void saveCounters(String request, List<StmtCount> counterList);
}
