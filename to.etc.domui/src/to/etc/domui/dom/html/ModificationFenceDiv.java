package to.etc.domui.dom.html;

/**
 * 
 * 
 *
 * @author <a href="mailto:imilovanovic@execom.eu">Igor Milovanović</a>
 * Created on Dec 8, 2009
 */
public class ModificationFenceDiv extends Div implements IUserInputModifiedFence {

	private boolean m_finalUserInputModifiedFence;

	private boolean m_modified;

	private boolean m_ignoreModifiedInputs;

	public boolean isIgnoreModifiedInputs() {
		return m_ignoreModifiedInputs;
	}

	public void setIgnoreModifiedInputs(boolean ignoreModifiedInputs) {
		m_ignoreModifiedInputs = ignoreModifiedInputs;
	}

	@Override
	public boolean isFinalUserInputModifiedFence() {
		return m_finalUserInputModifiedFence;
	}

	public void setFinalUserInputModifiedFence(boolean finalUserInputModifiedFence) {
		m_finalUserInputModifiedFence = finalUserInputModifiedFence;
	}

	@Override
	public boolean isModified() {
		return m_ignoreModifiedInputs ? false : m_modified;
	}

	@Override
	public void setModified(boolean as) {
		m_modified = as;
	}

}
