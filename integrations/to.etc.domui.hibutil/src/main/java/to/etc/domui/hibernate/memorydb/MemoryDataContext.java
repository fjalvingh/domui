package to.etc.domui.hibernate.memorydb;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import to.etc.webapp.core.IRunnable;
import to.etc.webapp.query.ICriteriaTableDef;
import to.etc.webapp.query.IQDataContextListener;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextFactory;
import to.etc.webapp.query.QNotFoundException;
import to.etc.webapp.query.QSelection;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-09-19.
 */
public class MemoryDataContext implements QDataContext {
	private final MemoryDb m_mdb;

	private Map<Class<?>, Map<Object, Object>> m_entityPerTypeMap = new HashMap<>();

	public MemoryDataContext(MemoryDb mdb) {
		m_mdb = mdb;
	}

	@Override public <T> T find(Class<T> clz, Object pk) throws Exception {
		clz = MemoryDb.fixClass(clz);
		Map<Object, Object> eptm = m_entityPerTypeMap.computeIfAbsent(clz, a -> new HashMap<>());
		T o = (T) eptm.get(pk);
		if(o == MemoryDb.NOT_FOUND)
			return null;

		//-- Not locally cached nor seen before -> ask root
		o = m_mdb.findEntity(clz, pk);
		if(null == o) {
			eptm.put(pk, MemoryDb.NOT_FOUND);
		} else {
			o = createProxied(m_mdb.getMeta(clz), o);
			eptm.put(pk, o);
		}
		return o;
	}

	<T> T findLocally(Class<T> clz, Object pk) {
		clz = MemoryDb.fixClass(clz);
		Map<Object, Object> eptm = m_entityPerTypeMap.computeIfAbsent(clz, a -> new HashMap<>());
		T o = (T) eptm.get(pk);
		if(o == MemoryDb.NOT_FOUND)
			return null;
		return o;
	}

	private boolean isORMProxy(Object object) {
		return object instanceof HibernateProxy;
	}

	@Override public <T> T getInstance(Class<T> clz, Object pk) throws Exception {
		return find(clz, pk);
	}

	/**
	 * Create a copy of the input object, and replace all entities (parent relations)
	 * with lazies that load the parent when accessed, and all child lists with lazies
	 * that load the children when accessed.
	 */
	private <T> T createProxied(EntityMeta em, T se) throws Exception {
		T de = (T) em.getEntityClass().newInstance();

		for(AttributeMeta attribute : em.getAttributes()) {
			switch(attribute.getRelType()) {
				default:
					throw new IllegalStateException(attribute.getRelType() + "??");
				case NONE:
					copyAttributeValue(de, se, attribute);
					break;

				case PARENT:
					copyParentValue(de, se, attribute);
					break;

				case CHILDLIST:
					copyChildListValue(de, se, attribute);
					break;
			}
		}

		return de;
	}

	/**
	 * Creates a proxy for any collection. The proxy will instantiate all entities
	 * in the list in this context as soon as any list method gets called.
	 */
	private <T, V, M> void copyChildListValue(T de, T se, AttributeMeta attribute) throws Exception{
		V sourceValue = (V) attribute.getValue(se);
		if(null == sourceValue) {							// Bail out quickly
			attribute.setValue(de, null);
			return;
		}

		//-- The return class MUST be List.
		if(! (sourceValue instanceof List)) {
			throw new IllegalStateException(attribute + " contains not a List but a " + sourceValue.getClass().getName());
		}

		//-- We'll always proxy these lists.
		V newList = (V) m_mdb.getProxyBuilder().createListProxy(this, attribute, (List<M>) sourceValue);
		attribute.setValue(de, newList);
	}

	/**
	 * Copy a parent relation attribute. If that attribute is real then we also create
	 * a copy of the entity contained herein, to make sure all entities that are reachable
	 * can be changed.
	 * If this is a proxy we create a wrapper around the thing that will obtain the real
	 * instance when needed.
	 */
	private <T, V> void copyParentValue(T de, T se, AttributeMeta attribute) throws Exception {
		V sourceValue = (V) attribute.getValue(se);
		if(null == sourceValue) {							// Bail out quickly
			attribute.setValue(de, null);
			return;
		}
		EntityMeta targetEntity = attribute.getRelationEntity();
		if(isORMProxy(sourceValue)) {
			//-- Do we already have this locally?
			Object pk = targetEntity.getIdValue(sourceValue);
			if(null == pk)
				throw new IllegalStateException(attribute + ": value " + sourceValue + " has no primary key");
			Object localInstance = findLocally(targetEntity.getEntityClass(), pk);
			if(null != localInstance) {
				//-- Just use the local instance
				sourceValue = (V) localInstance;
			} else {
				//-- We really need a proxy
				sourceValue = m_mdb.getProxyBuilder().createParentProxy(this, attribute, sourceValue);
				m_entityPerTypeMap.computeIfAbsent(targetEntity.getEntityClass(), a -> new HashMap<>()).put(pk, sourceValue);
			}
		} else {
			//-- Not a proxy: real class. Reload it into this instance and use that as the value.
			sourceValue = loadHere(targetEntity, sourceValue);
		}
		attribute.setValue(de, sourceValue);
	}

	<T> T loadHere(EntityMeta em, T original) throws Exception {
		Object pk = em.getId().getValue(original);			// Get the PK value
		if(null == pk)
			throw new IllegalStateException("Instance " + original + " has a null primary key");
		return (T) get(em.getEntityClass(), pk);
	}

	/**
	 * Usable from proxy only, this loads the original value from the memoryDB, then
	 * creates a nonproxied copy. This nonproxied copy is then returned without it
	 * being registered in the entityMap - because that should already contain the
	 * proxy.
	 */
	<T> T loadCopyButDoNotRegister(EntityMeta em, T original) throws Exception {
		original = (T) m_mdb.findEntity(em.getEntityClass(), em.getIdValue(original));
		if(null == original)
			throw new IllegalStateException("Cannot relocate original!?");
		return createProxied(em, original);
	}

	/**
	 * Copy a non-relational attribute value.
	 */
	private <T> void copyAttributeValue(T de, T se, AttributeMeta attribute) {
		Object value = attribute.getValue(se);
		attribute.setValue(de, value);
	}

	@Override public void save(Object o) throws Exception {
		Class<?> actualClass = Hibernate.getClass(o);
		m_entityPerTypeMap.computeIfAbsent(actualClass, a -> new HashMap<>());



		throw new IllegalStateException("Not implemented");
	}



	@Override public <T> T get(Class<T> clz, Object pk) throws Exception {
		T t = find(clz, pk);
		if(null == t) {
			throw new QNotFoundException(clz, pk);
		}
		return t;
	}

	@Override public <T> List<T> query(QCriteria<T> q) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override public <T> T queryOne(QCriteria<T> q) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override public List<Object[]> query(QSelection<?> sel) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override public <R> List<R> query(Class<R> resultInterface, QSelection<?> sel) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override public Object[] queryOne(QSelection<?> q) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override public <R> R queryOne(Class<R> resultInterface, QSelection<?> sel) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override public <T> T find(ICriteriaTableDef<T> metatable, Object pk) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override public <T> T original(T copy) {
		throw new IllegalStateException("Not implemented");
	}

	@Override public void setKeepOriginals() {
	}

	@Override public <T> T getInstance(ICriteriaTableDef<T> clz, Object pk) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override public void attach(Object o) throws Exception {
	}

	@Override public void refresh(Object o) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override public void delete(Object o) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override public void startTransaction() throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override public void commit() throws Exception {
	}

	@Override public void rollback() throws Exception {
	}

	@Override public boolean inTransaction() throws Exception {
		return true;
	}

	@Override public Connection getConnection() throws Exception {
		throw new IllegalStateException("Direct connection access is not allowed for the in-memory test database");
	}

	@Override public void addCommitAction(IRunnable cx) {
		throw new IllegalStateException("Not implemented");
	}

	@Override public void addListener(IQDataContextListener qDataContextListener) {
		throw new IllegalStateException("Not implemented");
	}

	@Override public <T> T reload(T source) throws Exception {
		throw new IllegalStateException("Not implemented");
	}

	@Override public void setIgnoreClose(boolean on) {
	}

	@Override public void close() {
	}

	@Override public QDataContextFactory getFactory() {
		throw new IllegalStateException("Not implemented");
	}

}
