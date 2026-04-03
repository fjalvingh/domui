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
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
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
		detachLoadedChildren(session, o, Collections.newSetFromMap(new IdentityHashMap<>()));
	}

	/**
	 * Recursively detach loaded child entities from initialized collections.
	 * After session.remove(parent), children handled by JPA cascade are already
	 * REMOVED and session.contains() returns false for them, so only children
	 * without JPA cascade (still MANAGED) get detached.
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
			if(attr instanceof PluralAttributeMapping) {
				Object collectionValue = attr.getPropertyAccess().getGetter().get(entity);
				if(collectionValue instanceof Collection<?> coll && Hibernate.isInitialized(coll)) {
					for(Object child : new ArrayList<>(coll)) {
						if(session.contains(child)) {
							detachLoadedChildren(session, child, visited);
							session.detach(child);
						}
					}
				}
			}
		}
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
