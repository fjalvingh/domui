package to.etc.webapp.query;

public class QUnaryProperty extends QOperatorNode {
	private String m_property;

	public QUnaryProperty(QOperation operation, String property) {
		super(operation);
		m_property = property;
	}

	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitUnaryProperty(this);
	}

	public String getProperty() {
		return m_property;
	}
}
