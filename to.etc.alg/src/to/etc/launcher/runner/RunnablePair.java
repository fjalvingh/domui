package to.etc.launcher.runner;

import java.io.*;

import javax.annotation.*;

/**
 * Pojo containg suite and browserString.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Jul 31, 2013
 */
public class RunnablePair {
	private final @Nonnull
	File	m_suite;

	private final @Nonnull
	String	m_browserString;

	public RunnablePair(@Nonnull File suite, @Nonnull String browserString) {
		super();
		m_suite = suite;
		m_browserString = browserString;
	}

	public @Nonnull
	File getSuite() {
		return m_suite;
	}

	public @Nonnull
	String getBrowserString() {
		return m_browserString;
	}
}

