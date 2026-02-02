package to.etc.util;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.IExecute;

import java.time.Duration;
import java.util.Calendar;
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

	static private synchronized ScheduledExecutorService getScheduler() {
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
	static public ScheduledFuture<?> scheduleAtFixedRate(long delay, long period, TimeUnit unit, IExecute thingy) {
		Runnable wrappedRunnable = () -> {
			try {
				thingy.execute();
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

	/**
	 * Wait till the specified hour and minute before starting the thingy,
	 * then repeat it every interval.
	 */
	static public ScheduledFuture<?> scheduleAtFixedRate(int firstHour, int firstMinute, Duration interval, IExecute thingy) {
		Calendar cal = Calendar.getInstance();				// Get a calendar that says "now"

		long nowMillis = cal.getTimeInMillis();
		int orig = firstHour * 60 + firstMinute;			// When to start, as minute
		int nowm = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
		cal.set(Calendar.HOUR_OF_DAY, firstHour);
		cal.set(Calendar.MINUTE, firstMinute);

		if(orig < nowm) {									// That hour already passed today?
			cal.add(Calendar.DAY_OF_MONTH, 1);		// Then same time tomorrow
		}
		long delayMillis = cal.getTimeInMillis() - nowMillis;
		if(delayMillis <= 0)								// Should not happen
			delayMillis = 1000;

		return scheduleAtFixedRate(delayMillis, interval.toMillis(), TimeUnit.MILLISECONDS, thingy);
	}

	static public void shutdownNow() {
		ScheduledExecutorService scheduler;
		synchronized(TimerUtil.class) {
			scheduler = m_scheduler;
			if(null == scheduler)
				return;
			m_scheduler = null;
		}
		scheduler.shutdownNow();
	}
}
