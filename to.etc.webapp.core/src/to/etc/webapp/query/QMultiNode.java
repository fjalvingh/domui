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

	private List<QOperatorNode> m_buildList;

	/**
	 * Constructor to use when this node is in BUILD mode.
	 * @param operation
	 */
	QMultiNode(QOperation operation) {
		super(operation);
		m_buildList = new ArrayList<QOperatorNode>();
	}

	public QMultiNode(QOperation operation, QOperatorNode[] ch) {
		super(operation);
		//-- Check to see if we need to collapse..
		for(QOperatorNode qn : ch) {
			if(qn.getOperation() == operation) {
				//-- We need to collapse: we have a similar child.
				List<QOperatorNode> list = new ArrayList<QOperatorNode>(ch.length + 20);
				for(QOperatorNode n : ch) {
					if(n.getOperation() != operation)
						list.add(n);
					else {
						for(QOperatorNode sub : ((QMultiNode) n).getChildren()) {
							list.add(sub);
						}
					}
				}
				m_children = list.toArray(new QOperatorNode[list.size()]);
				return;
			}
		}

		m_children = ch;
	}

	QMultiNode(QOperation operation, List<QOperatorNode> ch) {
		this(operation, ch.toArray(new QOperatorNode[ch.size()]));
	}

	public QOperatorNode[] getChildren() {
		if(m_buildList != null) {
			m_children = m_buildList.toArray(new QOperatorNode[m_buildList.size()]);
			m_buildList = null;
		}
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
		if(m_buildList == null)
			throw new IllegalStateException("The query has been finalized and cannot change anymore.");
		m_buildList.add(n);
	}

	/**
	 * Used to replace the last node when it was found that there were actual extra nodes.
	 * @param mn
	 */
	void replaceTop(QMultiNode mn) {
		if(m_buildList == null || m_buildList.size() == 0)
			throw new IllegalStateException("The query has been finalized and cannot change anymore or there is a logic error in the query framework 8-/");
		m_buildList.set(m_buildList.size() - 1, mn);
	}
}
