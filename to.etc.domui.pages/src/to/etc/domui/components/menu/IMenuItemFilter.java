package to.etc.domui.components.menu;

/**
 *
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
public interface IMenuItemFilter {
	/**
	 * Called to set the node that needs to be handled.
	 * @param currentNode
	 */
	void		setNode(MenuItemImpl currentNode) throws Exception;

	/**
	 * Must return true if the current node is accessible by the user. This must check access rights (Rights).
	 * @return
	 */
	boolean		isAllowed();
}
