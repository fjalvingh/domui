package to.etc.domuidemo.pages.overview.tree2;

import to.etc.domui.component.tree.ITreeModel;
import to.etc.domui.component.tree.ITreeModelChangedListener;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.dom.html.NodeBase;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-8-17.
 */
final public class Tree2DemoModel implements ITreeModel<DemoNode> {
	final private DemoNode m_root;

	public Tree2DemoModel(NodeBase nb) throws Exception {
		List<Artist> artists = nb.getSharedContext().query(QCriteria.create(Artist.class).ascending("name"));
		m_root = new DemoNode(artists);
	}

	@Override public int getChildCount(@Nullable DemoNode item) throws Exception {
		return Objects.requireNonNull(item).getChildren().size();
	}

	@Override public boolean hasChildren(@Nullable DemoNode item) throws Exception {
		return Objects.requireNonNull(item).hasChildren();
	}

	@Nonnull @Override public DemoNode getRoot() throws Exception {
		return m_root;
	}

	@Nonnull @Override public DemoNode getChild(@Nullable DemoNode parent, int index) throws Exception {
		return Objects.requireNonNull(parent).getChildren().get(index);
	}

	@Nullable @Override public DemoNode getParent(@Nullable DemoNode child) throws Exception {
		return Objects.requireNonNull(child).getParent();
	}

	@Override public void addChangeListener(@Nonnull ITreeModelChangedListener<DemoNode> l) {
	}

	@Override public void removeChangeListener(@Nonnull ITreeModelChangedListener<DemoNode> l) {
	}

	@Override public void expandChildren(@Nullable DemoNode item) throws Exception {
	}

	@Override public void collapseChildren(@Nullable DemoNode item) throws Exception {
	}
}
