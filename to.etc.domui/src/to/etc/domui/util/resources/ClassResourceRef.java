package to.etc.domui.util.resources;

import java.io.*;

public class ClassResourceRef implements IResourceRef {
	/** When running in debug mode AND if a source for this resource can be found- this contains a ref to it. */
	private IModifyableResource m_source;

	private Class< ? > m_base;

	private String m_name;

	public ClassResourceRef(IModifyableResource mr, String name) {
		m_source = mr;
		m_base = ClassResourceRef.class;
		m_name = name;
	}

	//	public ClassResourceRef(Class< ? > base, String name) {
	//		m_base = base;
	//		m_name = name;
	//	}

	public InputStream getInputStream() throws Exception {
		return m_base.getResourceAsStream(m_name);
	}

	/**
	 * @see to.etc.domui.util.resources.IModifyableResource#getLastModified()
	 */
	public long getLastModified() {
		return m_source == null ? 0 : m_source.getLastModified();
	}
}
