package to.etc.domui.component.tree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-1-18.
 */
public class TreeNodeModel<T extends TreeNode> implements ITreeModel<T> {
	final private T m_root;

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
}
