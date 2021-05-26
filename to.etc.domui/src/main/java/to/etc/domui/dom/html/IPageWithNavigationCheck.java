package to.etc.domui.dom.html;

/**
 * Marks the UrlPage for check if user can leave the page with unsaved data, for both browser and domui (UIGoto) navigation.
 */
public interface IPageWithNavigationCheck {

	/**
	 * Returns if page has modifications that should be checked before allowing of leaving the page.
	 * @return
	 */
	boolean hasModification();
}
