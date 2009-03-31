package to.etc.domui.util.resources;

import java.io.*;

import to.etc.domui.util.*;

public class ClassResourceRef implements IResourceRef {
	private Class<?>		m_base;
	private String			m_name;

	public ClassResourceRef(Class< ? > base, String name) {
		m_base = base;
		m_name = name;
	}

	public InputStream getInputStream() throws Exception {
		return m_base.getResourceAsStream(m_name);
	}
	public boolean isModified() {
		return false;
	}
}
