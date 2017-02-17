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
 * Represents a <i>selection</i> of data elements from a database. This differs from
 * a QCriteria in that it collects not one persistent class instance per row but multiple
 * items per row, and each item can either be a persistent class or some property or
 * calculated value (max, min, count et al).
 *
 * <p>Even though this type has a generic type parameter representing the base object
 * being queried, the list() method for this object will return a List<Object[]> always.</p>
 *
 * <p>QSelection queries return an array of items for each row, and each element
 * of the array is typed depending on it's source. In addition, QSelection queries
 * expose the ability to handle grouping. QSelection criteria behave as and should
 * be seen as SQL queries in an OO wrapping.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
public class QSelection<T> extends QCriteriaQueryBase<T> {
	protected QSelection(@Nonnull Class<T> clz) {
		super(clz);
	}

	/**
	 * Create a selection query based on the specified persistent class (public API).
	 * @param <T>	The base type being queried
	 * @param root	The class representing the base type being queried, thanks to the brilliant Java Generics implementation.
	 * @return
	 */
	static public <T> QSelection<T>	create(Class<T> root) {
		return new QSelection<T>(root);
	}

	public void	visit(QNodeVisitor v) throws Exception {
		v.visitSelection(this);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Object selectors.									*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addColumn(@Nonnull QSelectionItem item, @Nullable String alias) {
		super.addColumn(item, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPropertySelection(@Nonnull QSelectionFunction f, @Nonnull String prop, @Nullable String alias) {
		super.addPropertySelection(f, prop, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> avg(@GProperty final @Nonnull String property, @Nullable String alias) {
		return (QSelection<T>) super.avg(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> avg(@GProperty final @Nonnull String property) {
		return (QSelection<T>) super.avg(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> count(@GProperty final @Nonnull String property, @Nullable String alias) {
		return (QSelection<T>) super.count(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> count(@GProperty final @Nonnull String property) {
		return (QSelection<T>) super.count(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> countDistinct(@GProperty final @Nonnull String property, @Nullable String alias) {
		return (QSelection<T>) super.countDistinct(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> countDistinct(@GProperty final @Nonnull String property) {
		return (QSelection<T>) super.countDistinct(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> distinct(@GProperty final @Nonnull String property, @Nullable String alias) {
		return (QSelection<T>) super.distinct(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> distinct(@GProperty final @Nonnull String property) {
		return (QSelection<T>) super.distinct(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> max(@GProperty final @Nonnull String property, @Nullable String alias) {
		return (QSelection<T>) super.max(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> max(@GProperty final @Nonnull String property) {
		return (QSelection<T>) super.max(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> min(@GProperty final @Nonnull String property, @Nullable String alias) {
		return (QSelection<T>) super.min(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> min(@GProperty final @Nonnull String property) {
		return (QSelection<T>) super.min(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> selectProperty(@GProperty final @Nonnull String property, @Nullable String alias) {
		return (QSelection<T>) super.selectProperty(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> selectProperty(@GProperty final @Nonnull String property) {
		return (QSelection<T>) super.selectProperty(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> sum(@GProperty final @Nonnull String property, @Nullable String alias) {
		return (QSelection<T>) super.sum(property, alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull QSelection<T> sum(@GProperty final @Nonnull String property) {
		return (QSelection<T>) super.sum(property);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Overrides to force return type needed for chaining	*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#add(to.etc.webapp.query.QOperatorNode)
	 */
	@Override
	public @Nonnull QSelection<T> add(final @Nonnull QOperatorNode r) {
		return (QSelection<T>) super.add(r);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#add(to.etc.webapp.query.QOrder)
	 */
	@Override
	public @Nonnull QSelection<T> add(final @Nonnull QOrder r) {
		return (QSelection<T>) super.add(r);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ascending(java.lang.String)
	 */
	@Override
	public @Nonnull QSelection<T> ascending(@GProperty final @Nonnull String property) {
		return (QSelection<T>) super.ascending(property);
	}
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#between(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public @Nonnull QSelection<T> between(@GProperty final @Nonnull String property, final @Nonnull Object a, final @Nonnull Object b) {
		return (QSelection<T>) super.between(property, a, b);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#descending(java.lang.String)
	 */
	@Override
	public @Nonnull QSelection<T> descending(@GProperty final @Nonnull String property) {
		return (QSelection<T>) super.descending(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#eq(java.lang.String, double)
	 */
	@Override
	public @Nonnull QSelection<T> eq(@GProperty final @Nonnull String property, final double value) {
		return (QSelection<T>) super.eq(property, value);
	}
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#eq(java.lang.String, long)
	 */
	@Override
	public @Nonnull QSelection<T> eq(@GProperty final @Nonnull String property, final long value) {
		return (QSelection<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#eq(java.lang.String, java.lang.Object)
	 */
	@Override
	public @Nonnull
	QSelection<T> eq(@GProperty final @Nonnull String property, final @Nullable Object value) {
		return (QSelection<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ge(java.lang.String, double)
	 */
	@Override
	public @Nonnull QSelection<T> ge(@GProperty final @Nonnull String property, final double value) {
		return (QSelection<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ge(java.lang.String, long)
	 */
	@Override
	public @Nonnull QSelection<T> ge(@GProperty final @Nonnull String property, final long value) {
		return (QSelection<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ge(java.lang.String, java.lang.Object)
	 */
	@Override
	public @Nonnull QSelection<T> ge(@GProperty final @Nonnull String property, final @Nonnull Object value) {
		return (QSelection<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#gt(java.lang.String, double)
	 */
	@Override
	public @Nonnull QSelection<T> gt(@GProperty final @Nonnull String property, final double value) {
		return (QSelection<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#gt(java.lang.String, long)
	 */
	@Override
	public @Nonnull QSelection<T> gt(@GProperty final @Nonnull String property, final long value) {
		return (QSelection<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#gt(java.lang.String, java.lang.Object)
	 */
	@Override
	public @Nonnull QSelection<T> gt(@GProperty final @Nonnull String property, final @Nonnull Object value) {
		return (QSelection<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ilike(java.lang.String, java.lang.Object)
	 */
	@Override
	public @Nonnull QSelection<T> ilike(@GProperty final @Nonnull String property, final @Nonnull Object value) {
		return (QSelection<T>) super.ilike(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#isnotnull(java.lang.String)
	 */
	@Override
	public @Nonnull QSelection<T> isnotnull(@GProperty final @Nonnull String property) {
		return (QSelection<T>) super.isnotnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#isnull(java.lang.String)
	 */
	@Override
	public @Nonnull QSelection<T> isnull(@GProperty final @Nonnull String property) {
		return (QSelection<T>) super.isnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#le(java.lang.String, double)
	 */
	@Override
	public @Nonnull QSelection<T> le(@GProperty final @Nonnull String property, final double value) {
		return (QSelection<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#le(java.lang.String, long)
	 */
	@Override
	public @Nonnull QSelection<T> le(@GProperty final @Nonnull String property, final long value) {
		return (QSelection<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#le(java.lang.String, java.lang.Object)
	 */
	@Override
	public @Nonnull QSelection<T> le(@GProperty final @Nonnull String property, final @Nonnull Object value) {
		return (QSelection<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#like(java.lang.String, java.lang.Object)
	 */
	@Override
	public @Nonnull QSelection<T> like(@GProperty final @Nonnull String property, final @Nonnull Object value) {
		return (QSelection<T>) super.like(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#lt(java.lang.String, double)
	 */
	@Override
	public @Nonnull QSelection<T> lt(@GProperty final @Nonnull String property, final double value) {
		return (QSelection<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#lt(java.lang.String, long)
	 */
	@Override
	public @Nonnull QSelection<T> lt(@GProperty final @Nonnull String property, final long value) {
		return (QSelection<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#lt(java.lang.String, java.lang.Object)
	 */
	@Override
	public @Nonnull QSelection<T> lt(@GProperty final @Nonnull String property, final @Nonnull Object value) {
		return (QSelection<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ne(java.lang.String, double)
	 */
	@Override
	public @Nonnull QSelection<T> ne(@GProperty final @Nonnull String property, final double value) {
		return (QSelection<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ne(java.lang.String, long)
	 */
	@Override
	public @Nonnull QSelection<T> ne(@GProperty final @Nonnull String property, final long value) {
		return (QSelection<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#ne(java.lang.String, java.lang.Object)
	 */
	@Override
	public @Nonnull
	QSelection<T> ne(@GProperty final @Nonnull String property, final @Nullable Object value) {
		return (QSelection<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#or(to.etc.webapp.query.QOperatorNode[])
	 */
	@Override
	@Deprecated
	@Nonnull
	public QSelection<T> or(@Nonnull final QOperatorNode a1, @Nonnull final QOperatorNode a2, @Nonnull final QOperatorNode... a) {
		return (QSelection<T>) super.or(a1, a2, a);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QCriteriaQueryBase#sqlCondition(java.lang.String)
	 */
	@Override
	public @Nonnull QSelection<T> sqlCondition(final @Nonnull String sql) {
		return (QSelection<T>) super.sqlCondition(sql);
	}

	@Override
	@Nonnull
	public QSelection<T> fetch(@Nonnull @GProperty String property, @Nonnull QFetchStrategy strategy) {
		super.fetch(property, strategy);
		return this;
	}

	@Nonnull
	public QSelection<T> fetch(@Nonnull @GProperty String property) {
		super.fetch(property, QFetchStrategy.EAGER);
		return this;
	}

	@Nonnull
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

	public QSelection<T> testId(@Nullable String testId) {
		setTestId(testId);
		return this;
	}
}
