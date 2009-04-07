package to.etc.webapp.query;

public class QUnaryNode extends QOperatorNode {
	private QOperatorNode		m_node;
	
	public QUnaryNode(QOperation operation, QOperatorNode lhs) {
		super(operation);
		m_node = lhs;
	}
	public QOperatorNode getNode() {
		return m_node;
	}
	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitUnaryNode(this);
	}
}

