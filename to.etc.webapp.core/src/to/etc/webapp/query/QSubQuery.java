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

	/**
	 * Joins the parent to this subquery on the specified property, provided that the property exists in both entities
	 * @param parentProperty
	 * @param property
	 */
	public QSubQuery<T, P> join(@Nonnull @GProperty String property) {
		add(new QPropertyJoinComparison(QOperation.EQ, property, property));
		return this;
	}

	/**
	 * Adds an eq restriction on the parent to this subquery on the specified properties,
	 * provided that the propertnames are not equal but their type is
	 * Use join if property names are the same.
	 * @param parentProperty
	 * @param property
	 */
	public QSubQuery<T, P> eq(@Nonnull @GProperty("T") String parentProperty, @Nonnull @GProperty("U") String property) {
		add(new QPropertyJoinComparison(QOperation.EQ, parentProperty, property));
		return this;
	}

	/**
	 * Adds an ne restriction on the parent to this subquery on the specified properties,
	 * @param parentProperty
	 * @param property
	 */
	public QSubQuery<T, P> ne(@Nonnull @GProperty("T") String parentProperty, @Nonnull @GProperty("U") String property) {
		add(new QPropertyJoinComparison(QOperation.NE, parentProperty, property));
		return this;
	}

	/**
	 * Adds an lt restriction on the parent to this subquery on the specified properties,
	 * @param parentProperty
	 * @param property
	 */
	public QSubQuery<T, P> lt(@Nonnull @GProperty("T") String parentProperty, @Nonnull @GProperty("U") String property) {
		add(new QPropertyJoinComparison(QOperation.LT, parentProperty, property));
		return this;
	}

	/**
	 * Adds an le restriction on the parent to this subquery on the specified properties,
	 * @param parentProperty
	 * @param property
	 */
	public QSubQuery<T, P> le(@Nonnull @GProperty("T") String parentProperty, @Nonnull @GProperty("U") String property) {
		add(new QPropertyJoinComparison(QOperation.LE, parentProperty, property));
		return this;
	}

	/**
	 * Adds an lt restriction on the parent to this subquery on the specified properties,
	 * @param parentProperty
	 * @param property
	 */
	public QSubQuery<T, P> gt(@Nonnull @GProperty("T") String parentProperty, @Nonnull @GProperty("U") String property) {
		add(new QPropertyJoinComparison(QOperation.GT, parentProperty, property));
		return this;
	}

	/**
	 * Adds an ge restriction on the parent to this subquery on the specified properties,
	 * @param parentProperty
	 * @param property
	 */
	public QSubQuery<T, P> ge(@Nonnull @GProperty("T") String parentProperty, @Nonnull @GProperty("U") String property) {
		add(new QPropertyJoinComparison(QOperation.GE, parentProperty, property));
		return this;
	}
}
