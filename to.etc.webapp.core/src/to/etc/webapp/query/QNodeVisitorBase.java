package to.etc.webapp.query;

import java.util.*;

/**
 * Base class for visiting a node tree. The methods in this base class cause all
 * children of a the tree to be visited in order.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class QNodeVisitorBase implements QNodeVisitor {
	public void visitPropertyComparison(QPropertyComparison n) throws Exception {
		n.getExpr().visit(this);
	}

	//	public void visitBinaryNode(QBinaryNode n) throws Exception {
	//		n.getLhs().visit(this);
	//		n.getRhs().visit(this);
	//	}
	public void visitUnaryNode(QUnaryNode n) throws Exception {
		n.getNode().visit(this);
	}

	public void visitUnaryProperty(QUnaryProperty n) throws Exception {}

	public void visitBetween(QBetweenNode n) throws Exception {
		n.getA().visit(this);
		n.getB().visit(this);
	}

	public void visitRestrictionsBase(QRestrictionsBase n) throws Exception {
		QOperatorNode r = n.getRestrictions();
		if(r != null)
			r.visit(this);
	}

	public void visitCriteria(QCriteria< ? > qc) throws Exception {
		visitRestrictionsBase(qc);
		visitOrderList(qc.getOrder());
	}

	public void visitOrderList(List<QOrder> orderlist) throws Exception {
		for(QOrder o : orderlist)
			o.visit(this);
	}

	public void visitLiteral(QLiteral n) throws Exception {}

	public void visitMulti(QMultiNode n) throws Exception {
		for(QOperatorNode o : n.getChildren())
			o.visit(this);
	}

	public void visitOrder(QOrder o) throws Exception {}

	public void visitPropertySelection(QPropertySelection n) throws Exception {
	}
	public void visitSelectionColumn(QSelectionColumn n) throws Exception {
		n.getItem().visit(this);
	}
	public void visitSelectionItem(QSelectionItem n) throws Exception {
	}
	public void visitMultiSelection(QMultiSelection n) throws Exception {
		for(QSelectionItem it: n.getItemList())
			it.visit(this);
	}
}
