package to.etc.domui.hibernate.model;

import java.util.*;

import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.impl.*;
import org.hibernate.metadata.*;
import org.hibernate.persister.collection.*;
import org.hibernate.persister.entity.*;
import org.hibernate.type.*;

import to.etc.domui.component.meta.*;
import to.etc.util.*;
import to.etc.webapp.*;
import to.etc.webapp.qsql.*;
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

	private Class< ? > m_rootClass;

	static private final class CritEntry {
		/** The class type queried by this subcriterion */
		private Class< ? > m_actualClass;

		/** Either a Criteria or a DetachedCriteria object, sigh */
		private Object m_abomination;

		public CritEntry(Class< ? > actualClass, Object abomination) {
			m_actualClass = actualClass;
			m_abomination = abomination;
		}

		/**
		 * Return either the Criteria or DetachedCriteria.
		 * @return
		 */
		public Object getAbomination() {
			return m_abomination;
		}

		/**
		 * Return the actual type queried by the criterion.
		 * @return
		 */
		public Class< ? > getActualClass() {
			return m_actualClass;
		}
	}

	/**
	 * Maps all subcriteria created for path entries, indexed by their FULL path name
	 * from the root object. This prevents us from creating multiple subcriteria for
	 * fields that lay after the same path reaching a parent relation.
	 */
	private Map<String, CritEntry> m_subCriteriaMap = Collections.EMPTY_MAP;

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

	/**
	 * Create a join either on a Criteria or a DetachedCriteria. Needed because the idiot that creates those
	 * did not interface/baseclass them.
	 * @param current
	 * @param name
	 * @param joinType
	 * @return
	 */
	private Object createSubCriteria(Object current, String name, int joinType) {
		if(current instanceof Criteria)
			return ((Criteria) current).createCriteria(name, joinType);
		else if(current instanceof DetachedCriteria)
			return ((DetachedCriteria) current).createCriteria(name, joinType);
		else
			throw new IllegalStateException("? Unexpected criteria abomination: " + current);
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
	public void visitRestrictionsBase(QCriteriaQueryBase< ? > n) throws Exception {
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
		m_rootClass = qc.getBaseClass();
		super.visitCriteria(qc);

		//-- 2. Handle limits and start: applicable to root criterion only
		if(qc.getLimit() > 0)
			m_rootCriteria.setMaxResults(qc.getLimit());
		if(qc.getStart() > 0) {
			m_rootCriteria.setFirstResult(qc.getStart());
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Property path resolution code.						*/
	/*--------------------------------------------------------------*/
	/** The current subcriteria. */
	private Object m_subCriteria;

	/** Temp array used in parser to decode the properties reached; used to prevent multiple object allocations. */
	private PropertyMetaModel[] m_pendingJoinProps = new PropertyMetaModel[20];

	private String[] m_pendingJoinPaths = new String[20];

	private int m_pendingJoinIx;

	private String m_inputPath;

	private StringBuilder	m_sb = new StringBuilder();

	/** The current subcriteria's base class. */
	//	private Class< ? > m_subCriteriaClass;

	/**
	 * This parses all dotted paths, and translates them to the proper Hibernate paths and joins. A big
	 * bundle of hair is growing on this code. Take heed changing it!
	 * <p>A dotted path reaching some final property on some part of a persistent class tree must
	 * be interpreted in multiple ways, depending on the parts of the tree that are reached. All cases only
	 * resolve the part <i>before</i> the last name in the path. So a dotted path like <i>address.city</i> will
	 * resolve the "address" into a subcritera (see below) and return the name "city" as a property name to resolve
	 * on the Address persistent class.
	 *
	 * <p>The possibilities for different resolutions are:
	 * <dl>
	 *	<dt>Parent relations<dt>
	 *	<dd>When a parent relation is encountered directly on some persistent class it implies that a
	 *		<i>join</i> is to be done with that persistent class. If the property is non-null(1) an inner
	 *		join will be generated; if a property is optional it will generate an outer join to it. In Hibernate
	 *		terms: this will create another Criteria on that property, and that criteria instance should be used
	 *		to append criterions on that other table. If such a relation property is used more than once (because
	 *		multiple restrictions are placed on the joined table) we <i>must</i> reuse the same sub-Criteria
	 *		instance initially created to prevent multiple joins to that same table.</dd>
	 *	<dt>Compound primary keys</dt>
	 *	<dd>These are the hardest. A compound PK usually consists of "simple" fields and "parent" relation types.
	 *	The "simple" fields are usually of basic types like numbers or strings, and are present in the root table
	 *	only, forming the unique part of that root type's primary key. The "parent" relation properties in the PK
	 *	represent the parent entities that are part of the identifying relation for the root table.
	 *	<p>The problem is that depending on what is reached we either need to specify a join, or we just reach "deeper"
	 *	inside the compound key structure, reaching a property that is ultimately still a field in the root table.
	 *	</p>
	 *	<p>When parsing a PK structure deeply we usually have properties that alternate between pk fields and relation
	 *	fields. Only when this path touches a non-PK field need we create a subcriteria to the reached tables. In all
	 *	other cases, if the final property reaches a primary key field of an identifying PK somewhere in the root
	 *	table the column representing that field is part of that root table, so no joins are wanted.</p>
	 * </dd>
	 *
	 * </dl>
	 * Notes:
	 * <p>(1): the nullity of a property should be directly derived off the datamodel. If it is changed from the
	 * datamodel's definition then the resulting join will not be technically correct: it follows the override,
	 * not the actual definition from the data model.</p>
	 */
	private String parseSubcriteria(String input) {
		m_subCriteria = null;
		m_inputPath = input;

		/*
		 * We are going to traverse all names and determine the reached type. When a relation type
		 * is encountered it's put on the "pending relation stack". The next name will determine
		 * what to do with that relation:
		 * - if there is NO next field the relation itself is meant. No join should be added.
		 * - if the next field is a non-PK field then the relation MUST be joined in using a
		 *   subcriterion, and the next field will take off from there
		 * - if the next field is a PK field we delay the join by keeping the relation stacked
		 *   and we follow the PK field, maintaining the subpath. If the entire path is always
		 *   either a parent relation or a PK field then we are staying in the /same/ record, never
		 *   leaving it's PK. In that case this will return the entire dotted path to that PK field,
		 *   for use by Hibernate.
		 * - if the next field is NOT a pk we know that we must join fully all relations stacked.
		 */
		Class< ? > currentClass = m_rootClass; // The current class reached by the property; start @ the root entity
		String path = null; // The full path currently reached
		String subpath = null; // The subpath reached from the last PK association change, if applicable
		int ix = 0;
		final int len = input.length();
		m_pendingJoinIx = 0;
		boolean last = false;
		boolean inpk = false; // Not following a PK path
		boolean previspk = false;
		while(!last) {
			//-- Get next name.
			int pos = input.indexOf('.', ix); // Move to the NEXT dot,
			String name;
			if(pos == -1) {
				//-- QUICK EXIT: if the entire name has no dots quit immediately with the input.
				if(ix == 0)
					return input;

				//-- Get the last name fragment.
				name = input.substring(ix);
				ix = len;
				last = true;
			} else {
				name = input.substring(ix, pos);
				ix = pos + 1;
			}

			//-- Create the path and the subpath by adding the current name.
			path = path == null ? name : path + "." + name; // Full dotted path to the currently reached name
			subpath = subpath == null ? name : subpath + "." + name; // Partial dotted path (from the last relation) to the currently reached name

			//-- Get the property metadata and the reached class.
			PropertyMetaModel pmm = MetaManager.getPropertyMeta(currentClass, name);
			if(pmm.isPrimaryKey()) {
				if(previspk)
					throw new IllegalStateException("Pk field immediately after PK field - don't know what this is!?");

				inpk = true;
				previspk = true;
				pushPendingJoin(path, pmm);
				currentClass = pmm.getActualType();
			} else if(pmm.getRelationType() != PropertyRelationType.NONE) {
				/*
				 * This is a relation. If we are NOT in a PK currently AND there are relations queued then
				 * we are sure that the queued ones must be joined, so flush as a simple join.
				 */
				//-- This is a relation type. If another relation was queued flush it: we always need a join.
				if(m_pendingJoinIx > 0 && !previspk) {
					flushJoin();
					inpk = false;
				}

				//-- Now queue this one- we decide whether to join @ the next name.
				pushPendingJoin(path, pmm);
				currentClass = pmm.getActualType();
				previspk = false;
			} else if(!last)
				throw new QQuerySyntaxException("Property " + subpath + " in path " + input + " must be a parent relation or a compound primary key (property=" + pmm + ")");
			else
				pushPendingJoin(path, pmm); // Push the last segment too, even though it is not a join!!
		}

		/*
		 * We have reached the last field, and it's meta is on the stack. Always remove it there (because it will never
		 * be joined). Then see if data is left on the stack and handle that.
		 */
		if(m_pendingJoinIx <= 0)
			throw new IllegalStateException("Logic failure"); // Cannot happen
//		PropertyMetaModel pmm = m_pendingJoinProps[--m_pendingJoinIx];
//		path = m_pendingJoinPaths[m_pendingJoinIx];
//		String	name = pmm.getName();	// This will be the name of the property to return if the stack is empty

		//-- In all cases: we just need the qualified subname to this property, because all needed joins have been done already.
		m_sb.setLength(0);
		for(int i = 0; i < m_pendingJoinIx; i++) {
			if(m_sb.length() > 0)
				m_sb.append('.');
			m_sb.append(m_pendingJoinProps[i].getName());
		}
		String name = m_sb.toString();
		return name;
	}

	/**
	 * Flush everything on the join stack and create the pertinent joins. The stack
	 * is guaranteed to end in a RELATION property, but it can have PK fragments in
	 * between. Those fragments are all part of a single record (the one that has
	 * that PK) and should not be "joined". Instead the entire subpath leading to
	 * that first relation that "exits" that record will be used as a path for the
	 * subcriterion.
	 */
	private void flushJoin() {
		//-- Create the join path upto and including till the last relation (subpath from last criterion to it).
		m_sb.setLength(0);
		PropertyMetaModel pmm = null;
		for(int i = 0; i < m_pendingJoinIx; i++) {
			pmm = m_pendingJoinProps[i];
			if(m_sb.length() != 0)
				m_sb.append('.');
			m_sb.append(pmm.getName());
		}
		String subpath = m_sb.toString(); // This leads to the relation;

		//-- Now create/retrieve the subcriterion to that relation
		String path = m_pendingJoinPaths[m_pendingJoinIx - 1]; // The full path to this relation,
		CritEntry	ce = m_subCriteriaMap.get(path);	// Is a criteria entry present already?
		if(ce != null) {
			m_subCriteria = ce.getAbomination();		// Obtain cached version
		} else {
			//-- We need to create this join.
			int joinType = pmm.isRequired() ? CriteriaSpecification.INNER_JOIN : CriteriaSpecification.LEFT_JOIN;

			if(m_subCriteria == null) { // Current is the ROOT criteria?
				m_subCriteria = createSubCriteria(m_currentCriteria, subpath, joinType);
			} else {
				//-- Create a new version on the current subcriterion (multi join)
				m_subCriteria = createSubCriteria(m_subCriteria, subpath, joinType);
			}

			//-- Cache this so later paths refer to the same subcriteria
			if(m_subCriteriaMap == Collections.EMPTY_MAP)
				m_subCriteriaMap = new HashMap<String, CritEntry>();
			m_subCriteriaMap.put(path, new CritEntry(pmm.getActualType(), m_subCriteria));
		}
		m_pendingJoinIx = 0;
	}

	private void dumpStateError(String string) {
		throw new IllegalStateException(string);
	}

	/**
	 * Push a pending join or PK fragment on the TODO stack.
	 * @param path
	 * @param pmm
	 */
	private void pushPendingJoin(String path, PropertyMetaModel pmm) {
		if(m_pendingJoinIx >= m_pendingJoinPaths.length)
			throw new QQuerySyntaxException("The property path " + m_inputPath + " is too complex");
		m_pendingJoinPaths[m_pendingJoinIx] = path;
		m_pendingJoinProps[m_pendingJoinIx++] = pmm;
	}

//	/**
//	 * Flush all entries on the pending relation stack. This stack should ALWAYS contain
//	 * relation and PK entries ajacent, i.e. a relation item ALWAYS follows a PK item and
//	 * a PK item always follows a relation item.
//	 *
//	 * If this stack holds a PK path it ALWAYS starts with a PK
//	 *
//	 */
//	private void flushPendingRelationJoins() {
//		int joinType = pmm.isRequired() ? CriteriaSpecification.INNER_JOIN : CriteriaSpecification.LEFT_JOIN;
//
//		if(m_subCriteria == null) { // Current is the ROOT criteria?
//			m_subCriteria = createSubCriteria(m_currentCriteria, subpath, joinType);
//		} else {
//			//-- Create a new version on the current subcriterion (multi join)
//			m_subCriteria = createSubCriteria(m_subCriteria, subpath, joinType);
//		}
//
//		//-- Store this subcriteria so that other paths will use the same one
//		if(m_subCriteriaMap == Collections.EMPTY_MAP)
//			m_subCriteriaMap = new HashMap<String, CritEntry>();
//		currentClass = pmm.getActualType();
//		m_subCriteriaMap.put(path, new CritEntry(currentClass, m_subCriteria));
//		subpath = null; // Restart subpath @ next property.
//	}

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
		// jal 20100122 FIXME Remove to allow combinatories on subchildren; needs to be revisited when join/logic is formalized.
		//		if(c1 == null)
		//			throw new IllegalStateException("? Odd multi - no members?!");
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
			throw new IllegalStateException("Cannot find child's parent property in crufty Hibernate metadata: " + keyCols);
		String childupprop = childmd.getPropertyNames()[fkindex];

		//-- Well, that was it. What a sheitfest. Add the join condition to the parent
		String parentAlias = getParentAlias();
		dc.add(Restrictions.eqProperty(childupprop + "." + childmd.getIdentifierPropertyName(), parentAlias + "." + parentmd.getIdentifierPropertyName()));

		//-- Sigh; Recursively apply all parts to the detached thingerydoo
		Object old = m_currentCriteria;
		Class< ? > oldroot = m_rootClass;
		m_rootClass = q.getBaseClass();
		m_currentCriteria = dc;
		where.visit(this);
		if(m_last != null) {
			dc.add(m_last);
			m_last = null;
		}
		m_currentCriteria = old;
		m_rootClass = oldroot;
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
	private ProjectionList m_proli;

	private Projection m_lastProj;

	@Override
	public void visitMultiSelection(QMultiSelection n) throws Exception {
		throw new ProgrammerErrorException("multi-operation selections not supported by Hibernate");
	}

	@Override
	public void visitSelection(QSelection< ? > s) throws Exception {
		if(m_proli != null)
			throw new IllegalStateException("? Projection list already initialized??");
		m_rootClass = s.getBaseClass();
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
		throw new ProgrammerErrorException("Unexpected selection item: " + n);
	}

	@Override
	public void visitPropertySelection(QPropertySelection n) throws Exception {
		switch(n.getFunction()){
			default:
				throw new IllegalStateException("Unexpected selection item function: " + n.getFunction());
			case AVG:
				m_lastProj = Projections.avg(n.getProperty());
				break;
			case MAX:
				m_lastProj = Projections.max(n.getProperty());
				break;
			case MIN:
				m_lastProj = Projections.min(n.getProperty());
				break;
			case SUM:
				m_lastProj = Projections.sum(n.getProperty());
				break;
			case COUNT:
				m_lastProj = Projections.count(n.getProperty());
				break;
			case COUNT_DISTINCT:
				m_lastProj = Projections.countDistinct(n.getProperty());
				break;
			case ID:
				m_lastProj = Projections.id();
				break;
			case PROPERTY:
				m_lastProj = Projections.property(n.getProperty());
				break;
			case ROWCOUNT:
				m_lastProj = Projections.rowCount();
				break;
		}
	}

	@Override
	public void visitSelectionSubquery(QSelectionSubquery n) {
		//-- 2. Create an exists subquery; create a sub-statement
		DetachedCriteria dc = DetachedCriteria.forClass(n.getSelectionQuery().getBaseClass(), nextAlias());
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
			throw new IllegalStateException("Cannot find child's parent property in crufty Hibernate metadata: " + keyCols);
		String childupprop = childmd.getPropertyNames()[fkindex];

		//-- Well, that was it. What a sheitfest. Add the join condition to the parent
		String parentAlias = getParentAlias();
		dc.add(Restrictions.eqProperty(childupprop + "." + childmd.getIdentifierPropertyName(), parentAlias + "." + parentmd.getIdentifierPropertyName()));

		//-- Sigh; Recursively apply all parts to the detached thingerydoo
		Object old = m_currentCriteria;
		Class< ? > oldroot = m_rootClass;
		m_rootClass = q.getBaseClass();
		m_currentCriteria = dc;
		where.visit(this);
		if(m_last != null) {
			dc.add(m_last);
			m_last = null;
		}
		m_currentCriteria = old;
		m_rootClass = oldroot;
		m_last = exists;
	}
}
