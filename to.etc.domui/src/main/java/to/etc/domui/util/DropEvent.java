/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.util;

import to.etc.domui.dom.html.*;

public final class DropEvent {
	private int m_index;

	private int m_colIndex;

	private NodeBase m_draggedNode;

	private NodeContainer m_dropTargetNode;

	private String m_insertBeforeSiblingID;

	private DropMode m_mode;

	public int getColIndex() {
		return m_colIndex;
	}

	public String getInsertBeforeSiblingID() {
		return m_insertBeforeSiblingID;
	}

	public DropMode getMode() {
		return m_mode;
	}

	public DropEvent(NodeContainer dropTargetNode, NodeBase draggedNode, int index) {
		m_dropTargetNode = dropTargetNode;
		m_draggedNode = draggedNode;
		m_index = index;
		m_mode = DropMode.ROW;
	}

	public DropEvent(NodeContainer dropTargetNode, NodeBase draggedNode, int index, int colIndex) {
		this(dropTargetNode, draggedNode, index);
		m_colIndex = colIndex;
		m_mode = DropMode.ROW;
	}

	public DropEvent(NodeContainer dropTargetNode, NodeBase draggedNode, String insertBeforeSiblingID) {
		m_dropTargetNode = dropTargetNode;
		m_draggedNode = draggedNode;
		m_insertBeforeSiblingID = insertBeforeSiblingID;
		m_mode = DropMode.DIV;
	}

	public NodeBase getDraggedNode() {
		return m_draggedNode;
	}

	public NodeContainer getDropTargetNode() {
		return m_dropTargetNode;
	}

	public int getIndex() {
		return m_index;
	}
}
