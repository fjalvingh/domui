package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

public class AutoLayout extends AutoComponent implements IFbLayout {
	public AutoLayout(@NonNull Class< ? extends NodeBase> componentClass) {
		super(componentClass);
	}

	@Override
	public NodeContainer createNodeInstance() throws Exception {
		return (NodeContainer) super.createNodeInstance();
	}


}
