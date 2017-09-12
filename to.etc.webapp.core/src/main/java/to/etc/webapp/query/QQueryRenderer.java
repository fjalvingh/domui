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

import to.etc.util.StringTool;
import to.etc.webapp.qsql.QQuerySyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Render a QCriteria query as something more or less human-readable. This does not extend {@link QNodeVisitorBase} anymore because that
 * way we're forced to implement new nodes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 17, 2009
 */
public class QQueryRenderer extends QRenderingVisitorBase implements QNodeVisitor {
	private StringBuilder	m_sb = new StringBuilder(128);

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
	protected void appendWhere(String what) {
		append(what);
	}

	@Override
	public void visitCriteria(@Nonnull QCriteria< ? > qc) throws Exception {
		renderFrom(qc);
		if(qc.getRestrictions() != null)
			append(" WHERE ");
		visitRestrictionsBase(qc);
		visitOrderList(qc.getOrder());
	}

	private void renderFrom(QCriteriaQueryBase< ? > qc) {
		append("FROM ");
		Class< ? > baseClass = qc.getBaseClass();
		if(baseClass != null)
			append(baseClass.getName());
		else {
			ICriteriaTableDef< ? > metaTable = qc.getMetaTable();
			if(metaTable != null) {
				append("[META:");
				append(metaTable.toString());
				append("]");
			} else
				append("[unknown-table]");
		}
	}

	@Override
	public void visitSelection(@Nonnull QSelection< ? > s) throws Exception {
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
	public void visitSelectionColumn(@Nonnull QSelectionColumn n) throws Exception {
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
	public void visitSelectionItem(@Nonnull QSelectionItem n) throws Exception {
		if(m_currentColumn++ > 0)
			append(",");
		append("[?").append(n.getFunction().name()).append("]");
	}

	@Override
	public void visitPropertySelection(@Nonnull QPropertySelection n) throws Exception {
		if(m_currentColumn++ > 0)
			append(",");
		append(n.getFunction().name().toLowerCase());
		append("(").append(n.getProperty()).append(")");
	}

	@Override
	public void visitPropertyComparison(@Nonnull QPropertyComparison n) throws Exception {
		int oldprec = precedenceOpen(n);

		append(n.getProperty());
		appendOperation(n.getOperation());
		n.getExpr().visit(this);

		precedenceClose(oldprec);
	}

	@Override
	public void visitPropertyIn(@Nonnull QPropertyIn n) throws Exception {
		int oldprec = precedenceOpen(n);

		append(n.getProperty());
		append(" in (");
		m_curPrec = 0;

		QOperatorNode expr = n.getExpr();
		if(expr instanceof QLiteral) {
			QLiteral lit = (QLiteral) expr;
			Object value = lit.getValue();
			if(value instanceof List) {
				List<Object> list = (List<Object>) value;
				int ct = 0;
				for(Object o: list) {
					if(ct++ > 0)
						append(",");
					renderValue(o);
				}
			} else {
				throw new QQuerySyntaxException("Unexpected literal of type " + value + " in 'in' expression for property " + n.getProperty());
			}
		} else {
			expr.visit(this);
		}
		append(")");
		precedenceClose(oldprec);
	}

	@Override
	public void visitPropertyJoinComparison(@Nonnull QPropertyJoinComparison n) throws Exception {
		int oldprec = precedenceOpen(n);

		append("[parent].");
		append(n.getParentProperty());
		appendOperation(n.getOperation());
		append(n.getSubProperty());

		precedenceClose(oldprec);
	}


	@Override
	public void visitUnaryProperty(@Nonnull QUnaryProperty n) throws Exception {
		int oldprec = precedenceOpen(n);

		appendOperation(n.getOperation());
		append("(");
		append(n.getProperty());
		append(")");

		precedenceClose(oldprec);
	}

	@Override
	public void visitBetween(@Nonnull QBetweenNode n) throws Exception {
		int oldprec = precedenceOpen(n);

		append(n.getProp());
		append(" between ");
		n.getA().visit(this);
		append(" and ");
		n.getB().visit(this);

		precedenceClose(oldprec);
	}

	@Override
	public void visitOrder(@Nonnull QOrder o) throws Exception {
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
	public void visitLiteral(@Nonnull QLiteral n) throws Exception {
		int oldprec = precedenceOpen(n);

		//-- Render the literal type
		Object	val = n.getValue();
		renderValue(val);
		precedenceClose(oldprec);
	}

	private void renderValue(@Nullable Object val) {
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
	}

	@Override
	protected void	appendOperation(QOperation op) {
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

	@Override
	public void visitSqlRestriction(@Nonnull QSqlRestriction v) throws Exception {
		append("SQL['");
		append(v.getSql());
		append("'");
		if(v.getParameters().length > 0) {
			for(int i = 0; i < v.getParameters().length; i++) {
				Object val = v.getParameters()[i];
				append(", #" + i);
				append("=");
				append(String.valueOf(val));
			}
		}
		append("]");
	}


	@Override
	public void visitExistsSubquery(@Nonnull QExistsSubquery< ? > q) throws Exception {
		append("exists (select 1 from $[parent." + q.getParentProperty() + "]");

		if(q.getRestrictions() != null) {
			append(" where ");
			q.getRestrictions().visit(this);
		}
		append(")");
	}

	@Override
	public void visitSelectionSubquery(QSelectionSubquery q) throws Exception {
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
		QOperatorNode.prune(r);
		if(r != null)
			r.visit(this);
	}
}
