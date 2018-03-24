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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.delayed.AsyncContainer;
import to.etc.domui.component.delayed.IActivity;
import to.etc.domui.component.delayed.IAsyncRunnable;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Page;
import to.etc.domui.state.DelayedActivityInfo.State;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This helper class does all of the handling for delayed activities for
 * a conversation. It contains all activity queues plus all handling of
 * the executor thread.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 7, 2008
 */
final public class DelayedActivitiesManager implements Runnable {
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
	public DelayedActivityInfo schedule(@Nonnull IAsyncRunnable a, @Nonnull AsyncContainer ac) throws Exception {
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
	 */
	public boolean cancelActivity(@Nonnull DelayedActivityInfo dai) {
		Thread tr;

		synchronized(this) {
			if(m_pendingQueue.remove(dai)) {
				dai.getContainer().confirmCancelled();
				return true;
			}

			//-- Is this thingy currently running?
			DelayedActivityInfo runningActivity = m_runningActivity;
			if(runningActivity != dai || runningActivity == null)
				return false;

			//-- The activity is currently running. Try to abort the task && thread.
			tr = m_executorThread;
			runningActivity.getMonitor().cancel();				// Force cancel indication.
		}
		tr.interrupt();
		return true;
	}

	private void wakeupListeners(int lingertime) {}

	/**
	 * Retrieves the current activity state. This creates Progress records for
	 * all activities currently active, and returns (and removes) all activities
	 * that have completed.
	 */
	private List<DelayedActivityInfo> getState() {
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

	/**
	 * Returns whether the client needs to use it's polltimer again and poll for changes.
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
			m_pendingQueue.forEach(a -> a.setState(State.DONE));
			m_pendingQueue.clear();
			wakeupListeners(100);				// Wake up anything that's listening quickly
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
	 */
	@Override
	public void run() {
		try {
			for(;;) {
				//-- Are we attempting to die?
				DelayedActivityInfo dai;
				synchronized(this) {
					if(m_terminated) 				// Manager is deadish?
						return; 					// Just quit immediately (nothing is currently running)

					//-- Anything to do?
					if(m_pendingQueue.size() == 0) {	// Something queued still?
						//-- Nope. We can stop properly.
						return;
					}

					//-- Schedule for a new execute.
					dai = m_pendingQueue.remove(0); 	// Get and remove from pending queue
					m_runningActivity = dai; 			// Make this the running dude
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
	 */
	private void execute(DelayedActivityInfo dai) {
		Exception errorx = null;
		try {
			dai.checkIsPageConnected();
			dai.callBeforeListeners();
			dai.getActivity().run(dai.getMonitor());
		} catch(Exception x) {
			if(!(x instanceof InterruptedException)) {
				errorx = x;
				if (LOG.isDebugEnabled()) {
					LOG.debug("Exception in async activity", x);
				}
			}
		} finally {
			dai.callAfterListeners();
		}

		//-- The activity has stopped. Register it for callback on the next page poll, so that it's result handler can be called.
		synchronized(this) {
			dai.finished(errorx);
			dai.setState(State.DONE);
			m_runningActivity = null; 		// Nothing is running anymore.
			if(m_terminated)				// Fondling a corpse? Ignore the result.
				return;

			//-- We're still alive; post the result in the done queue and awake listeners quickly.
			m_completionQueue.add(dai);		// Append to completion queue for access by whatever.
			wakeupListeners(1000);
		}
	}

	/**
	 * Apply all activity changes to the page. The page is either in "full render" or "delta render" modus.
	 */
	private void applyToTree(List<DelayedActivityInfo> infoList) throws Exception {
		//-- Handle progress reporting
		for(DelayedActivityInfo dai : infoList) {
			AsyncContainer c = dai.getContainer();
			try {
				c.updateProgress(dai);
			} catch(Exception x) {
				System.err.println("Async action update exception: " + x);
				x.printStackTrace();
			}
		}
	}

	public void processDelayedResults(Page pg) throws Exception {
		List<DelayedActivityInfo> list = getState();
		if(! list.isEmpty())
			applyToTree(list);

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
