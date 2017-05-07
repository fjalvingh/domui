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

/**
 * Factory for creating syntax tree nodes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class QRestriction {
	private QRestriction() {}

	@Nonnull
	static public final QMultiNode and(QOperatorNode... list) {
		return new QMultiNode(QOperation.AND, list);
	}

	@Nonnull
	static public final QMultiNode or(QOperatorNode... list) {
		return new QMultiNode(QOperation.OR, list);
	}

	/**
	 * Equals a property to a value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	@Nonnull
	static public final QPropertyComparison eq(String property, Object value) {
		return new QPropertyComparison(QOperation.EQ, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison eq(String property, long value) {
		return new QPropertyComparison(QOperation.EQ, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison eq(String property, double value) {
		return new QPropertyComparison(QOperation.EQ, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison ne(String property, Object value) {
		return new QPropertyComparison(QOperation.NE, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison ne(String property, long value) {
		return new QPropertyComparison(QOperation.NE, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison ne(String property, double value) {
		return new QPropertyComparison(QOperation.NE, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison gt(String property, Object value) {
		return new QPropertyComparison(QOperation.GT, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison gt(String property, long value) {
		return new QPropertyComparison(QOperation.GT, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison gt(String property, double value) {
		return new QPropertyComparison(QOperation.GT, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison lt(String property, Object value) {
		return new QPropertyComparison(QOperation.LT, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison lt(String property, long value) {
		return new QPropertyComparison(QOperation.LT, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison lt(String property, double value) {
		return new QPropertyComparison(QOperation.LT, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison ge(String property, Object value) {
		return new QPropertyComparison(QOperation.GE, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison ge(String property, long value) {
		return new QPropertyComparison(QOperation.GE, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison ge(String property, double value) {
		return new QPropertyComparison(QOperation.GE, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison le(String property, Object value) {
		return new QPropertyComparison(QOperation.LE, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison le(String property, long value) {
		return new QPropertyComparison(QOperation.LE, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison le(String property, double value) {
		return new QPropertyComparison(QOperation.LE, property, createValueNode(value));
	}

	@Nonnull
	static public final QPropertyComparison like(String property, Object value) {
		return new QPropertyComparison(QOperation.LIKE, property, createValueNode(value));
	}

	@Nonnull
	static public final QBetweenNode between(String property, Object a, Object b) {
		return new QBetweenNode(QOperation.BETWEEN, property, createValueNode(a), createValueNode(b));
	}

	@Nonnull
	static public final QBetweenNode between(String property, long a, long b) {
		return new QBetweenNode(QOperation.BETWEEN, property, createValueNode(a), createValueNode(b));
	}

	@Nonnull
	static public final QBetweenNode between(String property, double a, double b) {
		return new QBetweenNode(QOperation.BETWEEN, property, createValueNode(a), createValueNode(b));
	}

	@Nonnull
	static public final QPropertyComparison ilike(String property, Object value) {
		return new QPropertyComparison(QOperation.ILIKE, property, createValueNode(value));
	}

	@Nonnull
	static public final QUnaryProperty isnull(String property) {
		return new QUnaryProperty(QOperation.ISNULL, property);
	}

	@Nonnull
	static public final QUnaryProperty isnotnull(String property) {
		return new QUnaryProperty(QOperation.ISNOTNULL, property);
	}

	@Nonnull
	static public final <V> QPropertyIn in(String property, List<V> list) {
		return new QPropertyIn(QOperation.IN, property, createValueNode(list));
	}
	@Nonnull
	static public final <V> QPropertyIn in(String property, QSelection<V> subQuery) {
		return new QPropertyIn(QOperation.IN, property, createValueNode(subQuery));
	}

	@Nonnull
	static public final QSqlRestriction sqlCondition(@Nonnull String sql) {
		return new QSqlRestriction(sql, new Object[0]);
	}

	@Nonnull
	static public final QSqlRestriction sqlCondition(@Nonnull String sql, @Nonnull Object[] param) {
		return new QSqlRestriction(sql, param);
	}

	/**
	 * This will recognize values or subcriteria
	 * @param value
	 * @return
	 */
	@Nonnull
	static public QOperatorNode createValueNode(@Nullable Object value) {
		if(value instanceof QOperatorNode) {
			return (QOperatorNode) value;
		} else if(value instanceof QSelection< ? >) {
			return new QSelectionSubquery((QSelection< ? >) value);
		}
		return new QLiteral(value);
	}

	@Nonnull
	static private QOperatorNode createValueNode(long value) {
		return new QLiteral(Long.valueOf(value));
	}

	@Nonnull
	static private QOperatorNode createValueNode(double value) {
		return new QLiteral(Double.valueOf(value));
	}
}
