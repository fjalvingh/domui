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

import org.eclipse.jdt.annotation.NonNull;

public class QPropertyComparison extends QOperatorNode {
	@NonNull
	final private String m_property;

	@NonNull
	final private QOperatorNode m_expr;

	public QPropertyComparison(@NonNull QOperation operation, @NonNull String property, @NonNull QOperatorNode expr) {
		super(operation);
		m_property = property;
		m_expr = expr;
	}

	@Override
	@NonNull
	public QPropertyComparison dup() {
		return new QPropertyComparison(getOperation(), getProperty(), getExpr().dup());
	}

	@Override
	public void visit(@NonNull QNodeVisitor v) throws Exception {
		v.visitPropertyComparison(this);
	}

	@NonNull
	public QOperatorNode getExpr() {
		return m_expr;
	}

	@NonNull
	public String getProperty() {
		return m_property;
	}
}
