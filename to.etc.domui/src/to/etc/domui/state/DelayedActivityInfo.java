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

import javax.annotation.*;

import to.etc.domui.component.delayed.*;

public class DelayedActivityInfo {
	final private DelayedActivitiesManager m_manager;

	final private AsyncContainer m_container;

	final private IAsyncRunnable m_activity;

	private DelayedProgressMonitor m_monitor;

	private Exception m_exception;

	private int m_pctComplete = -1;

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

	public AsyncContainer getContainer() {
		return m_container;
	}
}
