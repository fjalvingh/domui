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
package to.etc.webapp.query;

import java.util.*;

import javax.annotation.*;

/**
 * A node representing the same operation spanning several
 * nodes (like x AND y AND z)
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class QMultiNode extends QOperatorNode {
	private List<QOperatorNode> m_children;

	/**
	 * Constructor to use when this node is in BUILD mode.
	 * @param operation
	 */
	QMultiNode(QOperation operation) {
		super(operation);
		m_children = new ArrayList<QOperatorNode>();
	}

	@Override
	public QMultiNode dup() {
		QMultiNode n = new QMultiNode(getOperation());
		for(QOperatorNode child : m_children) {
			n.add(child.dup());
		}
		return n;
	}

	public QMultiNode(QOperation operation, QOperatorNode[] ch) {
		super(operation);
		m_children = new ArrayList<QOperatorNode>(ch.length);

		//-- Check to see if we need to collapse..
		for(QOperatorNode qn : ch) {
			if(qn.getOperation() == operation) {
				//-- We need to collapse: we have a similar child.
				for(QOperatorNode sub : ((QMultiNode) qn).getChildren()) {
					m_children.add(sub);
				}
			} else
				m_children.add(qn);
		}
	}

	QMultiNode(QOperation operation, List<QOperatorNode> ch) {
		this(operation, ch.toArray(new QOperatorNode[ch.size()]));
	}

	public List<QOperatorNode> getChildren() {
		return m_children;
	}

	@Override
	public void visit(@Nonnull QNodeVisitor v) throws Exception {
		v.visitMulti(this);
	}

	/**
	 * In build mode, add a node.
	 * @param n
	 */
	void add(QOperatorNode n) {
		m_children.add(n);
	}

	/**
	 * Used to replace the last node when it was found that there were actual extra nodes.
	 * @param mn
	 */
	void replaceTop(QMultiNode mn) {
		m_children.set(m_children.size() - 1, mn);
	}
}
