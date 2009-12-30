package to.etc.webapp.query;

class QRestrictorImpl<T> extends QRestrictor<T> {
	/** The OR node we're constructing thingerydoos for. */
	private QMultiNode m_orNode;

	QRestrictorImpl(QRestrictor<T> parent, QMultiNode ornode) {
		super(parent.getBaseClass());
		m_orNode = ornode;
	}

	@Override
	public QOperatorNode getRestrictions() {
		return m_orNode;
	}

	@Override
	public boolean hasRestrictions() {
		return true;
	}

	@Override
	public void internalAdd(QOperatorNode r) {
		m_orNode.add(r);
	}

	@Override
	public QRestrictor<T> or() {
		if(m_orNode.getOperation() == QOperation.OR)
			return this;
		QMultiNode or = new QMultiNode(QOperation.OR);
		add(or);
		return new QRestrictorImpl<T>(this, or);
	}

	@Override
	public QRestrictor<T> and() {
		if(m_orNode.getOperation() == QOperation.AND)
			return this;
		QMultiNode and = new QMultiNode(QOperation.AND);
		add(and);
		return new QRestrictorImpl<T>(this, and);
	}
}
