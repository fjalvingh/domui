package to.etc.webapp.query;

public class QBinaryNode extends QOperatorNode {
	private QOperatorNode		m_lhs;
	private QOperatorNode		m_rhs;
	
	public QBinaryNode(QOperation operation, QOperatorNode lhs,  QOperatorNode rhs) {
		super(operation);
		m_lhs = lhs;
		m_rhs = rhs;
	}
	public QOperatorNode getLhs() {
		return m_lhs;
	}
	public QOperatorNode getRhs() {
		return m_rhs;
	}
	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitBinaryNode(this);
	}
}
