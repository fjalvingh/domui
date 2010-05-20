package to.etc.domui.component.tree;

public interface ITreeModelChangedListener {

	public void nodeWillExpand(ITreeNode< ? > item) throws Exception;

	public void nodeWillCollapse(ITreeNode< ? > item) throws Exception;
}
