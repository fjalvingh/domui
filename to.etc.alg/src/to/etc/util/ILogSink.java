package to.etc.util;

/**
 * Stuff to write logging to.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 16, 2005
 */
public interface ILogSink {
	void log(String msg);

	void exception(Throwable t, String msg);
}
