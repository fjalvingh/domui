package to.etc.webapp.query;

import java.util.*;

import javax.annotation.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1/5/16.
 */
abstract public class QRenderingVisitorBase implements QNodeVisitor { // jal: DO NOT EXTEND QNodeVisitorBase here!
	protected int m_curPrec;

	abstract protected void appendWhere(@Nonnull String what);

	abstract protected void appendOperation(@Nonnull QOperation operation);


	final protected void precedenceClose(int oldprec) {
		if(oldprec > m_curPrec)
			appendWhere(")");
		m_curPrec = oldprec;
	}

	final protected int precedenceOpen(@Nonnull QOperatorNode n) {
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			appendWhere("(");
		return oldprec;
	}

	/**
	 * Returns the operator precedence
	 * @param ot
	 * @return
	 */
	final protected int getOperationPrecedence(final QOperation ot) {
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

			case LT: case LE: case GT: case GE: case EQ: case NE: case ISNULL: case ISNOTNULL: case IN:
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

	final protected String renderOperation(QOperation op) {
		switch(op) {
			default:
				throw new IllegalStateException("Unexpected operation type=" + op);
			case AND:
				return "and";
			case OR:
				return "or";
			case NOT:
				return "not";
			case BETWEEN:
				return "between";
			case EQ:
				return "=";
			case NE:
				return "!=";
			case LT:
				return "<";
			case LE:
				return "<=";
			case GT:
				return ">";
			case GE:
				return ">=";
			case ILIKE:
				return "ilike";
			case LIKE:
				return "like";
			case ISNOTNULL:
				return "is not null";
			case ISNULL:
				return "is null";
			case SQL:
				return "SQL";
		}
	}

	protected String translateOrder(QOrder o) {
		switch(o.getDirection()){
			default:
				throw new IllegalStateException("Bad order: " + o.getDirection());
			case ASC:
				return "asc";
			case DESC:
				return "desc";
		}
	}

	/**
	 * Render an operator set.
	 * @see to.etc.webapp.query.QNodeVisitorBase#visitMulti(to.etc.webapp.query.QMultiNode)
	 */
	@Override
	final public void visitMulti(@Nonnull QMultiNode n) throws Exception {
		if(n.getChildren().size() == 0)
			return;
		if(n.getChildren().size() == 1) {				// Should not really happen
			n.getChildren().get(0).visit(this);
			return;
		}
		int oldprec = precedenceOpen(n);
		int	ct = 0;
		for(QOperatorNode c: n.getChildren()) {
			if(ct++ > 0)
				appendOperation(n.getOperation());

			//-- Visit lower
			c.visit(this);
		}
		precedenceClose(oldprec);
	}

	@Override
	final public void visitUnaryNode(final @Nonnull QUnaryNode n) throws Exception {
		switch(n.getOperation()){
			default:
				throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
			case SQL:
				if(n.getNode() instanceof QLiteral) {
					QLiteral l = (QLiteral) n.getNode();
					appendWhere((String) l.getValue());
					return;
				}
				break;

			case NOT:
				if(n.getNode() == null) {
					return;
				}
				appendOperation(n.getOperation());

				int oldprec = precedenceOpen(n);
				n.getNode().visit(this);
				precedenceClose(oldprec);
				return;
		}
		throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
	}


	@Override
	public void visitCriteria(@Nonnull QCriteria< ? > qc) throws Exception {
		visitRestrictionsBase(qc);
		visitOrderList(qc.getOrder());
	}

	@Override
	public void visitOrderList(@Nonnull List<QOrder> orderlist) throws Exception {
		for(QOrder o : orderlist)
			o.visit(this);
	}

	@Override
	public void visitMultiSelection(@Nonnull QMultiSelection n) throws Exception {
		for(QSelectionItem it: n.getItemList())
			it.visit(this);
	}
	@Override
	public void visitSelectionColumn(@Nonnull QSelectionColumn n) throws Exception {
		n.getItem().visit(this);
	}
	@Override
	public void visitRestrictionsBase(@Nonnull QCriteriaQueryBase< ? > n) throws Exception {
		QOperatorNode r = n.getRestrictions();
		QOperatorNode.prune(r);
		if(r != null)
			r.visit(this);
	}

}
