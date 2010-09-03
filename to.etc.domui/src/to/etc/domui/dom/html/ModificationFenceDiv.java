package to.etc.domui.dom.html;

/**
 * This is DIV that is used as user input modified flag fence.
 * Usually it is used to ignore input controls modification in some screen region.
 * When components are used for read only data presentation purposes (like viewers of selection filters)
 * those components does not modify any page value content.
 *
 * @author <a href="mailto:imilovanovic@execom.eu">Igor Milovanović</a>
 * Created on Dec 8, 2009
 */
public class ModificationFenceDiv extends Div implements IUserInputModifiedFence {

	private boolean m_finalUserInputModifiedFence = true;

	private boolean m_modified;

	private boolean m_ignoreModifiedInputs = true;

	/**
	 * Indicates wether component keep tracks on its childs modifications.
	 * By default set to true.
	 * @return
	 */
	public boolean isIgnoreModifiedInputs() {
		return m_ignoreModifiedInputs;
	}

	public void setIgnoreModifiedInputs(boolean ignoreModifiedInputs) {
		m_ignoreModifiedInputs = ignoreModifiedInputs;
	}

	/**
	 * @see to.etc.domui.dom.html.IUserInputModifiedFence#isFinalUserInputModifiedFence()
	 * By default set to true.
	 */
	@Override
	public boolean isFinalUserInputModifiedFence() {
		return m_finalUserInputModifiedFence;
	}

	/**
	 * @see to.etc.domui.dom.html.IUserInputModifiedFence#isFinalUserInputModifiedFence()
	 */
	public void setFinalUserInputModifiedFence(boolean finalUserInputModifiedFence) {
		m_finalUserInputModifiedFence = finalUserInputModifiedFence;
	}

	@Override
	public void onModifyFlagRaised() {
	//by default do nothing
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
