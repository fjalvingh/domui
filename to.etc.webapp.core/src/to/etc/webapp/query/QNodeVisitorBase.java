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
 * Base class for visiting a node tree. The methods in this base class cause all
 * children of a the tree to be visited in order.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
abstract public class QNodeVisitorBase implements QNodeVisitor {
	@Override
	public void visitPropertyComparison(@Nonnull QPropertyComparison n) throws Exception {
		n.getExpr().visit(this);
	}

	@Override
	public void visitPropertyIn(@Nonnull QPropertyIn n) throws Exception {
		n.getExpr().visit(this);
	}

	@Override
	public void visitUnaryNode(@Nonnull QUnaryNode n) throws Exception {
		n.getNode().visit(this);
	}

	@Override
	public void visitUnaryProperty(@Nonnull QUnaryProperty n) throws Exception {}

	@Override
	public void visitSqlRestriction(@Nonnull QSqlRestriction v) throws Exception {}

	@Override
	public void visitBetween(@Nonnull QBetweenNode n) throws Exception {
		n.getA().visit(this);
		n.getB().visit(this);
	}

	@Override
	public void visitRestrictionsBase(@Nonnull QCriteriaQueryBase< ? > n) throws Exception {
		QOperatorNode r = n.getRestrictions();
		if(r != null)
			r.visit(this);
	}

	@Override
	public void visitCriteria(@Nonnull QCriteria< ? > qc) throws Exception {
		visitRestrictionsBase(qc);
		visitOrderList(qc.getOrder());
	}
	@Override
	public void visitSelection(@Nonnull QSelection< ? > s) throws Exception {
		visitSelectionColumns(s);
		visitRestrictionsBase(s);
		visitOrderList(s.getOrder());
	}

	public void visitSelectionColumns(@Nonnull QSelection< ? > s) throws Exception {
		for(@Nonnull
		QSelectionColumn col : s.getColumnList())
			col.visit(this);
	}

	@Override
	public void visitOrderList(@Nonnull List<QOrder> orderlist) throws Exception {
		for(QOrder o : orderlist)
			o.visit(this);
	}

	@Override
	public void visitLiteral(@Nonnull QLiteral n) throws Exception {}

	@Override
	public void visitMulti(@Nonnull QMultiNode n) throws Exception {
		for(QOperatorNode o : n.getChildren())
			o.visit(this);
	}

	@Override
	public void visitOrder(@Nonnull QOrder o) throws Exception {}

	@Override
	public void visitPropertySelection(@Nonnull QPropertySelection n) throws Exception {
	}
	@Override
	public void visitSelectionColumn(@Nonnull QSelectionColumn n) throws Exception {
		n.getItem().visit(this);
	}
	@Override
	public void visitSelectionItem(@Nonnull QSelectionItem n) throws Exception {
	}
	@Override
	public void visitMultiSelection(@Nonnull QMultiSelection n) throws Exception {
		for(QSelectionItem it: n.getItemList())
			it.visit(this);
	}

	@Override
	public void visitExistsSubquery(@Nonnull QExistsSubquery< ? > q) throws Exception {
		throw new UnsupportedOperationException("Subqueries are not supported");
	}

	@Override
	public void visitSubquery(@Nonnull QSubQuery< ? , ? > n) throws Exception {
		throw new UnsupportedOperationException("Subqueries are not supported");
	}

	@Override
	public void visitSelectionSubquery(@Nonnull QSelectionSubquery n) throws Exception {
		throw new UnsupportedOperationException("Subqueries are not supported");
	}

	@Override
	public void visitPropertyJoinComparison(@Nonnull QPropertyJoinComparison qPropertyJoinComparison) throws Exception {
	}
}
