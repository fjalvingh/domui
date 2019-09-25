package to.etc.domui.hibernate.memorydb;

import javassist.util.proxy.Proxy;
import org.hibernate.proxy.HibernateProxy;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EXPERIMENTAL In-memory database backing an existing test database.
 *
 * <p>This mirrors an existing database but allows changes on that database
 * in memory only, so that real changes are never sent to the database nor
 * the original objects from that database.</p>
 *
 * <p>This only works with non-SQL access, i.e. pure QDataContext and QCriteria based
 * database access.</p>
 *
 * <p>It works as follows: to use this you create a single instance of this class which
 * wraps a real QDataContext. This real context will be used to actually read the original
 * records from a source database. To use the in-memory variant you call createDataContext()
 * on it which will deliver a QDataContext.</p>
 *
 * <p>Any query on that QDataContext will try to obtain data from in-memory maps inside the
 * QDataContext and if not there from this class. If nothing is found/present in those
 * caches it will fire a query into the original database and cache the result here.</p>
 *
 * <p>The returned QDataContext will always create copies of all objects it gets from this
 * master class. These copies can then be changed at will, and will reside into that
 * context only. Saving them will not have an effect on the real database, only on the
 * context they are saved in.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-09-19.
 */
final public class MemoryDb {
	static final Object NOT_FOUND = new Object();

	private final QDataContext m_source;

	private Map<Class<?>, Map<Object, Object>> m_entityPerTypeMap = new HashMap<>();

	private final MetaCache m_meta = new MetaCache();

	private final ProxyBuilder m_proxyBuilder = new ProxyBuilder();

	public MemoryDb(QDataContext source) {
		m_source = source;
	}

	/**
	 * If the class instance is a proxy class this tries to get the actual class.
	 */
	static <T> Class<T> fixClass(Class<T> in) {
		if(HibernateProxy.class.isAssignableFrom(in))
			return (Class<T>) in.getSuperclass();
		if(Proxy.class.isAssignableFrom(in))
			return (Class<T>) in.getSuperclass();
		return in;
	}

	public QDataContext createDataContext() {
		return new MemoryDataContext(this);
	}

	/**
	 * Get an original copy for the specified thing. Return null if not found.
	 */
	<T> T findEntity(Class<T> clz, Object pk) throws Exception {
		System.out.print("mdb: find " + clz.getSimpleName() + "#" + pk);
		Map<Object, Object> emap = m_entityPerTypeMap.computeIfAbsent(clz, a -> new HashMap<>());
		T o = (T) emap.get(pk);

		if(o == NOT_FOUND) {
			System.out.println(" - not found (cached)");
			return null;
		}

		//-- Try the original db
		o = m_source.find(clz, pk);
		if(null == o) {
			System.out.println(" - not found (new query)");
			emap.put(pk, NOT_FOUND);
			return null;
		}
		emap.put(pk, o);
		System.out.println(" - found");
		return o;
	}

	public EntityMeta getMeta(Class<?> clz) {
		return m_meta.getEntity(clz);
	}

	public ProxyBuilder getProxyBuilder() {
		return m_proxyBuilder;
	}

	public static boolean isMdbProxy(Object val) {
		return val instanceof Proxy;
	}

	public <T> T getOriginal(T instance) {
		Class<?> clz = fixClass(instance.getClass());
		Map<Object, Object> map = m_entityPerTypeMap.get(clz);
		if(null == map)
			return null;
		Object pk = getMeta(clz).getId().getValue(instance);
		if(null == pk)
			throw new IllegalStateException(instance + " has no primary key assigned");
		Object o = map.get(pk);
		return (T) o;
	}

	/**
	 * Issue a query in the source, and copy all of the results into the entity map
	 * so that subcontexts can find them without database I/O. This will return the
	 * list of entities found- as entities of the real data context.
	 */
	public <T> List<T> prepare(QCriteria<T> q) throws Exception {
		List<T> sourceList = m_source.query(q);
		for(T item : sourceList) {
			Class<?> clz = fixClass(item.getClass());
			Map<Object, Object> map = m_entityPerTypeMap.computeIfAbsent(clz, a -> new HashMap<>());
			EntityMeta em = getMeta(clz);
			Object pk = em.getIdValue(item);
			if(null == pk)
				throw new IllegalStateException("?? entity with null pk?");
			Object stored = map.get(pk);
			if(stored == null) {
				map.put(pk, item);
			} else if(stored != item) {
				throw new IllegalStateException("Duplicate entity stored in original cache: " + item + " and " + stored);
			}
		}
		return sourceList;
	}
}
