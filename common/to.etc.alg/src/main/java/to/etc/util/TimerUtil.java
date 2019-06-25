package to.etc.util;

import java.util.Timer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-6-19.
 */
final public class TimerUtil {
	static private final Timer m_timer = new Timer();

	private TimerUtil() {
	}

	static public Timer getTimer() {
		return m_timer;
	}
}
