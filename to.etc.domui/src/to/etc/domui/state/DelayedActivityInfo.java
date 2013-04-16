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

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.delayed.*;

public class DelayedActivityInfo {
	final private DelayedActivitiesManager m_manager;

	final private AsyncContainer m_container;

	final private IAsyncRunnable m_activity;

	private DelayedProgressMonitor m_monitor;

	private Exception m_exception;

	private int m_pctComplete = -1;

	private String m_statusMessage;

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
	public DelayedProgressMonitor getMonitor() {
		if(m_monitor == null)
			throw new IllegalStateException("? Unexpected access to monitor after task completed?");
		return m_monitor;
	}

	void setMonitor(DelayedProgressMonitor monitor) {
		m_monitor = monitor;
	}

	public Exception getException() {
		return m_exception;
	}

	void setException(Exception exception) {
		m_exception = exception;
	}

	public void cancel() {
		m_manager.cancelActivity(this);
	}

	int getPercentageComplete() {
		synchronized(m_manager) {
			return m_pctComplete;
		}
	}

	void setPercentageComplete(int pct) {
		m_pctComplete = pct;
	}

	String getStatusMessage() {
		synchronized(m_manager) {
			return m_statusMessage;
		}
	}

	void setStatusMessage(String statusMessage) {
		m_statusMessage = statusMessage;
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
		for(IAsyncListener< ? > al : m_container.getPage().getApplication().getAsyncListenerList()) {
			handleListenerBefore(al);
		}
	}

	private <T> void handleListenerBefore(IAsyncListener<T> al) throws Exception {
		T context = (T) m_listenerDataMap.get(al);						// Any data stored by scheduler
		al.onActivityStart(m_activity, context);
	}

	public void callAfterListeners() {
		for(IAsyncListener< ? > al : m_container.getPage().getApplication().getAsyncListenerList()) {
			handleListenerAfter(al);
		}
	}

	private <T> void handleListenerAfter(IAsyncListener<T> al) {
		try {
			T context = (T) m_listenerDataMap.get(al);						// Any data stored by scheduler
			al.onActivityEnd(m_activity, context);
		} catch(Exception x) {
			System.err.println("Ignored exception in IAsyncListener#onEnd: " + x);
			x.printStackTrace();
		}
	}
}
