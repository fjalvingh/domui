package to.etc.webapp.query;

public class QPropertyComparison extends QOperatorNode {
	private String m_property;

	private QOperatorNode m_expr;

	public QPropertyComparison(QOperation operation, String poperty, QOperatorNode expr) {
		super(operation);
		m_property = poperty;
		m_expr = expr;
	}

	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitPropertyComparison(this);
	}

	public QOperatorNode getExpr() {
		return m_expr;
	}

	public String getProperty() {
		return m_property;
	}
}
