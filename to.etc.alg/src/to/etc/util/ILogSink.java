package to.etc.util;

/**
 * Stuff to write logging to.
 *
 * @author jal
 * Created on Apr 16, 2005
 */
public interface ILogSink {
	void log(String msg);

	void exception(Throwable t, String msg);
}
