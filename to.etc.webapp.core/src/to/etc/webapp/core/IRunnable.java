package to.etc.webapp.core;

/**
 * Like Runnable, buth without the need to wrap every bloody exception.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 28, 2010
 */
public interface IRunnable {
	void run() throws Exception;
}
