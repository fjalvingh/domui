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
	private List<QOperatorNode> m_children;

	/**
	 * Constructor to use when this node is in BUILD mode.
	 * @param operation
	 */
	QMultiNode(QOperation operation) {
		super(operation);
		m_children = new ArrayList<QOperatorNode>();
	}

	public QMultiNode(QOperation operation, QOperatorNode[] ch) {
		super(operation);
		m_children = new ArrayList<QOperatorNode>(ch.length);

		//-- Check to see if we need to collapse..
		for(QOperatorNode qn : ch) {
			if(qn.getOperation() == operation) {
				//-- We need to collapse: we have a similar child.
				for(QOperatorNode sub : ((QMultiNode) qn).getChildren()) {
					m_children.add(sub);
				}
			} else
				m_children.add(qn);
		}
	}

	QMultiNode(QOperation operation, List<QOperatorNode> ch) {
		this(operation, ch.toArray(new QOperatorNode[ch.size()]));
	}

	public List<QOperatorNode> getChildren() {
		return m_children;
	}

	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitMulti(this);
	}

	/**
	 * In build mode, add a node.
	 * @param n
	 */
	void add(QOperatorNode n) {
		m_children.add(n);
	}

	/**
	 * Used to replace the last node when it was found that there were actual extra nodes.
	 * @param mn
	 */
	void replaceTop(QMultiNode mn) {
		m_children.set(m_children.size() - 1, mn);
	}
}
