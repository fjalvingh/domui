package to.etc.webapp.qsql;

public class PClassRef {
	static private int m_nextid;

	private Class< ? > m_dataClass;

	private String m_alias;

	public PClassRef(Class< ? > dataClass, String alias) {
		m_dataClass = dataClass;
		m_alias = alias;
	}

	public PClassRef(Class< ? > dataClass) {
		m_dataClass = dataClass;
		m_alias = "T" + nextId();
	}

	static private synchronized int nextId() {
		return ++m_nextid;
	}

	public Class< ? > getDataClass() {
		return m_dataClass;
	}

	public String getAlias() {
		return m_alias;
	}
}
