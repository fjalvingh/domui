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
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Page;
import to.etc.domui.state.DelayedActivityInfo.State;
import to.etc.parallelrunner.IAsyncRunnable;
import to.etc.util.CancelledException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This helper class does all the handling for delayed activities for
 * a conversation. It contains all activity queues plus all handling of
 * the executor thread.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 7, 2008
 */
final public class DelayedActivitiesManager {
	private static final Logger LOG = LoggerFactory.getLogger(DelayedActivitiesManager.class);

	private Thread m_executorThread;

	private List<DelayedActivityInfo> m_pendingQueue = new ArrayList<DelayedActivityInfo>();

	private List<DelayedActivityInfo> m_completionQueue = new ArrayList<DelayedActivityInfo>();

	@Nullable
	private DelayedActivityInfo m_runningActivity;

	/** When set this forces termination of any handling thread for the asynchronous actions. */
	private boolean m_terminated;

	/**
	 * The set of nodes that need a callback for changes to the UI every polltime seconds.
	 */
	private Set<NodeContainer> m_pollSet = new HashSet<NodeContainer>();

	protected DelayedActivitiesManager() {
	}

	/**
	 * Schedule a new activity for execution. This does not actually start the executor; it merely queues the thingy. If
	 * the executor *is* running though it can start with the action.
	 */
	public DelayedActivityInfo schedule(@NonNull IAsyncRunnable a, @NonNull AsyncContainer ac) throws Exception {
		//-- Schedule.
		synchronized(this) {
			for(DelayedActivityInfo tdai : m_pendingQueue) {
				if(tdai.getActivity() == a)
					throw new IllegalStateException("The same activity instance is ALREADY scheduled!!");
			}
		}
		DelayedActivityInfo dai = new DelayedActivityInfo(this, a, ac);

		//-- Call listeners.
		dai.callScheduled();

		synchronized(this) {
			m_pendingQueue.add(dai);
			return dai;
		}
	}

	//public void cancelActivity(IActivity a) {
	//	DelayedActivityInfo d = null;
	//	synchronized(this) {
	//		for(DelayedActivityInfo dai : m_pendingQueue) {
	//			if(dai.getActivity() == a) {
	//				d = dai;
	//				break;
	//			}
	//		}
	//	}
	//	if(d == null)
	//		throw new IllegalStateException("Activity is not scheduled");
	//	cancelActivity(d);
	//}

	/**
	 * Cancels an activity, if possible. If the thing is pending it gets removed. If it is
	 * executing we try to cancel the executor.
	 * If the cancel worked the cancelled activity will get a callback indicating that
	 * it was cancelled. If the activity completed before it could be cancelled it
	 * does NOT get this cancellation thing- but a completion event instead.
	 */
	public boolean cancelActivity(@NonNull DelayedActivityInfo dai) {
		Thread tr;

		synchronized(this) {
			//-- If it has not yet ran: remove it from the queue...
			if(m_pendingQueue.remove(dai)) {
				m_completionQueue.add(dai);						// This should cause a cancelled callback to take place
				dai.finished(new CancelledException());			// Cancelled and finished
				return true;
			}

			//-- If we're already done or cancelled -> ignore
			if(dai.getState() == State.CANCELLED || dai.getState() == State.DONE)
				return true;

			//-- Always mark it as cancelled
			dai.cancelled();

			//-- Is this thingy currently running?
			DelayedActivityInfo runningActivity = m_runningActivity;
			tr = m_executorThread;
			if(runningActivity != dai || runningActivity == null || tr == null) {
				//-- Not in queue but also not running -> this is a bug.
				if(! m_completionQueue.contains(dai))
					m_completionQueue.add(dai);					// Make sure it gets called back
				return false;
			}

			//-- The activity is currently running. Try to abort the task && thread.
			runningActivity.getMonitor().cancel();				// Force cancel indication.
		}

		//-- We're cancelling... If the receiver can accept cancels call it to do that.
		IAsyncRunnable activity = dai.getActivity();
		try {
			activity.cancel(tr);
		} catch(Exception x) {
			LOG.error("Cancelling activity " + activity + " failed: " + x, x);
		}
		return true;
	}

	private void wakeupListeners(int lingertime) {}

	/**
	 * Retrieves the current activity state. This creates Progress records for
	 * all activities currently active, and returns (and removes) all activities
	 * that have completed.
	 */
	private List<DelayedActivityInfo> getAllActivities() {
		//		System.out.println("$$$$$ getState called.");
		synchronized(this) {
			List<DelayedActivityInfo> result = new ArrayList<>(5);

			//-- Do we need progress report(s)?
			DelayedActivityInfo runningActivity = m_runningActivity;
			if(runningActivity != null) {
				result.add(runningActivity);
			}

			//-- Handle all completed thingies.
			result.addAll(m_completionQueue);
			m_completionQueue.clear();
			return result;
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
			if(m_executorThread != null)					// Active thread?
				return true;

			//-- Must a thread be started?
			if(m_pendingQueue.isEmpty()) 					// Pending requests?
				return false;

			//-- Prepare to start the executor.
			t = m_executorThread = new Thread(() -> run());
			m_executorThread.setName("xc");
			m_executorThread.setDaemon(true);
			m_executorThread.setPriority(Thread.MIN_PRIORITY);
		}
		t.start();
		return true;
	}

	/**
	 * Returns whether the client needs to use it's polltimer again and poll for changes.
	 */
	public boolean callbackRequired() {
		synchronized(this) {
			return !m_pendingQueue.isEmpty() || !m_completionQueue.isEmpty() || m_runningActivity != null || !m_pollSet.isEmpty();
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
			m_pendingQueue.forEach(a -> a.setState(State.DONE));
			m_pendingQueue.clear();
			wakeupListeners(100);				// Wake up anything that's listening quickly
		}

		//-- Do our utmost to kill the task, not gently.
		try {
			if(pendingcorpse != null)
				pendingcorpse.getMonitor().cancel();	// Forcefully cancel;
		} catch(Exception x) {
			LOG.error("Failed to cancel activity: " + x, x);
		}

		//-- Signal the thread
		try {
			if(killme != null) {
				killme.interrupt();
			}
		} catch(Exception x) {
			//-- Ignore, nothing can be done
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Executor thread.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Main action runner. This is the thread's executor function. While the manager is
	 * active this will execute activities in the PENDING queue one by one until the queue
	 * is empty. If that happens it will commit suicide. This suicidal act will not invalidate
	 * the manager; at any time can new actions be posted and a new thread be started.
	 */
	private void run() {
		try {
			for(;;) {
				//-- Are we attempting to die?
				DelayedActivityInfo dai;
				synchronized(this) {
					if(m_terminated || m_pendingQueue.isEmpty()) {
						/*
						 * Terminate means the page died, so we have nothing to do
						 * anymore. There is no way to report back the results of
						 * cancellation.
						 */
						m_pendingQueue.clear();
						m_executorThread = null;		// Prevent race - important
						return;							// Quit and terminate thread
					}

					//-- Schedule for a new execute.
					dai = m_pendingQueue.remove(0); 	// Get and remove from pending queue
					m_runningActivity = dai; 			// Make this the running dude
				}

				try {
					execute(dai);
				} finally {
					synchronized(this) {
						m_runningActivity = null;
					}
				}
			}
		} catch(Exception x) {
			//-- Do not report trouble if the manager is in the process of dying
			if(!isTerminated()) {
				LOG.error("FATAL Exception in DelayedActivitiesManager.run()!??!?!?!?\nAsy tasks WILL NOT COMPLETE anymore.", x);
			}
		} finally {
			/*
			 * Be very, very certain that we handle state @ thread
			 * termination properly. Watch out though: we still need
			 * to clear this when terminating inside the atomic block
			 * there to prevent a race.
			 */
			synchronized(this) {
				m_executorThread = null;
			}
		}
	}

	/**
	 * Execute the action and handle its result.
	 */
	private void execute(DelayedActivityInfo dai) {
		try {
			dai.execute();
		} finally {
			synchronized(this) {
				m_completionQueue.add(dai);		// Append to completion queue for access by whatever.
				wakeupListeners(1000);
			}
		}
	}

	/**
	 * Apply all activity changes to the page. The page is either in "full render" or "delta render" mode.
	 */
	private void applyToTree(List<DelayedActivityInfo> infoList) throws Exception {
		//-- Handle progress reporting
		for(DelayedActivityInfo dai : infoList) {
			AsyncContainer c = dai.getContainer();
			try {
				c.updateProgress(dai);
			} catch(Exception x) {
				LOG.error("Async action update exception: " + x, x);
			}
		}
	}

	public void processDelayedResults(Page pg) throws Exception {
		List<DelayedActivityInfo> list = getAllActivities();
		if(! list.isEmpty())
			applyToTree(list);

		//-- Handle executable requests from background tasks
		pg.internalPolledEntry();

		//-- Handle PollThingy callbacks.
		for(NodeContainer nc : new HashSet<>(m_pollSet)) {
			if(nc.isAttached()) {
				((IPolledForUpdate) nc).checkForChanges();
			}
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
	 */
	public <T extends NodeContainer & IPolledForUpdate> void registerPoller(T nc) {
		m_pollSet.add(nc);
	}

	/**
	 * Deregister a node from the poll-regularly queue.
	 */
	public <T extends NodeBase & IPolledForUpdate> void unregisterPoller(T nc) {
		m_pollSet.remove(nc);
	}
}
