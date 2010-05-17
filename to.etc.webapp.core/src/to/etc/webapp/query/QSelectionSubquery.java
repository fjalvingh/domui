package to.etc.webapp.query;

public class QSelectionSubquery extends QOperatorNode {
	private QSelection< ? > m_parentQuery;

	public QSelectionSubquery(QSelection< ? > parent) {
		super(QOperation.SELECTION_SUBQUERY);
		m_parentQuery = parent;
	}

	public QSelection< ? > getSelectionQuery() {
		return m_parentQuery;
	}

	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitSelectionSubquery(this);
	}
}
