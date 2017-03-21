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
 * Describes a join between a query and one of it's subqueries. The join is defined in {@link QSubQuery}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 5, 2013
 */
public class QPropertyJoinComparison extends QOperatorNode {
	@Nonnull
	final private String m_parentProperty;

	@Nonnull
	final private String m_subProperty;

	public QPropertyJoinComparison(@Nonnull QOperation operation, @Nonnull String parentProperty, @Nonnull String subProperty) {
		super(operation);
		m_parentProperty = parentProperty;
		m_subProperty = subProperty;
	}

	@Override
	public QPropertyJoinComparison dup() {
		return new QPropertyJoinComparison(getOperation(), getParentProperty(), getSubProperty());
	}

	@Nonnull
	public String getParentProperty() {
		return m_parentProperty;
	}

	@Nonnull
	public String getSubProperty() {
		return m_subProperty;
	}

	@Override
	public void visit(@Nonnull QNodeVisitor v) throws Exception {
		v.visitPropertyJoinComparison(this);
	}
}
