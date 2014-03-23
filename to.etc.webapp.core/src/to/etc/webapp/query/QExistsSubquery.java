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
 * Represents an "exists" subquery on some child relation of a record. This
 * is always defined as a subquery on a parent record's child-record-set, and
 * it adds requirements on the existence of children having certain restrictions.
 * This should be rendered as a joined-subquery, like:
 * <pre>
 *  [[select xxx from parent_table p where ...]] - rendered above this
 *  exists (select 1 from child_table a where a.pk = p.fk and [[conditions in this node]]).
 * </pre>
 *
 * @param <T>	The type of the child record persistent class, so the T from the List<T> getChildList() in this subquery's parent.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2009
 */
public class QExistsSubquery<T> extends QOperatorNode {
	private QRestrictor< ? > m_parentQuery;

	private String m_parentProperty;

	private Class<T> m_baseClass;

	private QOperatorNode m_restrictions;

	public QExistsSubquery(QRestrictor< ? > parent, Class<T> baseClass, String property) {
		super(QOperation.EXISTS_SUBQUERY);
		m_parentQuery = parent;
		m_parentProperty = property;
		m_baseClass = baseClass;
	}

	@Override
	public QExistsSubquery<T> dup() {
		QExistsSubquery<T> q = new QExistsSubquery<T>(m_parentQuery, m_baseClass, getParentProperty());
		QOperatorNode r = getRestrictions();
		if(null != r)
			q.setRestrictions(r.dup());
		return q;
	}

	public QRestrictor< ? > getParentQuery() {
		return m_parentQuery;
	}

	public Class< ? > getBaseClass() {
		return m_baseClass;
	}
	public String getParentProperty() {
		return m_parentProperty;
	}

	public QOperatorNode getRestrictions() {
		return m_restrictions;
	}

	public void setRestrictions(QOperatorNode restrictions) {
		m_restrictions = restrictions;
	}

	@Override
	public void visit(@Nonnull QNodeVisitor v) throws Exception {
		v.visitExistsSubquery(this);
	}
}
