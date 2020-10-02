package to.etc.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.FunctionEx;
import to.etc.parallelrunner.IAsyncRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * This wraps a tree of dependent things, and supports "creating" each dependent thing
 * in the correct order in parallel. It can return at any time a list of T that can be
 * created (because all their children are successfully created) from which a single
 * one can be picked. Each item can be marked as successfully created or failed. When
 * successfully created the all parents of the item will now be considered as creatable
 * provided that all their children are successful.
 *
 * For failed items it also marks all parents that depend on the items as failed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-6-18.
 */
@NonNullByDefault
final public class DependentTaskSource<T extends IAsyncRunnable> {
	private final Map<T, Task<T>> m_taskMap = new HashMap<>();

	private Set<Task<T>> m_todo = new HashSet<>();

	/** Tasks returned by getNextRunnable and the like but for which run() has not yet been called. */
	private Set<Task<T>> m_scheduled = new HashSet<>();

	/** Tasks that are actually running */
	private Set<Task<T>> m_running = new HashSet<>();

	/** Tasks that ran but failed with an exception */
	private Set<Task<T>> m_errorSet = new HashSet<>();

	/** Tasks that never ran but were cancelled because some dependency failed */
	private Set<Task<T>> m_cancelledSet = new HashSet<>();

	private Set<Task<T>> m_doneSet = new HashSet<>();

	private Set<Task<T>> m_allDoneSet = new HashSet<>();

	private boolean m_cancelChildrenOnError;

	private List<ITaskListener<T>> m_listeners = new ArrayList<>();

	@Nullable
	private List<Task<T>> m_runnableTasks;

	public interface ITaskListener<V extends IAsyncRunnable> {
		default void onTaskStarted(Task<V> task) throws Exception {}

		default void onTaskFinished(Task<V> task, @Nullable Exception failure) throws Exception {}
	}

	public DependentTaskSource() {
	}

	public synchronized void addItem(T item, Collection<? extends T> itemChildren) {
		if(m_runnableTasks != null)
			throw new IllegalStateException("Implementation restriction: you cannot add tasks once you have started consuming them");
		Task<T> task = task(item);
		for(T child: itemChildren) {
			Task<T> childTask = task(child);
			task.addChild(childTask);
		}
		checkCircularDependencies(task);
	}

	/**
	 * Make sure that no child dependency depends on a parent.
	 */
	private void checkCircularDependencies(Task<T> task) {
		checkCircularDependencies(task, new Stack<>());
	}

	private void checkCircularDependencies(Task<T> task, Stack<T> stack) {
		if(stack.contains(task.getItem())) {
			//-- Circular dependency.
			StringBuilder sb = new StringBuilder();
			sb.append("Circular dependency in task " + task.getItem() + ": it has a circular dependency through ");
			sb.append(stack.stream().map(a -> a.toString()).collect(Collectors.joining(" -> ")));
			throw new IllegalStateException(sb.toString());
		}
		stack.add(task.getItem());
		for(Task<T> child : task.getChildren()) {
			checkCircularDependencies(child, stack);
		}
		stack.pop();
	}

	/**
	 * Get the next task that is executable, in any order. If nothing is runnable (yet) then
	 * return null. If a task is returned it is set to RUNNING, so do not call START on it.
	 */
	@Nullable
	public synchronized Task<T> getNextRunnable() {
		try {
			return getNextRunnable(null);
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	/**
	 * Get the next task that is runnable, and take your own pick from all of them that are
	 * runnable. This allows ordering task execution order of runnable tasks.
	 */
	@Nullable
	public synchronized Task<T> getNextRunnable(@Nullable FunctionEx<List<Task<T>>, Task<T>> calculateBest) throws Exception {
		List<Task<T>> tasks = internalRunnableTasks();
		if(tasks.isEmpty())
			return null;

		Task<T> task = calculateBest == null ? tasks.get(0) : calculateBest.apply(tasks);
		if(null != task)
			task.selected();
		return task;
	}

	/**
	 * Blocks until a new task is available, or until all has been done that can be done.
	 */
	@Nullable
	public synchronized Task<T> getNextRunnableBlocking(@Nullable FunctionEx<List<Task<T>>, Task<T>> calculateBest) throws Exception {
		for(;;) {
			Task<T> task = getNextRunnable(calculateBest);
			if(null != task)
				return task;
			if(isFinished())
				return null;
			wait(5000);
		}
	}

	/**
	 * Get ALL runnable tasks. Only use this if you synchronized on this instance, because the list will
	 * change when other threads complete.
	 */
	public synchronized List<Task<T>> getAllRunnableButDoNotJustStartThem() {
		return new ArrayList<>(internalRunnableTasks());
	}

	private synchronized List<Task<T>> internalRunnableTasks() {
		List<Task<T>> runnableTasks = m_runnableTasks;
		if(runnableTasks == null) {						// Only happens at start (1x)
			m_todo.addAll(m_taskMap.values());
			m_runnableTasks = runnableTasks = new ArrayList<>();
			calculateRunnableTasks();
		} else if(runnableTasks.size() == 0) {
			if(m_running.size() == 0) {
				if(m_todo.size() > 0)
					throw new IllegalStateException("There are no tasks running, but none of the todo tasks became available");
			}
		}
		return runnableTasks;
	}

	/**
	 * Called only once, this calculates the initial set of items that are runnable by
	 * finding all of them that have no children.
	 */
	private synchronized void calculateRunnableTasks() {
		if(m_todo.isEmpty())
			return;

		List<Task<T>> runnableTasks = Objects.requireNonNull(m_runnableTasks);
		for(Task<T> task: m_todo) {
			if(task.getChildren().isEmpty()) {
				runnableTasks.add(task);
			}
		}
		if(runnableTasks.size() == 0)
			throw new IllegalStateException("Nothing is runnable: loops in dependencies");
	}

	/**
	 * Returns true if there are no more tasks that can be started. It does NOT mean that
	 * all tasks are FINISHED.
	 */
	public synchronized boolean isEmpty() {
		internalRunnableTasks();
		return m_todo.isEmpty();
	}

	/**
	 * Returns T if all is done and all running tasks have completed.
	 */
	public synchronized boolean isFinished() {
		internalRunnableTasks();
		return m_todo.isEmpty() && m_running.isEmpty() && m_scheduled.isEmpty();
	}

	public synchronized void waitFinished() throws InterruptedException {
		for(;;) {
			if(isFinished())
				return;
			wait();
		}
	}

	/**
	 * Get all tasks that were cancelled (because their dependencies failed to run).
	 */
	public synchronized Set<Task<T>> getCancelledSet() {
		return new HashSet<>(m_cancelledSet);
	}

	/**
	 * Returns the current set of successfully executed tasks.
	 */
	public synchronized List<Task<T>> getSuccessfulTasks() {
		return new ArrayList<>(m_allDoneSet);
	}

	/**
	 * Get all tasks that failed execution (this does not include cancelled tasks).
	 */
	public synchronized List<Task<T>> getFailedTasks() {
		return new ArrayList<>(m_errorSet);
	}

	/**
	 * Return all tasks that have actually run, either successfully or
	 * unsuccessfully (again excluding cancelled ones).
	 */
	public synchronized List<Task<T>> getAllExecutedTasks() {
		return new ArrayList<>(m_allDoneSet);
	}

	/**
	 * Get all tasks that are currently executing.
	 */
	public synchronized List<Task<T>> getRunning() {
		return new ArrayList<>(m_running);
	}

	private Task<T> task(T item) {
		return m_taskMap.computeIfAbsent(item, a -> new Task<>(this, item));
	}

	public synchronized void addListener(ITaskListener<T> l) {
		m_listeners.add(l);
	}

	private synchronized List<ITaskListener<T>> getListeners() {
		return new ArrayList<>(m_listeners);
	}

	public void dumpDependencies() {
		for(Task<T> task: m_taskMap.values()) {
			System.out.println(task + " dependencies:");
			for(Task<T> child: task.getChildren()) {
				System.out.println("     " + child);
			}
		}
	}

	public int size() {
		return m_taskMap.size();
	}

	public enum TaskState {
		NONE,
		RUNNING,
		COMPLETED,
		FAILED,
		CANCELLED
	}

	public final static class Task<V extends IAsyncRunnable> implements IAsyncRunnable {
		private final DependentTaskSource<V> m_source;

		private final V m_item;

		private final List<Task<V>> m_parents = new ArrayList<>();

		private final List<Task<V>> m_children = new ArrayList<>();

		private TaskState m_state = TaskState.NONE;

		@Nullable
		private Task<V> m_failedTask;

		@Nullable
		private Exception m_exception;

		@Nullable
		private String m_error;

		Task(DependentTaskSource<V> source, V item) {
			m_source = source;
			m_item = item;
		}

		/**
		 * Called when this task has been returned. It removes the task from the list of runnable tasks
		 * so that it cannot be returned again.
		 */
		void selected() {
			synchronized(m_source) {
				if(m_state != TaskState.NONE)
					throw new IllegalStateException("The task " + this + " has already been started and is in state " + m_state);
				m_state = TaskState.RUNNING;
				m_source.m_todo.remove(this);
				m_source.m_scheduled.add(this);
				Objects.requireNonNull(m_source.m_runnableTasks).remove(this);
			}
		}

		@Override
		public void run(@Nullable Progress optionalProgress) {
			Progress progress = optionalProgress == null ? new Progress("") : optionalProgress;

			synchronized(m_source) {
				if(! m_source.m_scheduled.remove(this))
					throw new IllegalStateException("Running a task that is not yet returned by getNextRunnable() is not allowed (or perhaps you're running it twice)");
				m_source.m_running.add(this);
			}
			Exception errorX = null;
			try {
				run(optionalProgress);
			} catch(Exception x) {
				errorX = x;
			} finally {
				completed(errorX, null);
			}
		}

		//public void completedSuccessfully() {
		//	completed(null, null);
		//}

		private void completed(@Nullable Exception exception, @Nullable String errorMessage) {
			synchronized(m_source) {
				if(m_state != TaskState.RUNNING)
					throw new IllegalStateException("The task " + this + " can only be completed in RUNNING state but it is in state " + m_state);

				m_source.m_running.remove(this);
				m_source.m_allDoneSet.add(this);
				if(! StringTool.isBlank(errorMessage) || exception != null) {
					m_error = errorMessage;
					m_exception = exception;
					m_state = TaskState.FAILED;

					failParents(this);
					m_source.m_errorSet.add(this);
				} else {
					m_state = TaskState.COMPLETED;
					m_source.m_doneSet.add(this);

					//-- Remove me from all parents, and mark the parent as RUNNABLE if it has no more children and is not failed.
					for(Task<V> parent: m_parents) {
						parent.m_children.remove(this);
						if(parent.m_children.isEmpty()) {
							if(parent.m_state == TaskState.NONE) {
								Objects.requireNonNull(m_source.m_runnableTasks).add(parent);
							}
						}
					}
				}
				m_source.notifyAll();			// Some state changed, notify waiters
			}
		}

		/**
		 * Cancel all parents (and optionally their children) that are not active yet.
		 */
		private void failParents(Task<V> failedTask) {
			for(Task<V> parent: m_parents) {
				parent.m_children.remove(this);

				if(parent.m_state == TaskState.NONE) {
					//-- Not yet failed -> fail it now.
					parent.m_state = TaskState.CANCELLED;
					parent.m_failedTask = failedTask;
					m_source.m_todo.remove(parent);
					m_source.m_cancelledSet.add(parent);
					m_source.m_allDoneSet.add(parent);

					if(m_source.m_cancelChildrenOnError)
						cancelChildren(failedTask);

					parent.failParents(failedTask);
				}
			}
		}

		/**
		 * Mark all children and subchildren of a task as failed because some task failed.
		 */
		private void cancelChildren(Task<V> failedTask) {
			for(Task<V> child: m_children) {
				if(child.m_state == TaskState.NONE) {
					child.m_state = TaskState.CANCELLED;
					child.m_failedTask = failedTask;
					m_source.m_todo.remove(child);
					child.cancelChildren(failedTask);
					m_source.m_cancelledSet.add(child);
					m_source.m_allDoneSet.add(child);
				}
			}
			m_children.clear();
		}

		public synchronized TaskState getState() {
			return m_state;
		}

		public V getItem() {
			return m_item;
		}

		public void addChild(Task<V> childTask) {
			m_children.add(childTask);
			childTask.m_parents.add(this);
		}

		public List<Task<V>> getChildren() {
			return m_children;
		}

		@Nullable
		public synchronized Exception getException() {
			return m_exception;
		}

		@Nullable
		public synchronized String getUserError() {
			return m_error;
		}

		@Nullable
		public synchronized String getErrorMessage() {
			String error = m_error;
			if(null != error)
				return error;
			Exception exception = m_exception;
			if(null != exception)
				return exception.toString();
			return null;
		}

		@Nullable
		public Task<V> getFailedTask() {
			return m_failedTask;
		}

		@Override public String toString() {
			return "task " + m_item;
		}
	}
}
