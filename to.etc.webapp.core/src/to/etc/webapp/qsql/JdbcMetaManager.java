package to.etc.webapp.qsql;

import java.util.*;

/**
 * Singleton to manage all JDBC class metadata.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public class JdbcMetaManager {
	static private final Map<Class< ? >, JdbcClassMeta> m_classMap = new HashMap<Class< ? >, JdbcClassMeta>();

	static private Map<Class< ? >, List<ITypeConverter>> m_converterMap = new HashMap<Class< ? >, List<ITypeConverter>>();

	static public JdbcClassMeta getMeta(Class< ? > jdbcClass) throws Exception {
		JdbcClassMeta	cm;
		synchronized(m_classMap) { // Atomically add or get in 1st lock
			cm = m_classMap.get(jdbcClass);
			if(cm == null) {
				cm = new JdbcClassMeta(jdbcClass);
				m_classMap.put(jdbcClass, cm);
			}
		}
		cm.initialize(); // Initialize in 2nd lock
		return cm;
	}

	static public synchronized void register(ITypeConverter c, Class< ? >... clses) {
		for(Class< ? > tc : clses) {
			List<ITypeConverter> cl = m_converterMap.get(tc);
			if(cl == null)
				cl = new ArrayList<ITypeConverter>();
			else
				cl = new ArrayList<ITypeConverter>(cl);
			cl.add(c);
			m_converterMap.put(tc, cl);
		}
	}

	static private synchronized List<ITypeConverter> getConverterList(Class< ? > type) {
		List<ITypeConverter> cl = m_converterMap.get(type);
		return cl;
	}

	static ITypeConverter findConverter(JdbcPropertyMeta pm) {
		List<ITypeConverter> cl = getConverterList(pm.getActualClass());
		if(cl == null)
			return null;
		ITypeConverter best = null;
		int bestscore = 0;
		for(ITypeConverter tc : cl) {
			int score = tc.accept(pm);
			if(score > bestscore) {
				bestscore = score;
				best = tc;
			}
		}
		return best;
	}

	static ITypeConverter getConverter(JdbcPropertyMeta pm) {
		ITypeConverter tc = findConverter(pm);
		if(tc == null)
			throw new IllegalStateException("No converter for " + pm);
		return tc;
	}

	static {
		register(new StringType(), String.class);
		register(new IntegerType(), Integer.class, int.class);
		register(new LongType(), Long.class, long.class);
	}


}
