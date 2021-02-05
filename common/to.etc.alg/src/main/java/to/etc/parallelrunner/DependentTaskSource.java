package to.etc.parallelrunner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.FunctionEx;
import to.etc.util.ExceptionUtil;
import to.etc.util.MessageException;
import to.etc.util.Progress;
import to.etc.util.WrappedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
final public class DependentTaskSource<T, X extends IAsyncRunnable> {
	private final Map<T, Task<T, X>> m_taskMap = new HashMap<>();

	private Set<Task<T, X>> m_todo = new HashSet<>();

	/** Tasks returned by getNextRunnable and the like but for which run() has not yet been called. */
	private Set<Task<T, X>> m_scheduled = new HashSet<>();

	/** Tasks that are actually running */
	private Set<Task<T, X>> m_running = new HashSet<>();

	/** Tasks that ran but failed with an exception */
	private Set<Task<T, X>> m_errorSet = new HashSet<>();

	/** Tasks that never ran but were cancelled because some dependency failed */
	private Set<Task<T, X>> m_cancelledSet = new HashSet<>();

	private Set<Task<T, X>> m_doneSet = new HashSet<>();

	private Set<Task<T, X>> m_allDoneSet = new HashSet<>();

	private boolean m_cancelChildrenOnError;

	private List<ITaskListener<T, X>> m_listeners = new ArrayList<>();

	private int m_maxParallel;

	@Nullable
	private List<Task<T, X>> m_runnableTasks;

	final private FunctionEx<Task<T, X>, X> m_executorFactory;

	public interface ITaskListener<V, X extends IAsyncRunnable> {
		default void onTaskStarted(Task<V, X> task) throws Exception {
		}

		default void onTaskFinished(Task<V, X> task, @Nullable Throwable failure) throws Exception {
		}
	}

	public DependentTaskSource(FunctionEx<Task<T, X>, X> executorFactory) {
		m_executorFactory = executorFactory;
	}

	public synchronized void addItem(T item, Collection<? extends T> itemChildren) {
		if(m_runnableTasks != null)
			throw new IllegalStateException("Implementation restriction: you cannot add tasks once you have started consuming them");
		Task<T, X> task = task(item);
		for(T child : itemChildren) {
			Task<T, X> childTask = task(child);
			task.addChild(childTask);
		}
		checkCircularDependencies(task);
	}

	/**
	 * Make sure that no child dependency depends on a parent.
	 */
	private void checkCircularDependencies(Task<T, X> task) {
		checkCircularDependencies(task, new Stack<>());
	}

	private void checkCircularDependencies(Task<T, X> task, Stack<T> stack) {
		if(stack.contains(task.getItem())) {
			//-- Circular dependency.
			StringBuilder sb = new StringBuilder();
			sb.append("Circular dependency in task " + task.getItem() + ": it has a circular dependency through ");
			sb.append(stack.stream().map(a -> a.toString()).collect(Collectors.joining(" -> ")));
			throw new IllegalStateException(sb.toString());
		}
		stack.add(task.getItem());
		for(Task<T, X> child : task.getChildren()) {
			checkCircularDependencies(child, stack);
		}
		stack.pop();
	}

	/**
	 * Get the next task that is executable, in any order. If nothing is runnable (yet) then
	 * return null. If a task is returned it is set to RUNNING, so do not call START on it.
	 */
	@Nullable
	public synchronized Task<T, X> getNextRunnable() {
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
	public synchronized Task<T, X> getNextRunnable(@Nullable FunctionEx<List<Task<T, X>>, Task<T, X>> calculateBest) throws Exception {
		List<Task<T, X>> tasks = internalRunnableTasks();
		if(tasks.isEmpty())
			return null;

		Task<T, X> task = calculateBest == null ? tasks.get(0) : calculateBest.apply(tasks);
		if(null != task) {
			selectTask(task);
		}
		return task;
	}

	/**
	 * Called when a task has been returned as a run candidate. It removes the task from the list of runnable tasks
	 * so that it cannot be returned again.
	 */
	private void selectTask(Task<T, X> task) {
		//System.out.println(">>>> selectTask " + task);
		synchronized(this) {
			if(task.m_state != TaskState.NONE)
				throw new IllegalStateException("The task " + this + " has already been started and is in state " + task.m_state);
			task.m_state = TaskState.SCHEDULED;
			m_todo.remove(task);
			m_scheduled.add(task);
			Objects.requireNonNull(m_runnableTasks).remove(task);
		}
	}

	/**
	 * Blocks until a new task is available, or until all has been done that can be done.
	 */
	@Nullable
	public synchronized Task<T, X> getNextRunnableBlocking(@Nullable FunctionEx<List<Task<T, X>>, Task<T, X>> calculateBest) throws Exception {
		for(; ; ) {
			Task<T, X> task = getNextRunnable(calculateBest);
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
	public synchronized List<Task<T, X>> getAllRunnableButDoNotJustStartThem() {
		return new ArrayList<>(internalRunnableTasks());
	}

	private synchronized List<Task<T, X>> internalRunnableTasks() {
		List<Task<T, X>> runnableTasks = m_runnableTasks;
		if(runnableTasks == null) {                        // Only happens at start (1x)
			m_todo.addAll(m_taskMap.values());
			m_runnableTasks = runnableTasks = new ArrayList<>();
			calculateRunnableTasks();
		} else if(runnableTasks.size() == 0) {
			if(m_running.size() == 0 && m_scheduled.isEmpty()) {
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

		List<Task<T, X>> runnableTasks = Objects.requireNonNull(m_runnableTasks);
		for(Task<T, X> task : m_todo) {
			if(task.getChildren().isEmpty()) {
				runnableTasks.add(task);
			}
		}
		if(runnableTasks.size() == 0)
			throw new IllegalStateException("Nothing is runnable: loops in dependencies");
	}

	private void runTask(Task<T, X> task, Progress progress) {
		synchronized(this) {
			if(!m_scheduled.remove(task))
				throw new IllegalStateException("Running a task that is not yet returned by getNextRunnable() is not allowed (or perhaps you're running it twice)");
			m_running.add(task);
			int sz = m_running.size();
			if(sz > m_maxParallel)
				m_maxParallel = sz;
			task.m_state = TaskState.RUNNING;
		}
		Throwable errorX = null;
		try {
			X executor = m_executorFactory.apply(task);
			task.setExecutor(executor);
			task.setStartTime(new Date());
			executor.run(progress);
		} catch(Exception | Error x) {
			if(x instanceof MessageException) {
				System.err.println("ERROR " + task + ": " + x.getMessage());
			} else {
				System.err.println("ERROR " + task + ": " + x);
				x.printStackTrace();
			}
			errorX = x;
		} finally {
			task.setEndTime(new Date());
			handleCompletedTask(task, errorX);
		}
	}

	private void handleCompletedTask(Task<T, X> task, @Nullable Throwable exception) {
		task.completed(exception);								// ORDERED Mark the task itself as done so that the listener can be called
		m_listeners.forEach(a -> ExceptionUtil.silentFails(() -> a.onTaskFinished(task, exception)));

		//-- Now remove the task from running lists et al.
		synchronized(this) {
			m_running.remove(task);
			m_allDoneSet.add(task);
			if(exception != null) {
				m_errorSet.add(task);
			} else {
				m_doneSet.add(task);
			}
			notifyAll();										// Some state changed, notify waiters
		}
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
		for(; ; ) {
			if(isFinished())
				return;
			wait();
		}
	}

	/**
	 * Get all tasks that were cancelled (because their dependencies failed to run).
	 */
	public synchronized Set<Task<T, X>> getCancelledSet() {
		return new HashSet<>(m_cancelledSet);
	}

	/**
	 * Returns the current set of successfully executed tasks.
	 */
	public synchronized List<Task<T, X>> getSuccessfulTasks() {
		return new ArrayList<>(m_allDoneSet);
	}

	/**
	 * Get all tasks that failed execution (this does not include cancelled tasks).
	 */
	public synchronized List<Task<T, X>> getFailedTasks() {
		return new ArrayList<>(m_errorSet);
	}

	/**
	 * Return all tasks that have actually run, either successfully or
	 * unsuccessfully (again excluding cancelled ones).
	 */
	public synchronized List<Task<T, X>> getAllExecutedTasks() {
		return new ArrayList<>(m_allDoneSet);
	}

	/**
	 * Get all tasks that are currently executing.
	 */
	public synchronized List<Task<T, X>> getRunning() {
		return new ArrayList<>(m_running);
	}

	public int getMaxParallel() {
		return m_maxParallel;
	}

	private Task<T, X> task(T item) {
		return m_taskMap.computeIfAbsent(item, a -> new Task<>(this, item));
	}

	public synchronized void addListener(ITaskListener<T, X> l) {
		m_listeners.add(l);
	}

	private synchronized List<ITaskListener<T, X>> getListeners() {
		return new ArrayList<>(m_listeners);
	}

	public void dumpDependencies() {
		for(Task<T, X> task : m_taskMap.values()) {
			System.out.println(task + " dependencies:");
			for(Task<T, X> child : task.getChildren()) {
				System.out.println("     " + child);
			}
		}
	}

	public int size() {
		return m_taskMap.size();
	}

	public enum TaskState {
		NONE,
		SCHEDULED,
		RUNNING,
		COMPLETED,
		FAILED,
		CANCELLED
	}

	public final static class Task<V, X extends IAsyncRunnable> implements IAsyncRunnable {
		private final DependentTaskSource<V, X> m_source;

		private final V m_item;

		private final List<Task<V, X>> m_parents = new ArrayList<>();

		private final List<Task<V, X>> m_children = new ArrayList<>();

		private TaskState m_state = TaskState.NONE;

		@Nullable
		private Task<V, X> m_failedTask;

		@Nullable
		private Throwable m_exception;

		@Nullable
		private X m_executor;

		@Nullable
		private Date m_startTime;

		@Nullable
		private Date m_endTime;

		Task(DependentTaskSource<V, X> source, V item) {
			m_source = source;
			m_item = item;
		}

		@Override
		public void run(@Nullable Progress optionalProgress) {
			Progress progress = optionalProgress == null ? new Progress("") : optionalProgress;
			m_source.runTask(this, progress);
		}

		private void completed(@Nullable Throwable exception) {
			//-- Part 1: set the task itself to completed state
			synchronized(m_source) {
				if(m_state != TaskState.RUNNING)
					throw new IllegalStateException("The task " + this + " can only be completed in RUNNING state but it is in state " + m_state);
				if(exception != null) {
					//System.out.println("Task " + this + " failed, cancelling parents");
					m_exception = exception;
					m_state = TaskState.FAILED;

					cancelParents(this);
				} else {
					m_state = TaskState.COMPLETED;

					//-- Remove me from all parents, and mark the parent as RUNNABLE if it has no more children and is not failed.
					for(Task<V, X> parent : m_parents) {
						parent.m_children.remove(this);
						if(parent.m_children.isEmpty()) {
							if(parent.m_state == TaskState.NONE) {
								Objects.requireNonNull(m_source.m_runnableTasks).add(parent);
							}
						}
					}
				}
				m_source.notifyAll();            // Some state changed, notify waiters
			}
		}

		/**
		 * Cancel all parents (and optionally their children) that are not active yet.
		 */
		private void cancelParents(Task<V, X> failedTask) {
			synchronized(m_source) {
				for(Task<V, X> parent : m_parents) {
					//System.out.println("Setting parent " + parent + " to failed");
					parent.m_children.remove(this);

					if(parent.m_state == TaskState.NONE || parent.m_state == TaskState.SCHEDULED) {
						//-- Not yet failed -> fail it now.
						parent.m_state = TaskState.CANCELLED;
						parent.m_failedTask = failedTask;
						m_source.m_todo.remove(parent);
						m_source.m_cancelledSet.add(parent);
						m_source.m_allDoneSet.add(parent);

						if(m_source.m_cancelChildrenOnError)
							cancelChildren(failedTask);

						parent.cancelParents(failedTask);
					} else {
						//System.out.println("Setting parent " + parent + " to cancelled was not needed: it was already " + parent.m_state);
					}
				}
			}
		}

		/**
		 * Mark all children and subchildren of a task as failed because some task failed.
		 */
		private void cancelChildren(Task<V, X> failedTask) {
			for(Task<V, X> child : m_children) {
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

		public DependentTaskSource<V, X> getSource() {
			return m_source;
		}

		public synchronized TaskState getState() {
			return m_state;
		}

		public V getItem() {
			return m_item;
		}

		public void addChild(Task<V, X> childTask) {
			m_children.add(childTask);
			childTask.m_parents.add(this);
		}

		public List<Task<V, X>> getChildren() {
			return m_children;
		}

		@Nullable
		public synchronized Throwable getException() {
			return m_exception;
		}

		@Nullable
		public synchronized String getErrorMessage() {
			Throwable exception = m_exception;
			if(null != exception) {
				return exception.toString();
			}
			return null;
		}

		@Nullable
		public Task<V, X> getFailedTask() {
			return m_failedTask;
		}

		@Override
		public String toString() {
			return "task " + m_item;
		}

		@Nullable
		public synchronized X getExecutor() {
			return m_executor;
		}

		synchronized void setExecutor(X executor) {
			m_executor = executor;
		}

		@Nullable
		synchronized public Date getStartTime() {
			return m_startTime;
		}

		synchronized void setStartTime(@Nullable Date startTime) {
			m_startTime = startTime;
		}

		@Nullable
		synchronized public Date getEndTime() {
			return m_endTime;
		}

		synchronized void setEndTime(@Nullable Date endTime) {
			m_endTime = endTime;
		}
	}
}
