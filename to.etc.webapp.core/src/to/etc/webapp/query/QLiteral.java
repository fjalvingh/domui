package to.etc.webapp.query;

public class QLiteral extends QOperatorNode {
	private Object		m_value;

	public QLiteral(Object value) {
		super(QOperation.LITERAL);
		m_value = value;
	}
	public Object getValue() {
		return m_value;
	}
	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitLiteral(this);
	}
}
