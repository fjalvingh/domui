package to.etc.dbutil.schema;

import java.io.*;

public class Package implements Serializable {
	private String m_name;

	private String m_definition;

	private String m_body;

	public Package() {}

	public Package(String name, String definition, String body) {
		m_name = name;
		m_definition = definition;
		m_body = body;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getDefinition() {
		return m_definition;
	}

	public void setDefinition(String definition) {
		m_definition = definition;
	}

	public String getBody() {
		return m_body;
	}

	public void setBody(String body) {
		m_body = body;
	}
}
