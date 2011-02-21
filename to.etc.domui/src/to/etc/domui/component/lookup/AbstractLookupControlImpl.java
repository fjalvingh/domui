package to.etc.domui.component.lookup;

import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

/**
 * Abstract default implementation of a ILookupControlInstance. This merely keeps the
 * list of Nodes representing the visual representation; concrete implementations must
 * handle the other parts.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 31, 2009
 */
abstract public class AbstractLookupControlImpl implements ILookupControlInstance {
	private NodeBase[] m_nodes;

	private InputState m_inputState = InputState.DEFAULT;

	abstract public AppendCriteriaResult appendCriteria(QCriteria< ? > crit) throws Exception;

	public AbstractLookupControlImpl(NodeBase... nodes) {
		m_nodes = nodes;
	}

	public NodeBase[] getInputControls() {
		return m_nodes;
	}

	public NodeBase getLabelControl() {
		return null;
	}

	/**
	 * Default implementation
	 *
	 * @see to.etc.domui.component.lookup.ILookupControlInstance#clearInput()
	 */
	public void clearInput() {
		boolean done = false;
		if(m_nodes != null) {
			for(NodeBase m_node : m_nodes) {
				if(m_node instanceof IInputNode< ? >) {
					((IInputNode< ? >) m_node).setValue(null);
					done = true;
				}
			}
		}
		if(!done)
			throw new IllegalStateException("The implementation for " + this + " needs an overridden clearInput() method");
	}

	/**
	 * Default implementation
	 *
	 * @see to.etc.domui.component.lookup.ILookupControlInstance#setDisabled(Boolean))
	 */
	public void setDisabled(boolean disabled) {
		setInputState(disabled ? InputState.FORCE_DISABLED : InputState.FORCE_EDITABLE);
	}

	/**
	 * @see to.etc.domui.component.lookup.ILookupControlInstance#isDisabled()
	 */
	public boolean isDisabled() {
		return InputState.FORCE_DISABLED == getInputState();
	}

	/**
	 * @return the inputState
	 */
	public InputState getInputState() {
		return m_inputState;
	}

	/**
	 * @param inputState the inputState to set
	 */
	private void setInputState(InputState inputState) {
		if(inputState == null || inputState == InputState.DEFAULT || inputState == getInputState()) {
			return;
		}
		boolean done = false;
		if(m_nodes != null) {
			for(NodeBase m_node : m_nodes) {
				if(m_node instanceof IInputNode< ? >) {
					switch(inputState){
						case FORCE_EDITABLE:
							((IInputNode< ? >) m_node).setDisabled(false);
							break;

						case FORCE_DISABLED:
							((IInputNode< ? >) m_node).setDisabled(true);
							break;

						default:
							break;
					}
				}
				done = true;
			}
		}
		if(!done)
			throw new IllegalStateException("The implementation for " + this + " needs an overridden 'setInputState' method");
	}

}
