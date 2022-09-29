package to.etc.parallelrunner.bulk;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.FunctionEx;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-05-22.
 */
final public class BulkTaskRunner<T> implements AutoCloseable {

	private final List<AbstractTaskExecutor<T>> m_allThreadList = new ArrayList<>();

	private final List<AbstractTaskExecutor<T>> m_freeThreadList = new ArrayList<>();

	private boolean m_finished;

	private Exception m_failed;

	private Consumer<? super AbstractTaskExecutor<T>> m_onTaskCompleted;

	private BiConsumer<? super AbstractTaskExecutor<T>, Throwable> m_onTaskFailed;

	/**
	 * Starts execution of threads. Uses specified capacity of threads, and blocks in adding tasks if no threads are available.
	 * Call addTask to add tasks, and waitTillFinished at the end to wait for all work to complete. Call close after that.
	 */
	public void start(FunctionEx<BulkTaskRunner<T>, AbstractTaskExecutor<T>> executorSupplier, int nThreads) throws Exception {
		start(executorSupplier, nThreads, null, null);
	}

	/**
	 * Adds optional callbacks for each individual completed or failed executor task to handle possible re-work in tasks.
	 */
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

	private void waitForStart() throws InterruptedException {
		Exception error;
		long ets = System.currentTimeMillis() + 120 * 1000;
		try {
			synchronized(this) {
				for(; ; ) {
					error = m_failed;
					if(error != null) {
						break;
					}
					if(m_freeThreadList.size() == m_allThreadList.size()) {
						return;                                    // All are there
					} else {
						if(System.currentTimeMillis() >= ets) {
							error = new IllegalStateException("Threads do not become available in time");
							break;
						}

						wait(60_000);
					}
				}
			}
		} catch(Exception x) {
			safeClose();
			throw x;
		}
	}

	private void safeClose() {
		try {
			close();
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	public void addTask(T task) throws Exception {
		AbstractTaskExecutor<T> exec;
		synchronized(this) {
			for(;;) {
				if(m_finished)
					throw new IllegalStateException("Attempt to add task while we're finished");
				if(m_freeThreadList.size() > 0) {
					exec = m_freeThreadList.remove(m_freeThreadList.size() - 1);
					break;
				}
				wait();
			}
		}
		exec.setTask(task);
	}

	synchronized void startFailed(Exception x) {
		m_failed = x;
		notifyAll();
	}

	public void waitTillFinished() throws Exception {
		System.out.println("Waiting all tasks to finish");
		Exception error;
		synchronized(this) {
			for(;;) {
				if(m_finished) {
					return; //it is closed already
				}
				if(m_freeThreadList.size() == m_allThreadList.size()) {
					m_finished = true;
					System.out.println("All tasks finished");
					return;
				}else {
					try {
						wait(60_000);
					} catch(Exception x) {
						error = x;
						break;
					}
				}
			}
		}

		safeClose();
		if(null != error) {
			throw error;
		}
	}

	@Override
	public void close() throws Exception {
		System.out.println("Closing bulk task runner");
		List<AbstractTaskExecutor<T>> all;
		synchronized(this) {
			m_finished = true;
			all = new ArrayList<>(m_allThreadList);
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
		if(!m_freeThreadList.contains(executor)) {
			m_freeThreadList.add(executor);
			notifyAll();
		}
	}

	public void reportStatus() {
		synchronized(this) {
			System.out.println("---- executor status");
			System.out.println(m_allThreadList.size() + " total threads");
			System.out.println(m_freeThreadList.size() + " free threads");
		}
	}

	/**
	 * Called when an executor THREAD has stopped.
	 */
	synchronized void taskTerminated(AbstractTaskExecutor<T> executor) {
		m_allThreadList.remove(executor);
		m_freeThreadList.remove(executor);
	}
}
