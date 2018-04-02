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

import to.etc.webapp.annotations.GProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents the selection of a list of persistent entity classes from the database. A QCriteria
 * has a fixed type (the type of the class being selected) and maintains the list of conditions (criteria's)
 * that the selection must hold.
 * This is a concrete representation of something representing a query tree. To use a QCriteria in an actual
 * query you need a translator which translates the QCriteria tree into something for the target persistence
 * layer. Implementations of such a translator for Hibernate, SPF, simple SQL/JDBC and in-memory lists exist.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class QCriteria<T> extends QCriteriaQueryBase<T> {
	protected QCriteria(@Nonnull final Class<T> b) {
		super(b);
	}

	private QCriteria(@Nonnull final ICriteriaTableDef<T> td) {
		super(td);
	}

	/**
	 * Create a QCriteria to select a set of the specified class. When used on it's own without
	 * added criteria this selects all possible items.
	 */
	@Nonnull
	static public <U> QCriteria<U> create(@Nonnull final Class<U> clz) {
		return new QCriteria<>(clz);
	}

	/**
	 * Create a QCriteria on some metadata structured data.
	 */
	@Nonnull
	static public <U> QCriteria<U> create(@Nonnull final ICriteriaTableDef<U> root) {
		return new QCriteria<>(root);
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
	 */
	public void visit(@Nonnull final QNodeVisitor v) throws Exception {
		v.visitCriteria(this);
	}

	@Nonnull
	@Override
	public QCriteria<T> add(@Nonnull final QOperatorNode r) {
		return (QCriteria<T>) super.add(r);
	}

	@Override
	@Nonnull
	public QCriteria<T> add(@Nonnull final QOrder r) {
		return (QCriteria<T>) super.add(r);
	}

	@Override
	@Nonnull
	public QCriteria<T> ascending(@Nonnull @GProperty final String property) {
		return (QCriteria<T>) super.ascending(property);
	}

	@Override
	@Nonnull
	public QCriteria<T> between(@Nonnull @GProperty final String property, @Nonnull final Object a, @Nonnull final Object b) {
		return (QCriteria<T>) super.between(property, a, b);
	}

	@Override
	@Nonnull
	public QCriteria<T> descending(@Nonnull @GProperty final String property) {
		return (QCriteria<T>) super.descending(property);
	}

	@Nonnull
	@Override
	public QCriteria<T> eq(@Nonnull @GProperty final String property, final double value) {
		return (QCriteria<T>) super.eq(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> eq(@Nonnull @GProperty final String property, final long value) {
		return (QCriteria<T>) super.eq(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> eq(@Nonnull @GProperty final String property, @Nullable final Object value) {
		return (QCriteria<T>) super.eq(property, value);
	}

	@Override
	@Nonnull
	public <V, R extends QField<R, T>> QCriteria<T> eq(@Nonnull final QField<R, V> property, @Nonnull final V value) {
		return (QCriteria<T>) super.eq(property, value);
	}

	@Override
	@Nonnull
	public <V, R extends QField<R, T>> QCriteria<T> ne(@Nonnull final QField<R, V> property, @Nonnull final V value) {
		return (QCriteria<T>) super.ne(property, value);
	}

	@Override
	@Nonnull
	public <R extends QField<R, T>> QCriteria<T> eq(@Nonnull final QFieldDouble<R> property, final double value) {
		return (QCriteria<T>) super.eq(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> ge(@Nonnull @GProperty final String property, final double value) {
		return (QCriteria<T>) super.ge(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> ge(@Nonnull @GProperty final String property, final long value) {
		return (QCriteria<T>) super.ge(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> ge(@Nonnull @GProperty final String property, @Nonnull final Object value) {
		return (QCriteria<T>) super.ge(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> gt(@Nonnull @GProperty final String property, final double value) {
		return (QCriteria<T>) super.gt(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> gt(@Nonnull @GProperty final String property, final long value) {
		return (QCriteria<T>) super.gt(property, value);
	}

	@Override
	public @Nonnull QCriteria<T> gt(@Nonnull @GProperty final String property, @Nonnull final Object value) {
		return (QCriteria<T>) super.gt(property, value);
	}

	@Override
	public @Nonnull QCriteria<T> ilike(@Nonnull @GProperty final String property, @Nonnull final Object value) {
		return (QCriteria<T>) super.ilike(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> isnotnull(@Nonnull @GProperty final String property) {
		return (QCriteria<T>) super.isnotnull(property);
	}

	@Override
	@Nonnull
	public QCriteria<T> isnull(@Nonnull @GProperty final String property) {
		return (QCriteria<T>) super.isnull(property);
	}

	@Override
	@Nonnull
	public QCriteria<T> le(@Nonnull @GProperty final String property, final double value) {
		return (QCriteria<T>) super.le(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> le(@Nonnull @GProperty final String property, final long value) {
		return (QCriteria<T>) super.le(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> le(@Nonnull @GProperty final String property, @Nonnull final Object value) {
		return (QCriteria<T>) super.le(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> like(@Nonnull @GProperty final String property, @Nonnull final Object value) {
		return (QCriteria<T>) super.like(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> lt(@Nonnull @GProperty final String property, final double value) {
		return (QCriteria<T>) super.lt(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> lt(@Nonnull @GProperty final String property, final long value) {
		return (QCriteria<T>) super.lt(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> lt(@Nonnull @GProperty final String property, @Nonnull final Object value) {
		return (QCriteria<T>) super.lt(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> ne(@Nonnull @GProperty final String property, final double value) {
		return (QCriteria<T>) super.ne(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> ne(@Nonnull @GProperty final String property, final long value) {
		return (QCriteria<T>) super.ne(property, value);
	}

	@Override
	@Nonnull
	public QCriteria<T> ne(@Nonnull @GProperty final String property, @Nullable final Object value) {
		return (QCriteria<T>) super.ne(property, value);
	}

	@Override
	@Deprecated
	@Nonnull
	public QCriteria<T> or(@Nonnull final QOperatorNode a1, @Nonnull final QOperatorNode a2, @Nonnull final QOperatorNode... a) {
		return (QCriteria<T>) super.or(a1, a2, a);
	}

	@Override
	@Nonnull
	public QCriteria<T> sqlCondition(@Nonnull final String sql) {
		return (QCriteria<T>) super.sqlCondition(sql);
	}

	@Nonnull
	@Override
	public QCriteria<T> sqlCondition(@Nonnull final String sql, @Nonnull Object[] params) {
		return (QCriteria<T>) super.sqlCondition(sql, params);
	}

	@Override
	@Nonnull
	public QCriteria<T> limit(final int limit) {
		return (QCriteria<T>) super.limit(limit);
	}

	@Override
	@Nonnull
	public QCriteria<T> start(final int start) {
		return (QCriteria<T>) super.start(start);
	}

	@Override
	@Nonnull
	public QCriteria<T> fetch(@Nonnull @GProperty String property, @Nonnull QFetchStrategy strategy) {
		super.fetch(property, strategy);
		return this;
	}

	@Nonnull
	public QCriteria<T> fetch(@Nonnull @GProperty String property) {
		super.fetch(property, QFetchStrategy.EAGER);
		return this;
	}

	@Nonnull
	@Override
	public <V> QCriteria<T> in(@Nonnull @GProperty String property, List<V> inlist) {
		super.in(property, inlist);
		return this;
	}

	@Nonnull
	@Override
	public <V> QCriteria<T> in(@Nonnull @GProperty String property, QSelection<?> selection) {
		super.in(property, selection);
		return this;
	}

	@Nonnull
	@Override
	public <V, R extends QField<R, T>> QCriteria<T> in(@Nonnull QField<R, V> property, @Nonnull List<V> value) {
		super.in(property, value);
		return this;
	}

	@Override
	@Nonnull
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

	/**
	 * Set a test ID on the query, so that JUnit tests can easily provide a substituted answer.
	 */
	public QCriteria<T> testId(@Nonnull String testId){
		setTestId(testId);
		return this;
	}
}
