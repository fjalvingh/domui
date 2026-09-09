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
 * <p>Every change to the model, wherever in it it is made, arrives here as a
 * {@link GraphOp} and is passed to whatever listens - which is how a panel showing this
 * model knows what to send to the browser without redrawing it.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class GraphModel {
	private final List<GraphCell> m_cellList = new ArrayList<>();

	private final Map<String, GraphCell> m_cellMap = new HashMap<>();

	private final List<IGraphModelListener> m_listenerList = new ArrayList<>(1);

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
		GraphNode node = new GraphNode(this, "n" + (++m_nodeIdCounter), label, parent, x, y, width, height);
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
		changed(GraphOp.add(cell));
	}

	/**
	 * Remove a cell, with everything that cannot exist without it: the children of a node,
	 * and the edges that end on it. Those go first, so a list of changes can be applied in
	 * the order it was made.
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
		changed(GraphOp.remove(cell));
	}

	public void clear() {
		for(GraphCell cell : new ArrayList<>(m_cellList)) {
			remove(cell);
		}
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
	 * Incremented by every change to the model. A change list carries the version it was
	 * made against, so the side receiving it can tell whether it is still about the model
	 * it has.
	 */
	public int getVersion() {
		return m_version;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Being told about changes							        */
	/*----------------------------------------------------------------------*/

	public void addChangeListener(IGraphModelListener listener) {
		if(!m_listenerList.contains(listener)) {
			m_listenerList.add(listener);
		}
	}

	public void removeChangeListener(IGraphModelListener listener) {
		m_listenerList.remove(listener);
	}

	/**
	 * Record a change without changing anything, so that what the model holds is sent to
	 * the browser again. This is how a refused change is put back: the browser made it, the
	 * model did not, and telling the browser what the cell really is undoes it there.
	 */
	public void resend(GraphOp op) {
		changed(op);
	}

	/** Called by the model's own classes when something in it changed. */
	void changed(GraphOp op) {
		m_version++;
		for(IGraphModelListener listener : new ArrayList<>(m_listenerList)) {
			listener.onGraphChanged(this, op);
		}
	}

	@Override
	public String toString() {
		return "GraphModel[" + m_cellList.size() + " cells, version " + m_version + "]";
	}
}
