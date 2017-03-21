package to.etc.domui.component.lookup;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

/**
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/10/16.
 */
public abstract class BaseAbstractLookupControlImpl<T> implements ILookupControlInstance<T> {

	private NodeBase[] m_nodes;

	@Override
	@Nonnull
	abstract public AppendCriteriaResult appendCriteria(@Nonnull QCriteria< ? > crit) throws Exception;

	public BaseAbstractLookupControlImpl(NodeBase... nodes) {
		if(nodes.length == 0)
			throw new IllegalStateException("A node should be passed in here.");
		m_nodes = nodes;
	}

	@Override
	public NodeBase[] getInputControls() {
		return m_nodes;
	}

	@Override
	public NodeBase getLabelControl() {
		return null;
	}

	/**
	 * Default implementation
	 *
	 * @see to.etc.domui.component.lookup.ILookupControlInstance#clearInput()
	 */
	@Override
	public void clearInput() {
		boolean done = false;
		if(m_nodes != null) {
			for(NodeBase m_node : m_nodes) {
				if(m_node instanceof IControl< ? >) {
					((IControl< ? >) m_node).setValue(null);
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
	@Override
	public void setDisabled(boolean disabled) {
		boolean done = false;
		if(m_nodes != null) {
			for(NodeBase m_node : m_nodes) {
				if(m_node instanceof IControl< ? >) {
					((IControl< ? >) m_node).setDisabled(disabled);
				}
				done = true;
			}
		}
		if(!done)
			throw new IllegalStateException("The implementation for " + this + " needs an overridden setDisabled() method");
	}
}
