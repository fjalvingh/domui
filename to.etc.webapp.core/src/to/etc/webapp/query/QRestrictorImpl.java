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

class QRestrictorImpl<T> extends QRestrictor<T> {
	/** The OR node we're constructing thingerydoos for. */
	private QOperatorNode m_orNode;

	QRestrictorImpl(QRestrictor<T> parent, QMultiNode ornode) {
		super(parent.getBaseClass(), ornode.getOperation());
		m_orNode = ornode;
	}

	//	QRestrictorImpl(Class<T> clz, QOperatorNode ornode) {
	//		super(clz, ornode.getOperation());
	//		m_orNode = ornode;
	//	}

	@Override
	public QOperatorNode getRestrictions() {
		return m_orNode;
	}

	@Override
	public void setRestrictions(QOperatorNode n) {
		m_orNode = n;
	}
}
