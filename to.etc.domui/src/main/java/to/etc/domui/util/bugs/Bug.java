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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Accessor to post odd conditions for later review.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2010
 */
final public class Bug {
	static private final ThreadLocal<List<IBugListener>> m_threadListener = new ThreadLocal<>();

	static private List<IBugListener> m_listeners = new ArrayList<>();

	static private List<IBugInfoContributor> m_contributors = new ArrayList<>();

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
		addContributions(bi);										// Let all contributors contribute

		List<IBugListener> threadListeners = getThreadListeners();
		report(threadListeners, bi);
		List<IBugListener> gll = getGlobalListeners();
		report(gll, bi);
		if(true || (gll.size() == 0 && threadListeners.size() == 0)) {
			//-- No one listens -> report to console.
			System.out.println(bi.toString());
			Throwable x = bi.getException();
			if(null != x) {
				x.printStackTrace();
			}
		}
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
	 * be threadsafe.
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

	static public synchronized void addConstributor(IBugInfoContributor contributor) {
		List<IBugInfoContributor> contributors = m_contributors;
		if(contributors.contains(contributor))
			return;
		contributors = new ArrayList<>(contributors);
		contributors.add(contributor);
		m_contributors = contributors;
	}

	static public synchronized void removeConstributor(IBugInfoContributor contributor) {
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
		BugItem bug = new BugItem(message, t, contextItems);
		postBug(bug);
	}
}
