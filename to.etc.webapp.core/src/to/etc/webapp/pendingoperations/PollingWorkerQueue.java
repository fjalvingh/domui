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
 * Generic Executor which polls for jobs to execute. Providers for jobs can be easily registered.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 4, 2009
 */
public class PollingWorkerQueue {
	static private PollingWorkerQueue m_instance = new PollingWorkerQueue();

	private boolean m_initialized;

	private boolean m_terminating;

	private int m_runningThreads;

	private int m_threadsExecutingTasks;

	private int m_minThreads = 5;

	private int m_maxThreads;

	private final List<IPollQueueTaskProvider> m_providerList = new ArrayList<IPollQueueTaskProvider>();

	private int m_nextProviderIndex;

	private long m_tsLastCheck;

	private long m_tsLastBlock;

	private final long m_checkInterval = 10 * 1000;

	private final PolledActionQueue m_actionQueue = new PolledActionQueue();

	static public void initialize() throws Exception {
		m_instance.init();
	}

	static public PollingWorkerQueue getInstance() {
		m_instance.checkInit();
		return m_instance;
	}

	private synchronized void checkInit() {
		if(!m_initialized)
			throw new IllegalStateException("PollingWorkerQueue has not been initialized");
	}

	/**
	 * Initialize: run minThread threads.
	 * @throws Exception
	 */
	private synchronized void init() throws Exception {
		if(m_minThreads <= 0)
			m_minThreads = 2;
		if(m_maxThreads < m_minThreads)
			m_maxThreads = m_minThreads + 5;
		for(int i = 0; i < m_minThreads; i++)
			startThread();
		m_initialized = true;
		registerProvider(m_actionQueue);
	}

	private void startThread() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				handlerThreadMain();
			}
		});
		t.setDaemon(true);
		t.setPriority(Thread.NORM_PRIORITY);
		t.setName("PollExecutor");
		t.start();
	}

	/**
	 * Register another provider to get tasks from.
	 * @param provider
	 */
	public void registerProvider(final IPollQueueTaskProvider provider) {
		synchronized(this) {
			if(m_providerList.contains(provider))
				throw new IllegalStateException("Duplicate registration of provider=" + provider);
			m_providerList.add(provider);
			notifyAll();

			try {
				provider.initializeOnRegistration(this);
			} catch(Exception x) {
				System.out.println("pwq: initialization of " + provider + " failed with " + x);
				x.printStackTrace();
			}
		}
	}

	public synchronized void checkProvider(final IPollQueueTaskProvider provider) {
		if(m_runningThreads == 0 || m_terminating)
			throw new IllegalStateException("The PollingExecutor service is NOT RUNNING");
		notify();
	}

	public synchronized void terminate() {
		if(m_initialized && !m_terminating) {
			m_terminating = true;
			notifyAll();
		}
	}

	public void addWork(final Runnable run) {
		m_actionQueue.schedule(run);
	}

	public long getTsLastBlock() {
		return m_tsLastBlock;
	}

	public long getTsLastCheck() {
		return m_tsLastCheck;
	}

	public synchronized int getRunningThreads() {
		return m_runningThreads;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Worker thread code.									*/
	/*--------------------------------------------------------------*/
	/**
	 * The handler for each worker thread. Each thread is fully equal to each other thread. A worker is
	 * either obtaining work (by polling each provider or waiting till it's time to poll again), or is
	 * executing work obtained earlier.
	 */
	void handlerThreadMain() {
		//-- Notice initialization.
		synchronized(this) {
			m_runningThreads++; // One more thread is running,
		}

		//-- Worker maincode; protected by finally to decrement #running threads.
		Throwable error = null;
		try {
			protectedMain();
		} catch(Throwable t) {
			error = t;
		} finally {
			synchronized(this) {
				m_runningThreads--;
				if(m_terminating)
					error = null;
			}

			if(error != null) {
				System.err.println("PollingExecutor: worker thread TERMINATED with error=" + error);
				error.printStackTrace();
			}
		}
	}

	private void protectedMain() {
		int ntodo = -1;
		for(;;) {
			long cts = System.currentTimeMillis();

			//-- Select a provider to query for work, and handle blocking if all of them were queried.
			IPollQueueTaskProvider provider;
			synchronized(this) {
				if(m_terminating) // Normal termination -> exit immediately
					return;

				//-- Am I running/starting in a "scanning all providers" loop?
				if(ntodo == -1)
					ntodo = m_providerList.size();
				if(ntodo == 0) {
					//-- Completed loop/no work to do. Sleep till it's time to check again,
					try {
						m_tsLastBlock = cts;
						wait(m_checkInterval);
					} catch(InterruptedException x) {}

					//-- Woke up. Must be time for another scan.
					cts = System.currentTimeMillis();
				}

				//-- We need to check the next provider;
				m_tsLastCheck = cts;
				int pls = m_providerList.size();
				if(pls == 0) {
					ntodo = 0;
					continue;
				}
				if(m_nextProviderIndex >= pls)
					m_nextProviderIndex = 0;
				provider = m_providerList.get(m_nextProviderIndex++);
			}

			//-- Outside of the main lock, ask the provider for work.
			Runnable task = null;
			try {
				task = provider.getRunnableTask();
			} catch(Exception x) {
				x.printStackTrace(); // Just dump && ignore
			}

			//-- If there's nothing to do - decrement the check count and loop further.
			if(task == null) {
				ntodo--;
				continue;
			}

			//-- We have one!! Execute it, and leave todo unaltered. If the #of executing tasks is >= the actual #of tasks try to add a new one;
			synchronized(this) {
				m_threadsExecutingTasks++;
				if(m_threadsExecutingTasks >= m_runningThreads) { // All thingies are executing stuff?
					if(m_runningThreads < m_maxThreads) { // We have threads to spare - start another one
						startThread();
					}
				}
			}
			try {
				task.run();
			} catch(Exception x) {
				x.printStackTrace(); // On failure just dump;
			} finally {
				synchronized(this) {
					m_threadsExecutingTasks--;
				}
			}
		}
	}
}
