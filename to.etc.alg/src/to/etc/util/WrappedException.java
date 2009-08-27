package to.etc.util;

import java.lang.reflect.*;

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
