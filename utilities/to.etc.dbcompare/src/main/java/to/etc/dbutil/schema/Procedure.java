package to.etc.dbutil.schema;

import java.io.*;

/**
 * Top-level procedure or function (no package).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 14, 2007
 */
public class Procedure implements Serializable {
	private String m_name;

	private String m_code;

	public Procedure() {}

	public Procedure(String name, String code) {
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
