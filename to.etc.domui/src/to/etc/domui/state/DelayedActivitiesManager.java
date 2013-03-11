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

import to.etc.domui.component.delayed.*;
import to.etc.domui.dom.html.*;

/**
 * This helper class does all of the handling for delayed activities for
 * a conversation. It contains all activity queues plus all handling of
 * the executor thread.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 7, 2008
 */
public class DelayedActivitiesManager implements Runnable {
	//	private ConversationContext			m_conversation;
	private Thread m_executorThread;

	private List<DelayedActivityInfo> m_pendingQueue = new ArrayList<DelayedActivityInfo>();

	private List<DelayedActivityInfo> m_completionQueue = new ArrayList<DelayedActivityInfo>();

	private DelayedActivityInfo m_runningActivity;

	/** When set this forces termination of any handling thread for the asynchronous actions. */
	private boolean m_terminated;

	//	/** When > 0, this defines that we need to poll continuously at at *least* this frequency (#of millis between polls) */
	//	private int m_continuousPollingInterval;

	/**
	 * The set of nodes that need a callback for changes to the UI every polltime seconds.
	 */
	private Set<NodeContainer> m_pollSet = new HashSet<NodeContainer>();

	protected DelayedActivitiesManager(ConversationContext conversation) {
	//		m_conversation = conversation;
	}

	/**
	 * Schedule a new activity for execution. This does not actually start the executor; it merely queues the thingy. If
	 * the executor *is* running though it can start with the action.
	 *
	 * @param a
	 * @return
	 */
	public DelayedActivityInfo schedule(IActivity a, AsyncContainer ac) {
		synchronized(this) {
			for(DelayedActivityInfo dai : m_pendingQueue) {
				if(dai.getActivity() == a)
					throw new IllegalStateException("The same activity instance is ALREADY scheduled!!");
			}

			DelayedActivityInfo dai = new DelayedActivityInfo(this, a, ac);
			m_pendingQueue.add(dai);
			return dai;
		}
	}

	public void cancelActivity(IActivity a) {
		DelayedActivityInfo d = null;
		synchronized(this) {
			for(DelayedActivityInfo dai : m_pendingQueue) {
				if(dai.getActivity() == a) {
					d = dai;
					break;
				}
			}
		}
		if(d == null)
			throw new IllegalStateException("Activity is not scheduled");
		cancelActivity(d);
	}

	/**
	 * Cancels an activity, if possible. If the thing is pending it gets removed. If it is
	 * executing we try to cancel the executor.
	 * @param dai
	 */
	public boolean cancelActivity(DelayedActivityInfo dai) {
		Thread tr;

		synchronized(this) {
			if(m_pendingQueue.remove(dai)) {
				dai.getContainer().confirmCancelled();
				return true;
			}

			//-- Is this thingy currently running?
			if(m_runningActivity != dai)
				return false;

			//-- The activity is currently running. Try to abort the task && thread.
			tr = m_executorThread;
			m_runningActivity.getMonitor().cancel(); // Force cancel indication.
		}
		tr.interrupt();
		return true;
	}

	private void wakeupListeners(int lingertime) {}

	void completionStateChanged(DelayedActivityInfo dai, int pct) {
		synchronized(this) {
			dai.setPercentageComplete(pct);
		}
	}


	/**
	 * Retrieves the current activity state. This creates Progress records for
	 * all activities currently active, and returns (and removes) all activities
	 * that have completed.
	 *
	 * @return
	 */
	private DelayedActivityState getState() {
		//		System.out.println("$$$$$ getState called.");
		synchronized(this) {
			List<DelayedActivityState.Progress> pl = Collections.EMPTY_LIST;

			//-- Do we need progress report(s)?
			if(m_runningActivity != null) {
				int pct = m_runningActivity.getPercentageComplete();
				if(pct > 0) {
					//					System.out.println("$$$$$ getState, pct="+pct);
					pl = new ArrayList<DelayedActivityState.Progress>();
					pl.add(new DelayedActivityState.Progress(m_runningActivity.getContainer(), pct, null));
				}
			}

			//-- Handle all completed thingies.
			List<DelayedActivityInfo> comp = m_completionQueue.size() == 0 ? (List<DelayedActivityInfo>) Collections.EMPTY_LIST : new ArrayList<DelayedActivityInfo>(m_completionQueue);
			m_completionQueue.clear();
			if(comp.size() == 0 && pl.size() == 0)
				return null;
			return new DelayedActivityState(pl, comp);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Executor thread control.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Initiate background processing, if needed. Returns T if background processing is active, or
	 * when data is present in the completion queue.
	 */
	public boolean start() {
		Thread t;
		synchronized(this) {
			if(m_executorThread != null) // Active thread?
				return true; // Begone.

			//-- Must a thread be started?
			if(m_pendingQueue.size() == 0) // Pending requests?
				return false; // Nope -> begone

			//-- Prepare to start the executor.
			m_executorThread = new Thread(this);
			m_executorThread.setName("xc");
			m_executorThread.setDaemon(true);
			m_executorThread.setPriority(Thread.MIN_PRIORITY);
			t = m_executorThread; // Prevent naked access to m_ variable even though it is untouched by other code.
		}
		t.start();
		return true;
	}

	//	/**
	//	 * Returns whether the client needs to use it's polltimer again and poll for changes. It returns 0 if there is no need to poll.
	//	 * @return
	//	 */
	//	public int getPollInterval() {
	//		synchronized(this) {
	//			//-- Determine the minimal poll interval.
	//			int pinterval = m_continuousPollingInterval;
	//			if(m_pendingQueue.size() > 0 || m_completionQueue.size() > 0 || m_runningActivity != null || m_pollSet.size() > 0) {
	//				int di = DomApplication.get().getDefaultPollInterval();
	//				if(pinterval == 0 || pinterval > di)
	//					pinterval = di;
	//			}
	//			return pinterval;
	//		}
	//	}

	/**
	 * Returns whether the client needs to use it's polltimer again and poll for changes.
	 * @return
	 */
	public boolean callbackRequired() {
		synchronized(this) {
			return m_pendingQueue.size() > 0 || m_completionQueue.size() > 0 || m_runningActivity != null || m_pollSet.size() > 0;
		}
	}


	public boolean isTerminated() {
		synchronized(this) {
			return m_terminated;
		}
	}

	/**
	 * Forcefully terminate all handling of actions. Called at conversation close time. This
	 * marks this thingy as terminated (which will prevent any still running thread from barfing
	 * all over it's corpse), then clears all queues and tries to clobber the thread to death. It
	 * does so by first marking the IActivity as "cancelled", then by interrupting the thread. After
	 * this the thread is left to it's own devices; if it does not stop immediately but only after
	 * it's task has completed it's "result completed" handler will catch the fact that it's actually
	 * trying to fondle a dead body and throw (up).
	 */
	public void terminate() {
		Thread killme = null;
		DelayedActivityInfo pendingcorpse = null;

		synchronized(this) {
			if(m_terminated)
				return;
			m_terminated = true;
			if(m_executorThread != null) {
				killme = m_executorThread;
				m_executorThread = null;
			}
			pendingcorpse = m_runningActivity;
			m_runningActivity = null;

			m_completionQueue.clear();
			m_pendingQueue.clear();
			wakeupListeners(100); // Wakeup anything that's listening quickly
		}

		//-- Do our utmost to kill the task, not gently.
		try {
			if(pendingcorpse != null)
				pendingcorpse.getMonitor().cancel(); // Forcefully cancel;
		} catch(Exception x) {
			x.printStackTrace();
		}

		//-- Signal the thread
		try {
			if(killme != null) {
				killme.interrupt();
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Executor thread.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Main action runnert. This is the thread's executor function. While the manager is
	 * active this will execute activities in the PENDING queue one by one until the queue
	 * is empty. If that happens it will commit suicide. This suicidal act will not invalidate
	 * the manager; at any time can new actions be posted and a new thread be started.
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			for(;;) {
				//-- Are we attempting to die?
				DelayedActivityInfo dai;
				synchronized(this) {
					if(m_terminated) // Manager is deadish?
						return; // Just quit immediately (nothing is currently running)

					//-- Anything to do?
					if(m_pendingQueue.size() == 0) { // Something queued still?
						//-- Nope. We can stop properly.
						return;
					}

					//-- Schedule for a new execute.
					dai = m_pendingQueue.remove(0); // Get and remove from pending queue
					m_runningActivity = dai; // Make this the running dude
				}
				execute(dai);
			}
		} catch(Exception x) {
			//-- Do not report trouble if the manager is in the process of dying
			if(!isTerminated()) {
				System.err.println("FATAL Exception in DelayedActivitiesManager.run()!??!?!?!?\nAsy tasks WILL NOT COMPLETE anymore.");
				x.printStackTrace();
			}
		} finally {
			/*
			 * Be very, very certain that we handle state @ thread termination properly.
			 */
			synchronized(this) {
				m_executorThread = null; // I'm gone...
			}
		}
	}

	/**
	 * Execute the action and handle it's result.
	 * @param dai
	 */
	private void execute(DelayedActivityInfo dai) {
		DelayedProgressMonitor mon = new DelayedProgressMonitor(this, dai);
		dai.setMonitor(mon);

		Exception errorx = null;
		Div result = null;
		try {
			result = dai.getActivity().run(mon);
		} catch(Exception x) {
			if(!(x instanceof InterruptedException))
				errorx = x;
		}

		/*
		 * Register the result.
		 */
		synchronized(this) {
			m_runningActivity = null; // Nothing is running anymore.
			if(m_terminated) // Fondling a corpse? Ignore the result.
				return;

			//-- We're still alive; post the result in the done queue and awake listeners quickly.
			if(errorx != null)
				dai.setException(errorx); // Mark as fatally wounded.
			else
				dai.setExecutionResult(result); // Mark as properly thingesed
			m_completionQueue.add(dai); // Append to completion queue for access by whatever.
			wakeupListeners(1000);
		}
	}

	/**
	 * Apply all activity changes to the page. The page is either in "full render" or "delta render" modus.
	 *
	 * @param das
	 */
	public void applyToTree(DelayedActivityState das) {
		//-- Handle progress reporting
		for(DelayedActivityState.Progress p : das.getProgressList()) {
			AsyncContainer c = p.getContainer();
			c.updateProgress(p.getPctComplete(), p.getMessage());
		}

		//-- Handle completion reporting
		for(DelayedActivityInfo dai : das.getCompletionList()) {
			AsyncContainer c = dai.getContainer();
			c.updateCompleted(dai);
		}
	}

	public void processDelayedResults(Page pg) throws Exception {
		DelayedActivityState das = getState();
		if(das != null)
			applyToTree(das);

		//-- Handle PollThingy callbacks.
		for(NodeContainer nc : m_pollSet) {
			((IPolledForUpdate) nc).checkForChanges();
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Polled UI node handling.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Registers a node as a thingy which needs to be called every polltime seconds to
	 * update the screen. This is not an asy action by itself (it starts no threads) but
	 * it will cause the poll handler to start, and will use the same response mechanism
	 * as the asy callback code.
	 *
	 * @param <T>
	 * @param nc
	 */
	public <T extends NodeContainer & IPolledForUpdate> void registerPoller(T nc) {
		m_pollSet.add(nc);
	}

	/**
	 * Deregister a node from the poll-regularly queue.
	 * @param <T>
	 * @param nc
	 */
	public <T extends NodeBase & IPolledForUpdate> void unregisterPoller(T nc) {
		m_pollSet.remove(nc);
	}

	//	/**
	//	 * Set (or reset) continuous polling at least [interval] times apart, in milliseconds.
	//	 * @param interval
	//	 */
	//	public void setContinuousPolling(int interval) {
	//		m_continuousPollingInterval = interval;
	//	}
}
