package to.etc.domui.component.lookup;

import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

abstract public class DefaultLookupThingy implements LookupFieldQueryBuilderThingy {
	private NodeBase[]		m_nodes;

	public DefaultLookupThingy(NodeBase... nodes) {
		m_nodes = nodes;
	}

	abstract public boolean appendCriteria(QCriteria<?> crit) throws Exception;

	public NodeBase[] getInputControls() {
		return m_nodes;
	}
}
