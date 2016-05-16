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

public class QOrder extends QOperatorNode {
	@Nonnull
	final private String m_property;

	@Nonnull
	final private QSortOrderDirection m_direction;

	public QOrder(@Nonnull QSortOrderDirection direction, @Nonnull String property) {
		super(QOperation.ORDER);
		m_direction = direction;
		m_property = property;
	}

	@Override
	public QOrder dup() {
		return new QOrder(getDirection(), getProperty());
	}

	@Nonnull
	public String getProperty() {
		return m_property;
	}

	@Nonnull
	public QSortOrderDirection getDirection() {
		return m_direction;
	}

	@Nonnull
	static public final QOrder ascending(String name) {
		return new QOrder(QSortOrderDirection.ASC, name);
	}

	@Nonnull
	static public final QOrder descending(String name) {
		return new QOrder(QSortOrderDirection.DESC, name);
	}

	@Nonnull
	static public final QOrder order(String name, QSortOrderDirection dir) {
		return new QOrder(dir, name);
	}

	@Override
	public void visit(@Nonnull QNodeVisitor v) throws Exception {
		v.visitOrder(this);
	}

	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;
		QOrder qOrder = (QOrder) o;
		return Objects.equals(m_property, qOrder.m_property) &&
			Objects.equals(m_direction, qOrder.m_direction);
	}

	@Override
	public int hashCode() {
		return Objects.hash(m_property, m_direction);
	}
}
