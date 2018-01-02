package to.etc.domui.component.tree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-1-18.
 */
public class TreeNodeModel<T extends TreeNode> implements ITreeModel<T>, ITreeEditModel<T> {
	final private T m_root;

	final private List<ITreeModelChangedListener<T>> m_listeners = new ArrayList<>();

	public TreeNodeModel(T root) {
		m_root = root;
	}

	@Override public int getChildCount(@Nullable T item) throws Exception {
		return Objects.requireNonNull(item).getChildren().size();
	}

	@Nullable @Override public T getRoot() throws Exception {
		return m_root;
	}

	@Nonnull @Override public T getChild(@Nullable T parent, int index) throws Exception {
		return (T) Objects.requireNonNull(parent).getChildren().get(index);
	}

	@Nullable @Override public TreeNode getParent(@Nullable TreeNode child) throws Exception {
		return Objects.requireNonNull(child).getParent();
	}

	@Override public void addChangeListener(@Nonnull ITreeModelChangedListener<T> l) {
		m_listeners.add(l);
	}

	@Override public void removeChangeListener(@Nonnull ITreeModelChangedListener<T> l) {
		m_listeners.remove(l);
	}

	@Override public void update(T node) throws Exception {
		for(ITreeModelChangedListener<T> listener : m_listeners) {
			listener.onNodeUpdated(node);
		}
	}

	@Override public void remove(T node) throws Exception {
		TreeNode parent = Objects.requireNonNull(node.getParent());
		int index = parent.removeChild(node);
		if(index < 0)
			return;
		for(ITreeModelChangedListener<T> listener : m_listeners) {
			listener.onNodeRemoved((T) parent, index, node);
		}
	}

	@Override public void add(T newParent, int newIndex, T nodeToAdd) throws Exception {
		newParent.getChildren().add(newIndex, nodeToAdd);
		for(ITreeModelChangedListener<T> listener : m_listeners) {
			listener.onNodeAdded(newParent, newIndex, nodeToAdd);
		}
	}
}
