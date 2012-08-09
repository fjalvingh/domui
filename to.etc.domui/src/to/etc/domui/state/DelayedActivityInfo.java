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

import to.etc.domui.component.delayed.*;
import to.etc.domui.dom.html.*;

public class DelayedActivityInfo {
	private DelayedActivitiesManager m_manager;

	private AsyncContainer m_container;

	private IActivity m_activity;

	private DelayedProgressMonitor m_monitor;

	private Exception m_exception;

	private Div m_executionResult;

	private int m_pctComplete = -1;

	private String m_statusMessage;
	
	protected DelayedActivityInfo(DelayedActivitiesManager manager, IActivity activity, AsyncContainer ac) {
		m_activity = activity;
		m_manager = manager;
		m_container = ac;
	}

	public IActivity getActivity() {
		return m_activity;
	}

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

	public Div getExecutionResult() {
		return m_executionResult;
	}

	void setExecutionResult(Div executionResult) {
		m_executionResult = executionResult;
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
}
