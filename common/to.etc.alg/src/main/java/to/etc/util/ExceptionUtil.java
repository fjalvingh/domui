package to.etc.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.RunnableEx;
import to.etc.function.SupplierEx;

import java.sql.SQLException;
import java.util.function.Function;

/**
 * ExceptionUtil
 * A collection of utility methods for working with Exceptions
 */
@NonNullByDefault
final public class ExceptionUtil {
	private ExceptionUtil() {}

	/**
	 * If runnable throws an exception, wraps the checked exception into a runtime.
	 * @param runnableEx runnable
	 */
	public static void silentThrows(RunnableEx runnableEx)  {
		try {
			runnableEx.run();
		}catch(Exception ex) {
			throw WrappedException.wrap(ex);
		}
	}

	/**
	 * If supplier throws an exception, wraps the checked exception into a runtime.
	 * @param supplierEx runnable
	 */
	public static <T> T silentThrows(SupplierEx<T> supplierEx) {
		try {
			return supplierEx.get();
		} catch(Exception e) {
			throw WrappedException.wrap(e);
		}
	}

	/**
	 * This method will run the runnable and if runnable throws an exception, the exception is ignored.
	 * Use sparingly!
	 * @param runnableEx runnable
	 */
	public static void silentFails(RunnableEx runnableEx) {
		try {
			runnableEx.run();
		}catch(Exception ignored){}
	}

	/**
	 * Runs the supplier. If the supplier fails, returns the default value.
	 * @param supplierEx supplier
	 * @param defaultValue default value
	 * @param <T> any type.
	 * @return value from the supplier or defaultValue
	 */
	@Nullable
	public static <T> T silentFails(SupplierEx<T> supplierEx, @Nullable T defaultValue) {
		try {
			return supplierEx.get();
		}catch(Exception ignored) {
			return defaultValue;
		}
	}

	/**
	 * Walk an entire exception tree to find something specific, which is then returned.
	 */
	@Nullable
	public static <T> T findException(Throwable in, Function<Throwable, T> matcher) {
		T res = matcher.apply(in);
		if(null != res)
			return res;

		if(in instanceof SQLException) {
			SQLException sx = (SQLException) in;
			SQLException next = sx.getNextException();
			if(next != in && next != null) {
				res = findException(next, matcher);
				if(null != res)
					return res;
			}
		}

		Throwable cause = in.getCause();
		if(cause != null && cause != in) {
			return findException(cause, matcher);
		}
		return null;
	}

	/**
	 * Returns T if the exception is an exception that signals
	 * a common bug, i.e. NPE, IAE, ISE etc.
	 */
	public static boolean isBugException(Throwable t) {
		String packageName = t.getClass().getPackageName();
		if(packageName.startsWith("java.lang"))
			return true;
		return false;
	}

}
