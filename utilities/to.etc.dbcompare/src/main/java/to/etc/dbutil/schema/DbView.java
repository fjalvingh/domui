package to.etc.dbutil.schema;

import java.io.*;

/**
 * Represents a database view, as source code.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 14, 2007
 */
public class DbView implements Serializable {
	private String m_name;

	private String m_sql;

	public DbView(String name, String sql) {
		m_name = name;
		m_sql = sql;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getSql() {
		return m_sql;
	}

	public void setSql(String sql) {
		m_sql = sql;
	}
}
