package to.etc.domui.component.misc;

import to.etc.domui.dom.html.*;

/**
 * General purpose interface wich is meant to be used when dinamic links between nodes are needed.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 16 Jun 2011
 */
public interface INodeProvider {

	/**
	 * Returns dinamic reference to required NodeBase
	 * @param sender Call originator
	 * @return
	 */
	public NodeBase getNode(NodeBase sender);

}
