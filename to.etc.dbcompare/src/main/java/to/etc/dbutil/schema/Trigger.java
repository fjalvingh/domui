package to.etc.dbutil.schema;

import java.io.*;

public class Trigger implements Serializable {
	private String m_name;

	private String m_code;

	public Trigger() {}

	public Trigger(String name, String code) {
		m_name = name;
		m_code = code;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getCode() {
		return m_code;
	}

	public void setCode(String code) {
		m_code = code;
	}
}
