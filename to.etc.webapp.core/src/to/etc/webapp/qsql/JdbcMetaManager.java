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

	static public JdbcClassMeta getMeta(Class< ? > jdbcClass) {
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

}
