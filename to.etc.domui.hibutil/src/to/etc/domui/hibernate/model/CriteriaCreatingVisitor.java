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

import java.util.*;
import java.util.concurrent.*;

import javax.annotation.*;

import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.impl.*;
import org.hibernate.metadata.*;
import org.hibernate.persister.collection.*;
import org.hibernate.persister.entity.*;
import org.hibernate.type.*;

import to.etc.domui.component.meta.*;
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
public class CriteriaCreatingVisitor implements QNodeVisitor {
	final private Session m_session;

	/** The topmost Criteria: the one that will be returned to effect the translated query */
	private final Criteria m_rootCriteria;

	/**
	 * This either holds a Criteria or a DetachedCriteria; since these are not related (sigh) we must
	 * use instanceof everywhere. Bad, bad, bad hibernate design.
	 */
	private Object m_currentCriteria;

	private Criterion m_last;

	/** After a SUBSELECT parse, (subquery/comparison against subquery) this contains the DetachedCriteria instance created for that query. */
	private Object m_lastSubqueryCriteria;

	/** The next number to use for generating unique names. */
	private int m_aliasIndex;

	private Class< ? > m_rootClass;

	/**
	 * Maps parent relation dotted paths to the alias created for that path.
	 */
	private Map<String, String> m_aliasMap = new HashMap<String, String>();

	public CriteriaCreatingVisitor(Session ses, final Criteria crit) {
		m_session = ses;
		m_rootCriteria = crit;
		m_currentCriteria = crit;
	}

	/**
	 * Does a check to see if the class is a persistent class- because Hibernate itself is too
	 * bloody stupid to do it. Querying an unknown class in Hibernate will return an empty
	 * result set, sigh.
	 * @param clz
	 * @return
	 */
	public void checkHibernateClass(Class< ? > clz) {
		ClassMetadata childmd = m_session.getSessionFactory().getClassMetadata(clz);
		if(childmd == null)
			throw new IllegalArgumentException("The class " + clz + " is not known by Hibernate as a persistent class");
	}

	/**
	 * Create a new unique alias name.
	 * @return
	 */
	private String nextAlias() {
		return "a_" + (++m_aliasIndex);
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

	@Override
	public void visitRestrictionsBase(QCriteriaQueryBase< ? > n) throws Exception {
		QOperatorNode r = n.getRestrictions();
		if(r == null)
			return;
		QOperatorNode.prune(r);

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
			if(null != m_last)
				addCriterion(m_last);
			m_last = null;
		}

		checkSubqueriesUsed(n);
	}

	private void checkSubqueriesUsed(QCriteriaQueryBase<?> n) {
		if(n.getUnusedSubquerySet().size() > 0) {
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
	public void visitCriteria(final QCriteria< ? > qc) throws Exception {
		checkHibernateClass(qc.getBaseClass());
		m_rootClass = qc.getBaseClass();

		visitRestrictionsBase(qc);
		visitOrderList(qc.getOrder());

		//-- 2. Handle limits and start: applicable to root criterion only
		if(qc.getLimit() > 0)
			m_rootCriteria.setMaxResults(qc.getLimit());
		if(qc.getStart() > 0) {
			m_rootCriteria.setFirstResult(qc.getStart());
		}
		if(qc.getTimeout() > 0)
			m_rootCriteria.setTimeout(qc.getTimeout());

		//-- 3. Handle fetch.
		handleFetch(qc);
	}

	/**
	 * Handle fetch selections.
	 * @param qc
	 */
	private void handleFetch(QCriteriaQueryBase< ? > qc) {
		for(Map.Entry<String, QFetchStrategy> ms : qc.getFetchStrategies().entrySet()) {
			PropertyMetaModel< ? > pmm = MetaManager.findPropertyMeta(m_rootClass, ms.getKey());
			if(null == pmm)
				throw new QQuerySyntaxException("The 'fetch' path '" + ms.getKey() + " does not resolve on class " + m_rootClass);
			if(ms.getValue() == QFetchStrategy.LAZY)
				continue;

			switch(pmm.getRelationType()){
				case DOWN:
					m_rootCriteria.setFetchMode(ms.getKey(), FetchMode.SELECT);
					break;
//					throw new QQuerySyntaxException("The 'fetch' path '" + ms.getKey()
//						+ " is a child relation (list-of-children). Fetch is not yet supported for that because Hibernate will duplicate the master.");

				case UP:
					m_rootCriteria.setFetchMode(ms.getKey(), FetchMode.JOIN);
					break;

				case NONE:
					throw new QQuerySyntaxException("The 'fetch' path '" + ms.getKey() + " is not recognized as a relation property");
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Property path resolution code.						*/
	/*--------------------------------------------------------------*/
	/** Temp array used in parser to decode the properties reached; used to prevent multiple object allocations. */
	private PropertyMetaModel< ? >[] m_pendingJoinProps = new PropertyMetaModel[20];

	private String[] m_pendingJoinPaths = new String[20];

	private int m_pendingJoinIx;

	private String m_inputPath;

	private StringBuilder	m_sb = new StringBuilder();

	private PropertyMetaModel< ? > m_targetProperty;

	/** The current subcriteria's base class. */
	//	private Class< ? > m_subCriteriaClass;

	/**
	 * Parse a dotted path property. This needs to "translate" the dotted path into a query path, and it
	 * needs to generate the proper joins. A big bundle of hair is growing on this code. Take heed changing
	 * it!
	 *
	 * <h1>Path types</h1>
	 * <p>A simple path contains no dots. This references any property in the current object, which can be of
	 * any type. Because this never implies any kind of join we can return the name as-is.</p>
	 *
	 * <h2>Non-PK single relation path</h2>
	 * <p>For now we only support parent relations, i.e. where the relation reaches one record. Child relations
	 * are disabled for now. The old code supported child relations and translated them into "exists" subqueries;
	 * that code is disabled.</p>
	 *
	 * <p>A path like "relation.field" creates an <i>alias</i> "_a0" for relation. This same alias will be used
	 * for <i>all</i> references to direct properties of that relation. The alias represents the actual joined
	 * table for the relation. For a reference of this type the alias is created or obtained from the earlier
	 * time it was created, then this returns "_a0.field" as a property reference on that specific table.</p>
	 *
	 * <h2>Non-PK multi-relation path</h2>
	 * <p>A path like relation1.relation2.field reaches a field in the parent-of-the-parent. It can reach
	 * as high as parents exists. These create joins where each child record joins with it's parent, all the
	 * way up to the last one. For every relation traversed we need an alias. So the following is created:
	 * <ol>
	 *	<li>Create alias "_a0" for path "relation1", the subpath is now "relation2.field" based off "_a0".</li>
	 *	<li>Create alias "_a1" for path "relation1.relation2", subpath is now "field" based off "_a1".</li>
	 * </ol>
	 * Because "field" is a non-relation this ends the code; the returned value is "_a1.field".
	 *
	 * <h2>Primary key paths</h2>
	 * <p>Something special happens however when we are accessing only the PK for the specified relation. For
	 * instance when we query something like "relation.id = 1234". In this case we do <i>not</i> want a join
	 * because the field referred to actually resides inside the root record: it is the FK column of "relation".
	 * This means that when we encounter a relation we need to <i>postpone</i> creation of it's alias until
	 * we know what we are reaching <i>after</i> that relation. If we reach the PK of it we don't need the
	 * join; we just need to return the actual path like "relation.id" for Hibernate.</p>
	 *
	 * <h2>Complex primary key paths</h2>
	 * <p>For paths that reach a PK after traversing more than one relation, like "relation.btwCode.id=12", we
	 * do need to join the first segment, "relation", and create an alias "_a0" for it. We then see that the next
	 * fragment, based off that "_a0" alias refers to the PK of the second relation. That means it does not
	 * need a join; the returned path should be "_a0.btwCode.id".</p>
	 *
	 * <h2>Compound primary keys</h2>
	 * <p>A bigger problem will occur when we are using compound primary keys (identifying relations),
	 * especially when these can also contain relations to objects containing compound primary keys. For example
	 * lets take the following (pretty nonsense) types:
	 * <pre>
	 * Product {
	 *   [id] Long id; // Simple PK, no problems
	 *   String name; // Sample field.
	 * }
	 *
	 * ProductVersionPK {
	 *   [ManyToOne] Product product; // Relation to product as part of the PK
	 *   String name; // The name, part of the PK
	 * }
	 *
	 * ProductVersion {
	 *   [Id] ProductVersionPK id;	// Compound PK!
	 *   String description;	// Whatever
	 * }
	 *
	 * VersionBugPK {
	 *   [ManyToOne] ProductVersion version; // Relation to version as part of PK
	 *   Long bugId; // Id, part of PK
	 * }
	 *
	 * VersionBug {
	 *   [Id] VersionBugPK id;  // Compound PK containing relation to another thingy with a compound PK
	 *   Date reported;
	 * }
	 * </pre>
	 * Using these classes, if we want to load all VersionBugs for a single product we would do something like:
	 * <pre>
	 * QCriteria<VersionBug> q = ....;
	 * q.eq("id.version.id.product", product);
	 * </pre>
	 * The dotted path "id.version.id.product" refers to the "product" relation inside ProductVersionPK. Another way to
	 * do it is:
	 * <pre>
	 * QCriteria<VersionBug> q = ....;
	 * q.eq("id.version.id.product.id", Long.valueOf(120114323));
	 * </pre>
	 * This refers to the actual PK of the Product. Both queries generate the same SQL.</p>
	 *
	 * <p>The problem with this is that although we are traversing multiple parent relations we are <i>not</i> needing
	 * any join here because the fields that are reached at the end are <i>within the root table/class itself</i>. So
	 * the actual path to pass to Hibernate is that full path.</p>
	 *
	 * <p>When we are accessing properties in the relation not part of the PK we do need to generate aliases, but the
	 * paths are different. For instance if we query:
	 * <pre>
	 * QCriteria<VersionBug> q = ....;
	 * q.eq("id.version.id.product.name", "DomUI");
	 * </pre>
	 * We <i>are</i> joining with Product; but in this case we need to generate the aliases differently:
	 * <ol>
	 *	<li>Generate alias "_a0" for path "id.version" referring to ProductVersion</li>
	 *	<li>Generate alias "_a1" for path "id.version.id.product", based from "_a0", referring to Product</li>
	 *	<li>Return the reference "_a1.name" referring to the joined table's NAME field.</li>
	 * </ol>
	 *
	 */

	private String parseSubcriteria(String input) {
		return parseSubcriteria(input, false);
	}

	private String parseSubcriteria(String input, boolean allowDown) {
		m_targetProperty = null;
		m_inputPath = input;
		Class< ? > currentClass = m_rootClass; // The current class reached by the property; start @ the root entity
		String path = null; // The full path currently reached, i.e. "id.version.id.product".
		String subpath = null; // The subpath reached from the last PK association change, i.e. "id.product"
		int ix = 0;
		final int len = input.length();
		m_pendingJoinIx = 0;
		boolean last = false;
		boolean previspk = false;

		String currentAlias = ""; // The last-assigned alias for the last-flushed path.
		while(!last) {
			//-- Get next name.
			int pos = input.indexOf('.', ix); // Move to the NEXT dot,
			String name;
			if(pos == -1) {
				//-- QUICK EXIT: if the entire name has no dots quit immediately with the input.
				if(ix == 0) {
					m_targetProperty = MetaManager.findPropertyMeta(currentClass, input);
					return input;
				}

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
			PropertyMetaModel< ? > pmm = MetaManager.getPropertyMeta(currentClass, name);
			m_targetProperty = pmm;
			if(pmm.isPrimaryKey()) {
				if(previspk)
					throw new IllegalStateException("The path " + subpath + " is a PK property immediately followed by another Pk property- that cannot happen.");

				//-- We have found the "id" field. Assume we don't know what comes after so store this.
				previspk = true;
				pushPendingJoin(path, pmm);

				if(last) {
					/*
					 * This ends in the PK field. This means that the entire stored JOIN path is part of the PK
					 * of the current alias - we may not join. Append the stored join path to the current alias
					 * and return that.
					 */


					//-- We have ended in a PK. All subrelations leading up to this are *part* of the PK of the current subcriterion. We need not join but just have to specify a dotted path.
					StringBuilder sb = sb();
					sb.append(currentAlias); // Add the last alias or the empty string,
					createPendingJoinPath(sb); // Add all properties not yet done
					return sb.toString();
				}
				currentClass = pmm.getActualType();
			} else if(pmm.getRelationType() == PropertyRelationType.DOWN) {
				if(allowDown && last) {
					currentAlias = flushJoin(currentAlias);
					StringBuilder sb = sb();
					sb.append(currentAlias).append('.').append(name);
					return sb.toString();
				}


				if(true) {
					/*
					 * For now, we're not allowing queries on children. The old version translated this into an
					 * "exists" query to prevent row explosion, but that is quite hard to do.
					 * When reimplementing, all references "inside" the subquery must be added to that subquery,
					 * and not result in another subquery. In addition, all combinatories that contain stuff
					 * inside that subquery must properly be added there. That is extremely complex.
					 */
					throw new IllegalStateException("You cannot query a value on a parent->child relation. Use an exists-subquery instead.");
				}

				/*
				 * Downward (childset) relation. This implicitly queries /existence/ of a child record having these
				 * characteristics. This can never be a last item.
				 */
				if(last)
					throw new QQuerySyntaxException("The path '" + path + " reaches a 'list-of-children' (DOWN) property (" + pmm + ")- it is meaningless here");

				/*
				 * Must be a List type, and we must be able to determine the type of the child.
				 */
				if(!List.class.isAssignableFrom(pmm.getActualType()))
					throw new ProgrammerErrorException("The property '" + path + "' should be a list (it is a " + pmm.getActualType() + ")");
				java.lang.reflect.Type coltype = pmm.getGenericActualType();
				if(coltype == null)
					throw new ProgrammerErrorException("The property '" + path + "' has an undeterminable child type");
				Class< ? > childtype = MetaManager.findCollectionType(coltype);

				//-- We are not really joining here; we're just querying. Drop the pending joinset;
				m_pendingJoinIx = 0; // Discard all pending;

				//
				//
				//				/*
				//				 * We need to create a joined "exists" query, or get the "existing" one.
				//				 */
				//				CritEntry ce = m_subCriteriaMap.get(path);
				//				if(ce != null) {
				//					//-- Use this.
				//					m_subCriteria = ce.getAbomination();
				//				} else {
				//					//-- Create a joined subselect
				//					DetachedCriteria dc = createExistsSubquery(childtype, currentClass, subpath);
				//					m_subCriteriaMap.put(path, new CritEntry(childtype, dc));
				//					m_subCriteria = dc;
				//				}
				//
				//				currentClass = childtype;

			} else if(pmm.getRelationType() != PropertyRelationType.NONE) {
				/*
				 * This is a parent relation. If we are NOT in a PK currently AND there are relations queued then
				 * we are sure that the queued ones must be joined, so flush as a simple join.
				 */
				if(m_pendingJoinIx > 0 && !previspk) {
					currentAlias = flushJoin(currentAlias);
				}

				//-- Now queue this one- we decide whether to join @ the next name.
				pushPendingJoin(path, pmm);
				if(last) {
					//-- Last entry is a relation: we do not need to join this last one, just refer to it using it's dotted path also.
					StringBuilder sb = sb();
					sb.append(currentAlias); // Add the last alias or the empty string,
					createPendingJoinPath(sb); // Add all properties not yet done
					return sb.toString();
				}

				currentClass = pmm.getActualType();
				previspk = false;
			} else if(!last)
				throw new QQuerySyntaxException("Property " + subpath + " in path " + input + " must be a parent relation or a compound primary key (property=" + pmm + ")");
			else {
				/*
				 * This is the last part and it is not a PK or relation itself. We need to decide what to do with the
				 * current join stack. If the previous item was a PK we do not join but return the compound path...
				 */
				if(previspk) {
					//-- This is a non-relation property immediately on a PK. Return dotted path.
					pushPendingJoin(path, pmm);
					StringBuilder sb = sb();
					sb.append(currentAlias); // Add the last alias or the empty string,
					createPendingJoinPath(sb); // Add all properties not yet done
					return sb.toString();
				}

				/*
				 * This is a normal property. Make sure a join is present then return the path inside that join.
				 */
				currentAlias = flushJoin(currentAlias);
				StringBuilder sb = sb();
				sb.append(currentAlias).append('.').append(name);
				return sb.toString();
			}
		}

		//-- Failsafe exit: all specific paths should have exited when last was signalled.
		throw new IllegalStateException("Should be unreachable?");
	}

	//	private DetachedCriteria createExistsSubquery(Class< ? > childtype, Class< ? > parentclass, String parentproperty) {
	//		DetachedCriteria dc = DetachedCriteria.forClass(childtype, nextAlias());
	//		Criterion exists = Subqueries.exists(dc);
	//		dc.setProjection(Projections.id()); // Whatever: just some thingy.
	//
	//		//-- Append the join condition; we need all children here that are in the parent's collection. We need the parent reference to use in the child.
	//		ClassMetadata childmd = m_session.getSessionFactory().getClassMetadata(childtype);
	//
	//		//-- Entering the crufty hellhole that is Hibernate meta"data": never seen more horrible cruddy garbage
	//		ClassMetadata parentmd = m_session.getSessionFactory().getClassMetadata(parentclass);
	//		int index = findMoronicPropertyIndexBecauseHibernateIsTooStupidToHaveAPropertyMetaDamnit(parentmd, parentproperty);
	//		if(index == -1)
	//			throw new IllegalStateException("Hibernate does not know property");
	//		Type type = parentmd.getPropertyTypes()[index];
	//		BagType bt = (BagType) type;
	//		final OneToManyPersister persister = (OneToManyPersister) ((SessionFactoryImpl) m_session.getSessionFactory()).getCollectionPersister(bt.getRole());
	//		String[] keyCols = persister.getKeyColumnNames();
	//
	//		//-- Try to locate those FK column names in the FK table so we can fucking locate the mapping property.
	//		int fkindex = findCruddyChildProperty(childmd, keyCols);
	//		if(fkindex < 0)
	//			throw new IllegalStateException("Cannot find child's parent property in crufty Hibernate metadata: " + keyCols);
	//		String childupprop = childmd.getPropertyNames()[fkindex];
	//
	//		//-- Well, that was it. What a sheitfest. Add the join condition to the parent
	//		String parentAlias = getParentAlias();
	//		dc.add(Restrictions.eqProperty(childupprop + "." + childmd.getIdentifierPropertyName(), parentAlias + "." + parentmd.getIdentifierPropertyName()));
	//
	//		addCriterion(exists);
	//		return dc;
	//	}

	private StringBuilder sb() {
		m_sb.setLength(0);
		return m_sb;
	}

	/**
	 * Append all property names to the path.
	 * @return
	 */
	private void createPendingJoinPath(StringBuilder sb) {
		for(int i = 0; i < m_pendingJoinIx; i++) {
			if(sb.length() > 0)
				sb.append('.');
			sb.append(m_pendingJoinProps[i].getName());
		}
	}

	/**
	 * Flush everything on the join stack and create the pertinent joins. The stack
	 * is guaranteed to end in a RELATION property, but it can have PK fragments in
	 * between. Those fragments are all part of a single record (the one that has
	 * that PK) and should not be "joined". Instead the entire subpath leading to
	 * that first relation that "exits" that record will be used as a path for the
	 * subcriterion.
	 */
	private String flushJoin(String rootAlias) {
		if(m_pendingJoinIx <= 0)
			throw new IllegalStateException("No join pending??");

		//-- Create the join path upto and including till the last relation (subpath from last criterion to it).
		m_sb.setLength(0);
		PropertyMetaModel< ? > pmm = null;
		for(int i = 0; i < m_pendingJoinIx; i++) {
			pmm = m_pendingJoinProps[i];
			if(m_sb.length() != 0)
				m_sb.append('.');
			m_sb.append(pmm.getName());
		}
		String subpath = m_sb.toString(); // This leads to the relation from the LAST alias (relative path)
		String path = m_pendingJoinPaths[m_pendingJoinIx - 1]; // The full path to this relation from the root class,
		String alias = getPathAlias(rootAlias, path, subpath, pmm);
		m_pendingJoinIx = 0;
		return alias;
	}

	/**
	 *
	 * @param rootAlias		The current alias which starts off this last segment, or "" if we start from root object.
	 * @param fullpath		The root object absolute path, i.e. the input up to the current level including the relation property
	 * @param relativepath	The relative path from the rootAlias.
	 * @param pmm
	 * @return
	 */
	private String getPathAlias(String rootAlias, String fullpath, String relativepath, PropertyMetaModel< ? > pmm) {
		String alias = m_aliasMap.get(fullpath); // Path is already known?
		if(null != alias)
			return alias;

		//-- This is new... Create the alias and refer it off the previous root alias,
		String nextAlias = nextAlias();
		String aliasedPath = relativepath;
		if(rootAlias.length() > 0)
			aliasedPath = rootAlias + "." + relativepath;

		m_aliasMap.put(fullpath, nextAlias);

		//-- We need to create this join.
		int joinType = pmm.isRequired() ? CriteriaSpecification.INNER_JOIN : CriteriaSpecification.LEFT_JOIN;
		if(m_currentCriteria instanceof Criteria) {
			((Criteria) m_currentCriteria).createAlias(aliasedPath, nextAlias, joinType); // Crapfest
		} else if(m_currentCriteria instanceof DetachedCriteria) {
			((DetachedCriteria) m_currentCriteria).createAlias(aliasedPath, nextAlias, joinType);
		} else
			throw new IllegalStateException("Unexpected current thing: " + m_currentCriteria);
		return nextAlias;
	}


	//	private void dumpStateError(String string) {
	//		throw new IllegalStateException(string);
	//	}

	/**
	 * Push a pending join or PK fragment on the TODO stack.
	 * @param path
	 * @param pmm
	 */
	private void pushPendingJoin(String path, PropertyMetaModel< ? > pmm) {
		if(m_pendingJoinIx >= m_pendingJoinPaths.length)
			throw new QQuerySyntaxException("The property path " + m_inputPath + " is too complex");
		m_pendingJoinPaths[m_pendingJoinIx] = path;
		m_pendingJoinProps[m_pendingJoinIx++] = pmm;
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
				handleLikeOperation(name, m_targetProperty, lit.getValue());
				return;
			case ILIKE:
				last = Restrictions.ilike(name, lit.getValue());
				break;
		}

		m_last = last;
	}

	@Override
	public void visitPropertyIn(@Nonnull QPropertyIn n) throws Exception {
		QOperatorNode rhs = n.getExpr();
		String name = n.getProperty();
		QLiteral lit = null;
		if(rhs.getOperation() == QOperation.LITERAL) {
			Object litval = ((QLiteral) rhs).getValue();
			if(litval instanceof List) {
				//-- If prop refers to some relation (dotted pair):
				name = parseSubcriteria(name);
				m_last = Restrictions.in(name, (List<Object>) litval);
				return;
			} else {
				throw new QQuerySyntaxException("Unexpected value for 'in' operation: " + litval+", should be List or subquery");
			}
		} else if(rhs.getOperation() == QOperation.SELECTION_SUBQUERY) {
			QSelectionSubquery qsq = (QSelectionSubquery) n.getExpr();
			qsq.visit(this); 										// Resolve subquery
			String fullName = parseSubcriteria(n.getProperty());	// Handle dotted pair in name
			m_last = Subqueries.propertyIn(fullName, (DetachedCriteria) m_lastSubqueryCriteria);
			return;
		} else
			throw new IllegalStateException("Unknown operands to " + n.getOperation() + ": " + name + " and " + rhs.getOperation());
	}

	private void handleLikeOperation(String name, PropertyMetaModel< ? > pmm, Object value) throws Exception {
		//-- Check if there is a type mismatch in parameter type...
		if(!(value instanceof String))
			throw new QQuerySyntaxException("The argument to 'like' must be a string (and cannot be null), the value passed is: " + value);

		if(pmm == null || pmm.getActualType() == String.class) {
			m_last = Restrictions.like(name, value);
			return;
		}

		ClassMetadata hibmd = m_session.getSessionFactory().getClassMetadata(pmm.getClassModel().getActualClass());
		if(null == hibmd)
			throw new QQuerySyntaxException("Cannot obtain Hibernate metadata for property=" + pmm);

		if(!(hibmd instanceof AbstractEntityPersister))
			throw new QQuerySyntaxException("Cannot obtain Hibernate metadata for property=" + pmm + ": expecting AbstractEntityPersister, got a " + hibmd.getClass());
		AbstractEntityPersister aep = (AbstractEntityPersister) hibmd;
		String[] colar = getPropertyColumnNamesFromLousyMetadata(aep, name);
		if(colar.length != 1)
			throw new IllegalStateException("Attempt to do a 'like' on a multi-column property: " + pmm);
		String columnName = colar[0];
		int dotix = name.lastIndexOf('.');
		if(dotix == -1) {
			//-- We need Hibernate metadata to find the column name....
			m_last = Restrictions.sqlRestriction("{alias}." + columnName + " like ?", value, Hibernate.STRING);
			return;
		}

		String sql = "{" + name + "} like ?";
		m_last = new HibernateAliasedSqlCriterion(sql, value, Hibernate.STRING);
	}

	/**
	 * Hibernate's jokish metadata does not include the PK in it's properties structures. So
	 * we explicitly need to check if the name is the PK property, then return the column names
	 * for that PK.
	 *
	 * @param aep
	 * @param compoundName
	 * @return
	 */
	@Nonnull
	private String[] getPropertyColumnNamesFromLousyMetadata(AbstractEntityPersister aep, String compoundName) {
		String name = compoundName;
		int dotix = compoundName.lastIndexOf('.');
		if(dotix != -1) {
			name = compoundName.substring(dotix + 1);
		}

		//-- The PK property is not part of the "properties" in hibernate's idiot metadata. So first check if we're looking at that ID property.
		if(name.equals(aep.getIdentifierPropertyName())) {
			return aep.getIdentifierColumnNames();
		}
		int ix = aep.getPropertyIndex(name);
		if(ix < 0)
			throw new QQuerySyntaxException("Cannot obtain Hibernate metadata for property=" + name + ": property index not found");
		String[] colar = aep.getPropertyColumnNames(ix);
		if(colar == null || colar.length != 1/* || colar[0] == null*/)
			throw new QQuerySyntaxException("'Like' cannot be done on multicolumn/0column property " + name);
		return colar;
	}


	private void handlePropertySubcriteriaComparison(QPropertyComparison n) throws Exception {
		QSelectionSubquery qsq = (QSelectionSubquery) n.getExpr();
		qsq.visit(this); // Resolve subquery
		String name = parseSubcriteria(n.getProperty()); // Handle dotted pair in name
		Criterion last = null;

		switch(n.getOperation()){
			default:
				throw new IllegalStateException("Unexpected operation: " + n.getOperation());

			case EQ:
				last = Subqueries.propertyIn(name, (DetachedCriteria) m_lastSubqueryCriteria);
				break;
			case NE:
				last = Subqueries.propertyNotIn(name, (DetachedCriteria) m_lastSubqueryCriteria);
				break;
//			case GT:
//				last = Restrictions.gt(name, lit.getValue());
//				break;
//			case GE:
//				last = Restrictions.ge(name, lit.getValue());
//				break;
//			case LT:
//				last = Restrictions.lt(name, lit.getValue());
//				break;
//			case LE:
//				last = Restrictions.le(name, lit.getValue());
//				break;
//			case LIKE:
//				last = Restrictions.like(name, lit.getValue());
//				break;
//			case ILIKE:
//				last = Restrictions.ilike(name, lit.getValue());
//				break;
		}
		m_last = last;
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
		m_last = Restrictions.between(name, a.getValue(), b.getValue()); // jal 20101228 was p.getProp() instead of name, which seems to be wrong.
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
			case NOT:
				n.getNode().visit(this);
				m_last = Restrictions.not(m_last);
				return;
		}
		throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
	}

	@Override
	public void visitSqlRestriction(@Nonnull QSqlRestriction v) throws Exception {
		if(v.getParameters().length == 0) {
			m_last = Restrictions.sqlRestriction(v.getSql());
			return;
		}

		//-- Parameterized SQL query -> convert to Hibernate types.
		Type[] htar = new Type[v.getParameters().length];
		for(int i = 0; i < v.getTypes().length; i++) {
			Class< ? > c = v.getTypes()[i];
			if(c == null)
				throw new QQuerySyntaxException("Type array for SQLRestriction cannot contain null");
			org.hibernate.TypeHelper th = m_session.getTypeHelper();

			Type t = th.basic(c.getName());
			if(null == t) {
				throw new QQuerySyntaxException("Type[" + i + "] in type array (a " + c + ") is not a proper Hibernate type");

			}
			htar[i] = t;
		}
		m_last = Restrictions.sqlRestriction(v.getSql(), v.getParameters(), htar);
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
		String parentAlias = getCurrentAlias();
		Class< ? > parentBaseClass = q.getParentQuery().getBaseClass();
		PropertyMetaModel< ? > pmm = MetaManager.getPropertyMeta(parentBaseClass, q.getParentProperty());

		//-- If we have a dotted name it can only be parent.parent.parent.childList like (with multiple parents). Parse all parents.
		String childListProperty = q.getParentProperty();
		int ldot = childListProperty.lastIndexOf('.');
		if(ldot != -1) {
			//-- Join all parents, and get the last parent's reference and name
			String last = parseSubcriteria(childListProperty, true);		// Create the join path;
			String parentpath = childListProperty.substring(0, ldot);		// This now holds parent.parent.parent
			childListProperty = childListProperty.substring(ldot + 1);		// And this childList

			//-- We need a "new" parent class: the class that actually contains the "child" list...
			PropertyMetaModel< ? > parentpm = MetaManager.getPropertyMeta(parentBaseClass, parentpath);
			parentBaseClass = parentpm.getActualType();

			//-- The above join will have created another alias to the joined table; this is the first part of the "last" reference (which is alias.property).
			ldot = last.indexOf('.');
			if(ldot < 0)
				throw new IllegalStateException("Invalid result from parseSubcriteria inside exists.");
			parentAlias = last.substring(0, ldot);
		}

		//-- Should be List type
		if(!List.class.isAssignableFrom(pmm.getActualType()))
			throw new ProgrammerErrorException("The property '" + q.getParentQuery().getBaseClass() + "." + q.getParentProperty() + "' should be a list (it is a " + pmm.getActualType() + ")");

		//-- Make sure there is a where condition to restrict
		QOperatorNode where = q.getRestrictions();
//		if(where == null)
//			throw new ProgrammerErrorException("exists subquery has no restrictions: " + this);

		//-- Get the list's generic compound type because we're unable to get it from Hibernate easily.
		Class< ? > coltype = MetaManager.findCollectionType(pmm.getGenericActualType());
		if(coltype == null)
			throw new ProgrammerErrorException("The property '" + q.getParentQuery().getBaseClass() + "." + q.getParentProperty() + "' has an undeterminable child type");

		//-- 2. Create an exists subquery; create a sub-statement
		DetachedCriteria dc = DetachedCriteria.forClass(coltype, nextAlias());
		Criterion exists = Subqueries.exists(dc);
		dc.setProjection(Projections.id());									// Whatever: just some thingy.

		//-- Append the join condition; we need all children here that are in the parent's collection. We need the parent reference to use in the child.
		ClassMetadata childmd = m_session.getSessionFactory().getClassMetadata(coltype);

		//-- Entering the crofty hellhole that is Hibernate meta"data" 8-(


		ClassMetadata parentmd = m_session.getSessionFactory().getClassMetadata(parentBaseClass);
		int index = findMoronicPropertyIndexBecauseHibernateIsTooStupidToHaveAPropertyMetaDamnit(parentmd, childListProperty);
		if(index == -1)
			throw new IllegalStateException("Hibernate does not know property '" + childListProperty + " in " + parentmd.getEntityName());
		Type type = parentmd.getPropertyTypes()[index];
		CollectionType bt = (CollectionType) type;
		final OneToManyPersister persister = (OneToManyPersister) ((SessionFactoryImpl) m_session.getSessionFactory()).getCollectionPersister(bt.getRole());
		String[] keyCols = persister.getKeyColumnNames();

		//-- Try to locate those FK column names in the FK table so we can fucking locate the mapping property.
		String childupprop = findCruddyChildProperty(childmd, keyCols);
		if(childupprop == null)
			throw new IllegalStateException("Cannot find child's parent property in crufty Hibernate metadata: " + Arrays.toString(keyCols));

		//-- Well, that was it. What a sheitfest. Add the join condition to the parent
		dc.add(Restrictions.eqProperty(childupprop + "." + childmd.getIdentifierPropertyName(), parentAlias + "." + parentmd.getIdentifierPropertyName()));

		//-- Sigh; Recursively apply all parts to the detached thingerydoo
		Object old = m_currentCriteria;
		Class< ? > oldroot = m_rootClass;
		Map<String, String> oldAliases = m_aliasMap;
		m_aliasMap = new HashMap<String, String>();

		m_rootClass = q.getBaseClass();
		checkHibernateClass(m_rootClass);
		m_currentCriteria = dc;
		if(where != null)
			where.visit(this);
		if(m_last != null) {
			dc.add(m_last);
			m_last = null;
		}
		m_aliasMap = oldAliases;
		m_currentCriteria = old;
		m_rootClass = oldroot;
		m_last = exists;
	}

	private String getCurrentAlias() {
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
	private String findCruddyChildProperty(ClassMetadata cm, String[] keyCols) {
		SingleTableEntityPersister fuckup = (SingleTableEntityPersister) cm;
		for(int i = fuckup.getPropertyNames().length; --i >= 0;) {
			String[] cols = fuckup.getPropertyColumnNames(i);
			if(Arrays.equals(keyCols, cols)) {
				return cm.getPropertyNames()[i];
			}
		}

		/*
		 * The identifier property is fully separate from all other properties because that
		 * makes it hard to use, of course. So explicitly check for a full identifying relation
		 * initially.
		 */
		String idname = fuckup.getIdentifierPropertyName();
		String[] cols = fuckup.getIdentifierColumnNames();
		if(Arrays.equals(keyCols, cols)) {
			return idname;
		}

		/*
		 * The ID property can be compound, in that case we need to handle it's
		 * component properties separately. This code is wrong because it only
		 * handles one level of indirection - but that is enough for me now, this
		 * is horrible. The proper way of implementing is to recursively determine
		 * the smallest property accessing the columns specified in this call, and
		 * to determine it's full path.
		 */
		Type idtype = fuckup.getIdentifierType();
		if(idtype instanceof ComponentType) {
			ComponentType ct = (ComponentType) idtype;

			//			for(int scp = 0; scp < fuckup.countSubclassProperties(); scp++) { There's no end to the incredible mess.
			//				String scpn = fuckup.getSubclassPropertyName(scp);
			//				cols = fuckup.getSubclassPropertyColumnNames(scpn);
			//				System.out.println("prop: " + scpn + ", cols=" + Arrays.toString(cols));
			//			}
			//

			String[] xx = fuckup.getSubclassPropertyColumnNames(idname);
			String[] cpnar = ct.getPropertyNames();
			for(int i = 0; i < cpnar.length; i++) {
				String pname = cpnar[i];
				cols = fuckup.getSubclassPropertyColumnNames(idname + "." + pname);
				if(Arrays.equals(keyCols, cols)) {
					return idname + "." + pname;
				}
			}
		}

		//-- All has failed- mapping unknown.
		return null;
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

	private String m_parentAlias;

	@Override
	public void visitMultiSelection(QMultiSelection n) throws Exception {
		throw new ProgrammerErrorException("multi-operation selections not supported by Hibernate");
	}

	@Override
	public void visitSelection(QSelection< ? > s) throws Exception {
		if(m_proli != null)
			throw new IllegalStateException("? Projection list already initialized??");
		checkHibernateClass(s.getBaseClass());
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

		//-- 3. Handle fetch.
		handleFetch(s);

	}

	@Override
	public void visitSelectionColumn(QSelectionColumn n) throws Exception {
		n.getItem().visit(this);
		if(m_lastProj != null)
			m_proli.add(m_lastProj);
	}

	@Override
	public void visitSelectionItem(QSelectionItem n) throws Exception {
		throw new ProgrammerErrorException("Unexpected selection item: " + n);
	}

	@Override
	public void visitPropertySelection(QPropertySelection n) throws Exception {
		String name = parseSubcriteria(n.getProperty());

		switch(n.getFunction()){
			default:
				throw new IllegalStateException("Unexpected selection item function: " + n.getFunction());
			case AVG:
				m_lastProj = Projections.avg(name);
				break;
			case MAX:
				m_lastProj = Projections.max(name);
				break;
			case MIN:
				m_lastProj = Projections.min(name);
				break;
			case SUM:
				m_lastProj = Projections.sum(name);
				break;
			case COUNT:
				m_lastProj = Projections.count(name);
				break;
			case COUNT_DISTINCT:
				m_lastProj = Projections.countDistinct(name);
				break;
			case ID:
				m_lastProj = Projections.id();
				break;
			case PROPERTY:
				m_lastProj = Projections.groupProperty(name);
				break;
			case ROWCOUNT:
				m_lastProj = Projections.rowCount();
				break;
			case DISTINCT:
				m_lastProj = Projections.distinct(Projections.property(name));
				break;
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
	public void visitSubquery(@Nonnull final QSubQuery< ? , ? > n) throws Exception {
		n.getParent().internalUseQuery(n);
		visitSelection(n);
//
//
//		DetachedCriteria dc = DetachedCriteria.forClass(n.getBaseClass(), nextAlias());
//		recurseSubquery(dc, n, new Callable<Void>() {
//			@Override
//			public Void call() throws Exception {
//				visitSelection(n);
//
//				return null;
//			}
//		});
	}

	/**
	 * Render a non-correlated subquery (the subquery has no references to the parent). This is legacy as
	 * it should be the same as correlated.
	 * @see to.etc.webapp.query.QNodeVisitor#visitSelectionSubquery(to.etc.webapp.query.QSelectionSubquery)
	 */
	@Override
	public void visitSelectionSubquery(@Nonnull final QSelectionSubquery n) throws Exception {
		DetachedCriteria dc = DetachedCriteria.forClass(n.getSelectionQuery().getBaseClass(), nextAlias());
		recurseSubquery(dc, n.getSelectionQuery(), new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				n.getSelectionQuery().visit(CriteriaCreatingVisitor.this);
				return null;
			}
		});
	}

	/**
	 * Save the whole current state, then recurse a subquery.
	 * @param dc
	 * @param n
	 * @throws Exception
	 */
	private void recurseSubquery(@Nonnull DetachedCriteria dc, @Nonnull QSelection< ? > n, Callable<Void> callable) throws Exception {
		//-- Recursively apply all parts to the detached thingerydoo
		ProjectionList oldpro = m_proli;
		m_proli = null;
		Projection oldlastproj = m_lastProj;
		m_lastProj = null;
		Object oldCriteria = m_currentCriteria;
		Class< ? > oldroot = m_rootClass;
		Map<String, String> oldAliases = m_aliasMap;
		m_aliasMap = new HashMap<String, String>();
		String oldParentAlias = m_parentAlias;

		//-- Set new clean state for the subselect.
		m_parentAlias = getCurrentAlias();
		m_rootClass = n.getBaseClass();
		checkHibernateClass(m_rootClass);
		m_currentCriteria = dc;
		callable.call();
		if(m_last != null) {
			dc.add(m_last);
			m_last = null;
		}
		m_currentCriteria = oldCriteria; // Restore root query
		m_rootClass = oldroot;
		m_proli = oldpro;
		m_lastProj = oldlastproj;
		m_lastSubqueryCriteria = dc;
		m_aliasMap = oldAliases;
		m_parentAlias = oldParentAlias;
	}

	@Override
	public void visitPropertyJoinComparison(@Nonnull QPropertyJoinComparison comparison) throws Exception {
		String alias = m_parentAlias + "." + parseSubcriteria(comparison.getParentProperty());
		switch(comparison.getOperation()) {
			default:
				throw new QQuerySyntaxException("Unsupported parent-join operation: "+comparison.getOperation());

			case EQ:
				m_last = Restrictions.eqProperty(alias, comparison.getSubProperty());
				break;

			case NE:
				m_last = Restrictions.neProperty(alias, comparison.getSubProperty());
				break;

			case LT:
				m_last = Restrictions.ltProperty(alias, comparison.getSubProperty());
				break;

			case LE:
				m_last = Restrictions.leProperty(alias, comparison.getSubProperty());
				break;

			case GT:
				m_last = Restrictions.gtProperty(alias, comparison.getSubProperty());
				break;

			case GE:
				m_last = Restrictions.geProperty(alias, comparison.getSubProperty());
				break;
		}
	}

	@Override
	public void visitOrderList(@Nonnull List<QOrder> orderlist) throws Exception {
		for(QOrder o : orderlist)
			o.visit(this);
	}

	public void visitSelectionColumns(@Nonnull QSelection< ? > s) throws Exception {
		for(@Nonnull QSelectionColumn col : s.getColumnList())
			col.visit(this);
	}
}
