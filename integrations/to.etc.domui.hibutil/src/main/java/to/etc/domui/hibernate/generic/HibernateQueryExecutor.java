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
package to.etc.domui.hibernate.generic;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionImpl;
import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.mapping.EntityValuedModelPart;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.query.Query;
import to.etc.domui.hibernate.model.GenericHibernateHandler;
import to.etc.webapp.query.ICriteriaTableDef;
import to.etc.webapp.query.IQueryExecutor;
import to.etc.webapp.query.IQueryExecutorFactory;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QSelection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This handler knows how to execute Hibernate queries using a basic Hibernate context.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 29, 2010
 */
public class HibernateQueryExecutor implements IQueryExecutor<BuggyHibernateBaseContext>, IQueryExecutorFactory {
	static public final IQueryExecutorFactory FACTORY = new HibernateQueryExecutor();

	protected HibernateQueryExecutor() {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IQAlternateContextFactory implementation.			*/
	/*--------------------------------------------------------------*/
	@Override
	public IQueryExecutor<?> findContextHandler(QDataContext root, ICriteriaTableDef<?> tableMeta) {
		return null; // Never acceptable
	}

	@Override
	public IQueryExecutor<?> findContextHandler(QDataContext root, Class<?> clz) {
		if(clz == null)
			return null;

		//-- Accept anything.
		return this;
	}

	@Override
	public IQueryExecutor<?> findContextHandler(QDataContext root, Object recordInstance) {
		return recordInstance == null ? null : this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IAbstractQueryHandler implementation.				*/
	/*--------------------------------------------------------------*/

	/**
	 * Delete the record passed. After removing, detach any loaded child
	 * entities that were not handled by JPA cascade, to prevent Hibernate 7.x
	 * TransientPropertyValueException when relying on database ON DELETE CASCADE.
	 */
	@Override
	public void delete(BuggyHibernateBaseContext root, Object o) throws Exception {
		Session session = root.getSession();
		session.remove(o);
		Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		detachLoadedChildren(session, o, visited);
		detachInboundReferences(session, o, visited);
	}

	/**
	 * Recursively detach loaded child entities from initialized collections.
	 * After session.remove(parent), children handled by JPA cascade are already
	 * REMOVED and session.contains() returns false for them, so only children
	 * without JPA cascade (still MANAGED) get detached.
	 *
	 * For uninitialized collections (children loaded via a separate query, not
	 * through the collection getter), we scan the persistence context for
	 * managed entities whose back-reference points to the deleted entity.
	 */
	private void detachLoadedChildren(Session session, Object entity, Set<Object> visited) {
		if(!visited.add(entity))
			return;
		Class<?> entityClass = Hibernate.getClass(entity);
		SessionFactoryImplementor sfi = session.getSessionFactory().unwrap(SessionFactoryImplementor.class);
		EntityPersister persister = sfi.getMappingMetamodel().getEntityDescriptor(entityClass);
		var attributeMappings = persister.getAttributeMappings();
		for(int i = 0; i < attributeMappings.size(); i++) {
			AttributeMapping attr = attributeMappings.get(i);
			if(attr instanceof PluralAttributeMapping pluralAttr) {
				Object collectionValue = attr.getPropertyAccess().getGetter().get(entity);
				if(collectionValue instanceof Collection<?> coll && Hibernate.isInitialized(coll)) {
					for(Object child : new ArrayList<>(coll)) {
						if(session.contains(child)) {
							detachLoadedChildren(session, child, visited);
							session.detach(child);
						}
					}
				} else {
					// Collection not initialized: children may still be in the persistence
					// context (loaded via a separate query). Scan for them using mappedBy.
					detachOrphanedChildrenFromPersistenceContext(session, entity, pluralAttr, visited);
				}
			} else if(attr instanceof EntityValuedModelPart) {
				// Follow loaded singular entity associations (ManyToOne/OneToOne)
				// to discover more of the entity graph whose children may need detaching
				Object related = attr.getPropertyAccess().getGetter().get(entity);
				if(related != null && Hibernate.isInitialized(related) && session.contains(related)) {
					detachLoadedChildren(session, related, visited);
				}
			}
		}
	}

	/**
	 * For an uninitialized collection on a deleted entity, scan the persistence
	 * context for managed entities of the collection's element type whose
	 * back-reference (mappedBy) points to the deleted entity, and detach them.
	 */
	private void detachOrphanedChildrenFromPersistenceContext(Session session, Object deletedEntity, PluralAttributeMapping pluralAttr, Set<Object> visited) {
		CollectionPersister collPersister = pluralAttr.getCollectionDescriptor();
		String mappedByProperty = collPersister.getMappedByProperty();
		if(mappedByProperty == null) {
			return;	// Not a bidirectional association; nothing to scan for
		}

		// Get element class from the element descriptor's Java type (getElementClass() returns null in Hibernate 7.2+)
		Class<?> elementClass = pluralAttr.getElementDescriptor().getJavaType().getJavaTypeClass();
		if(elementClass == null) {
			return;
		}

		// Get the element entity's persister to resolve the mappedBy attribute getter
		SessionFactoryImplementor sfi = session.getSessionFactory().unwrap(SessionFactoryImplementor.class);
		EntityPersister elementPersister;
		try {
			elementPersister = sfi.getMappingMetamodel().getEntityDescriptor(elementClass);
		} catch(Exception ex) {
			return; // Not an entity type (e.g. embeddable collection)
		}
		AttributeMapping backRef = elementPersister.findAttributeMapping(mappedByProperty);
		if(backRef == null) {
			return;
		}

		// Iterate all managed entities in the persistence context
		PersistenceContext pc = ((SessionImpl) session).getPersistenceContext();
		Iterator<Object> iter = pc.managedEntitiesIterator();
		List<Object> managedObjects = new ArrayList<>();
		while(iter.hasNext()) {
			managedObjects.add(iter.next());
		}

		List<Object> toDetach = new ArrayList<>();
		for(Object managed : managedObjects) {
			if(!elementClass.isInstance(managed)) {
				continue;
			}
			if(!session.contains(managed)) {
				continue;
			}
			Object ref = backRef.getPropertyAccess().getGetter().get(managed);
			if(ref == null) {
				continue;
			}
			// Check if the back-reference points to the deleted entity (by identity or by PK)
			if(isSameEntity(sfi, session, ref, deletedEntity)) {
				toDetach.add(managed);
			}
		}
		for(Object child : toDetach) {
			if(session.contains(child)) {
				detachLoadedChildren(session, child, visited);
				session.detach(child);
			}
		}
	}

	/**
	 * Check whether two entity references (which may be proxies) refer to the same
	 * persistent entity, comparing first by identity, then by class + primary key.
	 */
	private boolean isSameEntity(SessionFactoryImplementor sfi, Session session, Object a, Object b) {
		if(a == b) {
			return true;
		}
		Class<?> classA = Hibernate.getClass(a);
		Class<?> classB = Hibernate.getClass(b);
		if(!classA.equals(classB)) {
			return false;
		}
		EntityPersister persister = sfi.getMappingMetamodel().getEntityDescriptor(classA);
		Object idA = persister.getIdentifier(a, (SessionImpl) session);
		Object idB = persister.getIdentifier(b, (SessionImpl) session);
		return idA != null && idA.equals(idB);
	}

	/**
	 * Scan the entire persistence context for managed entities that have a
	 * singular entity association (ManyToOne / OneToOne) pointing to the
	 * deleted entity, and detach them. This catches unidirectional @ManyToOne
	 * relationships where the deleted entity has no corresponding @OneToMany
	 * collection, so the outward walk in {@link #detachLoadedChildren} cannot
	 * discover them.
	 */
	private void detachInboundReferences(Session session, Object deletedEntity, Set<Object> visited) {
		SessionFactoryImplementor sfi = session.getSessionFactory().unwrap(SessionFactoryImplementor.class);
		PersistenceContext pc = ((SessionImpl) session).getPersistenceContext();

		// Snapshot all managed entities to avoid ConcurrentModificationException during detach
		List<Object> snapshot = new ArrayList<>();
		Iterator<Object> it = pc.managedEntitiesIterator();
		while(it.hasNext()) {
			snapshot.add(it.next());
		}

		List<Object> toDetach = new ArrayList<>();
		for(Object managed : snapshot) {
			if(managed == deletedEntity) {
				continue;
			}
			if(!session.contains(managed)) {
				continue;
			}
			if(referencesEntity(sfi, session, managed, deletedEntity)) {
				toDetach.add(managed);
			}
		}
		for(Object entity : toDetach) {
			if(session.contains(entity)) {
				detachLoadedChildren(session, entity, visited);
				session.detach(entity);
			}
		}
	}

	/**
	 * Check whether a managed entity has any singular entity-valued attribute
	 * (ManyToOne / OneToOne) whose current value references the given target entity.
	 */
	private boolean referencesEntity(SessionFactoryImplementor sfi, Session session, Object managed, Object target) {
		Class<?> entityClass = Hibernate.getClass(managed);
		EntityPersister persister;
		try {
			persister = sfi.getMappingMetamodel().getEntityDescriptor(entityClass);
		} catch(Exception ex) {
			return false;
		}
		var attributeMappings = persister.getAttributeMappings();
		for(int i = 0; i < attributeMappings.size(); i++) {
			AttributeMapping attr = attributeMappings.get(i);
			if(attr instanceof EntityValuedModelPart) {
				Object ref = attr.getPropertyAccess().getGetter().get(managed);
				if(ref != null && isSameEntity(sfi, session, ref, target)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public <T> T find(BuggyHibernateBaseContext root, Class<T> clz, Object pk) throws Exception {
		return root.getSession().find(clz, pk);
		//return (T) root.getSession().get(clz, (Serializable) pk);
	}

	@Override
	public <T> T getInstance(BuggyHibernateBaseContext root, Class<T> clz, Object pk) throws Exception {
		return root.getSession().getReference(clz, pk);
	}

	@Override
	public <T> T find(BuggyHibernateBaseContext root, ICriteriaTableDef<T> metatable, Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for " + getClass().getName());
	}

	@Override
	public <T> T getInstance(BuggyHibernateBaseContext root, ICriteriaTableDef<T> clz, Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for " + getClass().getName());
	}

	@Override
	public <T> List<T> query(BuggyHibernateBaseContext root, QCriteria<T> q) throws Exception {
		Query<T> query = GenericHibernateHandler.createSelectionQuery(root.getSession(), q);
		return query.list();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public List<Object[]> query(BuggyHibernateBaseContext root, QSelection<?> sel) throws Exception {
		Query<?> query = GenericHibernateHandler.createSelectionQuery(root.getSession(), sel);
		List resl = query.list();
		if(resl.isEmpty())
			return Collections.EMPTY_LIST;
		if(sel.getColumnList().size() == 1 && !(resl.get(0) instanceof Object[])) {
			//-- Re-wrap this result as a list of Object[].
			for(int i = resl.size(); --i >= 0;) {
				resl.set(i, new Object[]{resl.get(i)});
			}
		}
		return resl;
	}

	@Override
	public void refresh(BuggyHibernateBaseContext root, Object o) throws Exception {
		root.getSession().refresh(o);
	}

	@Override
	public void save(BuggyHibernateBaseContext root, Object o) throws Exception {
		root.getSession().persist(o);
	}

	@Override
	public void attach(BuggyHibernateBaseContext root, Object o) throws Exception {
		root.getSession().refresh(o);
	}
}
