package to.etc.webapp.query;

abstract public class QOperatorNode {
	private QOperation m_operation;

	abstract public void visit(QNodeVisitor v) throws Exception;

	public QOperatorNode(QOperation operation) {
		m_operation = operation;
	}

	public QOperation getOperation() {
		return m_operation;
	}
}
