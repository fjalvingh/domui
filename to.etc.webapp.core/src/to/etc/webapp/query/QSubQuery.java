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

import to.etc.webapp.annotations.*;
import to.etc.webapp.qsql.*;

/**
 * A subquery linked inside a master query, that can be joined to the master query.
 *  *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 5, 2013
 */
public class QSubQuery<T, P> extends QSelection<T> {
	@Nonnull
	final private QRestrictor<P> m_parent;

	public QSubQuery(@Nonnull QRestrictor<P> parent, @Nonnull Class<T> chclazz) {
		super(chclazz);
		m_parent = parent;
	}

	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitSubquery(this);
	}

	public <A> QJoiner<A, P, T> join(@Nonnull QRestrictor<A> parent) {
		//-- Make sure parent is in my hierarchy
		QSubQuery<?, ?> r = this;
		for(;;) {
			if(r.m_parent == parent)
				break;

			if(r.m_parent instanceof QSubQuery)
				r = (QSubQuery<?, ?>) r.m_parent;
			else
				throw new QQuerySyntaxException("Parent passed is not a parent of this subquery: "+parent);
		}

		//-- Return the joiner.
		return new QJoiner<A, P, T>(parent, this);
	}

	/**
	 * Joins the parent to this subquery on the specified property, provided that the property exists in both entities
	 * @param parentProperty
	 * @param property
	 */
	public QSubQuery<T, P> join(@Nonnull @GProperty String property) {
		add(new QPropertyJoinComparison(QOperation.EQ, property, property));
		return this;
	}

	@Nonnull
	public QRestrictor<P> getParent() {
		return m_parent;
	}
}
