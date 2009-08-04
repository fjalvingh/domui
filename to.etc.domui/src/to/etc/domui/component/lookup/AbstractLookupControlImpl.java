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

	abstract public boolean appendCriteria(QCriteria<?> crit) throws Exception;

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
		if(m_nodes == null || m_nodes.length == 0 || !(m_nodes[0] instanceof IInputNode<?>))
			throw new IllegalStateException("The implementation for "+this+" needs an overridden clearInput() method");
		((IInputNode<?>)m_nodes[0]).setValue(null);
	}
}
