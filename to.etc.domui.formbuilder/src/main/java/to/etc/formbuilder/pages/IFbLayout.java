package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeContainer;

public interface IFbLayout extends IFbComponent {
	@Override
	@NonNull NodeContainer createNodeInstance() throws Exception;

}
