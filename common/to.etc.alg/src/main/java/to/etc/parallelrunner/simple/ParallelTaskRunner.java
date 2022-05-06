package to.etc.parallelrunner.simple;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.parallelrunner.AsyncWorker;
import to.etc.parallelrunner.IAsyncRunnable;
import to.etc.parallelrunner.simple.ParallelTaskRunner.IAsyncRunnableSimpleTask;
import to.etc.util.Progress;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public final class ParallelTaskRunner<T extends IAsyncRunnableSimpleTask> {

	/**
	 * Used with ParallelTaskRunner
	 */
	@NonNullByDefault
	public interface IAsyncRunnableSimpleTask extends IAsyncRunnable {

		void onBeforeScheduled();

		void onAfterExecuted(boolean cancelled, @Nullable Exception exception);
	}

	private final AsyncWorker m_asyncWorker;

	private Progress m_progress;

	private List<SimpleTaskExecutor<T>> m_idleExecutors;

	private List<SimpleTaskExecutor<T>> m_busyExecutors = new ArrayList<>();

	private final ReentrantLock m_lock = new ReentrantLock();

	private final Condition m_hasIdleExecutorsCondition = m_lock.newCondition();

	private final Condition m_noBusyExecutorsCondition = m_lock.newCondition();

	private boolean m_allTasksAdded = false;

	private int m_tasksAdded = 0;

	private int m_tasksCompleted = 0;

	public ParallelTaskRunner(Progress p, int capacity, AsyncWorker asyncWorker) {
		m_progress = p;
		m_asyncWorker = asyncWorker;

		m_idleExecutors = new ArrayList<>();
		for (int i = 1; i <= capacity; i++) {
			SimpleTaskExecutor<T> executor = new SimpleTaskExecutor<>(this, i);
			m_idleExecutors.add(executor);
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
			m_busyExecutors.add(simpleTaskExecutor);
			m_tasksAdded++;
			//System.out.println("Added to m_busyExecutors, size: " + m_busyExecutors.size());
			simpleTaskExecutor.schedule(m_progress, task);
		}finally {
			m_lock.unlock();
		}
	}

	void reportTaskDone(SimpleTaskExecutor<T> executor, T task, boolean cancelled, Exception ex) {
		m_lock.lock();
		try {
			m_busyExecutors.remove(executor);
			m_tasksCompleted++;
			//System.out.println("Removed from m_busyExecutors, size: " + m_busyExecutors.size());
			m_idleExecutors.add(executor);
			m_hasIdleExecutorsCondition.signal();
			if(m_busyExecutors.isEmpty() && m_allTasksAdded) {
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
				if(m_busyExecutors.isEmpty()) {
					break;
				}
				m_noBusyExecutorsCondition.await();
			}
		}finally {
			m_lock.unlock();
		}
		System.out.println("** All finished");
	}

	protected void onTaskStarted(T task) {
	}

	protected void onFinish(T task) {
	}

	protected void handleCancelledAfter(T task) {
	}

	boolean isCancelled() {
		return Objects.requireNonNull(m_progress).isCancelled();
	}

	public synchronized List<T> getRunningTasks() {
		return m_busyExecutors.stream().map(it -> it.getTask()).collect(Collectors.toList());
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
	static final public class SimpleTaskExecutor<V extends IAsyncRunnableSimpleTask>  {
		private final ParallelTaskRunner<V> m_runner;

		private V m_task;

		private final int m_index;

		public SimpleTaskExecutor(ParallelTaskRunner<V> runner, int index) {
			m_runner = runner;
			m_index = index;
		}

		public void schedule(Progress p, V task) {
			m_task = task;
			//System.out.println("Scheduling task: " + toString());
			task.onBeforeScheduled();
			m_runner.m_asyncWorker.schedule(toString(), task, (cancelled, exception) -> {
				m_task = null;
				task.onAfterExecuted(cancelled, exception);
				m_runner.reportTaskDone(this, task, cancelled, exception);
			});
		}

		@Nullable
		public V getTask() {
			return m_task;
		}

		@Override public String toString() {
			V task = m_task;
			String me = "T" + m_index + ": ";
			if(null == task) {
				me += "idle";
			}else {
				me += task.toString();
			}
			return me;
		}
	}
}

