package to.etc.domui.util.resources;

import java.io.*;

import to.etc.domui.util.*;

public class ClassResourceRef implements IResourceRef {
	private Class< ? > m_base;

	private String m_name;

	public ClassResourceRef(Class< ? > base, String name) {
		m_base = base;
		m_name = name;
	}

	public InputStream getInputStream() throws Exception {
		return m_base.getResourceAsStream(m_name);
	}

	/**
	 * We will not allow replacement of class files because we cannot know their date.
	 * @see to.etc.domui.util.resources.IModifyableResource#getLastModified()
	 */
	public long getLastModified() {
		return 0;
	}
}
