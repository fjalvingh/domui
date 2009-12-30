package to.etc.domui.hibernate.model;

import java.util.*;

import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.impl.*;
import org.hibernate.metadata.*;
import org.hibernate.persister.collection.*;
import org.hibernate.persister.entity.*;
import org.hibernate.type.*;

import to.etc.util.*;
import to.etc.webapp.*;
import to.etc.webapp.query.*;

/**
 * Thingy which creates a Hibernate Criteria thingy from a generic query. This is harder than
 * it looks because the Criteria and DetachedCriteria kludge and Hibernate's metadata dungheap
 * makes generic work very complex and error-prone.
 *
 * <p>It might be a better idea to start generating SQL from here, using Hibernate internal code
 * to instantiate the query's result only.</p>
 *
 * Please look a <a href="http://bugzilla.etc.to/show_bug.cgi?id=640">Bug 640</a> for more details, and see
 * the wiki page http://info.etc.to/xwiki/bin/view/Main/UIAbstractDatabase for more details
 * on the working of all this.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class CriteriaCreatingVisitor extends QNodeVisitorBase {
	private Session m_session;

	/** The topmost Criteria: the one that will be returned to effect the translated query */
	private final Criteria m_rootCriteria;

	/**
	 * This either holds a Criteria or a DetachedCriteria; since these are not related (sigh) we must
	 * use instanceof everywhere. Bad, bad, bad hibernate design.
	 */
	private Object m_currentCriteria;

	private Criterion m_last;

	private int m_aliasIndex;

	private Map<String, Object> m_subCriteriaMap = Collections.EMPTY_MAP;

	public CriteriaCreatingVisitor(Session ses, final Criteria crit) {
		m_session = ses;
		m_rootCriteria = crit;
		m_currentCriteria = crit;
	}

	private String nextAlias() {
		return "a" + (++m_aliasIndex);
	}

	private void addCriterion(Criterion c) {
		if(m_currentCriteria instanceof Criteria) {
			((Criteria) m_currentCriteria).add(c); // Crapfest
		} else if(m_currentCriteria instanceof DetachedCriteria) {
			((DetachedCriteria) m_currentCriteria).add(c);
		} else
			throw new IllegalStateException("Unexpected current thing: " + m_currentCriteria);
	}

	private void addOrder(Order c) {
		if(m_currentCriteria instanceof Criteria) {
			((Criteria) m_currentCriteria).addOrder(c); // Crapfest
		} else if(m_currentCriteria instanceof DetachedCriteria) {
			((DetachedCriteria) m_currentCriteria).addOrder(c);
		} else
			throw new IllegalStateException("Unexpected current thing: " + m_currentCriteria);
	}

	private void addSubCriterion(Criterion c) {
		if(m_subCriteria instanceof Criteria) {
			((Criteria) m_subCriteria).add(c); // Crapfest
		} else if(m_subCriteria instanceof DetachedCriteria) {
			((DetachedCriteria) m_subCriteria).add(c);
		} else
			throw new IllegalStateException("Unexpected current thing: " + m_subCriteria);
	}

	private void addSubOrder(Order c) {
		if(m_subCriteria instanceof Criteria) {
			((Criteria) m_subCriteria).addOrder(c); // Crapfest
		} else if(m_subCriteria instanceof DetachedCriteria) {
			((DetachedCriteria) m_subCriteria).addOrder(c);
		} else
			throw new IllegalStateException("Unexpected current thing: " + m_subCriteria);
	}

	@Override
	public void visitRestrictionsBase(QCriteriaQueryBase<?> n) throws Exception {
		QOperatorNode r = n.getRestrictions();
		if(r == null)
			return;
		if(r.getOperation() == QOperation.AND) {
			QMultiNode mn = (QMultiNode) r;
			for(QOperatorNode qtn : mn.getChildren()) {
				qtn.visit(this);
				if(m_last != null) {
					addCriterion(m_last);
					m_last = null;
				}
			}
		} else {
			r.visit(this);
			addCriterion(m_last);
			m_last = null;
		}
	}

	@Override
	public void visitCriteria(final QCriteria< ? > qc) throws Exception {
		super.visitCriteria(qc);

		//-- 2. Handle limits and start: applicable to root criterion only
		if(qc.getLimit() > 0)
			m_rootCriteria.setMaxResults(qc.getLimit());
		if(qc.getStart() > 0) {
			m_rootCriteria.setFirstResult(qc.getStart());
		}
	}

	private Object m_subCriteria;

	private String parseSubcriteria(String name) {
		m_subCriteria = null;
		int pos = name.indexOf('.'); // Is the name containing a dot?
		if(pos == -1) // If not: no subcriteria query.
			return name;

		//-- 2. Parse the name.
		String path = null;
		//		int len = name.length();
		int ix = 0;
		for(;;) {
			String sub = name.substring(ix, pos); // Current substring;
			ix = pos + 1;
			if(path == null)
				path = sub;
			else
				path = path + "." + sub;

			//-- Create/lookup the appropriate subcriteria association
			Object sc = m_subCriteriaMap.get(path);
			if(sc == null) { //-- This path is not yet known? Then we need to create it.
				if(m_subCriteria == null) {
					if(m_currentCriteria instanceof Criteria)
						m_subCriteria = ((Criteria) m_currentCriteria).createCriteria(sub);
					else if(m_currentCriteria instanceof DetachedCriteria)
						m_subCriteria = ((DetachedCriteria) m_currentCriteria).createCriteria(sub);
					else
						throw new IllegalStateException("Unknown current thingy: " + m_currentCriteria);

				} else {
					// FIXME jal 20091224 I think this is crap.
					//					m_subCriteria = sc;
					if(m_subCriteria instanceof Criteria)
						m_subCriteria = ((Criteria) m_subCriteria).createCriteria(sub);
					else if(m_subCriteria instanceof DetachedCriteria)
						m_subCriteria = ((DetachedCriteria) m_subCriteria).createCriteria(sub);
				}
				if(m_subCriteriaMap == Collections.EMPTY_MAP)
					m_subCriteriaMap = new HashMap<String, Object>();
				m_subCriteriaMap.put(path, m_subCriteria);
			} else
				m_subCriteria = sc;

			//-- Move to the next segment, if present,
			pos = name.indexOf('.', ix);
			if(pos == -1) {
				return name.substring(ix); // Return the last segment of the name which must be a field in this subcriteria
			}
		}
	}


	@Override
	public void visitPropertyComparison(QPropertyComparison n) throws Exception {
		QOperatorNode rhs = n.getExpr();
		String name = n.getProperty();
		QLiteral lit = null;
		if(rhs.getOperation() == QOperation.LITERAL) {
			lit = (QLiteral) rhs;
		} else
			throw new IllegalStateException("Unknown operands to " + n.getOperation() + ": " + name + " and " + rhs.getOperation());

		//-- If prop refers to some relation (dotted pair):
		name = parseSubcriteria(name);

		Criterion last = null;
		switch(n.getOperation()){
			default:
				throw new IllegalStateException("Unexpected operation: " + n.getOperation());

			case EQ:
				if(lit.getValue() == null) {
					last = Restrictions.isNull(name);
					break;
				}
				last = Restrictions.eq(name, lit.getValue());
				break;
			case NE:
				if(lit.getValue() == null) {
					last = Restrictions.isNotNull(name);
					break;
				}
				last = Restrictions.ne(name, lit.getValue());
				break;
			case GT:
				last = Restrictions.gt(name, lit.getValue());
				break;
			case GE:
				last = Restrictions.ge(name, lit.getValue());
				break;
			case LT:
				last = Restrictions.lt(name, lit.getValue());
				break;
			case LE:
				last = Restrictions.le(name, lit.getValue());
				break;
			case LIKE:
				last = Restrictions.like(name, lit.getValue());
				break;
			case ILIKE:
				last = Restrictions.ilike(name, lit.getValue());
				break;
		}

		if(m_subCriteria != null)
			addSubCriterion(last);
		else
			m_last = last;
		m_subCriteria = null;
	}

	@Override
	public void visitBetween(final QBetweenNode n) throws Exception {
		if(n.getA().getOperation() != QOperation.LITERAL || n.getB().getOperation() != QOperation.LITERAL)
			throw new IllegalStateException("Expecting literals as 2nd and 3rd between parameter");
		QLiteral a = (QLiteral) n.getA();
		QLiteral b = (QLiteral) n.getB();

		//-- If prop refers to some relation (dotted pair):
		String name = n.getProp();
		name = parseSubcriteria(name);

		if(m_subCriteria != null)
			addSubCriterion(Restrictions.between(name, a.getValue(), b.getValue()));
		else
			m_last = Restrictions.between(n.getProp(), a.getValue(), b.getValue());
		m_subCriteria = null;
	}

	/**
	 * Compound. Ands and ors.
	 *
	 * @see to.etc.webapp.query.QNodeVisitorBase#visitMulti(to.etc.webapp.query.QMultiNode)
	 */
	@Override
	public void visitMulti(final QMultiNode inn) throws Exception {
		//-- Walk all members, create nodes from 'm.
		Criterion c1 = null;
		for(QOperatorNode n : inn.getChildren()) {
			n.visit(this); // Convert node to Criterion thingydoodle
			if(c1 == null) {
				c1 = m_last; // If 1st one use as lhs,
				m_last = null;
			} else {
				switch(inn.getOperation()){
					default:
						throw new IllegalStateException("Unexpected operation: " + inn.getOperation());
					case AND:
						if(m_last != null) {
							c1 = Restrictions.and(c1, m_last);
							m_last = null;
						}
						break;
					case OR:
						if(m_last != null) {
							c1 = Restrictions.or(c1, m_last);
							m_last = null;
						}
						break;
				}
			}
		}
		if(c1 == null)
			throw new IllegalStateException("? Odd multi - no members?!");
		m_last = c1;
	}

	@Override
	public void visitOrder(final QOrder o) throws Exception {
		String name = o.getProperty();
		name = parseSubcriteria(name);
		Order ho = o.getDirection() == QSortOrderDirection.ASC ? Order.asc(name) : Order.desc(name);
		if(m_subCriteria != null) {
			addSubOrder(ho);
			m_subCriteria = null;
		} else
			addOrder(ho);
	}

	@Override
	public void visitUnaryNode(final QUnaryNode n) throws Exception {
		switch(n.getOperation()){
			default:
				throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
			case SQL:
				if(n.getNode() instanceof QLiteral) {
					QLiteral l = (QLiteral) n.getNode();
					String s = (String) l.getValue();
					m_last = Restrictions.sqlRestriction(s);
					return;
				}
				break;
		}
		throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
	}

	@Override
	public void visitUnaryProperty(final QUnaryProperty n) throws Exception {
		String name = n.getProperty();
		name = parseSubcriteria(name); // If this is a dotted name prepare a subcriteria on it.

		Criterion c;
		switch(n.getOperation()){
			default:
				throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());

			case ISNOTNULL:
				c = Restrictions.isNotNull(name);
				break;
			case ISNULL:
				c = Restrictions.isNull(name);
				break;
		}
		if(m_subCriteria != null)
			addSubCriterion(c);
		else
			m_last = c;
	}

	@Override
	public void visitLiteral(final QLiteral n) throws Exception {
		throw new IllegalStateException("? Unexpected literal: " + n);
	}

	/**
	 * Child-related subquery: determine existence of children having certain characteristics. Because
	 * the worthless Hibernate "meta model" API and the utterly disgusting way that mapping data is
	 * "stored" in Hibernate we resort to getting the generic type of the child property's collection
	 * to determine the type where the subquery is executed on.
	 * @see to.etc.webapp.query.QNodeVisitorBase#visitExistsSubquery(to.etc.webapp.query.QExistsSubquery)
	 */
	@Override
	public void visitExistsSubquery(QExistsSubquery< ? > q) throws Exception {
		//-- Sigh. Find the property.
		PropertyInfo pi = ClassUtil.findPropertyInfo(q.getParentQuery().getBaseClass(), q.getParentProperty());
		if(pi == null)
			throw new ProgrammerErrorException("The property '" + q.getParentProperty() + "' is not found in class " + q.getParentQuery().getBaseClass());

		//-- Should be List type
		if(!List.class.isAssignableFrom(pi.getActualType()))
			throw new ProgrammerErrorException("The property '" + q.getParentQuery().getBaseClass() + "." + q.getParentProperty() + "' should be a list (it is a " + pi.getActualType() + ")");

		//-- Make sure there is a where condition to restrict
		QOperatorNode where = q.getRestrictions();
		if(where == null)
			throw new ProgrammerErrorException("exists subquery has no restrictions: " + this);

		//-- Get the list's generic compound type because we're unable to get it from Hibernate easily. Idiots.
		Class< ? > coltype = pi.getCollectionValueType();
		if(coltype == null)
			throw new ProgrammerErrorException("The property '" + q.getParentQuery().getBaseClass() + "." + q.getParentProperty() + "' has an undeterminable child type");

		//-- 2. Create an exists subquery; create a sub-statement
		DetachedCriteria dc = DetachedCriteria.forClass(coltype, nextAlias());
		Criterion exists = Subqueries.exists(dc);
		dc.setProjection(Projections.id()); // Whatever: just some thingy.

		//-- Append the join condition; we need all children here that are in the parent's collection. We need the parent reference to use in the child.
		ClassMetadata childmd = m_session.getSessionFactory().getClassMetadata(coltype);

		//-- Entering the crofty hellhole that is Hibernate meta"data": never seen more horrible cruddy garbage
		ClassMetadata parentmd = m_session.getSessionFactory().getClassMetadata(q.getParentQuery().getBaseClass());
		int index = findMoronicPropertyIndexBecauseHibernateIsTooStupidToHaveAPropertyMetaDamnit(parentmd, q.getParentProperty());
		if(index == -1)
			throw new IllegalStateException("Hibernate does not know property");
		Type type = parentmd.getPropertyTypes()[index];
		BagType bt = (BagType) type;
		final OneToManyPersister persister = (OneToManyPersister) ((SessionFactoryImpl) m_session.getSessionFactory()).getCollectionPersister(bt.getRole());
		String[] keyCols = persister.getKeyColumnNames();

		//-- Try to locate those FK column names in the FK table so we can fucking locate the mapping property.
		int fkindex = findCruddyChildProperty(childmd, keyCols);
		if(fkindex < 0)
			throw new IllegalStateException("Cannot find child's parent property in cruddy Hibernate metadata toiletbowl: " + keyCols);
		String childupprop = childmd.getPropertyNames()[fkindex];

		//-- Well, that was it. What a sheitfest. Add the join condition to the parent
		String parentAlias = getParentAlias();
		dc.add(Restrictions.eqProperty(childupprop + "." + childmd.getIdentifierPropertyName(), parentAlias + "." + parentmd.getIdentifierPropertyName()));

		//-- Sigh; Recursively apply all parts to the detached thingerydoo
		Object old = m_currentCriteria;
		m_currentCriteria = dc;
		where.visit(this);
		if(m_last != null) {
			dc.add(m_last);
			m_last = null;
		}
		m_currentCriteria = old;
		m_last = exists;
	}

	private String getParentAlias() {
		if(m_currentCriteria instanceof Criteria)
			return ((Criteria) m_currentCriteria).getAlias();
		else if(m_currentCriteria instanceof DetachedCriteria)
			return ((DetachedCriteria) m_currentCriteria).getAlias();
		else
			throw new IllegalStateException("Unknown type");
	}

	/**
	 * Try to locate the property in the child that refers to the parent in a horrible way.
	 * @param cm
	 * @param keyCols
	 * @return
	 */
	private int findCruddyChildProperty(ClassMetadata cm, String[] keyCols) {
		SingleTableEntityPersister fuckup = (SingleTableEntityPersister) cm;
		for(int i = fuckup.getPropertyNames().length; --i >= 0;) {
			String[] cols = fuckup.getPropertyColumnNames(i);
			if(Arrays.equals(keyCols, cols)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Damn.
	 * @param md
	 * @param name
	 * @return
	 */
	static private int findMoronicPropertyIndexBecauseHibernateIsTooStupidToHaveAPropertyMetaDamnit(ClassMetadata md, String name) {
		for(int i = md.getPropertyNames().length; --i >= 0;) {
			if(md.getPropertyNames()[i].equals(name))
				return i;
		}
		return -1;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Selection translation to Projection.				*/
	/*--------------------------------------------------------------*/
	private ProjectionList		m_proli;
	private Projection			m_lastProj;

	@Override
	public void visitMultiSelection(QMultiSelection n) throws Exception {
		throw new ProgrammerErrorException("multi-operation selections not supported by Hibernate");
	}
	@Override
	public void visitSelection(QSelection< ? > s) throws Exception {
		if(m_proli != null)
			throw new IllegalStateException("? Projection list already initialized??");
		m_proli = Projections.projectionList();
		visitSelectionColumns(s);
		if(m_currentCriteria instanceof Criteria)
			((Criteria) m_currentCriteria).setProjection(m_proli);
		else if(m_currentCriteria instanceof DetachedCriteria)
			((DetachedCriteria) m_currentCriteria).setProjection(m_proli);
		else
			throw new IllegalStateException("Unsupported current: " + m_currentCriteria);
		visitRestrictionsBase(s);
		visitOrderList(s.getOrder());
	}

	@Override
	public void visitSelectionColumn(QSelectionColumn n) throws Exception {
		super.visitSelectionColumn(n);
		if(m_lastProj != null)
			m_proli.add(m_lastProj);
	}

	@Override
	public void visitSelectionItem(QSelectionItem n) throws Exception {
		throw new ProgrammerErrorException("Unexpected selection item: "+n);
	}
	@Override
	public void visitPropertySelection(QPropertySelection n) throws Exception {
		switch(n.getFunction()) {
			default:
				throw new IllegalStateException("Unexpected selection item function: "+n.getFunction());
			case AVG:	m_lastProj = Projections.avg(n.getProperty());	break;
			case MAX:	m_lastProj = Projections.max(n.getProperty());	break;
			case MIN:	m_lastProj = Projections.min(n.getProperty());	break;
			case SUM:	m_lastProj = Projections.sum(n.getProperty());	break;
			case COUNT:	m_lastProj = Projections.count(n.getProperty());	break;
			case COUNT_DISTINCT:	m_lastProj = Projections.countDistinct(n.getProperty());	break;
			case ID:	m_lastProj = Projections.id();	break;
			case PROPERTY:	m_lastProj = Projections.property(n.getProperty());	break;
			case ROWCOUNT:	m_lastProj = Projections.rowCount();			break;
		}
	}
}
