package to.etc.domui.util;

import to.etc.domui.dom.html.*;

public class UndefinedLabelStringRenderer implements ILabelStringRenderer<Object>, INodeContentRenderer<Object> {
	@Override
	public String getLabelFor(Object object) {
		return object == null ? "" : object.toString();
	}

	@Override
	public void renderNodeContent(NodeBase component, NodeContainer node, Object object, Object parameter) {
		throw new IllegalStateException();
	}
}
