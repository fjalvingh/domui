package to.etc.webapp.query;

/**
 * Restriction implementation which delegates setting the nodes into an {@link IQRestrictionContainer},
 * combining those nodes using "and".
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 23, 2009
 */
public class QDelegatingRestrictor<T> extends QRestrictor<T> {
	private IQRestrictionContainer m_container;

	QDelegatingRestrictor(Class<T> baseClass) {
		super(baseClass);
	}

	QDelegatingRestrictor(Class<T> baseClass, IQRestrictionContainer target) {
		super(baseClass);
		m_container = target;
	}

	void setContainer(IQRestrictionContainer c) {
		m_container = c;
	}

	@Override
	public QOperatorNode getRestrictions() {
		return m_container.getRestrictions();
	}

	@Override
	public boolean hasRestrictions() {
		return m_container.getRestrictions() != null;
	}

	@Override
	public QRestrictor<T> and() {
		return this;
	}

	@Override
	public QRestrictor<T> or() {
		QMultiNode or = new QMultiNode(QOperation.OR);
		add(or);
		return new QRestrictorImpl<T>(this, or);
	}

	/**
	 * Construct the restrictions by building an AND node, if needed.
	 * @see to.etc.webapp.query.QRestrictor#internalAdd(to.etc.webapp.query.QOperatorNode)
	 */
	@Override
	public void internalAdd(QOperatorNode r) {
		if(m_container.getRestrictions() == null) {
			m_container.setRestrictions(r); // Just set the single operation,
		} else if(m_container.getRestrictions().getOperation() == QOperation.AND) {
			//-- The target is already an AND - just add this new operation to it.
			((QMultiNode) m_container.getRestrictions()).add(r);
		} else {
			//-- We need to replace the target with an and node for the new 2 operations.
			QMultiNode and = new QMultiNode(QOperation.AND);
			and.add(m_container.getRestrictions());
			and.add(r);
			m_container.setRestrictions(and);
		}
	}
}
