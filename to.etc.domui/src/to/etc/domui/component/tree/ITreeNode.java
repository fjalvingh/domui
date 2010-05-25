package to.etc.domui.component.tree;

/**
 * Can be used to implement whatever's needed for a ITreeModel.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 17, 2008
 */
public interface ITreeNode<T extends ITreeNode<T>> {
	/**
	 * If possible this should quickly decide if this node has children or not. This is
	 * used to render an expanded node's state icons. If determining whether a node has
	 * children is an expensive option this method should return TRUE always; this causes
	 * the state icon to display as if children are available and the user has the possibility
	 * to expand that node. At that time we'll call getChildCount() which <i>must</i> determine
	 * the #of children. If that returns zero it will at that time properly re-render the state
	 * of the node, showing that the node is actually a leaf and cannot be expanded further.
	 * @param item
	 * @return
	 */
	public boolean hasChildren() throws Exception;

	/**
	 * Returns the #of children for this object. This must return the actual number.
	 * @param item
	 * @return
	 */
	public int getChildCount() throws Exception;

	/**
	 * Returns the nth child in the parent's list.
	 * @param parent
	 * @param index
	 * @return
	 * @throws Exception
	 */
	public T getChild(int index) throws Exception;

	public T getParent() throws Exception;
}
