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

import java.io.*;

import org.hibernate.*;
import org.hibernate.engine.*;
import org.hibernate.event.def.*;
import org.hibernate.impl.*;
import org.hibernate.proxy.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.hibernate.generic.*;
import to.etc.domui.util.db.*;
import to.etc.webapp.query.*;

public class HibernateModelCopier extends QBasicModelCopier {
	@Override
	public <T> boolean isUnloadedChildList(T source, PropertyMetaModel< ? > pmm) throws Exception {
		Object value = pmm.getValue(source);
		if(value == null)
			return false;
		return !Hibernate.isInitialized(value);
	}

	@Override
	public <T> boolean isUnloadedParent(T source, PropertyMetaModel< ? > pmm) throws Exception {
		Object value = pmm.getValue(source);
		if(value == null)
			return false;
		return !Hibernate.isInitialized(value);
	}

	protected boolean isPersistedEntry(QDataContext dc, Object instance) throws Exception {
		if(!(dc instanceof BuggyHibernateBaseContext))
			throw new IllegalArgumentException("The QDataContext type is not a Hibernate context");
		Session ses = ((BuggyHibernateBaseContext) dc).getSession();

		PersistenceContext pc = ((SessionImpl) ses).getPersistenceContext(); // The root of all hibernate persistent data
		return pc.getEntry(instance) != null;
	}

	/**
	 * Sigh. Overridden to force Hibernate to bloody use an existing primary key on NEW object, damnit. See <a href="http://info.etc.to/xwiki/bin/view/Ontwikkeling/HibernateToilet">Here</a>.
	 *
	 * @see to.etc.domui.util.db.QBasicModelCopier#save(to.etc.domui.util.db.QBasicModelCopier.CopyInfo, java.lang.Object)
	 */
	@Override
	protected void save(CopyInfo ci, Object instance) throws Exception {
		//-- Do we have an existing PK already?
		Object	pk = MetaManager.getPrimaryKey(instance);
		if(pk == null) {
			super.save(ci, instance);		// Just delegate to the usual code.
			return;
		}

		//-- We need to force Hibernate to use the existing PK, sigh.
		SessionImpl	ses = (SessionImpl) ((BuggyHibernateBaseContext)ci.getTargetDC()).getSession();
		ses.save(instance, (Serializable) pk); // Add nonsense cast.
	}

	/**
	 * Determine the object state using internal Hibernate data structures. Code was mostly stolen from {@link DefaultFlushEntityEventListener#dirtyCheck()}
	 * @param dc
	 * @param instance
	 * @return
	 */
	@Override
	protected QPersistentObjectState getObjectState(QDataContext dc, Object instance) throws Exception {
		if(!(dc instanceof BuggyHibernateBaseContext))
			throw new IllegalArgumentException("The QDataContext type is not a Hibernate context");
		SessionImpl ses = (SessionImpl) ((BuggyHibernateBaseContext) dc).getSession();

		PersistenceContext pc = ses.getPersistenceContext(); // The root of all hibernate persistent data
		EntityEntry	ee = pc.getEntry(instance);
		if(ee == null) {
			/*
			 * Incredible but true: proxies are not stored as entries in the hibernate session - the backing objects
			 * of the proxies are. This means that a prime invariant does NOT hold for proxies: the SAME database object
			 * in the SAME object, where one is retrieved using a lazy association and the other through Session.load,
			 * ARE NOT == (the same reference): the first one points to the proxy, the second one to the instance itself.
			 * This is another huge design blunder and again a pitfall: you cannot use == EVEN WHEN IN A SINGLE SESSION!
			 * This problem is most probably caused by the enormous design blunder that separates the proxy instance from
			 * the actual instance.
			 *
			 * In here it means we need to check if the object is indeed a proxy, and retry with the original object.
			 */
			if(instance instanceof HibernateProxy) { // Ohh Horror of horrors.
				HibernateProxy hp = (HibernateProxy) instance;
				Object ainstance = hp.getHibernateLazyInitializer().getImplementation();
				ee = pc.getEntry(ainstance);

				String clz = instance.getClass().getName();
				if(clz.contains("Relation"))
					System.out.println("DEBUG 2nd try for " + MetaManager.identify(instance));

				if(ee == null) {
					//-- DEBUG
					//					if(clz.contains("Relation")) {
					//						//-- The failing relation record.
					//						System.out.println("DEBUG Examining " + MetaManager.identify(instance));
					//
					//						Object pk = MetaManager.getPrimaryKey(instance);
					//						Map<Object, Object> eemap = pc.getEntityEntries();
					//						for(Iterator<Object> it = ((IdentityMap) eemap).keyIterator(); it.hasNext();) {
					//							Object ent = it.next();
					//							if(ent.getClass().getName().contains("Relation")) {
					//								//-- Probable match. Get PK's and compare
					//								Object epk = MetaManager.getPrimaryKey(ent);
					//								if(epk.equals(pk)) {
					//									System.out.println("Primary key matches " + ent);
					//									ee = pc.getEntry(ent);
					//									System.out.println("EntityEntry: " + ee);
					//								}
					//							}
					//						}
					//					}

					System.out.println("    state for " + MetaManager.identify(instance) + ": null in session");
					//-- ENDDEBUG

					return QPersistentObjectState.UNKNOWN;
				}
			}
		}
		if(null == ee)
			throw new IllegalStateException("current EntityEntry is null- that cannot happen?");

		System.out.println("    state for " + MetaManager.identify(instance) + ": exists=" + ee.isExistsInDatabase() + ", state=" + ee.getStatus());

		if(ee.getStatus() == Status.DELETED)
			return QPersistentObjectState.DELETED;
		if(!ee.isExistsInDatabase())
			return QPersistentObjectState.NEW;

		//-- Let's do a check.
		Object[] snapshot = ee.getLoadedState(); // Get snapshot @ load time
		if(snapshot == null)
			return QPersistentObjectState.NEW;
		Object[] values = ee.getPersister().getPropertyValues(instance, ses.getEntityMode()); // Load current instance's values.

		//-- Delegate to any interceptor.
		int[] dirtyProperties = ses.getInterceptor().findDirty(instance, ee.getId(), values, snapshot, ee.getPersister().getPropertyNames(), ee.getPersister().getPropertyTypes());
		if(dirtyProperties == null) {
			//-- Do it ourselves.
			dirtyProperties = ee.getPersister().findDirty(values, snapshot, instance, ses);
		}
		return dirtyProperties == null || dirtyProperties.length == 0 ? QPersistentObjectState.PERSISTED : QPersistentObjectState.DIRTY;
	}

	@Override
	protected QPersistentObjectState getObjectState(QDataContext dc, Class< ? > pclass, Object pk) throws Exception {
		if(pk == null)
			return QPersistentObjectState.NEW;
		Object instance;
		try {
			instance = dc.find(pclass, pk);
		} catch(ObjectNotFoundException onfx) {
			return QPersistentObjectState.DELETED;
		}
		if(instance == null)
			return QPersistentObjectState.UNKNOWN;
		return getObjectState(dc, instance);
	}
}
