package to.etc.domui.logic;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.webapp.query.QDataContext;

/**
 * Base class for all {@link LogicContextImpl} class wrappers.
 *
 * @author <a href="mailto:imilovanovic@execom.eu">Igor Milovanovic</a>
 * Created on May 23, 2014
 */
public abstract class BcBase implements IClassLogic {

	@NonNull
	private final ILogicContext m_lc;

	public BcBase(@NonNull ILogicContext lc) {
		m_lc = lc;
	}

	@Override
	@NonNull
	public ILogicContext lc() {
		return m_lc;
	}

	@NonNull
	protected QDataContext dc() {
		return m_lc.dc();
	}
}
