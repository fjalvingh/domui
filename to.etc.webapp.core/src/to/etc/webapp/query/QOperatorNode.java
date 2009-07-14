package to.etc.webapp.query;

abstract public class QOperatorNode extends QNodeBase {
	private QOperation m_operation;

	public QOperatorNode(QOperation operation) {
		m_operation = operation;
	}

	public QOperation getOperation() {
		return m_operation;
	}
}
