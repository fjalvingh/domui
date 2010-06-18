package to.etc.domui.util.bugs;

import javax.annotation.*;

/**
 * Accessor to post odd conditions for later review.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2010
 */
public class Bug {
	static private final ThreadLocal<IBugListener> m_listener = new ThreadLocal<IBugListener>();

	private Bug() {}

	/**
	 * Show a bug using the specified message.
	 * @param message
	 */
	static public void bug(@Nonnull String message) {
		BugItem	bi = new BugItem(message);
		postBug(bi);
	}

	static public void bug(@Nullable Throwable x, @Nonnull String message) {
		BugItem bi = new BugItem(message, x);
		postBug(bi);
	}

	static public void bug(@Nullable Throwable x) {
		BugItem bi = new BugItem(x.getMessage(), x);
		postBug(bi);
	}

	private static void postBug(BugItem bi) {
		IBugListener	listener = m_listener.get();
		if(null == listener) {
			System.out.println("BUG: " + bi.getMessage());
			if(null != bi.getException()) {
				bi.getException().printStackTrace();
			}
			return;
		}

		//-- Post the bug, but do not fail if posting throws booboo
		try {
			listener.bugSignaled(bi);
		} catch(Exception x) {
			x.printStackTrace(); // Explicitly do not handle
		}
	}

	@Nullable
	static public IBugListener getListener() {
		return m_listener.get();
	}

	static public void setListener(@Nullable IBugListener l) {
		m_listener.set(l);
	}
}
