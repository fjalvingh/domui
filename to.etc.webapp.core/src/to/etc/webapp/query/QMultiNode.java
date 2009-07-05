package to.etc.webapp.query;

import java.util.*;

/**
 * A node representing the same operation spanning several
 * nodes (like x AND y AND z)
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class QMultiNode extends QOperatorNode {
	private QOperatorNode[] m_children;

	public QMultiNode(QOperation operation, QOperatorNode[] ch) {
		super(operation);
		m_children = ch;
	}

	QMultiNode(QOperation operation, List<QOperatorNode> ch) {
		this(operation, ch.toArray(new QOperatorNode[ch.size()]));
	}

	public QOperatorNode[] getChildren() {
		return m_children;
	}

	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitMulti(this);
	}
}
