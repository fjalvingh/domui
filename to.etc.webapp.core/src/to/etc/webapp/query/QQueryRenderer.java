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

import java.math.*;
import java.util.*;

import javax.annotation.*;

import to.etc.util.*;

/**
 * Render a QCriteria query as something more or less human-readable. This does not extend {@link QNodeVisitorBase} anymore because that
 * way we're forced to implement new nodes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 17, 2009
 */
public class QQueryRenderer implements QNodeVisitor {
	private StringBuilder	m_sb = new StringBuilder(128);
	private int				m_curPrec = 0;

	private int m_orderIndx;

	private int m_currentColumn;

	protected int getCurrentColumn() {
		return m_currentColumn;
	}

	protected void setCurrentColumn(int currentColumn) {
		m_currentColumn = currentColumn;
	}

	/**
	 * Return the result of the conversion.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return m_sb.toString();
	}

	protected QQueryRenderer append(String s) {
		m_sb.append(s);
		return this;
	}

	@Override
	public void visitCriteria(QCriteria< ? > qc) throws Exception {
		renderFrom(qc);
		if(qc.getRestrictions() != null)
			append(" WHERE ");
		visitRestrictionsBase(qc);
		visitOrderList(qc.getOrder());
	}

	private void renderFrom(QCriteriaQueryBase< ? > qc) {
		append("FROM ");
		if(qc.getBaseClass() != null)
			append(qc.getBaseClass().getName());
		else if(qc.getMetaTable() != null) {
			append("[META:");
			append(qc.getMetaTable().toString());
			append("]");
		} else
			append("[unknown-table]");
	}

	@Override
	public void visitSelection(QSelection< ? > s) throws Exception {
		renderFrom(s);

		if(s.getColumnList().size() != 0) {
			//-- Restriction query: return the base class
			append(" SELECT ");
		}
		visitSelectionColumns(s);

		if(s.getRestrictions() != null)
			append(" WHERE ");
		visitRestrictionsBase(s);
		visitOrderList(s.getOrder());
	}

	@Override
	public void visitSelectionColumn(QSelectionColumn n) throws Exception {
		n.getItem().visit(this);
		String alias = n.getAlias();
		if(null != alias) {
			append(" as ").append(alias);
		}
	}

	public void visitSelectionColumns(QSelection< ? > s) throws Exception {
		m_currentColumn = 0;
		for(QSelectionColumn col : s.getColumnList())
			col.visit(this);
	}

	@Override
	public void visitSelectionItem(QSelectionItem n) throws Exception {
		if(m_currentColumn++ > 0)
			append(",");
		append("[?").append(n.getFunction().name()).append("]");
	}

	@Override
	public void visitPropertySelection(QPropertySelection n) throws Exception {
		if(m_currentColumn++ > 0)
			append(",");
		append(n.getFunction().name().toLowerCase());
		append("(").append(n.getProperty()).append(")");
	}

	/**
	 * Render an operator set.
	 * @see to.etc.webapp.query.QNodeVisitorBase#visitMulti(to.etc.webapp.query.QMultiNode)
	 */
	@Override
	public void visitMulti(QMultiNode n) throws Exception {
		if(n.getChildren().size() == 0)
			return;
		if(n.getChildren().size() == 1) {				// Should not really happen
			n.getChildren().get(0).visit(this);
			return;
		}
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			append("(");
		int	ct = 0;
		for(QOperatorNode c: n.getChildren()) {
			if(ct++ > 0)
				appendOperation(n.getOperation());

			//-- Visit lower
			c.visit(this);
		}
		if(oldprec > m_curPrec)
			append(")");
		m_curPrec = oldprec;
	}

	@Override
	public void visitPropertyComparison(QPropertyComparison n) throws Exception {
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			append("(");

		append(n.getProperty());
		appendOperation(n.getOperation());
		n.getExpr().visit(this);

		if(oldprec > m_curPrec)
			append(")");
		m_curPrec = oldprec;
	}

	@Override
	public void visitPropertyJoinComparison(@Nonnull QPropertyJoinComparison n) throws Exception {
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			append("(");

		append("[parent].");
		append(n.getParentProperty());
		appendOperation(n.getOperation());
		append(n.getSubProperty());

		if(oldprec > m_curPrec)
			append(")");
		m_curPrec = oldprec;
	}


	@Override
	public void visitUnaryProperty(QUnaryProperty n) throws Exception {
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			append("(");

		appendOperation(n.getOperation());
		append("(");
		append(n.getProperty());
		append(")");

		if(oldprec > m_curPrec)
			append(")");
		m_curPrec = oldprec;
	}

	@Override
	public void visitBetween(QBetweenNode n) throws Exception {
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			append("(");

		append(n.getProp());
		append(" between ");
		n.getA().visit(this);
		append(" and ");
		n.getB().visit(this);

		if(oldprec > m_curPrec)
			append(")");
		m_curPrec = oldprec;
	}

	@Override
	public void visitOrder(QOrder o) throws Exception {
		if(m_orderIndx++ == 0) {
			append(" order by ");
		} else {
			append(", ");
		}
		append(o.getProperty());
		append(" ");
		append(o.getDirection().toString());
	}

	@Override
	public void visitLiteral(QLiteral n) throws Exception {
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			append("(");

		//-- Render the literal type
		Object	val = n.getValue();
		if(val == null)
			append("dbnull");
		else if(val instanceof Integer) {
			append(val.toString());
		} else if(val instanceof Long) {
			append(val.toString());
			append("L");
		} else if(val instanceof Double) {
			append(val.toString());
			append("D");
		} else if(val instanceof Float) {
			append(val.toString());
			append("F");
		} else if(val instanceof BigDecimal) {
			append("BigDecimal(");
			append(val.toString());
			append(")");
		} else if(val instanceof BigInteger) {
			append("BigInteger(");
			append(val.toString());
			append(")");
		} else if(val instanceof String) {
			StringTool.strToJavascriptString(m_sb, (String)val, false);
		} else {
			append("Object[");
			append(val.toString());
			append("]");
		}
		if(oldprec > m_curPrec)
			append(")");
		m_curPrec = oldprec;
	}

	private void	appendOperation(QOperation op) {
		appendOperation(renderOperation(op));
	}

	private void appendOperation(String renderOperation) {
		if(Character.isLetter(renderOperation.charAt(0))) {
			if(m_sb.length() > 0 && m_sb.charAt(m_sb.length()-1) != ' ')
				append(" ");
			append(renderOperation);
			append(" ");
		} else
			append(renderOperation);
	}

	static private String	renderOperation(QOperation op) {
		switch(op) {
			default:
				throw new IllegalStateException("Unexpected operation type="+op);
			case AND:	return "and";
			case OR:	return "or";
			case NOT:	return "not";
			case BETWEEN:	return "between";
			case EQ:	return "=";
			case NE:	return "!=";
			case LT:	return "<";
			case LE:	return "<=";
			case GT:	return ">";
			case GE:	return ">=";
			case ILIKE:	return "ilike";
			case LIKE:	return "like";
			case ISNOTNULL:	return "isNotNull";
			case ISNULL:	return "isNull";
			case SQL:	return "SQL";
		}
	}

	/**
	 * Returns the operator precedence
	 * @param ot
	 * @return
	 */
	static public int	getOperationPrecedence(final QOperation ot) {
		switch(ot) {
			default:
				throw new IllegalStateException("Unknown operator "+ot);
			case OR:
				return 10;
			case AND:
				return 20;
			case NOT:
				return 25;
			/*case IN: */
			case BETWEEN: case LIKE: case ILIKE:
				return 30;

			case LT: case LE: case GT: case GE: case EQ: case NE: case ISNULL: case ISNOTNULL:
				return 40;
				//			case NOT:
				//				return 50;
				//				// ANY, ALL, SOME: 60
				//			case CONCAT:
				//				return 70;
				//			case PLUS: case MINUS:
				//				return 80;
				//			case MULT: case DIV: case MOD:
				//				return 90;
				//			case UMINUS:
				//				return 100;

			case LITERAL:
				return 100;
		}
	}

	@Override
	public void visitExistsSubquery(QExistsSubquery< ? > q) throws Exception {
		append("exists (select 1 from $[parent." + q.getParentProperty() + "] where ");
		if(q.getRestrictions() == null)
			append("MISSING WHERE - invalid exists subquery)");
		else {
			q.getRestrictions().visit(this);
			append(")");
		}
	}

	@Override
	public void visitSelectionSubquery(@Nonnull QSelectionSubquery q) throws Exception {
		int oldprec = m_curPrec;
		m_curPrec = 0;
		append("(");
		q.getSelectionQuery().visit(this);
		append(")");
		m_curPrec = oldprec;
	}

	@Override
	public void visitSubquery(@Nonnull QSubQuery< ? , ? > n) throws Exception {
		visitSelection(n);
	}

	@Override
	public void visitMultiSelection(@Nonnull QMultiSelection n) throws Exception {
		for(QSelectionItem it : n.getItemList())
			it.visit(this);
	}

	@Override
	public void visitOrderList(@Nonnull List<QOrder> orderlist) throws Exception {
		for(QOrder o : orderlist)
			o.visit(this);
	}

	@Override
	public void visitRestrictionsBase(@Nonnull QCriteriaQueryBase< ? > n) throws Exception {
		QOperatorNode r = n.getRestrictions();
		if(r != null)
			r.visit(this);
	}

	@Override
	public void visitUnaryNode(QUnaryNode n) throws Exception {
		appendOperation(n.getOperation());

		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			append("(");
		int ct = 0;

		//-- Visit lower
		n.getNode().visit(this);
		if(oldprec > m_curPrec)
			append(")");
		m_curPrec = oldprec;
	}
}
