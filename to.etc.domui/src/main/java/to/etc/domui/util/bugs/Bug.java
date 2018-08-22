/*
 * DomUI Java User Interface library
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
package to.etc.domui.util.bugs;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.janitor.Janitor;
import to.etc.domui.util.janitor.JanitorTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

/**
 * Accessor to post odd conditions for later review, either in the UI itself (BUGs) or
 * by reporting them to some bug tracker or email (PANIC).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2010
 */
final public class Bug {

	static private final ThreadLocal<List<IBugListener>> m_threadListener = new ThreadLocal<>();

	public static final long ONE_HOUR_IN_MILLIS = 60 * 60 * 1000L;

	static private List<IBugListener> m_listeners = new ArrayList<>();

	static private List<IBugInfoContributor> m_contributors = new ArrayList<>();

	static private volatile int m_maxDuplicates = 2;

	static private long m_lastSweepTime = System.currentTimeMillis();

	/** PANIC Bugs indexed by hash. */
	static private Map<String, BugOccurrence> m_occurrenceMap = new HashMap<>();

	private static boolean m_initialized;

	/** When set, the executor will be used to run global bug reports in the background. */
	@Nullable
	private static Executor m_executor;

	private Bug() {}

	/**
	 * Show a bug using the specified message.
	 */
	static public void bug(@NonNull String message) {
		BugItem	bi = new BugItem(message);
		postBug(bi);
	}

	static public void bug(List<NodeBase> msg) {
		BugItem bi = new BugItem(msg);
		postBug(bi);
	}

	static public void bug(@Nullable Throwable x, @NonNull String message) {
		BugItem bi = new BugItem(message, x);
		postBug(bi);
	}

	static public void bug(@NonNull Throwable x) {
		BugItem bi = new BugItem(x.getMessage(), x);
		postBug(bi);
	}

	private static void postBug(BugItem bi) {
		if(isRateLimited(bi))										// Ignore if we're receiving too many.
			return;

		addContributions(bi);										// Let all contributors contribute

		List<IBugListener> threadListeners = getThreadListeners();
		report(threadListeners, bi);
		List<IBugListener> gll = getGlobalListeners();
		Executor executor = getExecutor();
		if(null == executor) {
			report(gll, bi);
		} else if(gll.size() > 0) {
			executor.execute(() -> report(gll, bi));
		}
		if(true || (gll.size() == 0 && threadListeners.size() == 0)) {
			//-- No one listens -> report to console.
			System.out.println(bi.toString());
			Throwable x = bi.getException();
			if(null != x) {
				x.printStackTrace();
			}
		}
	}

	/**
	 * Checks whether this issue is rate-limited, i.e. it occurs too often.
	 * @return T if the issue should not be reported again.
	 */
	private static boolean isRateLimited(BugItem item) {
		if(item.getSeverity() != BugSeverity.PANIC)				// Only PANICs are rate-limited
			return false;
		if(m_maxDuplicates <= 0)								// Rate-limiting disabled?
			return false;

		String hash = item.getHash();
		BugOccurrence occ = m_occurrenceMap.computeIfAbsent(hash, a -> new BugOccurrence());
		if(occ.m_count++ >= m_maxDuplicates)
			return true;

		//-- We're still below the rate-> allow this one.
		return false;
	}

	private static void addContributions(BugItem bi) {
		for(IBugInfoContributor contributor : getContributors()) {
			try {
				contributor.onContribute(bi);
			} catch(Exception x) {
				System.err.println("=== Exception in BUG contributor ===");
				System.err.println("Listener class   : " + contributor.getClass().getName());
				System.err.println("Listener toString: " + contributor.toString());
				x.printStackTrace();
			}
		}
	}

	private static void report(List<IBugListener> list, BugItem bi) {
		for(IBugListener listener : list) {
			try {
				listener.bugSignaled(bi);
			} catch(Exception x) {
				System.err.println("=== Exception in BUG listener ===");
				System.err.println("Listener class   : " + listener.getClass().getName());
				System.err.println("Listener toString: " + listener.toString());
				x.printStackTrace();
			}
		}
	}

	/**
	 * Add a listener which will be called for every Bug reported, regardless of thread. The listener must
	 * be thread safe.
	 */
	static public synchronized void addGlobalListener(IBugListener listener) {
		List<IBugListener> listeners = m_listeners;
		if(listeners.contains(listener))
			return;
		listeners = new ArrayList<>(listeners);
		listeners.add(listener);
		m_listeners = listeners;
	}

	/**
	 * Remove an earlier placed global listener.
	 */
	static public synchronized void removeGlobalListener(IBugListener listener) {
		List<IBugListener> listeners = m_listeners;
		if(! listeners.contains(listener))
			return;
		listeners = new ArrayList<>(listeners);
		listeners.remove(listener);
		m_listeners = listeners;
	}

	private static synchronized List<IBugListener> getGlobalListeners() {
		return m_listeners;
	}

	@NonNull
	static private List<IBugListener> getThreadListeners() {
		List<IBugListener> l = m_threadListener.get();
		return l == null ? Collections.emptyList() : l;
	}

	/**
	 * Add a Bug listener for the current thread only. Listeners added with this should be removed as soon
	 * as the request handled by the thread finishes.
	 */
	static public void addThreadListener(@NonNull IBugListener l) {
		List<IBugListener> list = getThreadListeners();
		if(list.contains(l))
			return;
		list = new ArrayList<>(list);							// Copy
		list.add(l);
		m_threadListener.set(list);
	}

	static public void removeThreadListener(@NonNull IBugListener listener) {
		List<IBugListener> list = getThreadListeners();
		if(! list.contains(listener))
			return;
		list = new ArrayList<>(list);							// Copy
		list.remove(listener);
		m_threadListener.set(list);
	}

	static public synchronized void addContributor(IBugInfoContributor contributor) {
		List<IBugInfoContributor> contributors = m_contributors;
		if(contributors.contains(contributor))
			return;
		contributors = new ArrayList<>(contributors);
		contributors.add(contributor);
		m_contributors = contributors;
	}

	static public synchronized void removeContributor(IBugInfoContributor contributor) {
		List<IBugInfoContributor> contributors = m_contributors;
		if(! contributors.contains(contributor))
			return;
		contributors = new ArrayList<>(contributors);
		contributors.remove(contributor);
		m_contributors = contributors;
	}

	private static synchronized List<IBugInfoContributor> getContributors() {
		return m_contributors;
	}

	static public void panic(Throwable t, String message, Object... contextItems) {
		panic(t, message, Arrays.asList(contextItems));
	}

	static public void panic(Throwable t, String message, List<Object> contextItems) {
		BugItem bug = new BugItem(BugSeverity.PANIC, message, t, contextItems);
		postBug(bug);
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Maintenance													*/
	/*----------------------------------------------------------------------*/

	/**
	 * Initialize the Bug framework, and schedule a sweep for duplicate bugs every n hours. An optional
	 * Executor can be set which will then be used to run sweep and report tasks in the background.
	 */
	static public synchronized void initialize(int sweepintervalInHours, @Nullable Executor backgroundExecutor) throws Exception {
		m_executor = backgroundExecutor;
		if(m_initialized) {
			return;
		}
		m_initialized = true;

		Janitor.getJanitor().addTask(sweepintervalInHours * 60, sweepintervalInHours * 60, "BugDups", new JanitorTask() {
			@Override public void run() {
				Executor executor = m_executor;
				if(null == executor) {
					sweep();
				} else {
					executor.execute(Bug::sweep);
				}
			}
		});
	}

	public static synchronized void setExecutor(Executor executor) {
		m_executor = executor;
	}

	private static synchronized Executor getExecutor() {
		return m_executor;
	}

	/**
	 * Walk the occurrence table and remove expired items, and report all items that have
	 * occurred > maxDuplicates times. This must be called on a worker thread as it can
	 * take a while.
	 */
	public static void sweep() {
		List<TodoItem> todoList = new ArrayList<>();
		long cts = System.currentTimeMillis();
		long expire = cts - 2 * 24 * ONE_HOUR_IN_MILLIS;		// Expire everything that did not occur again in 2 days
		synchronized(Bug.class) {
			long last = m_lastSweepTime;
			m_lastSweepTime = cts;

			long delta = cts - m_lastSweepTime;
			if(delta < ONE_HOUR_IN_MILLIS) {
				delta = ONE_HOUR_IN_MILLIS;
			}
			long fence = cts - delta;

			Iterator<Entry<String, BugOccurrence>> iterator = m_occurrenceMap.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<String, BugOccurrence> next = iterator.next();

				BugOccurrence occ = next.getValue();
				int inPeriod = occ.m_count - occ.m_lastReportedCount;

				if(occ.m_since < fence && inPeriod > m_maxDuplicates) {
					//-- report an update on this one.
					todoList.add(new TodoItem(inPeriod, next.getKey(), occ.m_since));
					occ.m_since = cts;
					occ.m_lastReportedCount = occ.m_count;
				}

				if(occ.m_since < expire)
					iterator.remove();
			}
		}

		//-- Now report all to the reporters, when interested.
		for(TodoItem item : todoList) {
			for(IBugListener listener : getGlobalListeners()) {
				if(listener instanceof IBugListenerEx) {
					try {
						((IBugListenerEx) listener).reportRepeats(item.m_hash, item.m_since, item.m_count);
					} catch(Exception x) {
						System.err.println("=== Exception in BUG listener ===");
						System.err.println("Listener class   : " + listener.getClass().getName());
						System.err.println("Listener toString: " + listener.toString());
						x.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Counts how many times a bug occurred.
	 */
	static private final class BugOccurrence {
		private int m_count;

		private int m_lastReportedCount;

		private long m_since = System.currentTimeMillis();
	}

	static private final class TodoItem {
		private final int m_count;

		private final String m_hash;

		private final long m_since;

		public TodoItem(int count, String hash, long since) {
			m_count = count;
			m_hash = hash;
			m_since = since;
		}
	}
}
