package to.etc.domui.logic;

import to.etc.webapp.query.*;

import javax.annotation.*;

/**
 * Base class for all {@link LogicContextImpl} class wrappers.
 *
 * @author <a href="mailto:imilovanovic@execom.eu">Igor Milovanovic</a>
 * Created on May 23, 2014
 */
public abstract class BcBase implements IClassLogic {

	@Nonnull
	private final ILogicContext m_lc;

	public BcBase(@Nonnull ILogicContext lc) {
		m_lc = lc;
	}

	@Override
	@Nonnull
	public ILogicContext lc() {
		return m_lc;
	}

	@Nonnull
	protected QDataContext dc() {
		return m_lc.dc();
	}
}
