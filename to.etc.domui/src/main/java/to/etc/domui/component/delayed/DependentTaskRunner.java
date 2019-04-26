package to.etc.domui.component.delayed;

import to.etc.domui.trouble.UIException;
import to.etc.function.FunctionEx;
import to.etc.util.CancelledException;
import to.etc.util.DependentTaskSource;
import to.etc.util.DependentTaskSource.Task;
import to.etc.util.Progress;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-2-19.
 */
public class DependentTaskRunner<T extends IAsyncRunnable> {
	private final DependentTaskSource<T> m_taskSource = new DependentTaskSource<>();

	private final FunctionEx<List<Task<T>>, Task<T>> m_calculateBest = tasks -> tasks.get(0);

	private final FunctionEx<T, Integer> m_priorityCalculator = t -> 10;

	private final List<SingleTaskExecutor<T>> m_scheduledList = new ArrayList<>();

	private final List<SingleTaskExecutor<T>> m_finishedList = new ArrayList<>();

	private Progress m_progress;

	private List<SingleTaskExecutor<T>> m_runningTasks = new ArrayList<>();

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
			runDefinition(task);
		}
		System.out.println("** waiting for all task to finish");
		m_taskSource.waitFinished();
		System.out.println("** All finished");

		//-- All done; dump their log info
		List<SingleTaskExecutor<T>> fl = getFinishedList();
		for(SingleTaskExecutor<T> finished: fl) {
			handleFinishedAfter(finished);
		}

		//-- Also dump info for all cancelled thingies.
		for(Task<T> task: m_taskSource.getCancelledSet()) {
			handleCancelled(task);
		}
	}

	private void runDefinition(Task<T> tableTask) throws Exception {
		SingleTaskExecutor<T> tx = new SingleTaskExecutor<>(this, tableTask);
		synchronized(this) {
			m_scheduledList.add(tx);
		}
		schedule(tx, tableTask.getItem().toString());
	}

	private void schedule(SingleTaskExecutor<T> dx, String fullCode) throws Exception {
		AsyncWorker.getInstance().schedule("Run#" + fullCode, dx, (a, x) -> registerFinished(dx), m_priorityCalculator.apply(dx.getTask().getItem()));
	}

	private void registerFinished(SingleTaskExecutor<T> dx) {
		synchronized(this) {
			m_finishedList.add(dx);
		}
		Objects.requireNonNull(m_progress).increment(1);
		onFinish(dx);
	}

	protected void onFinish(SingleTaskExecutor<T> dx) {
	}

	protected void handleCancelled(Task<T> task) {
	}

	private <V extends IAsyncRunnable> void taskStarting(SingleTaskExecutor<T> exec) {
		synchronized(this) {
			m_runningTasks.add(exec);
		}
		onTaskStarted(exec);
	}

	protected void onTaskStarted(SingleTaskExecutor<T> exec) {
	}

	private <V extends IAsyncRunnable> void taskFinished(SingleTaskExecutor<T> exec) {
		synchronized(this) {
			m_runningTasks.remove(exec);
		}
	}

	/**
	 * Called with all finished tasks AFTER the complete run finishes.
	 */
	protected void handleFinishedAfter(SingleTaskExecutor<T> finished) {

	}

	public void addTask(T task, List<T> dependencies) {
		m_taskSource.addItem(task, dependencies);
	}

	public synchronized List<SingleTaskExecutor<T>> getFinishedList() {
		return new ArrayList<>(m_finishedList);
	}

	boolean isCancelled() {
		return Objects.requireNonNull(m_progress).isCancelled();
	}

	public synchronized List<SingleTaskExecutor<T>> getRunningTasks() {
		return new ArrayList<>(m_runningTasks);
	}

	/**
	 * The executor task which runs the payload and handles error and success registration.
	 */
	static public final class SingleTaskExecutor<V extends IAsyncRunnable> implements IAsyncRunnable {
		private final DependentTaskRunner<V> m_runner;

		private final Task<V> m_task;

		private Date m_startTime;

		private Date m_endTime;

		public SingleTaskExecutor(DependentTaskRunner<V> runner, Task<V> task) {
			m_runner = runner;
			m_task = task;
		}

		@Override public void run(Progress p) throws Exception {
			if(m_runner.isCancelled())
				return;

			String error = "";
			Exception exception = null;
			synchronized(this) {
				m_startTime = new Date();
			}
			m_runner.taskStarting(this);
			try {
				//System.out.println("-- starting " + m_task);
				p.setTotalWork(1.0);
				m_task.getItem().run(p);
				p.complete();
			} catch(Exception x) {
				exception = x;
				error = x.toString();
				if(! (x instanceof UIException)) {
					x.printStackTrace();
				}
			} finally {
				//System.out.println("-- finished " + m_task + " " + error);
				m_task.completed(exception, error);
				synchronized(this) {
					m_endTime = new Date();
				}
				m_runner.taskFinished(this);
			}

		}

		public Task<V> getTask() {
			return m_task;
		}

		@Override public String toString() {
			return m_task.getItem().toString();
		}
	}
}
