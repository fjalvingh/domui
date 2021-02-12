package to.etc.parallelrunner;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.FunctionEx;
import to.etc.util.CancelledException;
import to.etc.parallelrunner.DependentTaskSource.ITaskListener;
import to.etc.parallelrunner.DependentTaskSource.Task;
import to.etc.util.Progress;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-2-19.
 */
public class DependentTaskRunner<T extends IAsyncRunnable> {
	private final DependentTaskSource<T, SingleTaskExecutor<T>> m_taskSource = new DependentTaskSource<>(this::convertTaskToRunner);

	private FunctionEx<List<Task<T, SingleTaskExecutor<T>>>, Task<T, SingleTaskExecutor<T>>> m_calculateBest = tasks -> tasks.get(0);

	private FunctionEx<T, Integer> m_priorityCalculator = t -> 10;

	private Progress m_progress;

	private AsyncWorker m_executor;

	public DependentTaskRunner(AsyncWorker executor) {
		m_executor = executor;
		m_taskSource.addListener(new ITaskListener<T, SingleTaskExecutor<T>>() {
			@Override
			public void onTaskStarted(Task<T, SingleTaskExecutor<T>> task) throws Exception {
				DependentTaskRunner.this.onTaskStarted(task);
			}

			@Override
			public void onTaskFinished(Task<T, SingleTaskExecutor<T>> task, @Nullable Throwable failure) throws Exception {
				DependentTaskRunner.this.onFinish(task);
				m_progress.increment(1.0);
			}
		});
	}

	public void addTask(T task, Collection<? extends T> dependencies) {
		m_taskSource.addItem(task, dependencies);
	}

	public void runAll(Progress pin) throws Exception {
		m_progress = pin;
		pin.setTotalWork(m_taskSource.size());
		for(;;) {
			Task<T, SingleTaskExecutor<T>> task = m_taskSource.getNextRunnableBlocking(m_calculateBest);
			if(null == task)
				break;
			if(pin.isCancelled())
				throw new CancelledException();
			//System.out.println(">>> schedule " + task);
			scheduleTask(task);
		}
		System.out.println("** waiting for all task to finish");
		m_taskSource.waitFinished();
		System.out.println("** All finished");

		//-- All done; dump their log info
		List<Task<T, SingleTaskExecutor<T>>> fl = getFinishedList();
		for(Task<T, SingleTaskExecutor<T>> finished: fl) {
			handleFinishedAfter(finished);
		}

		//-- Also dump info for all cancelled thingies.
		for(Task<T, SingleTaskExecutor<T>> task: m_taskSource.getCancelledSet()) {
			handleCancelledAfter(task);
		}
	}

	private SingleTaskExecutor<T> convertTaskToRunner(Task<T, SingleTaskExecutor<T>> task) {
		return new SingleTaskExecutor<>(this, task);
	}

	public void setCalculateBest(FunctionEx<List<Task<T, SingleTaskExecutor<T>>>, Task<T, SingleTaskExecutor<T>>> calculateBest) {
		m_calculateBest = calculateBest;
	}

	public void setPriorityCalculator(FunctionEx<T, Integer> priorityCalculator) {
		m_priorityCalculator = priorityCalculator;
	}

	private void scheduleTask(Task<T, SingleTaskExecutor<T>> tableTask) throws Exception {
		m_executor.schedule("Run#" + tableTask.getItem().toString()
			, tableTask
			, (a, x) -> {}
			, m_priorityCalculator.apply(tableTask.getItem()));
	}

	protected void onTaskStarted(Task<T, SingleTaskExecutor<T>> exec) {
	}

	protected void onFinish(Task<T, SingleTaskExecutor<T>> dx) {
	}

	protected void handleCancelledAfter(Task<T, SingleTaskExecutor<T>> task) {
	}

	/**
	 * Called with all finished tasks AFTER the complete run finishes.
	 */
	protected void handleFinishedAfter(Task<T, SingleTaskExecutor<T>> finished) {
	}

	public List<Task<T, SingleTaskExecutor<T>>> getAllTasks() {
		return m_taskSource.getAllTasks();
	}

	public synchronized List<Task<T, SingleTaskExecutor<T>>> getFinishedList() {
		return m_taskSource.getAllExecutedTasks();
	}

	boolean isCancelled() {
		return Objects.requireNonNull(m_progress).isCancelled();
	}

	public synchronized List<Task<T, SingleTaskExecutor<T>>> getRunningTasks() {
		return m_taskSource.getRunning();
	}


	/**
	 * The executor task which runs the payload and handles error and success registration.
	 */
	static public final class SingleTaskExecutor<V extends IAsyncRunnable> implements IAsyncRunnable {
		private final DependentTaskRunner<V> m_runner;

		private final Task<V, SingleTaskExecutor<V>> m_task;

		public SingleTaskExecutor(DependentTaskRunner<V> runner, Task<V, SingleTaskExecutor<V>> task) {
			m_runner = runner;
			m_task = task;
		}

		@Override public void run(Progress p) throws Exception {
			if(m_runner.isCancelled())
				return;
			m_task.getItem().run(p);
		}

		public Task<V, SingleTaskExecutor<V>> getTask() {
			return m_task;
		}

		@Override public String toString() {
			return m_task.getItem().toString();
		}
	}
}
