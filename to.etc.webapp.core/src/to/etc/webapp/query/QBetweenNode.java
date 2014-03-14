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

/**
 * Represents a "between" operation where the base item is a property reference.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
public class QBetweenNode extends QOperatorNode {
	private QOperatorNode m_a;

	private QOperatorNode m_b;

	private String m_prop;

	public QBetweenNode(QOperation operation, String prop, QOperatorNode a, QOperatorNode b) {
		super(operation);
		m_a = a;
		m_b = b;
		m_prop = prop;
	}

	@Override
	public QBetweenNode dup() {
		return new QBetweenNode(getOperation(), getProp(), getA().dup(), getB().dup());
	}

	/**
	 * The low value of the between operation (prop between A and B)
	 * @return
	 */
	public QOperatorNode getA() {
		return m_a;
	}

	/**
	 * The high value of the between operation (prop between A and B)
	 * @return
	 */
	public QOperatorNode getB() {
		return m_b;
	}

	/**
	 * The name of the property.
	 * @return
	 */
	public String getProp() {
		return m_prop;
	}

	@Override
	public void visit(@Nonnull QNodeVisitor v) throws Exception {
		v.visitBetween(this);
	}
}
