package to.etc.util;

import org.eclipse.jdt.annotation.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-6-19.
 */
final public class TimerUtil {
	@Nullable
	static private ScheduledExecutorService m_scheduler;

	private TimerUtil() {
	}

	static private final ScheduledExecutorService getScheduler() {
		ScheduledExecutorService scheduler = m_scheduler;
		if(null == scheduler) {
			m_scheduler = scheduler = Executors.newScheduledThreadPool(2);
		}
		return scheduler;
	}

	static public ScheduledExecutorService getTimer() {
		return getScheduler();
	}
}
