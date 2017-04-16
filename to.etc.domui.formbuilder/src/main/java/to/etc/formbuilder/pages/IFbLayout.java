package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

public interface IFbLayout extends IFbComponent {
	@Override
	@Nonnull
	public NodeContainer createNodeInstance() throws Exception;

}
