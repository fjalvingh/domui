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

import javax.annotation.*;

abstract public class QOperatorNode extends QNodeBase {
	@Nonnull
	final private QOperation m_operation;

	public QOperatorNode(@Nonnull QOperation operation) {
		m_operation = operation;
	}

	@Nonnull
	public QOperation getOperation() {
		return m_operation;
	}

	abstract public QOperatorNode dup();

	/**
	 * This removes all and/or constructs that have no real children.
	 * @param node
	 */
	static public void prune(@Nullable QOperatorNode node) {
		if(null == node)
			return;
		if(node.getOperation() != QOperation.AND && node.getOperation() != QOperation.OR)
			return;

		//-- Prune all children of me
		QMultiNode mn = (QMultiNode) node;
		for(QOperatorNode child : mn.getChildren()) {
			prune(child);
		}

		//-- Now remove all children that have no children
		for(int i = mn.getChildren().size(); --i >= 0;) {
			QOperatorNode child = mn.getChildren().get(i);
			if(child instanceof QMultiNode) {
				QMultiNode chn = (QMultiNode) child;
				if(chn.getChildren().size() == 0) {
					mn.getChildren().remove(i);
				}
			}
		}
	}
}
