package to.etc.domui.logic;

import javax.annotation.*;

import to.etc.webapp.query.*;

/**
 * Base class for all {@link LogiContext} instance wrappers.
 *
 * @author <a href="mailto:imilovanovic@execom.eu">Igor Milovanovic</a>
 * Created on May 23, 2014
 */
public abstract class BcBase implements IClassLogic {

	@Nonnull
	private final LogiContext m_lc;

	public BcBase(@Nonnull LogiContext lc) {
		m_lc = lc;
	}

	@Override
	@Nonnull
	public LogiContext lc() {
		return m_lc;
	}

	@Nonnull
	protected QDataContext dc() {
		return m_lc.dc();
	}
}
