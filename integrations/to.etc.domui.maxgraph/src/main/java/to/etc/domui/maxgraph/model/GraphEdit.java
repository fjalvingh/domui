package to.etc.domui.maxgraph.model;

import java.util.ArrayList;
import java.util.List;

/**
 * One undoable step: everything a {@link GraphModel} was changed by inside one boundary,
 * and what puts it back.
 *
 * <p>What is recorded is not a description of a change but the change that reverses it,
 * made where the original was made - the only place that still knows what the cell held
 * before. Undoing runs those in reverse order, which is what gets a deleted node back in
 * one piece: {@link GraphModel#remove(GraphCell)} takes a node's children and the edges
 * that end on it away before the node itself, so reversing the list puts the node back
 * before the things that cannot exist without it.</p>
 *
 * <p>Each of them changes the model exactly like any other change does, so an undo reaches
 * the browser as the ordinary list of operations. There is nothing about undo on the wire,
 * and nothing about it in the browser at all.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphEdit {
	private final List<Runnable> m_undoList = new ArrayList<>();

	GraphEdit() {
	}

	void add(Runnable inverse) {
		m_undoList.add(inverse);
	}

	boolean isEmpty() {
		return m_undoList.isEmpty();
	}

	/** How many changes to the model this step is made of. */
	public int size() {
		return m_undoList.size();
	}

	/** Put the model back the way it was, the newest change first. */
	void undo() {
		for(int i = m_undoList.size(); --i >= 0;) {
			m_undoList.get(i).run();
		}
	}

	@Override
	public String toString() {
		return "GraphEdit[" + m_undoList.size() + " changes]";
	}
}
