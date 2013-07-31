package to.etc.launcher.runner;

import java.io.*;
import java.util.*;

import javax.annotation.*;

/**
 * Provides synchronized queue of {@link RunnablePair} objects, for multithread consuming.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Jul 31, 2013
 */
public class RunnableDataProvider implements IRunnablePairProvider {
	private @Nonnull
	final List<File>	m_suiteFiles;

	private @Nonnull
	final List<String>	m_browserStrings;

	private int			m_runnablePairIndex	= 0;

	/**
	 * Creates provider on top of specified matrix (suiteFiles per browserStrings).
	 * @param suiteFiles
	 * @param browserStrings
	 */
	public RunnableDataProvider(@Nonnull List<File> suiteFiles, @Nonnull List<String> browserStrings) {
		m_suiteFiles = suiteFiles;
		m_browserStrings = browserStrings;
	}

	/**
	 * Provides next {@link RunnablePair} for multithreaded test runners consumption.
	 *
	 * @see to.etc.launcher.runner.IRunnablePairProvider#getNext()
	 */
	@Override
	@Nullable
	public synchronized RunnablePair getNext() {
		m_runnablePairIndex++;
		if(m_runnablePairIndex > m_suiteFiles.size() * m_browserStrings.size()) {
			return null;
		} else {
			int fileIndex = (m_runnablePairIndex - 1) % m_suiteFiles.size();
			int browserInxdex = (m_runnablePairIndex - 1) / m_suiteFiles.size();
			return new RunnablePair(m_suiteFiles.get(fileIndex), m_browserStrings.get(browserInxdex));
		}
	}

}
