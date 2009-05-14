package to.etc.iocular;

/**
 * Base exception for all IOC container exceptions.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 27, 2007
 */
public class IocException extends RuntimeException {
	public IocException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public IocException(String arg0) {
		super(arg0);
	}

	public IocException(Throwable arg0) {
		super(arg0);
	}
}
