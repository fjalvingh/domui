package to.etc.domui.maxgraph;

import to.etc.domui.maxgraph.model.GraphChange;
import to.etc.domui.maxgraph.model.GraphModel;

import java.util.Collections;
import java.util.List;

/**
 * What the browser sent: what the user did to a drawing, and the model version they did it
 * to. Changes are about cells that exist; requests are for cells that do not exist yet,
 * because only the server hands out ids.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphChangeSet {
	private final int m_base;

	private final List<GraphChange> m_changes;

	private final List<GraphRequest> m_requests;

	GraphChangeSet(int base, List<GraphChange> changes, List<GraphRequest> requests) {
		m_base = base;
		m_changes = changes;
		m_requests = requests;
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

	public List<GraphRequest> getRequests() {
		return Collections.unmodifiableList(m_requests);
	}
}
