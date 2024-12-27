package to.etc.alg.process;

import to.etc.function.SupplierEx;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * This helper starts a new thread to execute something, and
 * returns a Future to get the result. It is a replacement
 * for the very often wrongly used Executors.newSingleThreadExecutor
 * for which only very bad examples seem to exist on the Internet.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 05-02-2024.
 */
final public class SingleThreadRunner {
	/**
	 * Start the code in a separate thread and return a Future to receive the
	 * result of the thing.
	 * This does NOT use a thread pool, so the thread gets deallocated as
	 * soon as it finishes which is often wanted for code that is not executing
	 * that much.
	 * If you need to limit the number of threads you need to use an Executor
	 * instead of this, but <b>DO NOT FORGET TO SHUTDOWN THAT EXECUTOR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!</b>.
	 */
	static public <T> Future<T> async(String threadName, SupplierEx<T> code) {
		return async(true, threadName, code);
	}

	static private <T> Future<T> async(boolean asDeamon, String threadName, SupplierEx<T> code) {
		CompletableFuture<T> future = new CompletableFuture<>();			// This will contain the result

		Runnable r = new Runnable() {
			@Override
			public void run() {
				T result;
				try {
					result = code.get();
				} catch(Throwable t) {
					future.completeExceptionally(t);
					return;
				}
				future.complete(result);
			}
		};

		Thread t = new Thread(r, threadName);
		t.setDaemon(asDeamon);
		t.start();
		return future;
	}
}
