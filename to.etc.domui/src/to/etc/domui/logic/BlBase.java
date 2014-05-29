package to.etc.domui.logic;

import javax.annotation.*;

import to.etc.webapp.query.*;

/**
 * Base class for all {@link LogiContext} instance wrappers.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 13, 2014
 */
public abstract class BlBase<T extends IIdentifyable< ? >> extends BcBase implements IInstanceLogic<T> {

	@Nonnull
	private final T m_instance;

	public BlBase(@Nonnull LogiContext lc, @Nonnull T instance) {
		super(lc);
		m_instance = instance;
	}

	@Override
	@Nonnull
	public T getInstance() {
		return m_instance;
	}

}
