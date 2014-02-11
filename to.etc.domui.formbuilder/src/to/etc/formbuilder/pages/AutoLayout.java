package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

public class AutoLayout extends AutoComponent implements IFbLayout {
	public AutoLayout(@Nonnull Class< ? extends NodeBase> componentClass) {
		super(componentClass);
	}

	@Override
	public NodeContainer createNodeInstance() throws Exception {
		return (NodeContainer) super.createNodeInstance();
	}


}
