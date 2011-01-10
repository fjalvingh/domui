package to.etc.domui.themes;

import to.etc.domui.util.resources.*;

/**
 * This implements IIsModified to make a theme part of a dependency list. Because a theme
 * can use a lot of files (all fragments and properties etc) we check for changes only every
 * few seconds. This implements that behaviour.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 10, 2011
 */
final public class ThemeModifyableResource implements IIsModified {
	/** The dependency list that is checked only every [m_interval] milliseconds. */
	final private ResourceDependencies m_wrappedDependencies;

	final private int m_interval;

	/** Timestamp when next test needs to take place. */
	private long m_tsNextTest;

	/** When set, the dependencies have actually changed. */
	private boolean m_modified;

	/**
	 * Create a wrapper.
	 * @param rdl		The resource list to wrap
	 * @param interval	The max age of the test, in milliseconds.
	 */
	public ThemeModifyableResource(final ResourceDependencies rdl, int interval) {
		m_wrappedDependencies = rdl;
		m_interval = interval;
	}

	/**
	 * If it's not yet time to check for changes again return false (unchanged), else
	 * check the original dependencies. If that did not change reset the timer.
	 *
	 * @see to.etc.domui.util.resources.IIsModified#isModified()
	 */
	@Override
	public boolean isModified() {
		synchronized(this) {
			if(m_modified)
				return true;

			long cts = System.currentTimeMillis();
			if(cts < m_tsNextTest)
				return false;

			//-- It's time to use the wrapped thing.
			m_tsNextTest = cts + m_interval;
			if(m_wrappedDependencies.isModified()) {
				//-- Ohh yes.. It has changed allright. Mark this as changed, and stop any further checking.
				m_modified = true;
			}
			return m_modified;
		}
	}
}
