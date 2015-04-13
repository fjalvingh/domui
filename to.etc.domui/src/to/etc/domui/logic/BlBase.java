package to.etc.domui.logic;

import javax.annotation.*;

import to.etc.webapp.query.*;

/**
 * DO NOT USE: this is a dumb idea. The class contains effectively nothing.
 *
 * Base class for all {@link LogicContextImpl} instance wrappers.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 13, 2014
 */
@Deprecated
public abstract class BlBase<T extends IIdentifyable< ? >> extends BcBase implements IInstanceLogic<T> {

	@Nonnull
	private final T m_instance;

	public BlBase(@Nonnull ILogicContext lc, @Nonnull T instance) {
		super(lc);
		m_instance = instance;
	}

	@Override
	@Nonnull
	public T getInstance() {
		return m_instance;
	}

}
