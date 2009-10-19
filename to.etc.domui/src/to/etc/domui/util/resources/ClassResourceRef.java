package to.etc.domui.util.resources;

import java.io.*;

import to.etc.domui.server.reloader.*;

public class ClassResourceRef implements IResourceRef {
	/** When running in debug mode AND if a source for this resource can be found- this contains a ref to it. */
	private IModifyableResource m_source;

	private Class< ? > m_base;

	private String m_name;

	/**
	 * Create a root-based class resource ref.
	 * @param mr
	 * @param name
	 */
	public ClassResourceRef(IModifyableResource mr, String name) {
		m_source = mr;
		m_base = ClassResourceRef.class;
		if(!name.startsWith("/"))
			throw new IllegalStateException("The root-based resource reference " + name + " must start with a '/'");
		m_name = name;
	}

	//	public ClassResourceRef(Class< ? > base, String name) {
	//		m_base = base;
	//		m_name = name;
	//	}

	/**
	 * This is a funny one.... When a class resource is loaded from a .jar file Java will cache that data. This means that even
	 * though the underlying .jar file has changed the resource as read originally will be returned, defeating the purpose of
	 * DomUI debug mode. To fix this we need to ensure that a //different// instance is returned every time the class is accessed...
	 */
	public InputStream getInputStream() throws Exception {
		if(m_source == null || !(m_source instanceof ClasspathJarRef))
			return m_base.getResourceAsStream(m_name);

		//-- This is a JAR reference; use it's classloader.
		ClasspathJarRef jref = (ClasspathJarRef) m_source;



	}

	/**
	 * @see to.etc.domui.util.resources.IModifyableResource#getLastModified()
	 */
	public long getLastModified() {
		return m_source == null ? 0 : m_source.getLastModified();
	}

	@Override
	public String toString() {
		return "ClassResourceRef[" + m_base + " - " + m_name + "]";
	}
}
