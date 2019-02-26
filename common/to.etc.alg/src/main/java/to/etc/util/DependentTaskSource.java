package to.etc.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.FunctionEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
final public class DependentTaskSource<T> {
	private final Map<T, Task<T>> m_taskMap = new HashMap<>();

	private Set<Task<T>> m_todo = new HashSet<>();

	private Set<Task<T>> m_running = new HashSet<>();

	private Set<Task<T>> m_errorSet = new HashSet<>();

	private Set<Task<T>> m_cancelledSet = new HashSet<>();

	private Set<Task<T>> m_doneSet = new HashSet<>();

	private boolean m_cancelChildrenOnError;

	@Nullable
	private List<Task<T>> m_runnableTasks;

	public synchronized void addItem(T item, List<T> itemChildren) {
		if(m_runnableTasks != null)
			throw new IllegalStateException("Implementation restriction: you cannot add tasks once you have started consuming them");
		Task<T> task = task(item);
		for(T child: itemChildren) {
			Task<T> childTask = task(child);
			task.addChild(childTask);
		}
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
			task.start();
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
			wait();
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
		if(runnableTasks == null) {
			m_todo.addAll(m_taskMap.values());
			m_runnableTasks = runnableTasks = new ArrayList<>();
			calculateRunnableTasks();
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
		return m_todo.isEmpty() && m_running.isEmpty();
	}

	public synchronized void waitFinished() throws InterruptedException {
		for(;;) {
			if(isFinished())
				return;
			wait();
		}
	}

	public synchronized Set<Task<T>> getCancelledSet() {
		return new HashSet<>(m_cancelledSet);
	}

	private Task<T> task(T item) {
		return m_taskMap.computeIfAbsent(item, a -> new Task<>(this, item));
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

	public final static class Task<V> {
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

		public Task(DependentTaskSource<V> source, V item) {
			m_source = source;
			m_item = item;
		}

		public void start() {
			synchronized(m_source) {
				if(m_state != TaskState.NONE)
					throw new IllegalStateException("The task " + this + " has already been started and is in state " + m_state);
				m_state = TaskState.RUNNING;
				m_source.m_todo.remove(this);
				m_source.m_running.add(this);
				Objects.requireNonNull(m_source.m_runnableTasks).remove(this);
			}
		}

		public void completedSuccessfully() {
			completed(null, null);
		}

		public void completed(@Nullable Exception exception, @Nullable String errorMessage) {
			synchronized(m_source) {
				if(m_state != TaskState.RUNNING)
					throw new IllegalStateException("The task " + this + " can only be completed in RUNNING state but it is in state " + m_state);

				m_source.m_running.remove(this);
				if(errorMessage != null || exception != null) {
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
				}
			}
			m_children.clear();
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
		public Exception getException() {
			return m_exception;
		}

		@Nullable
		public String getError() {
			return m_error;
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
