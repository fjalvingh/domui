package to.etc.util;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

	private Throwable m_exception;

	public FutureImpl() {
		m_description = null;
	}

	public FutureImpl(@Nullable String description) {
		m_description = description;
	}

	/**
	 * Create a future that's already done, with the specified result.
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
	 */
	public FutureImpl(@NonNull Exception error) {
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
	public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
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
	 */
	public synchronized void	set(@Nullable T value) {
		m_done = true;
		m_result = value;
		notifyAll();
	}

	/**
	 * Mark the future as ready, but aborted with an exception.
	 */
	public synchronized void set(@NonNull Throwable reason) {
		m_exception = reason;
		m_done = true;
		notifyAll();
	}

	@Nullable
	public String getDescription() {
		return m_description;
	}

	@Override
	public String toString() {
		String description = m_description;
		if(null != description)
			return description;
		synchronized(this) {
			if(m_done) {
				Throwable exception = m_exception;
				if(exception != null)
					return exception.toString();
				return String.valueOf(m_result);
			}
		}
		return "(future value)";
	}
}
