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
import org.eclipse.jdt.annotation.Nullable;
import to.etc.webapp.annotations.GProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

abstract public class QRestrictor<T, R extends QRestrictor<T, R>> {
	/** The base class being queried in this selector. */
	@Nullable
	private final Class<T> m_baseClass;

	/** The return data type; baseclass for class-based queries and metaTable.getDataClass() for metatable queries. */
	@NonNull
	private final Class<T> m_returnClass;

	/** If this is a selector on some metathing this represents the metathing. */
	@Nullable
	private final ICriteriaTableDef<T> m_metaTable;

	/** Is either OR or AND, indicating how added items are to be combined. */
	@NonNull
	private QOperation m_combinator;

	private final Set<QSubQuery<?, ?>> m_unusedSubquerySet = new HashSet<>();

	@Nullable
	abstract public QOperatorNode getRestrictions();

	abstract public void setRestrictions(@Nullable QOperatorNode n);

	protected QRestrictor(@NonNull Class<T> baseClass, @NonNull QOperation combinator) {
		m_baseClass = baseClass;
		m_returnClass = baseClass;
		m_combinator = combinator;
		m_metaTable = null;
	}

	protected QRestrictor(@NonNull ICriteriaTableDef<T> meta, @NonNull QOperation combinator) {
		m_metaTable = meta;
		m_returnClass = meta.getDataClass();
		m_combinator = combinator;
		m_baseClass = null;
	}

	protected QRestrictor(@NonNull QRestrictor<T, ?> parent, @NonNull QOperation combinator) {
		m_metaTable = parent.getMetaTable();
		m_baseClass = parent.getBaseClass();
		m_returnClass = parent.getReturnClass();
		m_combinator = combinator;
	}

	/**
	 * Returns the persistent class being queried and returned, <b>if this is a class-based query</b>.
	 */
	@Nullable
	public Class<T> getBaseClass() {
		return m_baseClass;
	}

	/**
	 * Returns the metatable being queried, or null.
	 */
	@Nullable
	public ICriteriaTableDef<T> getMetaTable() {
		return m_metaTable;
	}

	/**
	 * Return the datatype returned by a principal query using this criteria.
	 */
	@NonNull
	public Class<T> getReturnClass() {
		return m_returnClass;
	}

	/**
	 * Returns T if this has restrictions.
	 */
	public final boolean hasRestrictions() {
		return getRestrictions() != null;
	}

	/**
	 * Add a new restriction to the list of restrictions on the data. This will do "and" collapsing: when the node added is an "and"
	 * it's nodes will be added directly to the list (because that already represents an and combinatory).
	 */
	protected void internalAdd(@NonNull QOperatorNode r) {
		QOperatorNode restrictions = getRestrictions();
		if(restrictions == null) {
			setRestrictions(r); 						// Just set the single operation,
		} else if(restrictions.getOperation() == m_combinator) {
			//-- Already the proper combinator - add the node to it.
			((QMultiNode) restrictions).add(r);
		} else {
			//-- We need to replace the current restriction with a higher combinator node and add the items there.
			QMultiNode comb = new QMultiNode(m_combinator);
			comb.add(restrictions);
			comb.add(r);
			setRestrictions(comb);
		}
	}

	/**
	 * Return a thingy that constructs nodes combined with "or".
	 */
	@NonNull
	public QRestrictorImpl<T> or() {
		//if(m_combinator == QOperation.OR) // If I myself am combining with OR return myself
		//	return (R) this;
		QMultiNode or = new QMultiNode(QOperation.OR);
		add(or);
		return new QRestrictorImpl<T>(this, or);
	}

	@NonNull
	public QRestrictorImpl<T> and() {
		//if(m_combinator == QOperation.AND) // If I myself am combining with AND return myself
		//	return (R) this;
		QMultiNode and = new QMultiNode(QOperation.AND);
		add(and);
		return new QRestrictorImpl<T>(this, and);
	}

	/**
	 * Add NOT restriction.
	 */
	@NonNull
	public QRestrictor<T, QRestrictorImpl<T>> not() {
		QMultiNode and = new QMultiNode(QOperation.AND);
		QUnaryNode not = new QUnaryNode(QOperation.NOT, and);
		add(not);
		return new QRestrictorImpl<>(this, and);
	}

	/**
	 * This merges the "other" restrictor's restrictions inside this restriction. Both
	 * restrictions are merged by using an "and" between both complete sets. Only "this"
	 * restriction is altered; the original is kept as-is (the nodes are copied).
	 */
	public void mergeCriteria(@NonNull R other) {
		QOperatorNode othertree = other.getRestrictions();
		if(null == othertree)
			return;

		//-- Duplicate the other restrictions set, then "and" it with this entire set.
		othertree = othertree.dup();
		QOperatorNode thistree = getRestrictions();
		if(null == thistree) {
			setRestrictions(othertree);
			return;
		}

		QMultiNode and = new QMultiNode(QOperation.AND, new QOperatorNode[]{thistree, othertree});
		setRestrictions(and);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Adding selection restrictions (where clause)		*/
	/*--------------------------------------------------------------*/
	@NonNull
	public R add(@NonNull QOperatorNode n) {
		internalAdd(n);
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R eq(@NonNull @GProperty String property, @Nullable Object value) {
		return add(QRestriction.eq(property, value));
	}

	/**
	 * Compare a property with some value.
	 */
	@NonNull
	public <V> R eq(@NonNull QField<T, V> property, @NonNull V value) {
		return eq(property.getName(), value);
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R eq(@NonNull @GProperty String property, long value) {
		add(QRestriction.eq(property, value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R eq(@NonNull @GProperty String property, double value) {
		add(QRestriction.eq(property, value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R ne(@NonNull @GProperty String property, @Nullable Object value) {
		add(QRestriction.ne(property, value));
		return (R) this;
	}

	/**
	 * Compare a property with some value.
	 */
	@NonNull
	public <V> R ne(@NonNull QField<T, V> property, @NonNull V value) {
		return ne(property.getName(), value);
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R ne(@NonNull @GProperty String property, long value) {
		add(QRestriction.ne(property, value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R ne(@NonNull @GProperty String property, double value) {
		add(QRestriction.ne(property, value));
		return (R) this;
	}

	/**
	 * A property must be one of a list of values.
	 */
	@NonNull
	public <V> R in(@NonNull @GProperty String property, List<V> inlist) {
		add(QRestriction.in(property, inlist));
		return (R) this;
	}

	@NonNull
	public <V> R in(@NonNull QField<T, V> property, @NonNull List<V> inlist) {
		add(QRestriction.in(property.getName(), inlist));
		return (R) this;
	}

	@NonNull
	public <V> R in(@NonNull @GProperty String property, QSelection<?> selection) {
		add(QRestriction.in(property, selection));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R gt(@NonNull @GProperty String property, @NonNull Object value) {
		add(QRestriction.gt(property, value));
		return (R) this;
	}

	@NonNull
	public <V> R gt(@NonNull QField<T, V> property, @NonNull V value) {
		add(QRestriction.gt(property.getName(), value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R gt(@NonNull @GProperty String property, long value) {
		add(QRestriction.gt(property, value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R gt(@NonNull @GProperty String property, double value) {
		add(QRestriction.gt(property, value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R lt(@NonNull @GProperty String property, @NonNull Object value) {
		add(QRestriction.lt(property, value));
		return (R) this;
	}

	@NonNull
	public <V> R lt(@NonNull QField<T, V> property, @NonNull V value) {
		add(QRestriction.lt(property.getName(), value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R lt(@NonNull @GProperty String property, long value) {
		add(QRestriction.lt(property, value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R lt(@NonNull @GProperty String property, double value) {
		add(QRestriction.lt(property, value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R ge(@NonNull @GProperty String property, @NonNull Object value) {
		add(QRestriction.ge(property, value));
		return (R) this;
	}

	@NonNull
	public <V> R ge(@NonNull QField<T, V> property, @NonNull V value) {
		add(QRestriction.ge(property.getName(), value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R ge(@NonNull @GProperty String property, long value) {
		add(QRestriction.ge(property, value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R ge(@NonNull @GProperty String property, double value) {
		add(QRestriction.ge(property, value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R le(@NonNull @GProperty String property, @NonNull Object value) {
		add(QRestriction.le(property, value));
		return (R) this;
	}

	@NonNull
	public <V> R le(@NonNull QField<T, V> property, @NonNull V value) {
		add(QRestriction.le(property.getName(), value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R le(@NonNull @GProperty String property, long value) {
		add(QRestriction.le(property, value));
		return (R) this;
	}

	/**
	 * Compare a property with some literal object value.
	 */
	@NonNull
	public R le(@NonNull @GProperty String property, double value) {
		add(QRestriction.le(property, value));
		return (R) this;
	}

	/**
	 * Do a 'like' comparison. The wildcard marks here are always %; a literal % is to
	 * be presented as \%. The comparison is case-dependent.
	 */
	@NonNull
	public R like(@NonNull @GProperty String property, @NonNull String value) {
		add(QRestriction.like(property, value));
		return (R) this;
	}

	@NonNull
	public <V> R like(@NonNull QField<T, V> property, @NonNull String value) {
		add(QRestriction.like(property.getName(), value));
		return (R) this;
	}

	/**
	 * Compare the value of a property with two literal bounds.
	 */
	@NonNull
	public R between(@NonNull @GProperty String property, @NonNull Object a, @NonNull Object b) {
		add(QRestriction.between(property, a, b));
		return (R) this;
	}

	@NonNull
	public <V> R between(@NonNull QField<T, V> property, @NonNull V a, @NonNull V b) {
		add(QRestriction.between(property.getName(), a, b));
		return (R) this;
	}

	/**
	 * Do a case-independent 'like' comparison. The wildcard marks here are always %; a literal % is to
	 * be presented as \%. The comparison is case-independent.
	 */
	@NonNull
	public R ilike(@NonNull @GProperty String property, @NonNull String value) {
		add(QRestriction.ilike(property, value));
		return (R) this;
	}

	@NonNull
	public <V> R ilike(@NonNull QField<T, V> property, @NonNull String value) {
		add(QRestriction.ilike(property.getName(), value));
		return (R) this;
	}

	/**
	 * Add the restriction that the property specified must be null.
	 */
	@NonNull
	public R isnull(@NonNull @GProperty String property) {
		add(QRestriction.isnull(property));
		return (R) this;
	}

	@NonNull
	public <V> R isnull(@NonNull QField<T, V> property) {
		add(QRestriction.isnull(property.getName()));
		return (R) this;
	}

	/**
	 * Add the restriction that the property specified must be not-null.
	 */
	@NonNull
	public R isnotnull(@NonNull @GProperty String property) {
		add(QRestriction.isnotnull(property));
		return (R) this;
	}

	@NonNull
	public <V> R isnotnull(@NonNull QField<T, V> property) {
		add(QRestriction.isnotnull(property.getName()));
		return (R) this;
	}

	/**
	 * Add a restriction specified in bare SQL. (R) this is implementation-dependent.
	 */
	@NonNull
	public R sqlCondition(@NonNull String sql) {
		add(QRestriction.sqlCondition(sql));
		return (R) this;
	}

	/**
	 * Add a restriction in bare SQL, with JDBC parameters inside the string (specified as '?'). (R) this
	 * is implementation-dependent. The first ? in the string corresponds to params[0]. Parameters are
	 * not allowed to be null (i.e. the type is @NonNull Object[@NonNull] or something).
	 * Alternatively parameters can be given as ":nnn" where nnn is the 1-based index in the params array.
	 */
	@NonNull
	public R sqlCondition(@NonNull String sql, @NonNull Object[] params) {
		add(QRestriction.sqlCondition(sql, params));
		return (R) this;
	}

	/**
	 * Create a joined "exists" subquery on some child list property. The parameters passed have a relation with eachother;
	 * (R) this relation cannot be checked at compile time because Java still lacks property references (Sun is still too utterly
	 * stupid to define them). They will be checked at runtime when the query is executed.
	 *
	 * @param <U>			The type of the children.
	 * @param childclass	The class type of the children, because Java Generics is too bloody stupid to find out itself.
	 * @param childproperty	The name of the property <i>in</i> the parent class <T> that represents the List<U> of child records.
	 */
	@NonNull
	public <U> ExistsRestrictor<U> exists(@NonNull Class<U> childclass, @NonNull @GProperty("U") String childproperty) {
		final QExistsSubquery<U> sq = new QExistsSubquery<U>(this, childclass, childproperty);
		ExistsRestrictor<U> builder = new ExistsRestrictor<U>(childclass, QOperation.AND, sq);
		add(sq);
		return builder;
	}

	@NonNull
	public <P, U> ExistsRestrictor<U> exists(@NonNull Class<U> childclass, @NonNull QField<T, P> childproperty) {
		final QExistsSubquery<U> sq = new QExistsSubquery<>(this, childclass, childproperty.getName());
		ExistsRestrictor<U> builder = new ExistsRestrictor<U>(childclass, QOperation.AND, sq);
		add(sq);
		return builder;
	}

	static public final class ExistsRestrictor<U> extends QRestrictor<U, ExistsRestrictor<U>> {
		final private QExistsSubquery<U> m_sq;

		public ExistsRestrictor(@NonNull Class<U> parent, @NonNull QOperation combinator, QExistsSubquery<U> sq) {
			super(parent, combinator);
			m_sq = sq;
		}

		@Override
		public QOperatorNode getRestrictions() {
			return m_sq.getRestrictions();
		}

		@Override
		public void setRestrictions(@Nullable QOperatorNode n) {
			m_sq.setRestrictions(n);
		}
	}

	//@NonNull
	//public <P extends QField<P, T>, R extends QField<R, U>, U> QRestrictor<U> exists(@NonNull QList<P, R> listProperty) throws Exception {
	//	return (QRestrictor<U>) exists(listProperty.getRootClass(), listProperty.m_listName);
	//}
	//
	//@NonNull
	//public <P extends QField<P, T>, R extends QField<R, U>, U> QRestrictor<U> exists(@NonNull QList<P, R> listProperty) throws Exception {
	//	return (QRestrictor<U>) exists(listProperty.getRootClass(), listProperty.m_listName);
	//}

	public <U> QSubQuery<U, T> subquery(@NonNull Class<U> childClass) throws Exception {
		QSubQuery<U, T> subQuery = new QSubQuery<>(this, childClass);
		m_unusedSubquerySet.add(subQuery);
		return subQuery;
	}

	/**
	 * Internal method, used to be able to find QSubQueries that were allocated (using {@link #subquery(Class)} but not
	 * properly linked back into the main query. That is, if you create a correlated subquery like (select max(date) from xxx where xxx.id=parent.id)
	 * and you do not <i>use</i> that subquery inside the parent (select * from yyy where yyy.date = (subquery)) then
	 * the subquery is "lost". (R) this leads to unexpected results as the user assumes the subquery does something.
	 * Registering all subqueries created here allows any querying visitor to implement a check: it should
	 * remove all subqueries that it actually encounters and handles, and after that it checks to ensure that (R) this
	 * set is empty. If not there are subqueries left that were not joined back.
	 */
	public Set<QSubQuery<?, ?>> getUnusedSubquerySet() {
		return m_unusedSubquerySet;
	}

	/**
	 * See {@link #getUnusedSubquerySet()}
	 */
	public void internalUseQuery(QSubQuery<?, ?> q) {
		m_unusedSubquerySet.remove(q);
	}
}
