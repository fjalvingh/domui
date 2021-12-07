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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.delayed.AsyncContainer;
import to.etc.domui.component.delayed.IAsyncListener;
import to.etc.domui.server.DomApplication;
import to.etc.parallelrunner.IAsyncRunnable;
import to.etc.util.CancelledException;
import to.etc.util.Progress;
import to.etc.util.WrappedException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;

final public class DelayedActivityInfo {
	private static final Logger LOG = LoggerFactory.getLogger(DelayedActivitiesManager.class);

	public enum State {
		WAITING,
		RUNNING,
		CANCELLED,
		DONE
	}

	final private DelayedActivitiesManager m_manager;

	final private AsyncContainer m_container;

	final private IAsyncRunnable m_activity;

	final private Progress m_monitor = new Progress("");

	private Exception m_exception;

	@NonNull
	private State m_state = State.WAITING;

	@NonNull
	final private Map<IAsyncListener<?>, Object> m_listenerDataMap = new HashMap<IAsyncListener<?>, Object>();

	protected DelayedActivityInfo(@NonNull DelayedActivitiesManager manager, @NonNull IAsyncRunnable activity, @NonNull AsyncContainer ac) {
		m_activity = activity;
		m_manager = manager;
		m_container = ac;
	}

	void execute() {
		Exception errorx = null;
		try {
			checkIsPageConnected();
			callBeforeListeners();
			getActivity().run(getMonitor());
		} catch(InterruptedException ix) {
			//-- Were we cancelled?
			synchronized(m_manager) {
				if(m_state == State.CANCELLED) {
					errorx = new CancelledException();
				} else {
					errorx = ix;						// Really interrupted
				}
			}
		} catch(Exception x) {
			errorx = x;
			if(LOG.isDebugEnabled()) {
				LOG.debug("Exception in async activity", x);
			}
		} catch(Error x) {
			LOG.error("ERROR in async activity", x);
			errorx = new WrappedException(x);
		} finally {
			finished(errorx);
			callAfterListeners();
		}
	}

	public IAsyncRunnable getActivity() {
		return m_activity;
	}

	@NonNull
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

	void finished(@Nullable Exception errorx) {
		synchronized(m_manager) {
			m_state = State.DONE;
			m_exception = errorx;
		}
	}

	void cancelled() {
		synchronized(m_manager) {
			if(m_state == State.RUNNING || m_state == State.WAITING) {
				m_state = State.CANCELLED;
				m_exception = new CancellationException();
			}
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
	 */
	void callScheduled() throws Exception {
		for(IAsyncListener<?> al : m_container.getPage().getApplication().getAsyncListenerList()) {
			handleListenerScheduled(al);
		}
	}

	private <T> void handleListenerScheduled(IAsyncListener<T> al) throws Exception {
		T resval = al.onActivityScheduled(m_activity);
		m_listenerDataMap.put(al, resval);
	}

	public void callBeforeListeners() throws Exception {
		for(IAsyncListener<?> al : DomApplication.get().getAsyncListenerList()) {
			handleListenerBefore(al);
		}
	}

	private <T> void handleListenerBefore(IAsyncListener<T> al) throws Exception {
		T context = (T) m_listenerDataMap.get(al);// Any data stored by scheduler
		al.onActivityStart(m_activity, context);
	}

	public void callAfterListeners() {
		for(IAsyncListener<?> al : DomApplication.get().getAsyncListenerList()) {
			handleListenerAfter(al);
		}
	}

	private <T> void handleListenerAfter(IAsyncListener<T> al) {
		try {
			T context = (T) m_listenerDataMap.get(al);
			al.onActivityEnd(m_activity, context);
		} catch(Exception x) {
			LOG.warn("Ignored exception in IAsyncListener#onEnd: " + x, x);
		}
	}

	public void checkIsPageConnected() {
		try {
			m_container.getPage();
		} catch(IllegalStateException x) {
			LOG.warn("Ignored exception when container is not connected to page, something is about to fail because of " + x, x);
		}
	}
}
