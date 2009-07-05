package to.etc.domui.util;

import to.etc.domui.dom.html.*;

public interface INodeContentRenderer<T> {
	/**
	 * Render the content for a node.
	 * @param component
	 * @param node
	 * @param object
	 * @param parameters
	 * @throws Exception
	 */
	public void renderNodeContent(NodeBase component, NodeContainer node, T object, Object parameters) throws Exception;
}
