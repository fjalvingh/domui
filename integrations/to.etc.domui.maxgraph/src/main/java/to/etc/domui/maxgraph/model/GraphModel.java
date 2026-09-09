package to.etc.domui.maxgraph.model;

import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The drawing: the cells it is made of, in the order they were added.
 *
 * <p>This is what application code programs against, and the only thing the browser side
 * is ever told about. It knows nothing of maxGraph and nothing of DomUI, so a drawing can
 * be built and tested without either.</p>
 *
 * <p>Cell ids are allocated here and are stable for the life of the cell: they are how the
 * server and the browser name the same cell. Application data belongs on
 * {@link GraphCell#setUserObject(Object)}, which never leaves the server.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class GraphModel {
	private final List<GraphCell> m_cellList = new ArrayList<>();

	private final Map<String, GraphCell> m_cellMap = new HashMap<>();

	private int m_nodeIdCounter;

	private int m_edgeIdCounter;

	private int m_version;

	/*----------------------------------------------------------------------*/
	/*	CODING:	Building the drawing								        */
	/*----------------------------------------------------------------------*/

	/**
	 * Add a node to the drawing itself.
	 */
	public GraphNode addNode(@Nullable String label, double x, double y, double width, double height) {
		return addNode(null, label, x, y, width, height);
	}

	/**
	 * Add a node inside another node, whose position is relative to that parent.
	 */
	public GraphNode addNode(@Nullable GraphNode parent, @Nullable String label, double x, double y, double width, double height) {
		if(null != parent && parent.getModel() != this) {
			throw new IllegalArgumentException("The parent " + parent + " belongs to another model");
		}
		GraphNode node = new GraphNode(this, "n" + (++m_nodeIdCounter), label, parent, new GraphGeometry(x, y, width, height));
		register(node);
		return node;
	}

	public GraphEdge addEdge(@Nullable GraphNode source, @Nullable GraphNode target) {
		return addEdge(source, target, null);
	}

	public GraphEdge addEdge(@Nullable GraphNode source, @Nullable GraphNode target, @Nullable String label) {
		checkOwned(source);
		checkOwned(target);
		GraphEdge edge = new GraphEdge(this, "e" + (++m_edgeIdCounter), label, source, target);
		register(edge);
		return edge;
	}

	private void checkOwned(@Nullable GraphNode node) {
		if(null != node && node.getModel() != this) {
			throw new IllegalArgumentException("The node " + node + " belongs to another model");
		}
	}

	private void register(GraphCell cell) {
		m_cellList.add(cell);
		m_cellMap.put(cell.getId(), cell);
		changed();
	}

	/**
	 * Remove a cell, with everything that cannot exist without it: the children of a node,
	 * and the edges that end on it.
	 */
	public void remove(GraphCell cell) {
		if(m_cellMap.get(cell.getId()) != cell) {
			return;
		}
		if(cell instanceof GraphNode node) {
			for(GraphNode child : new ArrayList<>(node.getChildren())) {
				remove(child);
			}
			for(GraphCell other : new ArrayList<>(m_cellList)) {
				if(other instanceof GraphEdge edge && (edge.getSource() == node || edge.getTarget() == node)) {
					remove(edge);
				}
			}
			GraphNode parent = node.getParent();
			if(null != parent) {
				parent.internalRemoveChild(node);
			}
		}
		m_cellList.remove(cell);
		m_cellMap.remove(cell.getId());
		changed();
	}

	public void clear() {
		m_cellList.clear();
		m_cellMap.clear();
		changed();
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Reading it											        */
	/*----------------------------------------------------------------------*/

	/**
	 * The cells in the order they were added, which is also the order they must be created
	 * in: a parent before its children, and an edge after both of its ends.
	 */
	public List<GraphCell> getCells() {
		return Collections.unmodifiableList(m_cellList);
	}

	@Nullable
	public GraphCell getCell(String id) {
		return m_cellMap.get(id);
	}

	public boolean isEmpty() {
		return m_cellList.isEmpty();
	}

	/**
	 * Incremented by every change to the model. From phase 3 on this is what tells a
	 * change list coming from the browser whether it was made against what the model
	 * still holds.
	 */
	public int getVersion() {
		return m_version;
	}

	/** Called by the model's own classes when something in it changed. */
	void changed() {
		m_version++;
	}

	@Override
	public String toString() {
		return "GraphModel[" + m_cellList.size() + " cells, version " + m_version + "]";
	}
}
