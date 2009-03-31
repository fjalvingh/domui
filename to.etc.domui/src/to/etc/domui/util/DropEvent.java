package to.etc.domui.util;

import to.etc.domui.dom.html.*;

public final class DropEvent {
	private int				m_index;
	private NodeBase		m_draggedNode;
	private NodeContainer	m_dropTargetNode;

	public DropEvent(NodeContainer dropTargetNode, NodeBase draggedNode, int index) {
		m_dropTargetNode = dropTargetNode;
		m_draggedNode = draggedNode;
		m_index = index;
	}

	public NodeBase			getDraggedNode() {
		return m_draggedNode;
	}
	public NodeContainer	getDropTargetNode() {
		return m_dropTargetNode;
	}
	public int				getIndex() {
		return m_index;
	}
}
