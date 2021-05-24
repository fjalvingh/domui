package to.etc.domui.dom.html;

/**
 * UrlPage that implements it would be called to check if page leave navigation can be performed.
 */
public interface IPageWithNavigationCheck {

	/**
	 * Returns if page has modifications that should prevent navigation.
	 * @return
	 */
	boolean hasModification();

	/**
	 * Call to let user handle how to resolve the navigation on modified screen data.
	 * @return
	 */
	void handleNavigationOnModified();
}
