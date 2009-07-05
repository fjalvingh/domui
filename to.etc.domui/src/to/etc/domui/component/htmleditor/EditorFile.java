package to.etc.domui.component.htmleditor;

import java.util.*;

public class EditorFile {
	private String m_name;

	private Date m_date;

	private int m_size;

	public EditorFile() {}

	public EditorFile(String name, int size, Date date) {
		m_name = name;
		m_size = size;
		m_date = date;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public Date getDate() {
		return m_date;
	}

	public void setDate(Date date) {
		m_date = date;
	}

	public int getSize() {
		return m_size;
	}

	public void setSize(int size) {
		m_size = size;
	}
}
