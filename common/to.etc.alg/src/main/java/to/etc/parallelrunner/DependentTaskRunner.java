package to.etc.parallelrunner;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.FunctionEx;
import to.etc.util.CancelledException;
import to.etc.util.DependentTaskSource;
import to.etc.util.DependentTaskSource.ITaskListener;
import to.etc.util.DependentTaskSource.Task;
import to.etc.util.Progress;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-2-19.
 */
public class DependentTaskRunner<T extends IAsyncRunnable> {
	private final DependentTaskSource<T> m_taskSource = new DependentTaskSource<>();

	private FunctionEx<List<Task<T>>, Task<T>> m_calculateBest = tasks -> tasks.get(0);

	private FunctionEx<T, Integer> m_priorityCalculator = t -> 10;

	private Progress m_progress;

	public DependentTaskRunner() {
		m_taskSource.addListener(new ITaskListener<T>() {
			@Override
			public void onTaskStarted(Task<T> task) throws Exception {
				DependentTaskRunner.this.onTaskStarted(task);
			}

			@Override
			public void onTaskFinished(Task<T> task, @Nullable Exception failure) throws Exception {
				DependentTaskRunner.this.onFinish(task);
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
			Task<T> task = m_taskSource.getNextRunnableBlocking(m_calculateBest);
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
		List<Task<T>> fl = getFinishedList();
		for(Task<T> finished: fl) {
			handleFinishedAfter(finished);
		}

		//-- Also dump info for all cancelled thingies.
		for(Task<T> task: m_taskSource.getCancelledSet()) {
			handleCancelledAfter(task);
		}
	}

	public void setCalculateBest(FunctionEx<List<Task<T>>, Task<T>> calculateBest) {
		m_calculateBest = calculateBest;
	}

	public void setPriorityCalculator(FunctionEx<T, Integer> priorityCalculator) {
		m_priorityCalculator = priorityCalculator;
	}

	private void scheduleTask(Task<T> tableTask) throws Exception {
		AsyncWorker.getInstance().schedule("Run#" + tableTask.getItem().toString()
			, tableTask
			, (a, x) -> {}
			, m_priorityCalculator.apply(tableTask.getItem()));
	}

	protected void onTaskStarted(Task<T> exec) {
	}

	protected void onFinish(Task<T> dx) {
	}

	protected void handleCancelledAfter(Task<T> task) {
	}

	/**
	 * Called with all finished tasks AFTER the complete run finishes.
	 */
	protected void handleFinishedAfter(Task<T> finished) {
	}

	public synchronized List<Task<T>> getFinishedList() {
		return m_taskSource.getAllExecutedTasks();
	}

	boolean isCancelled() {
		return Objects.requireNonNull(m_progress).isCancelled();
	}

	public synchronized List<Task<T>> getRunningTasks() {
		return m_taskSource.getRunning();
	}
}
