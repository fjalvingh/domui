package to.etc.util;

/**
 * Interface to accept error messages.
 *
 * <p>Created on Jul 26, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public interface MessageSink {
	public void error(String code, Object... param);

	public void warning(String code, Object... param);

	public void info(String code, Object... param);
}
