package to.etc.webapp.query;

class QRestrictorImpl<T> extends QRestrictor<T> {
	/** The OR node we're constructing thingerydoos for. */
	private QOperatorNode m_orNode;

	QRestrictorImpl(QRestrictor<T> parent, QMultiNode ornode) {
		super(parent.getBaseClass(), ornode.getOperation());
		m_orNode = ornode;
	}

	@Override
	public QOperatorNode getRestrictions() {
		return m_orNode;
	}

	@Override
	public void setRestrictions(QOperatorNode n) {
		m_orNode = n;
	}
}
