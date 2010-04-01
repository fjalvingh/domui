package to.etc.webapp.query;

/**
 * Represents an "exists" subquery on some child relation of a record. This
 * is always defined as a subquery on a parent record's child-record-set, and
 * it adds requirements on the existence of children having certain restrictions.
 * This should be rendered as a joined-subquery, like:
 * <pre>
 *  [[select xxx from parent_table p where ...]] - rendered above this
 *  exists (select 1 from child_table a where a.pk = p.fk and [[conditions in this node]]).
 * </pre>
 *
 * @param <T>	The type of the child record persistent class, so the T from the List<T> getChildList() in this subquery's parent.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2009
 */
public class QExistsSubquery<T> extends QOperatorNode {
	private QRestrictor< ? > m_parentQuery;

	private String m_parentProperty;

	private Class< ? > m_baseClass;

	private QOperatorNode m_restrictions;

	public QExistsSubquery(QRestrictor< ? > parent, Class<T> baseClass, String property) {
		super(QOperation.EXISTS_SUBQUERY);
		m_parentQuery = parent;
		m_parentProperty = property;
		m_baseClass = baseClass;
	}

	public QRestrictor< ? > getParentQuery() {
		return m_parentQuery;
	}

	public Class< ? > getBaseClass() {
		return m_baseClass;
	}
	public String getParentProperty() {
		return m_parentProperty;
	}

	public QOperatorNode getRestrictions() {
		return m_restrictions;
	}

	public void setRestrictions(QOperatorNode restrictions) {
		m_restrictions = restrictions;
	}

	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitExistsSubquery(this);
	}
}
