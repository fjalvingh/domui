package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.state.UIGotoContext;

/**
 * Marks the UrlPage for custom handling of leaving the page with unsaved data, caused by domui navigation.
 */
public interface IPageWithDomuiNavigationCheck extends IPageWithNavigationCheck {

	/**
	 * Implements page specific code how user can handle navigation on modified screen data.
	 * @return
	 */
	void handleNavigationOnModified(@NonNull UIGotoContext gotoCtx);
}
