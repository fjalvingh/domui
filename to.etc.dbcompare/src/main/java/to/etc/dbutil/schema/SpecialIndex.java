package to.etc.dbutil.schema;

import java.io.*;

public class SpecialIndex implements Serializable {
	private String m_name;

	private String m_ddl;

	public SpecialIndex(String name, String ddl) {
		m_name = name;
		m_ddl = ddl;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getDdl() {
		return m_ddl;
	}

	public void setDdl(String ddl) {
		m_ddl = ddl;
	}
}
