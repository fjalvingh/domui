package to.etc.parallelrunner.simple;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.parallelrunner.IAsyncRunnable;
import to.etc.parallelrunner.simple.ParallelTaskRunner.IAsyncRunnableSimpleTask;
import to.etc.util.Progress;
import to.etc.util.WrappedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class ParallelTaskRunner<T extends IAsyncRunnableSimpleTask> {

	/**
	 * Used with ParallelTaskRunner
	 */
	@NonNullByDefault
	public interface IAsyncRunnableSimpleTask extends IAsyncRunnable {

		void onBeforeScheduled();

		void onAfterExecuted(boolean cancelled, @Nullable Exception exception);
	}

	private Progress m_progress;

	private final List<SimpleTaskExecutor<T>> m_idleExecutors;

	private final List<SimpleTaskExecutor<T>> m_allExecutors = new ArrayList<>();

	private final ReentrantLock m_lock = new ReentrantLock();

	private final Condition m_hasIdleExecutorsCondition = m_lock.newCondition();

	private final Condition m_noBusyExecutorsCondition = m_lock.newCondition();

	private boolean m_allTasksAdded = false;

	private int m_tasksAdded = 0;

	private int m_tasksCompleted = 0;

	public ParallelTaskRunner(Progress p, int capacity) {
		m_progress = p;

		m_idleExecutors = new ArrayList<>();
		for (int i = 1; i <= capacity; i++) {
			SimpleTaskExecutor<T> executor = new SimpleTaskExecutor<>(this, i);
			m_idleExecutors.add(executor);
			m_allExecutors.add(executor);
			executor.start();
		}
	}

	public void addTask(T task) throws InterruptedException {
		m_lock.lock();
		try {
			for(;;) {
				if(!m_idleExecutors.isEmpty()) {
					break;
				}
				m_hasIdleExecutorsCondition.await();
			}
			SimpleTaskExecutor simpleTaskExecutor = m_idleExecutors.remove(0);
			m_tasksAdded++;
			//System.out.println("Added to m_busyExecutors, size: " + m_busyExecutors.size());
			simpleTaskExecutor.schedule(task);
		}finally {
			m_lock.unlock();
		}
	}

	void reportTaskDone(SimpleTaskExecutor<T> executor, T task, boolean cancelled, Exception ex) {
		m_lock.lock();
		try {
			m_tasksCompleted++;
			//System.out.println("Removed from m_busyExecutors, size: " + m_busyExecutors.size());
			m_idleExecutors.add(executor);
			m_hasIdleExecutorsCondition.signal();
			if(m_idleExecutors.size() == m_allExecutors.size() && m_allTasksAdded) {
				//System.out.println("Signaling m_noBusyExecutorsCondition!");
				m_noBusyExecutorsCondition.signal();
			}
		}finally {
			m_lock.unlock();
		}
		if(cancelled) {
			System.out.println("task cancelled: " + task + "");
		}
		if(null != ex) {
			System.out.println("task has exception: " + ex.getMessage());
			ex.printStackTrace();
		}

		//TODO report exception
	}

	public void waitAllCompleted() throws Exception {
		m_allTasksAdded = true;
		System.out.println("** waiting for all task to finish");
		m_lock.lock();
		try {
			for(;;) {
				if(m_idleExecutors.size() == m_allExecutors.size()) {
					break;
				}
				m_noBusyExecutorsCondition.await();
			}
		}finally {
			m_lock.unlock();
		}
		for(SimpleTaskExecutor<T> executor: m_allExecutors) {
			executor.signalTerminate();
		}
		for(SimpleTaskExecutor<T> executor: m_allExecutors) {
			executor.join();
		}

		System.out.println("** All finished");
	}

	public void cancelAll() throws Exception {
		for(SimpleTaskExecutor<T> executor: m_allExecutors) {
			executor.signalTerminate();
		}
		for(SimpleTaskExecutor<T> executor: m_allExecutors) {
			executor.join();
		}

		System.out.println("** Cancel completed");
	}

	public int getNumberOfRunningExecutors() {
		m_lock.lock();
		try {
			return m_allExecutors.size() - m_idleExecutors.size();
		} finally {
			m_lock.unlock();
		}
	}

	public int getTasksAdded() {
		return m_tasksAdded;
	}

	public int getTasksCompleted() {
		return m_tasksCompleted;
	}

	/**
	 * The executor that runs tasks provided by runner.
	 */
	static final public class SimpleTaskExecutor<V extends IAsyncRunnableSimpleTask> extends Thread {
		private final ParallelTaskRunner<V> m_runner;

		@Nullable
		private V m_task;

		private final int m_index;

		private boolean m_terminate;

		public SimpleTaskExecutor(ParallelTaskRunner<V> runner, int index) {
			super();
			setDaemon(true);
			setName("pt" + index);
			m_runner = runner;
			m_index = index;
		}

		public void schedule(V task) {
			synchronized(this) {
				task.onBeforeScheduled();
				m_task = task;
				notifyAll();
			}
		}

		@Override public String toString() {
			V task = m_task;
			String me = getName() + ": ";
			if(null == task) {
				me += "idle";
			}else {
				me += task.toString();
			}
			return me;
		}

		@Override
		public void run() {
			for(;;) {
				V task;
				synchronized(this) {
					for(;;) {
						if(m_terminate) {
							return;
						}
						task = m_task;
						if(null != task) {
							m_task = null;
							break;
						}
						try {
							wait();
						} catch(InterruptedException e) {
							e.printStackTrace();
							throw WrappedException.wrap(e);
						}
					}
				}

				try {
					task.run(m_runner.m_progress);
					m_runner.reportTaskDone(this, task, false, null);
					task.onAfterExecuted(false, null);
				} catch(Exception e) {
					m_runner.reportTaskDone(this, task, false, e);
					task.onAfterExecuted(false, e);
				}
			}
		}

		void signalTerminate() {
			synchronized(this) {
				m_terminate = true;
				notifyAll();
			}
		}
	}
}

