package to.etc.domui.component.lookup;

import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeBase;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/10/16.
 */
@Deprecated
public abstract class BaseAbstractLookupControlImpl<T> implements ILookupControlInstance<T> {

	private NodeBase[] m_nodes;

	@Override
	@Nonnull
	abstract public AppendCriteriaResult appendCriteria(@Nonnull QCriteria<?> crit) throws Exception;

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
				if(m_node instanceof IControl<?>) {
					IControl<?> control = (IControl<?>) m_node;
					if(control.isMandatory()) {
						throw new ProgrammerErrorException("The manually added LookupForm input control "
							+ control
							+ " is lacking a clearInput() method. This is needed because clearing a "
							+ "mandatory control means setting it to its default value - and I do "
							+ "not just know that value. See https://github.com/fjalvingh/domui/issues/6."
						);
					}

					control.setValue(null);
					done = true;
				}
			}
		}
		if(!done)
			throw new IllegalStateException("The implementation for " + this + " needs an overridden clearInput() method");
	}

	/**
	 * Default implementation
	 */
	@Override
	public void setDisabled(boolean disabled) {
		boolean done = false;
		if(m_nodes != null) {
			for(NodeBase m_node : m_nodes) {
				if(m_node instanceof IControl<?>) {
					((IControl<?>) m_node).setDisabled(disabled);
				}
				done = true;
			}
		}
		if(!done)
			throw new IllegalStateException("The implementation for " + this + " needs an overridden setDisabled() method");
	}
}
