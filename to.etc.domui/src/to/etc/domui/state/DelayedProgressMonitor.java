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

public class DelayedProgressMonitor implements IProgress {
	private DelayedActivitiesManager m_manager;

	private DelayedActivityInfo m_activity;

	private int m_maxWork = -1;

	private int m_currentWork;

	private boolean m_canceled;

	protected DelayedProgressMonitor(DelayedActivitiesManager manager, DelayedActivityInfo activity) {
		m_manager = manager;
		m_activity = activity;
	}

	@Override
	public void cancel() {
		synchronized(m_manager) {
			m_canceled = true;
		}
	}

	@Override
	public boolean isCancelled() {
		synchronized(m_manager) {
			return m_canceled;
		}
	}

	@Override
	public void setCompleted(int work) {
		if(isCancelled())
			throw new DelayedActivityCanceledException();
		if(work > m_currentWork && isReporting()) {
			if(work > m_maxWork)
				work = m_maxWork;
			m_currentWork = work;
			m_manager.completionStateChanged(m_activity, getPercentComplete());
		}
	}

	@Override
	public void setTotalWork(int work) {
		if(isCancelled())
			throw new DelayedActivityCanceledException();
		m_maxWork = work;
		m_manager.completionStateChanged(m_activity, 0);
	}

	public boolean isReporting() {
		return m_maxWork > 0;
	}

	int getPercentComplete() {
		if(m_maxWork <= 0)
			return 0;
		int pct = 100 * (m_currentWork) / m_maxWork;
		//		System.out.println("%%%% work="+m_currentWork+", max="+m_maxWork+", pct="+pct);
		return pct;
	}
}
