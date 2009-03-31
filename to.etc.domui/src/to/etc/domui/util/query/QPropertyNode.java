package to.etc.domui.util.query;

public class QPropertyNode extends QOperatorNode {
	private String			m_name;
	
	public QPropertyNode(String name) {
		super(QOperation.PROP);
		m_name = name;
	}
	public String getName() {
		return m_name;
	}
	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitPropertyNode(this);
	}
	@Override
	public String toString() {
		return "Property: "+m_name;
	}
}
