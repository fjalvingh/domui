package to.etc.parallelrunner.simple;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.parallelrunner.AsyncWorker;
import to.etc.parallelrunner.IAsyncRunnable;
import to.etc.util.Progress;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ParallelTaskRunner<T extends IAsyncRunnable> {

	private final AsyncWorker m_asyncWorker;

	private Progress m_progress;

	private List<SimpleTaskExecutor<T>> m_idleExecutors;

	private List<SimpleTaskExecutor<T>> m_busyExecutors = new ArrayList<>();

	private final ReentrantLock m_lock = new ReentrantLock();

	private final Condition m_hasIdleExecutorsCondition = m_lock.newCondition();

	private final Condition m_noBusyExecutorsCondition = m_lock.newCondition();

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
			simpleTaskExecutor.schedule(m_progress, task);
		}finally {
			m_lock.unlock();
		}
	}

	void reportTaskDone(SimpleTaskExecutor<T> executor, T task, boolean cancelled, Exception ex) {
		m_lock.lock();
		try {
			m_busyExecutors.remove(executor);
			m_idleExecutors.add(executor);
			m_hasIdleExecutorsCondition.signal();
			if(m_busyExecutors.isEmpty()) {
				m_noBusyExecutorsCondition.signal();
			}
		}finally {
			m_lock.unlock();
		}

		//TODO report exception
	}

	public void waitAllCompleted() throws Exception {
		System.out.println("** waiting for all task to finish");
		m_lock.lock();
		try {
			for(;;) {
				if(!m_busyExecutors.isEmpty()) {
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


	/**
	 * The executor that runs tasks provided by runner.
	 */
	static public final class SimpleTaskExecutor<V extends IAsyncRunnable>  {
		private final ParallelTaskRunner<V> m_runner;

		private V m_task;

		private final int m_index;

		public SimpleTaskExecutor(ParallelTaskRunner<V> runner, int index) {
			m_runner = runner;
			m_index = index;
		}

		public void schedule(Progress p, V task) {
			m_task = task;
			m_runner.m_asyncWorker.schedule(toString(), task, (cancelled, exception) -> {
				m_task = null;
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

