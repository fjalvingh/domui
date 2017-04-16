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
package to.etc.webapp.pendingoperations;

import java.util.*;

/**
 * Queue containing jobs to execute. When jobs are added here the polling task
 * provider will be signalled.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 4, 2009
 */
public class PolledActionQueue implements IPollQueueTaskProvider {
	private PollingWorkerQueue m_executor;

	private Queue<Runnable> m_queue = new LinkedList<Runnable>();

	/**
	 * Adds a job to the execution queue. If a free thread is available the job gets
	 * run immediately.
	 * @param run
	 */
	public void schedule(Runnable run) {
		synchronized(m_executor) {
			if(m_queue.size() > 1000)
				throw new IllegalStateException("FATAL: The background execution queue is FULL (more than 1000 waiting jobs)");
			m_queue.add(run);
			m_executor.checkProvider(this);
		}
	}

	@Override
	public void initializeOnRegistration(PollingWorkerQueue pwq) throws Exception {
		m_executor = pwq;
	}

	@Override
	public Runnable getRunnableTask() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
