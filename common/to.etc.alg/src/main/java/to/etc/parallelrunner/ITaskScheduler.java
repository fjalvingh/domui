package to.etc.parallelrunner;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Schedules the actual execution of the tasks managed by a {@link DependentTaskRunner}. This
 * abstracts away <i>where</i> and <i>how</i> a runnable task gets its worker thread, so that a
 * runner can dispatch either to the shared {@link AsyncWorker} or to any plain
 * {@link java.util.concurrent.Executor}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@NonNullByDefault
@FunctionalInterface
public interface ITaskScheduler {
	/**
	 * Schedule the given runnable for asynchronous execution. Implementations that do not support
	 * naming or prioritisation are free to ignore the {@code name} and {@code priority} arguments.
	 */
	void schedule(String name, IAsyncRunnable runnable, int priority) throws Exception;
}
