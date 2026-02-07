/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.hibernate.model;

import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EntityType;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.criteria.JpaOrder;
import org.hibernate.query.criteria.JpaPredicate;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.qsql.QQuerySyntaxException;
import to.etc.webapp.query.QBetweenNode;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QCriteriaQueryBase;
import to.etc.webapp.query.QExistsSubquery;
import to.etc.webapp.query.QFetchStrategy;
import to.etc.webapp.query.QLiteral;
import to.etc.webapp.query.QMultiNode;
import to.etc.webapp.query.QMultiSelection;
import to.etc.webapp.query.QNodeVisitor;
import to.etc.webapp.query.QOperation;
import to.etc.webapp.query.QOperatorNode;
import to.etc.webapp.query.QOrder;
import to.etc.webapp.query.QPropertyComparison;
import to.etc.webapp.query.QPropertyIn;
import to.etc.webapp.query.QPropertyJoinComparison;
import to.etc.webapp.query.QPropertySelection;
import to.etc.webapp.query.QSelection;
import to.etc.webapp.query.QSelectionColumn;
import to.etc.webapp.query.QSelectionItem;
import to.etc.webapp.query.QSelectionSubquery;
import to.etc.webapp.query.QSortOrderDirection;
import to.etc.webapp.query.QSqlRestriction;
import to.etc.webapp.query.QSubQuery;
import to.etc.webapp.query.QUnaryNode;
import to.etc.webapp.query.QUnaryProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Thingy which creates a Hibernate Criteria thingy from a generic query. This is harder than
 * it looks because the Criteria and DetachedCriteria kludge and Hibernate's metadata dungheap
 * makes generic work very complex and error-prone.
 *
 * <p>It might be a better idea to start generating SQL from here, using Hibernate internal code
 * to instantiate the query's result only.</p>
 * <p>
 * Please look a <a href="http://bugzilla.etc.to/show_bug.cgi?id=640">Bug 640</a> for more details, and see
 * the wiki page http://info.etc.to/xwiki/bin/view/Main/UIAbstractDatabase for more details
 * on the working of all this.
 * <p>
 * https://docs.hibernate.org/orm/6.0/migration-guide/#_jakarta_persistence
 * https://docs.hibernate.org/orm/7.0/migration-guide/#jpa-32
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class CriteriaCreatingVisitor<T> implements QNodeVisitor {
	final private Session m_session;

	private final HibernateCriteriaBuilder m_criteriaBuilder;

	/**
	 * The topmost query: the one that will be returned to effect the translated query
	 */
	private final CriteriaQuery<?> m_topQuery;

	/**
	 * The JPA root item, i.e. the class we query.
	 */
	private final Root<?> m_topRoot;

	/**
	 * This either holds a Criteria or a DetachedCriteria; since these are not related (sigh) we must
	 * use instanceof everywhere. Bad, bad, bad hibernate design.
	 */
	private AbstractQuery<?> m_currentQuery;

	private Root<?> m_currentRoot;

	/**
	 * When inside a subquery, holds the parent query's root so that join comparisons
	 * can resolve parent properties against the correct root.
	 */
	@Nullable
	private Root<?> m_parentRoot;

	private JpaPredicate m_last;

	/**
	 * After a SUBSELECT parse, (subquery/comparison against subquery) this contains the Subquery instance created for that query.
	 */
	@Nullable
	private Subquery<?> m_lastSubqueryCriteria;

	/**
	 * When set before creating a subquery, determines the JPA type parameter for the Subquery.
	 * This is needed because Hibernate's semantic validation requires compatible types
	 * between the subquery result and the property it's compared against.
	 */
	@Nullable
	private Class<?> m_expectedSubqueryType;

	/**
	 * The next number to use for generating unique names.
	 */
	private int m_aliasIndex;

	private Class<?> m_rootClass;

	/**
	 * Maps parent relation dotted paths to the alias created for that path.
	 */
	private Map<String, String> m_aliasMap = new HashMap<String, String>();

	public CriteriaCreatingVisitor(Session ses, HibernateCriteriaBuilder criteriaBuilder, final CriteriaQuery<T> crit, QCriteriaQueryBase<?, ?> qc) {
		m_session = ses;
		m_criteriaBuilder = criteriaBuilder;
		m_topQuery = crit;
		m_currentQuery = crit;
		m_topRoot = crit.from(qc.getBaseClass());
		m_currentRoot = m_topRoot;
	}

	/**
	 * Does a check to see if the class is a persistent class- because Hibernate itself is too
	 * bloody stupid to do it. Querying an unknown class in Hibernate will return an empty
	 * result set, sigh.
	 */
	public void checkHibernateClass(Class<?> clz) {
		EntityType<?> entity = m_session.getSessionFactory().getMetamodel().entity(clz);
		if(null == entity)
			throw new IllegalArgumentException("The class " + clz + " is not known by Hibernate as a persistent class");
	}

	/**
	 * Create a new unique alias name.
	 */
	private String nextAlias() {
		return "a_" + (++m_aliasIndex);
	}

	private void addOrder(jakarta.persistence.criteria.Order c) {
		if(m_currentQuery instanceof CriteriaQuery<?> cc) {
			List<jakarta.persistence.criteria.Order> orderList = new ArrayList<>(cc.getOrderList());
			orderList.add(c);
			m_currentQuery = cc.orderBy(orderList);
		} else {
			throw new QQuerySyntaxException("Cannot add order to a subquery!");
		}
	}

	@Override
	public void visitRestrictionsBase(QCriteriaQueryBase<?, ?> n) throws Exception {
		QOperatorNode r = n.getRestrictions();
		if(r == null)
			return;
		QOperatorNode.prune(r);
		r.visit(this);
		JpaPredicate last = m_last;
		if(null != last) {
			m_currentQuery.where(last);
		}

		checkSubqueriesUsed(n);
	}

	private void checkSubqueriesUsed(QCriteriaQueryBase<?, ?> n) {
		if(!n.getUnusedSubquerySet().isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("There are ").append(n.getUnusedSubquerySet().size()).append(" subqueries that are not linked (used) in the main query!\n");
			int i = 1;
			for(QSubQuery<?, ?> subQuery : n.getUnusedSubquerySet()) {
				sb.append(i).append(": ").append(subQuery.toString()).append("\n");
				i++;
			}
			throw new QQuerySyntaxException(sb.toString());
		}
	}

	@Override
	public void visitCriteria(final QCriteria<?> qc) throws Exception {
		checkHibernateClass(qc.getBaseClass());
		m_rootClass = qc.getBaseClass();

		visitRestrictionsBase(qc);
		visitOrderList(qc.getOrder());

		//-- 3. Handle fetch.
		handleFetch(qc);
	}

	/**
	 * Handle fetch selections.
	 */
	private void handleFetch(QCriteriaQueryBase<?, ?> qc) {
		for(Map.Entry<String, QFetchStrategy> ms : qc.getFetchStrategies().entrySet()) {
			PropertyMetaModel<?> pmm = MetaManager.findPropertyMeta(m_rootClass, ms.getKey());
			if(null == pmm)
				throw new QQuerySyntaxException("The 'fetch' path '" + ms.getKey() + " does not resolve on class " + m_rootClass);
			if(ms.getValue() == QFetchStrategy.LAZY)
				continue;

			// QTODO - implement fetch modes
			//switch(pmm.getRelationType()){
			//	case DOWN:
			//		m_rootCriteria.setFetchMode(ms.getKey(), FetchMode.SELECT);
			//		break;
			//
			//	case UP:
			//		m_rootCriteria.setFetchMode(ms.getKey(), FetchMode.JOIN);
			//		break;
			//
			//	case NONE:
			//		throw new QQuerySyntaxException("The 'fetch' path '" + ms.getKey() + " is not recognized as a relation property");
			//}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Property path resolution code.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Parse a property path, starting from the root entity. This returns a JPA Path,
	 * and checks that all parts are indeed n -> 1 parts.
	 */
	private <V> Path<V> parsePropertyPath(String input) throws Exception {
		return parsePropertyPathFrom(m_currentRoot, input);
	}

	@SuppressWarnings("unchecked")
	private <V> Path<V> parsePropertyPathFrom(Path<?> root, String input) throws Exception {
		Path<?> currentPath = root;
		for(String segment : input.split("\\.")) {
			Path<?> nextPath = currentPath.get(segment);
			if(nextPath == null)
				throw new QQuerySyntaxException("The property path " + input + " refers to unknown property " + segment);
			currentPath = nextPath;
		}
		return (Path<V>) currentPath;
	}

	@Override
	public void visitPropertyComparison(QPropertyComparison n) throws Exception {
		QOperatorNode rhs = n.getExpr();
		String name = n.getProperty();
		QLiteral lit = null;
		if(rhs.getOperation() == QOperation.LITERAL) {
			lit = (QLiteral) rhs;
		} else if(rhs.getOperation() == QOperation.SELECTION_SUBQUERY) {
			handlePropertySubcriteriaComparison(n);
			return;
		} else
			throw new IllegalStateException("Unknown operands to " + n.getOperation() + ": " + name + " and " + rhs.getOperation());

		//-- If prop refers to some relation (dotted pair):
		switch(n.getOperation()) {
			default:
				throw new IllegalStateException("Unexpected operation: " + n.getOperation());

			case EQ:
				if(lit.getValue() == null) {
					m_last = m_criteriaBuilder.isNull(parsePropertyPath(name));
					break;
				}
				m_last = m_criteriaBuilder.equal(parsePropertyPath(name), lit.getValue());
				break;
			case NE:
				if(lit.getValue() == null) {
					m_last = m_criteriaBuilder.isNotNull(parsePropertyPath(name));
					break;
				}
				m_last = m_criteriaBuilder.notEqual(parsePropertyPath(name), lit.getValue());
				break;
			case GT:
				m_last = m_criteriaBuilder.greaterThan(parsePropertyPath(name), (Comparable<Object>) lit.getValue());
				break;
			case GE:
				m_last = m_criteriaBuilder.greaterThanOrEqualTo(parsePropertyPath(name), (Comparable<Object>) lit.getValue());
				break;
			case LT:
				m_last = m_criteriaBuilder.lessThan(parsePropertyPath(name), (Comparable<Object>) lit.getValue());
				break;
			case LE:
				m_last = m_criteriaBuilder.lessThanOrEqualTo(parsePropertyPath(name), (Comparable<Object>) lit.getValue());
				break;
			case LIKE:
				handleLikeOperation(name, lit.getValue());
				return;
			case ILIKE:
				m_last = m_criteriaBuilder.ilike(parsePropertyPath(name), (String) lit.getValue());
				break;
		}
	}

	@Override
	public void visitPropertyIn(@NonNull QPropertyIn n) throws Exception {
		QOperatorNode rhs = n.getExpr();
		String name = n.getProperty();
		QLiteral lit = null;
		if(rhs.getOperation() == QOperation.LITERAL) {
			Object litval = ((QLiteral) rhs).getValue();
			if(litval instanceof Collection<?> co) {
				//-- If prop refers to some relation (dotted pair):
				m_last = m_criteriaBuilder.in(parsePropertyPath(n.getProperty()), co);
				return;
			} else {
				throw new QQuerySyntaxException("Unexpected value for 'in' operation: " + litval + ", should be Collection or subquery");
			}
		} else if(rhs.getOperation() == QOperation.SELECTION_SUBQUERY) {
			// QTODO - implement subquery IN
			throw new NotImplementedException("in subquery not implemented yet");
			//QSelectionSubquery qsq = (QSelectionSubquery) n.getExpr();
			//qsq.visit(this);                                        // Resolve subquery
			//String fullName = parseSubcriteria(n.getProperty());    // Handle dotted pair in name
			//m_last = Subqueries.propertyIn(fullName, (DetachedCriteria) m_lastSubqueryCriteria);
			//return;
		} else
			throw new IllegalStateException("Unknown operands to " + n.getOperation() + ": " + name + " and " + rhs.getOperation());
	}

	private void handleLikeOperation(String propertyPathString, Object value) throws Exception {
		//-- Check if there is a type mismatch in parameter type...
		if(!(value instanceof String stringValue))
			throw new QQuerySyntaxException("The argument to 'like' must be a string (and cannot be null), the value passed is: " + value);

		PropertyMetaModel<?> pmm = MetaManager.getPropertyMeta(m_rootClass, propertyPathString);
		if(pmm.getActualType() == String.class) {
			m_last = m_criteriaBuilder.like(parsePropertyPath(propertyPathString), stringValue);
			return;
		}

		//-- Non-string property: cast to String using JPA cast(), then use native like()
		@SuppressWarnings("unchecked")
		JpaExpression<?> path = (JpaExpression<?>) parsePropertyPath(propertyPathString);
		JpaExpression<String> asString = m_criteriaBuilder.cast(path, String.class);
		m_last = m_criteriaBuilder.like(asString, stringValue);
	}

	private void handlePropertySubcriteriaComparison(QPropertyComparison n) throws Exception {
		QSelectionSubquery qsq = (QSelectionSubquery) n.getExpr();

		//-- Determine the expected return type from the property being compared
		Path<?> propPath = parsePropertyPath(n.getProperty());
		m_expectedSubqueryType = propPath.getJavaType();
		qsq.visit(this);                                            // Resolve subquery, sets m_lastSubqueryCriteria
		m_expectedSubqueryType = null;
		Subquery<?> subquery = m_lastSubqueryCriteria;
		if(null == subquery)
			throw new QQuerySyntaxException("Subquery was not created during visit of " + qsq);

		switch(n.getOperation()) {
			default:
				throw new IllegalStateException("Unexpected operation: " + n.getOperation());
			case EQ:
				m_last = m_criteriaBuilder.equal(parsePropertyPath(n.getProperty()), subquery);
				break;
			case NE:
				m_last = m_criteriaBuilder.notEqual(parsePropertyPath(n.getProperty()), subquery);
				break;
		}
	}

	@Override
	public void visitBetween(final QBetweenNode n) throws Exception {
		if(n.getA().getOperation() != QOperation.LITERAL || n.getB().getOperation() != QOperation.LITERAL)
			throw new IllegalStateException("Expecting literals as 2nd and 3rd between parameter");
		QLiteral a = (QLiteral) n.getA();
		QLiteral b = (QLiteral) n.getB();

		//-- If prop refers to some relation (dotted pair):
		m_last = m_criteriaBuilder.between(parsePropertyPath(n.getProp()), (Comparable) a.getValue(), (Comparable) b.getValue());
	}

	/**
	 * Compound. Ands and ors.
	 */
	@Override
	public void visitMulti(final QMultiNode inn) throws Exception {
		//-- Walk all members, create nodes from 'm.
		List<Predicate> list = new ArrayList<>(inn.getChildren().size());
		for(QOperatorNode on : inn.getChildren()) {
			on.visit(this); // Convert node to Criterion thingydoodle
			if(m_last != null) {
				list.add(m_last);
				m_last = null;
			}
		}

		switch(inn.getOperation()) {
			default:
				throw new IllegalStateException("Unexpected operation: " + inn.getOperation());
			case AND:
				m_last = m_criteriaBuilder.and(list);
				break;
			case OR:
				m_last = m_criteriaBuilder.or(list);
				break;
		}
	}

	@Override
	public void visitOrder(final QOrder o) throws Exception {
		Path<Object> path = parsePropertyPath(o.getProperty());
		JpaOrder jpaOrder = QSortOrderDirection.ASC == o.getDirection() ? m_criteriaBuilder.asc(path) : m_criteriaBuilder.desc(path);
		addOrder(jpaOrder);
	}

	@Override
	public void visitUnaryNode(final QUnaryNode n) throws Exception {
		switch(n.getOperation()) {
			default:
				throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());

			case SQL:
				if(n.getNode() instanceof QLiteral l) {
					String s = (String) l.getValue();
					JpaExpression<Boolean> expr = m_criteriaBuilder.sql(s, Boolean.class);
					m_last = m_criteriaBuilder.isTrue(expr);
					return;
				}
				break;

			case NOT:
				n.getNode().visit(this);
				m_last = m_criteriaBuilder.not(m_last);
				return;
		}
		throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
	}

	@Override
	public void visitSqlRestriction(@NonNull QSqlRestriction v) throws Exception {
		Expression<?>[] args = new Expression<?>[v.getParameters().length];
		for(int i = 0; i < v.getParameters().length; i++) {
			args[i] = m_criteriaBuilder.literal(v.getParameters()[i]);
		}
		JpaExpression<Boolean> expr = m_criteriaBuilder.sql(v.getSql(), Boolean.class, args);
		m_last = m_criteriaBuilder.isTrue(expr);
	}

	@Override
	public void visitUnaryProperty(final QUnaryProperty n) throws Exception {
		String name = n.getProperty();
		switch(n.getOperation()) {
			default:
				throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());

			case ISNOTNULL:
				m_last = m_criteriaBuilder.isNotNull(parsePropertyPath(name));
				break;
			case ISNULL:
				m_last = m_criteriaBuilder.isNull(parsePropertyPath(name));
				break;
		}
	}

	@Override
	public void visitLiteral(final QLiteral n) throws Exception {
		throw new IllegalStateException("? Unexpected literal: " + n);
	}

	/**
	 * Result of analyzing an exists path, containing all information needed to build the subquery and join condition.
	 *
	 * @param parentPrefix The path from the parent root to the entity that owns the child list (empty string if direct child)
	 * @param mappedBy The mappedBy property in the child entity that points back to its parent
	 * @param childClass The actual child class (type of items in the child list) - use this instead of q.getBaseClass()
	 *                   because refactorToSubExistsIfNeeded may have changed the path without updating baseClass
	 */
	private record ExistsPathInfo(String parentPrefix, String mappedBy, Class<?> childClass) {}

	/**
	 * Analyzes the path from parent to child and returns the information needed to build the join condition.
	 * The path can contain ManyToOne relations (parent navigation) followed by exactly one OneToMany relation
	 * (the child list). Multiple OneToMany relations in the path should have been refactored into nested
	 * EXISTS subqueries by {@link #refactorToSubExistsIfNeeded(QExistsSubquery)}.
	 */
	private ExistsPathInfo analyzeExistsPath(Class<?> parentClass, String parentToChildPath) {
		SessionFactoryImplementor sfi = m_session.getSessionFactory().unwrap(SessionFactoryImplementor.class);
		StringBuilder parentPrefix = new StringBuilder();
		Class<?> currentClass = parentClass;

		String[] segments = parentToChildPath.split("\\.");
		for(int i = 0; i < segments.length; i++) {
			String segment = segments[i];
			EntityPersister entityDescriptor = sfi.getMappingMetamodel().getEntityDescriptor(currentClass);
			AttributeMapping attributeMapping = entityDescriptor.findAttributeMapping(segment);
			if(null == attributeMapping)
				throw new QQuerySyntaxException("Invalid path '" + parentToChildPath + "': unknown property '" + segment + "' in class " + currentClass.getSimpleName());

			if(attributeMapping instanceof PluralAttributeMapping pa) {
				// This is a child list (OneToMany/ManyToMany) - should be the last segment
				if(i != segments.length - 1) {
					// Multiple child lists in path - this should have been refactored by refactorToSubExistsIfNeeded
					throw new QQuerySyntaxException("Invalid path '" + parentToChildPath + "': found child list property '" + segment
						+ "' but it's not the last segment. Multiple child lists should be refactored into nested EXISTS.");
				}
				String mappedBy = pa.getCollectionDescriptor().getMappedByProperty();
				if(mappedBy == null) {
					throw new QQuerySyntaxException("Invalid path '" + parentToChildPath + "': child list property '" + segment
						+ "' does not have a mappedBy - only bidirectional relations are supported for EXISTS subqueries.");
				}
				Class<?> childClass = pa.getElementDescriptor().getJavaType().getJavaTypeClass();
				return new ExistsPathInfo(parentPrefix.toString(), mappedBy, childClass);
			} else {
				// This is a parent relation (ManyToOne) or simple property - add to prefix and continue
				if(!parentPrefix.isEmpty()) {
					parentPrefix.append(".");
				}
				parentPrefix.append(segment);
				// Get the target class for the next iteration
				currentClass = attributeMapping.getJavaType().getJavaTypeClass();
			}
		}
		throw new QQuerySyntaxException("Invalid path '" + parentToChildPath + "': path must end with a child list property (OneToMany/ManyToMany)");
	}

	/**
	 * Child-related subquery: determine existence of children having certain characteristics. Because
	 * the worthless Hibernate "metamodel" API and the utterly disgusting way that mapping data is
	 * "stored" in Hibernate we resort to getting the generic type of the child property's collection
	 * to determine the type where the subquery is executed on.
	 *
	 * See https://vladmihalcea.com/exists-subqueries-jpa-hibernate/
	 * See Hibernate7JpaQueriesTest as an example source.
	 */
	@Override
	public <S> void visitExistsSubquery(QExistsSubquery<S> q) throws Exception {
		refactorToSubExistsIfNeeded(q);

		//-- Save current context
		Root<?> previousRoot = m_currentRoot;
		AbstractQuery<?> previousQuery = m_currentQuery;

		/*
		 * Analyze the path FIRST, before creating the subquery. This gives us:
		 * - The correct child class to query (important when refactorToSubExistsIfNeeded changed the path)
		 * - The parent prefix path for the join condition
		 * - The mappedBy property for the join condition
		 *
		 * We use pathInfo.childClass() instead of q.getBaseClass() because refactorToSubExistsIfNeeded
		 * may have modified the path without updating baseClass.
		 */
		ExistsPathInfo pathInfo = analyzeExistsPath(previousRoot.getJavaType(), q.getParentProperty());

		//-- Create a SubQuery using the correct child class from path analysis
		Subquery<Integer> subquery = m_currentQuery.subquery(Integer.class).select(m_criteriaBuilder.literal(1));
		Root<?> subRoot = subquery.from(pathInfo.childClass());

		//-- Swap to subquery context
		m_currentRoot = subRoot;
		m_currentQuery = subquery;

		q.getRestrictions().visit(this);                            // Calculate the basic criteria for the exists
		JpaPredicate existsCriteria = m_last;

		/*
		 * Form the join criterion. The path may contain ManyToOne relations (parent navigation)
		 * before the final OneToMany (child list).
		 * For example: "customer.invoices" where customer is ManyToOne and invoices is OneToMany.
		 *
		 * We build a join like: previousRoot.parentPrefix = subRoot.mappedBy
		 * For "customer.invoices": order.customer = invoice.customer
		 * For "albums": artist = album.artist (parentPrefix is empty)
		 */
		String parentPrefix = pathInfo.parentPrefix();
		String childMappedBy = pathInfo.mappedBy();

		//-- Build the parent side of the join (previousRoot or previousRoot.parentPrefix)
		Path<?> parentSide;
		if(parentPrefix.isEmpty()) {
			parentSide = previousRoot;
		} else {
			parentSide = previousRoot;
			for(String segment : parentPrefix.split("\\.")) {
				parentSide = parentSide.get(segment);
			}
		}

		//-- Build the child side of the join (subRoot.mappedBy)
		Path<?> childSide = subRoot.get(childMappedBy);

		//-- Add the join condition now.
		JpaPredicate joinPredicate = m_criteriaBuilder.equal(parentSide, childSide);
		subquery.where(existsCriteria, joinPredicate);
		m_last = m_criteriaBuilder.exists(subquery);
		m_currentRoot = previousRoot;
		m_currentQuery = previousQuery;
	}

	/**
	 * In case that we specify exists sub query with multiple lists on exists sub query property path,
	 * we refactor ongoing exists into 2 expressions. Current one we modify to just path until first encountered list property,
	 * and from the rest of the path we add new exists as subexpression of current one.
	 *
	 * For example, path "albumList.trackList" with baseClass=Track becomes:
	 * - Outer: path="albumList" (baseClass is ignored, we use analyzeExistsPath to get the correct class)
	 * - Inner: path="trackList", baseClass=Track (the original baseClass)
	 */
	private void refactorToSubExistsIfNeeded(QExistsSubquery<?> q) {
		//-- If we have a dotted name it can only be parent.parent.parent.childList like (with multiple parents). Parse all parents.
		String existsSubqueryPropertyPath = q.getParentProperty();
		if(existsSubqueryPropertyPath.indexOf('.') > -1) {
			Class<?> parentBaseClass = q.getParentQuery().getBaseClass();
			Triple<String, Class<?>, String> headClassAndTail = splitAfterFirstList(parentBaseClass, existsSubqueryPropertyPath);
			String headListExpression = headClassAndTail.getLeft();
			Class<?> actualTypeOfFirstList = headClassAndTail.getMiddle();
			String tailListExpression = headClassAndTail.getRight();
			if(null != actualTypeOfFirstList && null != tailListExpression) {
				QOperatorNode restrictions = q.getRestrictions();
				// The inner exists has the original baseClass (q.getBaseClass()) since it queries the final child type
				// The parent of the inner exists is actualTypeOfFirstList (the intermediate type)
				QCriteria<?> newParent = QCriteria.create(actualTypeOfFirstList);
				QExistsSubquery<?> subExistsQuery = new QExistsSubquery(newParent, q.getBaseClass(), tailListExpression);
				subExistsQuery.setRestrictions(restrictions);
				q.setRestrictions(subExistsQuery);
				q.setParentProperty(headListExpression);
			}
		}
	}

	private Triple<String, Class<?>, String> splitAfterFirstList(Class<?> currentClass, String input) {
		String path = null; // The full path currently reached, i.e. "id.version.id.product".
		int ix = 0;
		final int len = input.length();
		while(ix < len) {
			int pos = input.indexOf('.', ix); // Move to the NEXT dot,
			String name;
			if(pos == -1) {
				return Triple.of(input, null, null);
			} else {
				name = input.substring(ix, pos);
				ix = pos + 1;
			}
			path = path == null ? name : path + "." + name; // Full dotted path to the currently reached name

			PropertyMetaModel<?> pmm = MetaManager.getPropertyMeta(currentClass, name);
			if(null == pmm) {
				throw new IllegalStateException("Unable to resolve pmm from " + currentClass + ", property " + name);
			}
			if(List.class.isAssignableFrom(pmm.getActualType())) {
				java.lang.reflect.Type coltype = pmm.getGenericActualType();
				if(coltype == null)
					throw new ProgrammerErrorException("The property '" + path + "' has an undeterminable child type");
				Class<?> childtype = MetaManager.findCollectionType(coltype);
				return Triple.of(path, childtype, ix < len ? input.substring(ix) : null);
			} else {
				currentClass = pmm.getActualType();
			}
		}
		throw new IllegalStateException("Should not be possible to get here!?");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Selection translation to JPA Selections.			*/
	/*--------------------------------------------------------------*/
	private List<Selection<?>> m_selectionList = new ArrayList<>();

	private List<Expression<?>> m_groupByList = new ArrayList<>();

	@Nullable
	private Selection<?> m_lastSelection;

	@Override
	public void visitMultiSelection(QMultiSelection n) throws Exception {
		throw new ProgrammerErrorException("multi-operation selections not supported by Hibernate");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void visitSelection(QSelection<?> s) throws Exception {
		if(!m_selectionList.isEmpty())
			throw new IllegalStateException("Selections list is already used??");
		checkHibernateClass(s.getBaseClass());
		m_rootClass = s.getBaseClass();

		visitSelectionColumns(s);						// Append all selections to the selectionsList
		if(m_selectionList.isEmpty())
			throw new QQuerySyntaxException("No items to select in selection query");

		if(m_currentQuery instanceof CriteriaQuery<?> cq) {
			CompoundSelection<Object[]> array = m_criteriaBuilder.array(m_selectionList);
			((CriteriaQuery<Object[]>) cq).select(array);
		} else if(m_currentQuery instanceof Subquery<?> sq) {
			if(m_selectionList.size() != 1)
				throw new QQuerySyntaxException("Subquery must have exactly one selection column, but found " + m_selectionList.size());
			((Subquery) sq).select((Expression) m_selectionList.get(0));
		}
		m_currentQuery.groupBy(m_groupByList);

		visitRestrictionsBase(s);
		visitOrderList(s.getOrder());

		//-- 3. Handle fetch.
		handleFetch(s);
		m_selectionList.clear();
		m_groupByList.clear();
	}

	@Override
	public void visitSelectionColumn(QSelectionColumn n) throws Exception {
		m_lastSelection = null;
		n.getItem().visit(this);
		Selection<?> lastSelection = m_lastSelection;
		if(null != lastSelection)
			m_selectionList.add(lastSelection);
	}

	@Override
	public void visitSelectionItem(QSelectionItem n) throws Exception {
		throw new ProgrammerErrorException("Unexpected selection item: " + n);
	}

	@Override
	public void visitPropertySelection(QPropertySelection n) throws Exception {
		Path<?> path = parsePropertyPath(n.getProperty());
		switch(n.getFunction()) {
			default:
				throw new IllegalStateException("Unexpected selection item function: " + n.getFunction());
			case AVG:
				m_lastSelection = m_criteriaBuilder.avg(parsePropertyPath(n.getProperty()));
				break;
			case MAX:
				m_lastSelection = m_criteriaBuilder.max(parsePropertyPath(n.getProperty()));
				break;
			case MIN:
				m_lastSelection = m_criteriaBuilder.min(parsePropertyPath(n.getProperty()));
				break;
			case SUM:
				m_lastSelection = m_criteriaBuilder.sum(parsePropertyPath(n.getProperty()));
				break;
			case COUNT:
				m_lastSelection = m_criteriaBuilder.count(parsePropertyPath(n.getProperty()));
				break;
			case COUNT_DISTINCT:
				m_lastSelection = m_criteriaBuilder.countDistinct(parsePropertyPath(n.getProperty()));
				break;
			case ID:
				m_lastSelection = m_criteriaBuilder.id(parsePropertyPath(n.getProperty()));
				m_groupByList.add(parsePropertyPath(n.getProperty()));
				break;
			case PROPERTY:
				m_lastSelection = parsePropertyPath(n.getProperty());
				m_groupByList.add(parsePropertyPath(n.getProperty()));
				break;
			case ROWCOUNT:
				m_lastSelection = m_criteriaBuilder.count();
				break;
			case DISTINCT:
				throw new IllegalStateException("Not implemented yet: distinct selection");
				//m_lastSelection = m_criteriaBuilder.distinct(m_criteriaBuilder.property(parsePropertyPath(n.getProperty())));
				//break;
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Subqueries (correlated/uncorrelated).				*/
	/*--------------------------------------------------------------*/

	/**
	 * This handles rendering of a correlated subquery.
	 *
	 * @see to.etc.webapp.query.QNodeVisitor#visitSubquery(to.etc.webapp.query.QSubQuery)
	 */
	@Override
	public void visitSubquery(@NonNull final QSubQuery<?, ?> n) throws Exception {
		n.getParent().internalUseQuery(n);
		visitSelection(n);
	}

	/**
	 * Render a non-correlated subquery (the subquery has no references to the parent). This is legacy as
	 * it should be the same as correlated.
	 */
	@Override
	public void visitSelectionSubquery(@NonNull final QSelectionSubquery n) throws Exception {
		recurseSubquery(n.getSelectionQuery(), () -> {
			n.getSelectionQuery().visit(this);
			return null;
		});
	}

	/**
	 * Save the whole current state, then recurse a subquery. After the callable
	 * executes, the created subquery is stored in {@link #m_lastSubqueryCriteria}
	 * so it can be used for property comparisons.
	 */
	private void recurseSubquery(@NonNull QSelection<?> selection, Callable<Void> callable) throws Exception {
		List<Expression<?>> oldGroupBy = m_groupByList;
		m_groupByList = new ArrayList<>();
		List<Selection<?>> oldSelList = m_selectionList;
		m_selectionList = new ArrayList<>();

		Root<?> previousRoot = m_currentRoot;
		Root<?> previousParentRoot = m_parentRoot;
		AbstractQuery<?> previousQuery = m_currentQuery;
		Class<?> oldRootClass = m_rootClass;

		//-- Set new clean state for the subselect.
		m_rootClass = selection.getBaseClass();
		checkHibernateClass(m_rootClass);

		Class<?> subqueryType = m_expectedSubqueryType != null ? m_expectedSubqueryType : Object.class;
		Subquery<?> subquery = m_currentQuery.subquery(subqueryType);
		Root<?> subRoot = subquery.from(selection.getBaseClass());

		m_currentQuery = subquery;
		m_currentRoot = subRoot;
		m_parentRoot = previousRoot;
		callable.call();
		m_lastSubqueryCriteria = subquery;

		m_currentQuery = previousQuery;
		m_currentRoot = previousRoot;
		m_parentRoot = previousParentRoot;
		m_rootClass = oldRootClass;
		m_selectionList = oldSelList;
		m_groupByList = oldGroupBy;
	}

	@Override
	public void visitPropertyJoinComparison(@NonNull QPropertyJoinComparison comparison) throws Exception {
		//-- Parent property resolves against the parent root, sub property against current (subquery) root.
		Path<?> parentPath = parsePropertyPathFrom(m_parentRoot != null ? m_parentRoot : m_currentRoot, comparison.getParentProperty());
		Path<?> subPath = parsePropertyPath(comparison.getSubProperty());

		switch(comparison.getOperation()) {
			default:
				throw new QQuerySyntaxException("Unsupported parent-join operation: " + comparison.getOperation());
			case EQ:
				m_last = m_criteriaBuilder.equal(parentPath, subPath);
				break;
			case NE:
				m_last = m_criteriaBuilder.notEqual(parentPath, subPath);
				break;
			case LT:
				m_last = m_criteriaBuilder.lessThan((Expression<Comparable>) (Expression<?>) parentPath, (Expression<Comparable>) (Expression<?>) subPath);
				break;
			case LE:
				m_last = m_criteriaBuilder.lessThanOrEqualTo((Expression<Comparable>) (Expression<?>) parentPath, (Expression<Comparable>) (Expression<?>) subPath);
				break;
			case GT:
				m_last = m_criteriaBuilder.greaterThan((Expression<Comparable>) (Expression<?>) parentPath, (Expression<Comparable>) (Expression<?>) subPath);
				break;
			case GE:
				m_last = m_criteriaBuilder.greaterThanOrEqualTo((Expression<Comparable>) (Expression<?>) parentPath, (Expression<Comparable>) (Expression<?>) subPath);
				break;
		}
	}

	@Override
	public void visitOrderList(@NonNull List<QOrder> orderlist) throws Exception {
		for(QOrder o : orderlist)
			o.visit(this);
	}

	public void visitSelectionColumns(@NonNull QSelection<?> s) throws Exception {
		for(@NonNull QSelectionColumn col : s.getColumnList())
			col.visit(this);
	}
}
