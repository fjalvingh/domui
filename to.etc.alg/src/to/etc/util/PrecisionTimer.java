package to.etc.util;

/**
 * Implements a precision timer for (currently) the Windows platform. It contains
 * a native method which uses the Windows QueryPerformanceCounter() call to
 * retrieve a high-precision timer. This class can also be used on other
 * platforms; for those platforms where no specific DLL available the
 * System.currentTimeMillis() call will be used.. The C source for the native
 * DLL is present also.
 *
 * To make this work the DLL must be present in the path.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class PrecisionTimer {
	/// T if this class HAS found it's native library.
	static private boolean	m_has_impl;

	static private long		m_rootcount;

	/// Returns a timer value. The value is a number of microseconds (us).
	private PrecisionTimer() {
	}

	static public long getTime() {
		if(m_has_impl)
			return getOsTimer();
		else
			return (System.currentTimeMillis() - m_rootcount) * 1000;
	}

	static public boolean isPrecise() {
		return m_has_impl;
	}

	/**
	 *	Returns the approx. precision of the timer.
	 */


	static private native long getOsTimer();

	/**
	 *	Static constructor which loads the system library.
	 */
	static {
		m_rootcount = System.currentTimeMillis();
		try {
			System.loadLibrary("perftimer");
			m_has_impl = true;
		} catch(Throwable x) {
			m_has_impl = false;
			System.out.println("to.mumble.util.PrecisionTimer: Using System.currentTimeMillis() because precision timer DLL not found.");
		}
	}


	static public void main(String args[]) {
		if(isPrecise())
			System.out.println("Using the HIGH RESOLUTION timer library.");
		else
			System.out.println("Using System.currentTimeMillis() because precision timer DLL not found.");

		for(int i = 0; i < 30; i++) {
			getTime();
			System.currentTimeMillis();
		}
		long pt = getTime();
		long st = System.currentTimeMillis();
		long et = st + 4002;
		long est;
		while((est = System.currentTimeMillis()) < et)
			;
		long ept = getTime();

		System.out.println("System time lapse was " + (est - st) + " ms");
		System.out.println("Precision time lapse was " + (ept - pt) + " us");
	}

}
