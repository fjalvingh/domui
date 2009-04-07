package to.etc.webapp.query;

public class QBetweenNode extends QOperatorNode {
	private QOperatorNode		m_a;
	private QOperatorNode		m_b;
	private QOperatorNode		m_prop;
	
	public QBetweenNode(QOperation operation, QOperatorNode prop, QOperatorNode a, QOperatorNode b) {
		super(operation);
		m_a = a;
		m_b = b;
		m_prop = prop;
	}
	public QOperatorNode getA() {
		return m_a;
	}
	public QOperatorNode getB() {
		return m_b;
	}
	public QOperatorNode getProp() {
		return m_prop;
	}
	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitBetween(this);
	}
}
