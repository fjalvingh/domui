package to.etc.parallelrunner.bulk;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.FunctionEx;
import to.etc.util.WrappedException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-05-22.
 */
@NonNullByDefault
final public class BulkTaskRunner<T> implements AutoCloseable {

	private final List<AbstractTaskExecutor<T>> m_allThreadList = new ArrayList<>();

	private final List<AbstractTaskExecutor<T>> m_freeThreadList = new ArrayList<>();

	private boolean m_finished;

	private Exception m_failed;

	private Consumer<? super AbstractTaskExecutor<T>> m_onTaskCompleted;

	private BiConsumer<? super AbstractTaskExecutor<T>, Throwable> m_onTaskFailed;

	public void start(FunctionEx<BulkTaskRunner<T>, AbstractTaskExecutor<T>> executorSupplier, int nThreads) throws Exception {
		start(executorSupplier, nThreads, null, null);
	}

	public void start(FunctionEx<BulkTaskRunner<T>, AbstractTaskExecutor<T>> executorSupplier, int nThreads, @Nullable Consumer<? super AbstractTaskExecutor<T>> onTaskCompleted, @Nullable BiConsumer<? super AbstractTaskExecutor<T>, Throwable> onTaskFailed) throws Exception {
		m_onTaskCompleted = onTaskCompleted;
		m_onTaskFailed = onTaskFailed;
		try {
			synchronized(this) {
				m_finished = false;

				for(int i = 0; i < nThreads; i++) {
					AbstractTaskExecutor<T> executor = executorSupplier.apply(this);
					m_allThreadList.add(executor);
					executor.setDaemon(true);
					executor.start();
				}
			}

			//-- Wait for all threads to become available OR for a start failure.
			waitForStart();

		} catch(Exception | Error x) {
			close();
			throw x;
		}
	}

	private void waitForStart() throws Exception {
		Exception error;
		long ets = System.currentTimeMillis() + 120 * 1000;
		synchronized(this) {
			for(;;) {
				error = m_failed;
				if(error != null) {
					break;
				}
				if(m_freeThreadList.size() == m_allThreadList.size())
					return;									// All are there

				if(System.currentTimeMillis() >= ets) {
					error = new IllegalStateException("Threads do not become available in time");
					break;
				}

				try {
					wait(60_000);
				} catch(Exception x) {
					error = x;
					break;
				}
			}
		}

		try {
			close();
		} catch(Exception x) {
			x.printStackTrace();
		}
		if(null != error) {
			throw error;
		}
	}

	public void addTask(T task) {
		synchronized(this) {
			for(;;) {
				if(m_finished)
					throw new IllegalStateException("Attempt to add task while we're finished");
				if(m_freeThreadList.size() > 0) {
					AbstractTaskExecutor<T> exec = m_freeThreadList.remove(m_freeThreadList.size() - 1);
					exec.setTask(task);
					return;
				}
				try {
					wait();
				} catch(Exception x) {
					throw WrappedException.wrap(x);
				}
			}
		}
	}

	synchronized void startFailed(Exception x) {
		m_failed = x;
		notifyAll();
	}

	@Override
	public void close() throws Exception {
		List<AbstractTaskExecutor<T>> all;
		synchronized(this) {
			m_finished = true;
			all = m_allThreadList;
		}

		for(AbstractTaskExecutor<T> executor : all) {
			executor.setFinished();
		}
		for(AbstractTaskExecutor<T> executor : all) {
			executor.join();
		}

		synchronized(this) {
			m_allThreadList.clear();
			m_freeThreadList.clear();
		}
	}

	void taskFinished(AbstractTaskExecutor<T> executor) {
		Consumer<? super AbstractTaskExecutor<T>> onTaskCompleted = m_onTaskCompleted;
		if(null != onTaskCompleted) {
			onTaskCompleted.accept(executor);
		}
	}

	void taskFailed(AbstractTaskExecutor<T> executor, Throwable ex) {
		BiConsumer<? super AbstractTaskExecutor<T>, Throwable> onTaskFailed = m_onTaskFailed;
		if(null != onTaskFailed) {
			onTaskFailed.accept(executor, ex);
		}
	}

	public int getNumberOfRunningExecutors() {
		synchronized(this) {
			return m_allThreadList.size() - m_freeThreadList.size();
		}
	}

	/**
	 * Called when a task is free.
	 */
	synchronized void taskFree(AbstractTaskExecutor<T> executor) {
		m_freeThreadList.add(executor);
		notifyAll();
	}
}
