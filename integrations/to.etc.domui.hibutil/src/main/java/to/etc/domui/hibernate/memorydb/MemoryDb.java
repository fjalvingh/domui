package to.etc.domui.hibernate.memorydb;

import to.etc.webapp.query.QDataContext;

import java.util.HashMap;
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

	public MemoryDb(QDataContext source) {
		m_source = source;
	}

	public QDataContext createDataContext() {
		return new MemoryDataContext(this);
	}

	/**
	 * Get an original copy for the specified thing. Return null if not found.
	 */
	<T> T findEntity(Class<T> clz, Object pk) throws Exception {
		Map<Object, Object> emap = m_entityPerTypeMap.computeIfAbsent(clz, a -> new HashMap<>());
		T o = (T) emap.get(pk);

		if(o == NOT_FOUND) {
			return null;
		}

		//-- Try the original db
		o = m_source.find(clz, pk);
		if(null == o) {
			emap.put(pk, NOT_FOUND);
			return null;
		}
		emap.put(pk, o);
		return o;
	}

	public EntityMeta getMeta(Class<?> clz) {
		return m_meta.getEntity(clz);
	}
}
