package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.state.UIGotoContext;

/**
 * Marks the UrlPage for custom handling of leaving the page with
 * unsaved data, caused by domui navigation.
 */
public interface IPageWithNavigationHandler extends IPageWithNavigationCheck {
	/**
	 * Implements page specific code how user can handle navigation on modified screen data.
	 */
	void handleNavigationOnModified(@NonNull UIGotoContext gotoCtx);

	/**
	 * Implements page specific code how user can handle callback that would cause page navigation on modified screen data.
	 */
	void handleNavigationOnModified(@NonNull Runnable callback);
}
