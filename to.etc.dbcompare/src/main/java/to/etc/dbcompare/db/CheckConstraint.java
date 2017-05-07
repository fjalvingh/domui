package to.etc.dbcompare.db;

import java.io.*;

public class CheckConstraint implements Serializable {
	private String	m_name;

	private String	m_text;

	public CheckConstraint() {
	}

	public CheckConstraint(String name, String text) {
		m_name = name;
		m_text = text;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getText() {
		return m_text;
	}

	public void setText(String text) {
		m_text = text;
	}
}
