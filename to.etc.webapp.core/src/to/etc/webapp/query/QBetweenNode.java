package to.etc.webapp.query;

/**
 * Represents a "between" operation where the base item is a property reference.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
public class QBetweenNode extends QOperatorNode {
	private QOperatorNode m_a;

	private QOperatorNode m_b;

	private String m_prop;

	public QBetweenNode(QOperation operation, String prop, QOperatorNode a, QOperatorNode b) {
		super(operation);
		m_a = a;
		m_b = b;
		m_prop = prop;
	}

	/**
	 * The low value of the between operation (prop between A and B)
	 * @return
	 */
	public QOperatorNode getA() {
		return m_a;
	}

	/**
	 * The high value of the between operation (prop between A and B)
	 * @return
	 */
	public QOperatorNode getB() {
		return m_b;
	}

	/**
	 * The name of the property.
	 * @return
	 */
	public String getProp() {
		return m_prop;
	}

	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitBetween(this);
	}
}
