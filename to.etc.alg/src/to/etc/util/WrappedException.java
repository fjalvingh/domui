package to.etc.util;

import java.lang.reflect.*;

/**
 * Helper class for managing the checked exception idiocy rampant
 * in Java code. It wraps checked exceptions in unchecked ones, and
 * has a method to unwrap them again at a higher level.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
public class WrappedException extends RuntimeException {
	public WrappedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public WrappedException(final Throwable cause) {
		super(cause.toString(), cause);
	}

	static public RuntimeException wrap(final Exception x) {
		if(x instanceof RuntimeException)
			return (RuntimeException) x;
		throw new WrappedException(x);
	}

	public static Exception unwrap(Exception x) {
		for(;;) {
			if(x instanceof WrappedException) {
				Throwable t = x.getCause();
				if(!(t instanceof Exception)) // Can we unwrap?
					return x; // No, keep wrapped
				x = (Exception) x.getCause();
			} else if(x instanceof InvocationTargetException) {
				Throwable t = x.getCause();
				if(!(t instanceof Exception)) // Can we unwrap?
					return x; // No, keep wrapped
				x = (Exception) x.getCause();
			} else
				return x;
		}
	}
}
