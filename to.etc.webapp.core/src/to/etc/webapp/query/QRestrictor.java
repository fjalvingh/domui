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
 * Builds the "where" part of a query, or a part of that "where" part, under construction. The nodes added,
 * when &gt; 1, are combined using either OR or AND.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2009
 */
abstract public class QRestrictor<T> {
	/** The base class being queried in this selector. */
	private final Class<T> m_baseClass;

	/** The return data type; baseclass for class-based queries and metaTable.getDataClass() for metatable queries. */
	private final Class<T> m_returnClass;

	/** If this is a selector on some metathing this represents the metathing. */
	@Nullable
	private final ICriteriaTableDef<T> m_metaTable;

	/** Is either OR or AND, indicating how added items are to be combined. */
	private QOperation m_combinator;

	abstract public QOperatorNode getRestrictions();

	abstract public void setRestrictions(QOperatorNode n);

	protected QRestrictor(Class<T> baseClass, QOperation combinator) {
		m_baseClass = baseClass;
		m_returnClass = baseClass;
		m_combinator = combinator;
		m_metaTable = null;
	}

	protected QRestrictor(ICriteriaTableDef<T> meta, QOperation combinator) {
		m_metaTable = meta;
		m_returnClass = meta.getDataClass();
		m_combinator = combinator;
		m_baseClass = null;
	}

	/**
	 * Returns the persistent class being queried and returned, <b>if this is a class-based query</b>.
	 * @return
	 */
	@Nullable
	public Class<T> getBaseClass() {
		return m_baseClass;
	}

	/**
	 * Returns the metatable being queried, or null.
	 * @return
	 */
	@Nullable
	public ICriteriaTableDef<T> getMetaTable() {
		return m_metaTable;
	}

	/**
	 * Return the datatype returned by a principal query using this criteria.
	 * @return
	 */
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
	 * Add a new restriction to the list of restrictions on the data. This will do "and" collapsion: when the node added is an "and"
	 * it's nodes will be added directly to the list (because that already represents an and combinatory).
	 * @param r
	 * @return
	 */
	protected void internalAdd(QOperatorNode r) {
		if(getRestrictions() == null) {
			setRestrictions(r); // Just set the single operation,
		} else if(getRestrictions().getOperation() == m_combinator) {
			//-- Already the proper combinator - add the node to it.
			((QMultiNode) getRestrictions()).add(r);
		} else {
			//-- We need to replace the current restriction with a higher combinatory node and add the items there.
			QMultiNode comb = new QMultiNode(m_combinator);
			comb.add(getRestrictions());
			comb.add(r);
			setRestrictions(comb);
		}
	}

	/**
	 * Return a thingy that constructs nodes combined with "or".
	 * @return
	 */
	public QRestrictor<T> or() {
		if(m_combinator == QOperation.OR) // If I myself am combining with OR return myself
			return this;
		QMultiNode or = new QMultiNode(QOperation.OR);
		add(or);
		return new QRestrictorImpl<T>(this, or);
	}

	public QRestrictor<T> and() {
		if(m_combinator == QOperation.AND) // If I myself am combining with OR return myself
			return this;
		QMultiNode and = new QMultiNode(QOperation.AND);
		add(and);
		return new QRestrictorImpl<T>(this, and);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Adding selection restrictions (where clause)		*/
	/*--------------------------------------------------------------*/
	public QRestrictor<T> add(QOperatorNode n) {
		internalAdd(n);
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> eq(@GProperty final String property, Object value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> eq(@GProperty final String property, long value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> eq(@GProperty final String property, double value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ne(@GProperty final String property, Object value) {
		add(QRestriction.ne(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ne(@GProperty final String property, long value) {
		add(QRestriction.ne(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ne(@GProperty final String property, double value) {
		add(QRestriction.ne(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> gt(@GProperty final String property, Object value) {
		add(QRestriction.gt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> gt(@GProperty final String property, long value) {
		add(QRestriction.gt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> gt(@GProperty final String property, double value) {
		add(QRestriction.gt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> lt(@GProperty final String property, Object value) {
		add(QRestriction.lt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> lt(@GProperty final String property, long value) {
		add(QRestriction.lt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> lt(@GProperty final String property, double value) {
		add(QRestriction.lt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ge(@GProperty final String property, Object value) {
		add(QRestriction.ge(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ge(@GProperty final String property, long value) {
		add(QRestriction.ge(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ge(@GProperty final String property, double value) {
		add(QRestriction.ge(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> le(@GProperty final String property, Object value) {
		add(QRestriction.le(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> le(@GProperty final String property, long value) {
		add(QRestriction.le(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> le(@GProperty final String property, double value) {
		add(QRestriction.le(property, value));
		return this;
	}

	/**
	 * Do a 'like' comparison. The wildcard marks here are always %; a literal % is to
	 * be presented as \%. The comparison is case-dependent.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> like(@GProperty final String property, Object value) {
		add(QRestriction.like(property, value));
		return this;
	}

	/**
	 * Compare the value of a property with two literal bounds.
	 * @param property
	 * @param a
	 * @param b
	 * @return
	 */
	public QRestrictor<T> between(@GProperty final String property, Object a, Object b) {
		add(QRestriction.between(property, a, b));
		return this;
	}

	/**
	 * Do a case-independent 'like' comparison. The wildcard marks here are always %; a literal % is to
	 * be presented as \%. The comparison is case-independent.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ilike(@GProperty final String property, Object value) {
		add(QRestriction.ilike(property, value));
		return this;
	}

	/**
	 * Add a set of OR nodes to the set.
	 * @param a
	 * @return
	 */
	@Deprecated
	public QRestrictor<T> or(QOperatorNode a1, QOperatorNode a2, QOperatorNode... rest) {
		QOperatorNode[] ar = new QOperatorNode[rest.length + 2];
		ar[0] = a1;
		ar[1] = a2;
		System.arraycopy(rest, 0, ar, 2, rest.length);
		add(QRestriction.or(ar));
		return this;
	}

	/**
	 * Add the restriction that the property specified must be null.
	 * @param property
	 * @return
	 */
	public QRestrictor<T> isnull(@GProperty final String property) {
		add(QRestriction.isnull(property));
		return this;
	}

	/**
	 * Add the restriction that the property specified must be not-null.
	 *
	 * @param property
	 * @return
	 */
	public QRestrictor<T> isnotnull(@GProperty final String property) {
		add(QRestriction.isnotnull(property));
		return this;
	}

	/**
	 * Add a restriction specified in bare SQL. This is implementation-dependent.
	 * @param sql
	 * @return
	 */
	public QRestrictor<T> sqlCondition(String sql) {
		add(QRestriction.sqlCondition(sql));
		return this;
	}

	/**
	 * Create a joined "exists" subquery on some child list property. The parameters passed have a relation with eachother;
	 * this relation cannot be checked at compile time because Java still lacks property references (Sun is still too utterly
	 * stupid to define them). They will be checked at runtime when the query is executed.
	 *
	 * @param <U>			The type of the children.
	 * @param childclass	The class type of the children, because Java Generics is too bloody stupid to find out itself.
	 * @param childproperty	The name of the property <i>in</i> the parent class <T> that represents the List<U> of child records.
	 * @return
	 */
	public <U> QRestrictor<U> exists(Class<U> childclass, @GProperty(parameter = 1) String childproperty) {
		final QExistsSubquery<U> sq = new QExistsSubquery<U>(this, childclass, childproperty);
		QRestrictor<U> builder = new QRestrictor<U>(childclass, QOperation.AND) {
			@Override
			public QOperatorNode getRestrictions() {
				return sq.getRestrictions();
			}

			@Override
			public void setRestrictions(QOperatorNode n) {
				sq.setRestrictions(n);
			}
		};
		add(sq);
		return builder;
	}
}
