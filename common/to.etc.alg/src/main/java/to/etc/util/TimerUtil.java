package to.etc.util;

import org.eclipse.jdt.annotation.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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
			m_scheduler = scheduler = Executors.newScheduledThreadPool(2, new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setName("timerThread");
					t.setDaemon(true);
					return t;
				}
			});
		}
		return scheduler;
	}

	static public ScheduledExecutorService getTimerService() {
		return getScheduler();
	}

	/**
	 * Schedule a task to execute regularly. Wraps the same method in ScheduledExecutorService to not
	 * stop running at the first fucking exception.
	 */
	static public ScheduledFuture<?> scheduleAtFixedRate(long delay, long period, TimeUnit unit, Runnable thingy) {
		Runnable wrappedRunnable = () -> {
			try {
				thingy.run();
			} catch(Throwable x) {
				System.err.println("[timerutil] run failed: " + x);
				x.printStackTrace();
			}
		};
		return getScheduler().scheduleAtFixedRate(wrappedRunnable, delay, period, unit);
	}

	static public ScheduledFuture<?> schedule(long delay, TimeUnit unit, Runnable what) {
		return getScheduler().schedule(what, delay, unit);
	}

	static public void shutdownNow() {
		getScheduler().shutdownNow();
	}
}
