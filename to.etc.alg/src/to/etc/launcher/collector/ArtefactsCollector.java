package to.etc.launcher.collector;

import java.io.*;
import java.net.*;

import javax.annotation.*;

/**
 * Base for collecting test artifacts within provided resources.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Aug 1, 2013
 */
public abstract class ArtefactsCollector {

	private IArtefactMatcher	m_matcher;

	public IArtefactMatcher getMatcher() {
		return m_matcher;
	}

	public void setMatcher(@Nullable IArtefactMatcher matcher) {
		m_matcher = matcher;
	}

	@Nonnull
	private final URLClassLoader	m_loader;

	public ArtefactsCollector(@Nonnull URLClassLoader loader) {
		m_loader = loader;
	}

	public @Nonnull
	URLClassLoader getLoader() {
		return m_loader;
	}

	protected String assamblePackagePath(@Nonnull File projectRoot, @Nonnull File file) {
		String packagePath;
		StringBuilder packageSb = new StringBuilder();
		File current = file.getParentFile();
		while(!current.getParentFile().equals(projectRoot)) {
			packageSb.insert(0, current.getName() + ".");
			current = current.getParentFile();
		}
		packagePath = packageSb.toString();
		return packagePath;
	}

}
