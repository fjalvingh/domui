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
package to.etc.domui.component.delayed;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.delayed.DelayedActivityInfo.State;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Page;
import to.etc.domui.server.DomApplication;
import to.etc.domui.state.IPolledForUpdate;
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

	/**
	 * All activities that need to be scheduled but have not yet been send
	 * to the executor.
	 */
	private List<DelayedActivityInfo> m_scheduledQueue = new ArrayList<>();

	/**
	 * All activities sent to the executor but not yet in progress.
	 */
	private List<DelayedActivityInfo> m_pendingQueue = new ArrayList<>();

	private List<DelayedActivityInfo> m_runningQueue = new ArrayList<>();

	/**
	 * All activities that have completed and are not yet handled by the UI.
	 */
	private List<DelayedActivityInfo> m_completedQueue = new ArrayList<>();

	/**
	 * Activities present in any of the queues.
	 */
	private Set<IAsyncRunnable> m_actionSet = new HashSet<>();

	//@Nullable
	//private DelayedActivityInfo m_runningActivity;

	/** When set this forces termination of any handling thread for the asynchronous actions. */
	private boolean m_terminated;

	/**
	 * The set of nodes that need a callback for changes to the UI every polltime seconds.
	 */
	private Set<NodeContainer> m_pollSet = new HashSet<NodeContainer>();

	public DelayedActivitiesManager() {
	}

	/**
	 * Schedule a new activity for execution. This does not actually start the executor; it merely queues the thingy. If
	 * the executor *is* running though it can start with the action.
	 */
	public DelayedActivityInfo schedule(@NonNull IAsyncRunnable a, @NonNull AsyncContainer ac) throws Exception {
		//-- Schedule.
		int prio;
		synchronized(this) {
			if(! m_actionSet.add(a))
				throw new IllegalStateException("The same activity instance is ALREADY scheduled!!");
			prio = m_runningQueue.size() + m_pendingQueue.size();		// The more is scheduled the lower the prio
		}
		DelayedActivityInfo dai = new DelayedActivityInfo(this, a, ac, prio);

		//-- Call listeners.
		dai.callScheduled();

		synchronized(this) {
			m_scheduledQueue.add(dai);
			return dai;
		}
	}

	/**
	 * Called when processing of the recently scheduled items can
	 * start. Add all "scheduled" items to the executor.
	 */
	public void start() {
		synchronized(this) {
			if(m_scheduledQueue.isEmpty()) {				// Nothing to do?
				return;
			}
			DelayedActivitiesExecutor dx = DomApplication.get().getDelayedExecutor();
			for(DelayedActivityInfo dai : m_scheduledQueue) {
				dx.schedule(dai);
				m_pendingQueue.add(dai);
			}
			m_scheduledQueue.clear();
		}
	}

	/**
	 * Cancels an activity, if possible. If the thing is pending it gets removed. If it is
	 * executing we try to cancel the executor.
	 * If the cancel worked the cancelled activity will get a callback indicating that
	 * it was cancelled. If the activity completed before it could be cancelled it
	 * does NOT get this cancellation thing- but a completion event instead.
	 */
	void cancelActivity(@NonNull DelayedActivityInfo dai) {
		Thread tr;

		synchronized(this) {
			DelayedActivitiesExecutor dx = DomApplication.get().getDelayedExecutor();

			//-- If it is not yet running remove it and mark as cancelled
			if(m_scheduledQueue.remove(dai) || m_pendingQueue.remove(dai)) {
				m_completedQueue.add(dai);						// This should cause a cancelled callback to take place
				dai.finished(new CancelledException());			// Cancelled and finished
				dx.remove(dai);
				return;
			}

			//-- If we're already done or cancelled -> ignore
			if(dai.getState() == State.CANCELLED || dai.getState() == State.DONE)
				return;

			//-- Always mark it as cancelled
			dai.setCancelled();

			//-- The activity is currently running. Try to abort the task && thread.
			dai.getMonitor().cancel();							// Also cancel the monitor, this might also kill the action
		}

		//-- We're cancelling... If the receiver can accept cancels call it to do that.
		IAsyncRunnable activity = dai.getActivity();
		try {
			activity.cancel(() -> dai.interrupt());
		} catch(Exception x) {
			LOG.error("Cancelling activity " + activity + " failed: " + x, x);
		}
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
			//-- Always show all running thingies
			List<DelayedActivityInfo> result = new ArrayList<>(m_runningQueue);

			//-- And add all completed thingies.
			result.addAll(m_completedQueue);
			m_completedQueue.clear();
			return result;
		}
	}

	/**
	 * Returns whether the client needs to use it's polltimer again and poll for changes.
	 */
	public boolean callbackRequired() {
		synchronized(this) {
			return !m_scheduledQueue.isEmpty()
				|| !m_pendingQueue.isEmpty()
				|| !m_completedQueue.isEmpty()
				|| !m_runningQueue.isEmpty()
				|| !m_pollSet.isEmpty();
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

		List<DelayedActivityInfo> killList = new ArrayList<>();
		synchronized(this) {
			if(m_terminated)
				return;
			m_terminated = true;

			//-- First, cancel all pending and unscheduled
			for(DelayedActivityInfo dai : m_scheduledQueue) {
				dai.setCancelled();
			}
			m_scheduledQueue.clear();

			DelayedActivitiesExecutor dx = DomApplication.get().getDelayedExecutor();
			for(DelayedActivityInfo dai : m_pendingQueue) {
				dai.setCancelled();
				dx.remove(dai);
			}
			m_pendingQueue.clear();

			//-- All running ones: they need to be killed, do that outside the lock
			for(DelayedActivityInfo dai : m_runningQueue) {
				dai.setCancelled();
				killList.add(dai);
			}
			m_runningQueue.clear();

			m_completedQueue.clear();
			wakeupListeners(100);				// Wake up anything that's listening quickly
		}

		//-- Do our utmost to kill the task, not gently.
		for(DelayedActivityInfo dai : killList) {
			try {
				dai.getMonitor().cancel();
				dai.cancel();
			} catch(Exception x) {
				LOG.error("Failed to cancel activity: " + x, x);
			}

			//-- Signal the thread
			try {
				dai.interrupt();
			} catch(Exception x) {
				//-- Ignore, nothing can be done
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

	/**
	 * Called when the activity starts to run.
	 */
	synchronized void registerRunning(DelayedActivityInfo dai) {
		m_pendingQueue.remove(dai);
		m_runningQueue.add(dai);
	}

	synchronized void registerCompleted(DelayedActivityInfo dai) {
		m_runningQueue.remove(dai);
		m_completedQueue.add(dai);
		m_actionSet.remove(dai.getActivity());				// No longer present
		wakeupListeners(1000);
	}
}
