package to.etc.util;

import java.util.concurrent.*;

import javax.annotation.*;

/**
 * Basic Implementation of {@code Future<T>} which is nicely missing from the JDK, sigh.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 14, 2013
 */
public class FutureImpl<T> implements Future<T> {
	final private String	m_description;

	private boolean m_cancelled;

	private boolean m_done;

	private T m_result;

	private Exception m_exception;

	public FutureImpl() {
		m_description = null;
	}

	public FutureImpl(@Nullable String description) {
		m_description = description;
	}

	/**
	 * Create a future that's already done, with the specified result.
	 * @param result
	 */
	public FutureImpl(@Nullable T result) {
		m_done = true;
		m_result = result;
		m_description = null;
	}

	public FutureImpl(@Nullable String description, @Nullable T result) {
		m_done = true;
		m_result = result;
		m_description = description;
	}


	/**
	 * Create a future that's already done, with the specified error.
	 * @param error
	 */
	public FutureImpl(@Nonnull Exception error) {
		m_exception = error;
		m_done = true;
		m_description = null;
	}

	@Override
	synchronized public boolean cancel(boolean mayInterruptIfRunning) {
		m_cancelled = true;
		return false;
	}

	@Override
	synchronized public boolean isCancelled() {
		return m_cancelled;
	}

	@Override
	synchronized public boolean isDone() {
		return m_done;
	}

	/**
	 * Get the result, wait if it's not yet available.
	 * @see java.util.concurrent.Future#get()
	 */
	@Override
	synchronized public T get() throws InterruptedException, ExecutionException {
		for(;;) {
			if(m_done) {									// Marked as ready?
				if(m_exception != null)
					throw new ExecutionException(m_exception);	// They are complete idiots, those java people.
				return m_result;							// Then return the result.
			}
			wait();											// Sleep until notify
		}
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		long ets = System.currentTimeMillis() + unit.toMillis(timeout);
		for(;;) {
			if(m_done) {									// Marked as ready?
				if(m_exception != null)
					throw new ExecutionException(m_exception);	// Sigh
				return m_result;							// Then return the result.
			}
			long rest = ets - System.currentTimeMillis();	// How much longer to wait?
			if(rest <= 0)
				throw new TimeoutException();
			wait(rest);										// Sleep until notify
		}
	}

	/**
	 * Return the result, and notify any waiters.
	 * @param value
	 */
	public synchronized void	set(@Nullable T value) {
		m_done = true;
		m_result = value;
		notifyAll();
	}

	/**
	 * Mark the future as ready, but aborted with an exception.
	 * @param reason
	 */
	public synchronized void set(@Nonnull Exception reason) {
		m_exception = reason;
		m_done = true;
		notifyAll();
	}

	@Override
	public String toString() {
		return m_description == null ? super.toString() : m_description;
	}
}
