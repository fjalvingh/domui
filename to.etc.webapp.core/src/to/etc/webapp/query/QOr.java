package to.etc.webapp.query;

/**
 * This is the result of the or() call on a parent RestrictionBase. It is created by calling {@link QRestrictor#or()} and
 * creates the leaves of an "or" operation. After creation of this node the first leaf (1st part of the OR) is active; all
 * operation functions called on this node are added to that "or" leaf as a set of operations separated by and. To create the
 * second leaf (or the nth leaf) of the OR node call {@link #next()}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2009
 */
public class QOr<T> extends QRestrictor<T> {
	//	private QRestrictionBase<T> m_dad;

	/** The OR node we're constructing thingerydoos for. */
	private QMultiNode m_orNode;

	private QOperatorNode m_currentNode;

	QOr(QRestrictor<T> parent, QMultiNode ornode) {
		super(parent.getBaseClass());
		//		m_dad = parent;
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
	/**
	 * We override add here so that all operations added through this node are added to the
	 * actual OR leaf being constructed. It works as follows:
	 * <ul>
	 * </ul>
	 * @see to.etc.webapp.query.QRestrictor#add(to.etc.webapp.query.QOperatorNode)
	 */
	@Override
	public void internalAdd(QOperatorNode r) {
		//-- Collapse any OR node added here into this OR being constructed now;
		if(r.getOperation() == QOperation.OR) {
			QMultiNode m = (QMultiNode) r;

			for(QOperatorNode qn : m.getChildren())
				m_orNode.add(qn);
			return;
		}

		//-- 1. Is a new leaf needed? If so just add @ 0.
		if(m_currentNode == null) {
			m_currentNode = r;
			m_orNode.add(r); // Add as only operator to the "or" initially.
		} else if(m_currentNode.getOperation() == QOperation.AND) {
			((QMultiNode) m_currentNode).add(r); // Already a multinode- just add to the 'and' in this leaf.
		} else {
			//-- We had a single node but have 2 now- we need to replace the earlier node with an AND node containing both nodes.
			QMultiNode mn = new QMultiNode(QOperation.AND);
			mn.add(m_currentNode); // Add the earlier-added operation
			mn.add(r); // Add the new operation
			m_currentNode = mn; // Use this as the target for the rest of the adds.
			m_orNode.replaceTop(mn);
		}
	}

	/**
	 * Create the next leaf: the next "or" or the next "and" in the parent; use the restrictions passed to add
	 * individual
	 * @return
	 */
	public QOr<T> next() {
		m_currentNode = null; // Forces a new leaf to be added to the OR the next time an operation is added
		return this;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Overrides to return the proper chain type.			*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#between(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public QOr<T> between(final String property, final Object a, final Object b) {
		return (QOr<T>) super.between(property, a, b);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#eq(java.lang.String, double)
	 */
	@Override
	public QOr<T> eq(final String property, final double value) {
		return (QOr<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#eq(java.lang.String, long)
	 */
	@Override
	public QOr<T> eq(final String property, final long value) {
		return (QOr<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#eq(java.lang.String, java.lang.Object)
	 */
	@Override
	public QOr<T> eq(final String property, final Object value) {
		return (QOr<T>) super.eq(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#ge(java.lang.String, double)
	 */
	@Override
	public QOr<T> ge(final String property, final double value) {
		return (QOr<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#ge(java.lang.String, long)
	 */
	@Override
	public QOr<T> ge(final String property, final long value) {
		return (QOr<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#ge(java.lang.String, java.lang.Object)
	 */
	@Override
	public QOr<T> ge(final String property, final Object value) {
		return (QOr<T>) super.ge(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#gt(java.lang.String, double)
	 */
	@Override
	public QOr<T> gt(final String property, final double value) {
		return (QOr<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#gt(java.lang.String, long)
	 */
	@Override
	public QOr<T> gt(final String property, final long value) {
		return (QOr<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#gt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QOr<T> gt(final String property, final Object value) {
		return (QOr<T>) super.gt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#ilike(java.lang.String, java.lang.Object)
	 */
	@Override
	public QOr<T> ilike(final String property, final Object value) {
		return (QOr<T>) super.ilike(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#isnotnull(java.lang.String)
	 */
	@Override
	public QOr<T> isnotnull(final String property) {
		return (QOr<T>) super.isnotnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#isnull(java.lang.String)
	 */
	@Override
	public QOr<T> isnull(final String property) {
		return (QOr<T>) super.isnull(property);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#le(java.lang.String, double)
	 */
	@Override
	public QOr<T> le(final String property, final double value) {
		return (QOr<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#le(java.lang.String, long)
	 */
	@Override
	public QOr<T> le(final String property, final long value) {
		return (QOr<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#le(java.lang.String, java.lang.Object)
	 */
	@Override
	public QOr<T> le(final String property, final Object value) {
		return (QOr<T>) super.le(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#like(java.lang.String, java.lang.Object)
	 */
	@Override
	public QOr<T> like(final String property, final Object value) {
		return (QOr<T>) super.like(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#lt(java.lang.String, double)
	 */
	@Override
	public QOr<T> lt(final String property, final double value) {
		return (QOr<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#lt(java.lang.String, long)
	 */
	@Override
	public QOr<T> lt(final String property, final long value) {
		return (QOr<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#lt(java.lang.String, java.lang.Object)
	 */
	@Override
	public QOr<T> lt(final String property, final Object value) {
		return (QOr<T>) super.lt(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#ne(java.lang.String, double)
	 */
	@Override
	public QOr<T> ne(final String property, final double value) {
		return (QOr<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#ne(java.lang.String, long)
	 */
	@Override
	public QOr<T> ne(final String property, final long value) {
		return (QOr<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#ne(java.lang.String, java.lang.Object)
	 */
	@Override
	public QOr<T> ne(final String property, final Object value) {
		return (QOr<T>) super.ne(property, value);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#or(to.etc.webapp.query.QOperatorNode[])
	 */
	@Override
	@Deprecated
	public QOr<T> or(final QOperatorNode a1, final QOperatorNode a2, final QOperatorNode... a) {
		return (QOr<T>) super.or(a1, a2, a);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QOrQueryBase#sqlCondition(java.lang.String)
	 */
	@Override
	public QOr<T> sqlCondition(final String sql) {
		return (QOr<T>) super.sqlCondition(sql);
	}
}
