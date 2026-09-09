package to.etc.domui.maxgraph;

import to.etc.domui.maxgraph.model.GraphChange;
import to.etc.domui.maxgraph.model.GraphModel;

import java.util.Collections;
import java.util.List;

/**
 * What the browser sent: the changes a user made to a drawing, and the model version they
 * were made against.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphChangeSet {
	private final int m_base;

	private final List<GraphChange> m_changes;

	GraphChangeSet(int base, List<GraphChange> changes) {
		m_base = base;
		m_changes = changes;
	}

	/**
	 * The {@link GraphModel#getVersion()} the browser's drawing was at. A change list made
	 * against another version than the model is at now is about a drawing that no longer
	 * exists, and is thrown away.
	 */
	public int getBase() {
		return m_base;
	}

	public List<GraphChange> getChanges() {
		return Collections.unmodifiableList(m_changes);
	}
}
