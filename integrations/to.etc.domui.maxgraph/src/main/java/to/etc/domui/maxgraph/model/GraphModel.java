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
 * <p>The model is also where undo lives, once {@link #setUndoEnabled(boolean)} says so:
 * every change records what puts it back, and {@link #undo()} makes those changes - so an
 * undo travels to a drawing on the screen as the same kind of change list as everything
 * else. It is this side that undoes, because it is this side that still has the object of
 * a cell the user deleted.</p>
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

	private final List<GraphEdit> m_undoList = new ArrayList<>();

	private final List<GraphEdit> m_redoList = new ArrayList<>();

	private boolean m_undoEnabled;

	private int m_undoLimit = 50;

	/** The step being recorded, while there is one; changes outside one are a step by themselves. */
	@Nullable
	private GraphEdit m_edit;

	private int m_editDepth;

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
		record(() -> remove(cell));
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
		//-- Taking a cell away takes more than one cell away, and that is one thing to undo.
		beginEdit();
		try {
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
			record(() -> reinsert(cell));
			changed(GraphOp.remove(cell));
		} finally {
			endEdit();
		}
	}

	/**
	 * Put a removed cell back, the same object under the same id - which is the one thing
	 * only this side can do: the browser threw its cell away and has nothing left to make
	 * one from, while here the cell is still whole, {@link GraphCell#getUserObject()} and
	 * all.
	 *
	 * <p>Only an undo gets here, and only in the reverse of the order things were removed
	 * in, so whatever a cell needs is already back by the time the cell itself is.</p>
	 */
	private void reinsert(GraphCell cell) {
		if(m_cellMap.containsKey(cell.getId())) {
			return;
		}
		m_cellList.add(cell);
		m_cellMap.put(cell.getId(), cell);
		if(cell instanceof GraphNode node) {
			GraphNode parent = node.getParent();
			if(null != parent) {
				parent.internalAddChild(node);
			}
		}
		record(() -> remove(cell));
		changed(GraphOp.add(cell));
	}

	public void clear() {
		edit(() -> {
			for(GraphCell cell : new ArrayList<>(m_cellList)) {
				remove(cell);
			}
		});
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
	/*	CODING:	Undo and redo										        */
	/*----------------------------------------------------------------------*/

	/**
	 * Whether the model remembers what it was changed by, so that {@link #undo()} can put
	 * it back. Off by default: a drawing that is only being built has nothing to undo, and
	 * remembering it would cost the whole of it.
	 *
	 * <p>Turn it on once the drawing is there - after the constructor built it, usually -
	 * and everything from then on is undoable.</p>
	 */
	public boolean isUndoEnabled() {
		return m_undoEnabled;
	}

	public GraphModel setUndoEnabled(boolean undoEnabled) {
		if(m_undoEnabled == undoEnabled) {
			return this;
		}
		m_undoEnabled = undoEnabled;
		if(!undoEnabled) {
			clearHistory();
		}
		return this;
	}

	/**
	 * How many steps are kept, the oldest being dropped past that. There is a limit because
	 * a step that removed cells holds on to them - and to the application's own data on
	 * them - for as long as it can still be undone.
	 */
	public int getUndoLimit() {
		return m_undoLimit;
	}

	public GraphModel setUndoLimit(int undoLimit) {
		m_undoLimit = undoLimit < 1 ? 1 : undoLimit;
		trim();
		return this;
	}

	/**
	 * Make everything this does one step, so that one undo takes all of it back. Changes
	 * made outside such a boundary are each a step of their own.
	 *
	 * <p>Nesting is allowed and joins the outermost one: a boundary inside a boundary is
	 * still one step.</p>
	 */
	public void edit(Runnable what) {
		beginEdit();
		try {
			what.run();
		} finally {
			endEdit();
		}
	}

	/** {@link #edit(Runnable)} for code that cannot be a lambda, like a request handler. */
	public void beginEdit() {
		if(m_editDepth++ == 0 && m_undoEnabled) {
			m_edit = new GraphEdit();
		}
	}

	public void endEdit() {
		if(m_editDepth > 0 && --m_editDepth > 0) {
			return;
		}
		m_editDepth = 0;
		GraphEdit edit = m_edit;
		m_edit = null;
		if(null != edit && !edit.isEmpty()) {
			m_undoList.add(edit);
			m_redoList.clear();
			trim();
		}
	}

	public boolean canUndo() {
		return !m_undoList.isEmpty();
	}

	public boolean canRedo() {
		return !m_redoList.isEmpty();
	}

	/**
	 * Take the last step back, and return whether there was one. What this changes is a
	 * change to the model like any other: listeners are told, the version moves on, and a
	 * panel showing the model sends it to the browser at the end of the request.
	 */
	public boolean undo() {
		return replay(m_undoList, m_redoList);
	}

	/** Make the last undone step again, and return whether there was one. */
	public boolean redo() {
		return replay(m_redoList, m_undoList);
	}

	/**
	 * Undoing and redoing are the same thing in opposite directions: run the last step of
	 * one stack, recording what that changes as the step that takes it back, and put that
	 * on the other stack.
	 */
	private boolean replay(List<GraphEdit> from, List<GraphEdit> to) {
		if(from.isEmpty()) {
			return false;
		}
		GraphEdit edit = from.remove(from.size() - 1);
		GraphEdit inverse = new GraphEdit();

		//-- Whatever boundary the caller was inside stays untouched: this step is its own.
		GraphEdit outer = m_edit;
		int depth = m_editDepth;
		m_edit = inverse;
		m_editDepth = 1;
		try {
			edit.undo();
		} finally {
			m_edit = outer;
			m_editDepth = depth;
		}
		if(!inverse.isEmpty()) {
			to.add(inverse);
		}
		return true;
	}

	/** Forget what the model was changed by. What it holds now is where it starts again. */
	public void clearHistory() {
		m_undoList.clear();
		m_redoList.clear();
	}

	private void trim() {
		while(m_undoList.size() > m_undoLimit) {
			m_undoList.remove(0);
		}
		while(m_redoList.size() > m_undoLimit) {
			m_redoList.remove(0);
		}
	}

	/**
	 * Called by the model's own classes before they change anything, with the change that
	 * puts it back. It has to be the state as it is at that moment: once the change is
	 * made, what it was is gone.
	 */
	void record(Runnable inverse) {
		if(!m_undoEnabled) {
			return;
		}
		GraphEdit edit = m_edit;
		if(null != edit) {
			edit.add(inverse);
			return;
		}
		//-- A change outside a boundary is a step of its own.
		edit = new GraphEdit();
		edit.add(inverse);
		m_undoList.add(edit);
		m_redoList.clear();
		trim();
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
