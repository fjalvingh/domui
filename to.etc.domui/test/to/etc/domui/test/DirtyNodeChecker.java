package to.etc.domui.test;

import to.etc.domui.dom.html.*;

public class DirtyNodeChecker extends NodeVisitorBase {
	@Override
	public void visitNodeBase(NodeBase n) throws Exception {
		if(n.hasChangedAttributes())
			throw new IllegalStateException("The node " + n + " has DIRTY ATTRIBUTES set");
	}

	@Override
	public void visitNodeContainer(NodeContainer n) throws Exception {
		n.internalCheckNotDirty();
		visitChildren(n);
	}
}
