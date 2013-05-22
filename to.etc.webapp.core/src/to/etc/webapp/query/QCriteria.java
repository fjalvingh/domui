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
 * Represents the selection of a list of persistent entity classes from the database. A QCriteria
 * has a fixed type (the type of the class being selected) and maintains the list of conditions (criteria's)
 * that the selection must hold.
 * This is a concrete representation of something representing a query tree. To use a QCriteria in an actual
 * query you need a translator which translates the QCriteria tree into something for the target persistence
 * layer. Implementations of such a translator for Hibernate and SPF exist.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class QCriteria<T> extends QCriteriaQueryBase<T> {
	private QCriteria(final Class<T> b) {
		super(b);
	}

	private QCriteria(final ICriteriaTableDef<T> td) {
		super(td);
	}


	//	/** 20100122 jal needs full deep copy
	//	 * Copy constructor.
	//	 * @param q
	//	 */
	//	protected QCriteria(final QCriteria<T> q) {
	//		super(q);
	//	}

	/**
	 * Create a QCriteria to select a set of the specified class. When used on it's own without
	 * added criteria this selects all possible items.
	 * @param <U>
	 * @param clz
	 * @return
	 */
	static public <U> QCriteria<U> create(final Class<U> clz) {
		return new QCriteria<U>(clz);
	}

	/**
	 * EXPERIMENTAL Create a QCriteria on some metadata structured data.
	 * @param <U>
	 * @param root
	 * @return
	 */
	static public <U> QCriteria<U> create(final ICriteriaTableDef<U> root) {
		return new QCriteria<U>(root);
	}

	//	/**
	//	 * Create a duplicate of this Criteria.
	//	 * @return
	//	 */
	//	public QCriteria<T> dup() {
	//		return new QCriteria<T>(this);
	//	}

	/**
	 * Visit everything in this QCriteria.
	 * @param v
	 * @throws Exception
	 */
	public void visit(final QNodeVisitor v) throws Exception {
		v.visitCriteria(this);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#add(to.etc.webapp.query.QOperatorNode)
	 */
	@Override
	public QCriteria<T> add(final QOperatorNode r) {
		return (QCriteria<T>) super.add(r);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#add(to.etc.webapp.query.QOrder)
	 */
	@Override
	public QCriteria<T> add(final QOrder r) {
		return (QCriteria<T>) super.add(r);
	}
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ascending(java.lang.String)
	 */
	@Override
	public QCriteria<T> ascending(@GProperty final String property) {
		return (QCriteria<T>) super.ascending(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#between(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public QCriteria<T> between(@GProperty final String property, final Object a, final Object b) {
		return (QCriteria<T>) super.between(property, a, b);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#descending(java.lang.String)
	 */
	@Override
	public QCriteria<T> descending(@GProperty final String property) {
		return (QCriteria<T>) super.descending(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#eq(java.lang.String, double)
	 */
	@Override
	public QCriteria<T> eq(@GProperty final String property, final double value) {
		return (QCriteria<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#eq(java.lang.String, long)
	 */
	@Override
	public QCriteria<T> eq(@GProperty final String property, final long value) {
		return (QCriteria<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#eq(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> eq(@GProperty final String property, final Object value) {
		return (QCriteria<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ge(java.lang.String, double)
	 */
	@Override
	public QCriteria<T> ge(@GProperty final String property, final double value) {
		return (QCriteria<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ge(java.lang.String, long)
	 */
	@Override
	public QCriteria<T> ge(@GProperty final String property, final long value) {
		return (QCriteria<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ge(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> ge(@GProperty final String property, final Object value) {
		return (QCriteria<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#gt(java.lang.String, double)
	 */
	@Override
	public QCriteria<T> gt(@GProperty final String property, final double value) {
		return (QCriteria<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#gt(java.lang.String, long)
	 */
	@Override
	public QCriteria<T> gt(@GProperty final String property, final long value) {
		return (QCriteria<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#gt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> gt(@GProperty final String property, final Object value) {
		return (QCriteria<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ilike(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> ilike(@GProperty final String property, final Object value) {
		return (QCriteria<T>) super.ilike(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#isnotnull(java.lang.String)
	 */
	@Override
	public QCriteria<T> isnotnull(@GProperty final String property) {
		return (QCriteria<T>) super.isnotnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#isnull(java.lang.String)
	 */
	@Override
	public QCriteria<T> isnull(@GProperty final String property) {
		return (QCriteria<T>) super.isnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#le(java.lang.String, double)
	 */
	@Override
	public QCriteria<T> le(@GProperty final String property, final double value) {
		return (QCriteria<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#le(java.lang.String, long)
	 */
	@Override
	public QCriteria<T> le(@GProperty final String property, final long value) {
		return (QCriteria<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#le(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> le(@GProperty final String property, final Object value) {
		return (QCriteria<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#like(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> like(@GProperty final String property, final Object value) {
		return (QCriteria<T>) super.like(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#lt(java.lang.String, double)
	 */
	@Override
	public QCriteria<T> lt(@GProperty final String property, final double value) {
		return (QCriteria<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#lt(java.lang.String, long)
	 */
	@Override
	public QCriteria<T> lt(@GProperty final String property, final long value) {
		return (QCriteria<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#lt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> lt(@GProperty final String property, final Object value) {
		return (QCriteria<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ne(java.lang.String, double)
	 */
	@Override
	public QCriteria<T> ne(@GProperty final String property, final double value) {
		return (QCriteria<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ne(java.lang.String, long)
	 */
	@Override
	public QCriteria<T> ne(@GProperty final String property, final long value) {
		return (QCriteria<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ne(java.lang.String, java.lang.Object)
	 */
	@Override
	public QCriteria<T> ne(@GProperty final String property, final Object value) {
		return (QCriteria<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#or(to.etc.webapp.query.QOperatorNode[])
	 */
	@Override
	@Deprecated
	public QCriteria<T> or(final QOperatorNode a1, final QOperatorNode a2, final QOperatorNode... a) {
		return (QCriteria<T>) super.or(a1, a2, a);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#sqlCondition(java.lang.String)
	 */
	@Override
	public QCriteria<T> sqlCondition(final String sql) {
		return (QCriteria<T>) super.sqlCondition(sql);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#sqlCondition(java.lang.String)
	 */
	@Nonnull
	@Override
	public QCriteria<T> sqlCondition(@Nonnull final String sql, @Nonnull Object[] params) {
		return (QCriteria<T>) super.sqlCondition(sql, params);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#limit(int)
	 */
	@Override
	public QCriteria<T> limit(final int limit) {
		return (QCriteria<T>) super.limit(limit);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#start(int)
	 */
	@Override
	public QCriteria<T> start(final int start) {
		return (QCriteria<T>) super.start(start);
	}

	@Override
	public String toString() {
		QQueryRenderer	r	= new QQueryRenderer();
		try {
			visit(r);
		} catch(Exception x) {
			x.printStackTrace();
			return "Invalid query: "+x;
		}
		return r.toString();
	}
}
