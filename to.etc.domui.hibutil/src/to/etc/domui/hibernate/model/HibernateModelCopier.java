package to.etc.domui.hibernate.model;

import org.hibernate.*;
import org.hibernate.engine.*;
import org.hibernate.event.def.*;
import org.hibernate.impl.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.hibernate.generic.*;
import to.etc.domui.util.db.*;
import to.etc.webapp.query.*;

public class HibernateModelCopier extends QBasicModelCopier {
	@Override
	protected <T> boolean isUnloadedChildList(T source, PropertyMetaModel pmm) throws Exception {
		Object value = pmm.getAccessor().getValue(source);
		if(value == null)
			return false;
		return !Hibernate.isInitialized(value);
	}

	@Override
	protected <T> boolean isUnloadedParent(T source, PropertyMetaModel pmm) throws Exception {
		Object value = pmm.getAccessor().getValue(source);
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
		if(ee == null)
			return QPersistentObjectState.UNKNOWN;
		if(ee.getStatus() == Status.DELETED)
			return QPersistentObjectState.DELETED;

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
