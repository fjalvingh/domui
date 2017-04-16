package to.etc.util;

import java.util.concurrent.*;

import javax.annotation.*;

/**
 * Basic non-blocking Implementation of {@code Future<T>} which immediately aborts on get() if no value is provided.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 14, 2013
 */
public class FutureValueNB<T> implements Future<T> {
	final private String	m_description;

	private boolean m_cancelled;

	private boolean m_done;

	private T m_result;

	private Exception m_exception;

	public FutureValueNB() {
		m_description = null;
	}

	public FutureValueNB(@Nullable String description) {
		m_description = description;
	}

	/**
	 * Create a future that's already done, with the specified result.
	 * @param result
	 */
	public FutureValueNB(@Nullable T result) {
		m_done = true;
		m_result = result;
		m_description = null;
	}

	public FutureValueNB(@Nullable String description, @Nullable T result) {
		m_done = true;
		m_result = result;
		m_description = description;
	}


	/**
	 * Create a future that's already done, with the specified error.
	 * @param error
	 */
	public FutureValueNB(@Nonnull Exception error) {
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
		if(!m_done)
			throw new IllegalStateException(this + ": value has not yet been set");

		if(m_exception != null)
			throw new ExecutionException(m_exception);	// They are complete idiots, those java people.
		return m_result;							// Then return the result.
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return get();
	}

	/**
	 * Return the result, and notify any waiters.
	 * @param value
	 */
	public synchronized void	set(@Nullable T value) {
		m_done = true;
		m_result = value;
	}

	/**
	 * Mark the future as ready, but aborted with an exception.
	 * @param reason
	 */
	public synchronized void set(@Nonnull Exception reason) {
		m_exception = reason;
		m_done = true;
	}

	@Override
	public String toString() {
		return m_description == null ? super.toString() : m_description;
	}
}
