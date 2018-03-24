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
package to.etc.domui.state;

import to.etc.domui.component.delayed.AsyncContainer;
import to.etc.domui.component.delayed.IAsyncListener;
import to.etc.domui.component.delayed.IAsyncRunnable;
import to.etc.domui.server.DomApplication;
import to.etc.util.Progress;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

final public class DelayedActivityInfo {
	public enum State {
		WAITING,
		RUNNING,
		DONE
	}

	final private DelayedActivitiesManager m_manager;

	final private AsyncContainer m_container;

	final private IAsyncRunnable m_activity;

	final private Progress m_monitor = new Progress("");

	private Exception m_exception;

	@Nonnull private State m_state = State.WAITING;

	@Nonnull
	final private Map<IAsyncListener< ? >, Object> m_listenerDataMap = new HashMap<IAsyncListener< ? >, Object>();

	protected DelayedActivityInfo(@Nonnull DelayedActivitiesManager manager, @Nonnull IAsyncRunnable activity, @Nonnull AsyncContainer ac) {
		m_activity = activity;
		m_manager = manager;
		m_container = ac;
	}

	public IAsyncRunnable getActivity() {
		return m_activity;
	}

	@Nonnull
	public Progress getMonitor() {
		return m_monitor;
	}

	public State getState() {
		synchronized(m_manager) {
			return m_state;
		}
	}

	void setState(State state) {
		synchronized(m_manager) {
			m_state = state;
		}
	}

	public void finished(@Nullable Exception errorx) {
		synchronized(m_manager) {
			m_state = State.DONE;
			m_exception = errorx;
		}
	}


	public Exception getException() {
		synchronized(m_manager) {
			return m_exception;
		}
	}

	public void cancel() {
		m_manager.cancelActivity(this);
	}

	public AsyncContainer getContainer() {
		return m_container;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handling listeners.									*/
	/*--------------------------------------------------------------*/

	/**
	 * Call the "scheduled" listeners and store their context.
	 * @throws Exception
	 */
	void callScheduled() throws Exception {
		//-- 1. Call all listeners.
		for(IAsyncListener< ? > al : m_container.getPage().getApplication().getAsyncListenerList()) {
			handleListenerScheduled(al);
		}
	}

	private <T> void handleListenerScheduled(IAsyncListener<T> al) throws Exception {
		T resval = al.onActivityScheduled(m_activity);
		m_listenerDataMap.put(al, resval);
	}

	public void callBeforeListeners() throws Exception {
		for(IAsyncListener< ? > al : DomApplication.get().getAsyncListenerList()) {
			handleListenerBefore(al);
		}
	}

	private <T> void handleListenerBefore(IAsyncListener<T> al) throws Exception {
		T context = (T) m_listenerDataMap.get(al);// Any data stored by scheduler
		al.onActivityStart(m_activity, context);
	}

	public void callAfterListeners() {
		for(IAsyncListener< ? > al : DomApplication.get().getAsyncListenerList()) {
			handleListenerAfter(al);
		}
	}

	private <T> void handleListenerAfter(IAsyncListener<T> al) {
		try {
			T context = (T) m_listenerDataMap.get(al);// Any data stored by scheduler
			al.onActivityEnd(m_activity, context);
		} catch(Exception x) {
			System.err.println("Ignored exception in IAsyncListener#onEnd: " + x);
			x.printStackTrace();
		}
	}

	public void checkIsPageConnected() {
		try {
			m_container.getPage();
		} catch(IllegalStateException x) {
			System.err.println("Ignored exception when container is not connected to page, something is about to fail because of " + x);
			x.printStackTrace();
		}
	}
}
