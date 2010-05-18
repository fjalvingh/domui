package to.etc.domui.component.tree;

/**
 * The listener that's notified when a tree expands or collapses a node.
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 16 Apr 2010
 */
public interface ITreeWillExpandListener {
	public void treeWillCollapse(Object treeNode) throws Exception;

	public void treeWillExpand(Object treeNode) throws Exception;
}
