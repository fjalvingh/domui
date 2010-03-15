package to.etc.domui.util.resources;

import java.io.*;

/**
 * Classpath resource for PRODUCTION (non-debug) mode. This refers to a .classpath reference and
 * is initialized only once.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 15, 2010
 */
public class ProductionClassResourceRef implements IResourceRef {
	private String m_path;

	private boolean m_exists;

	public ProductionClassResourceRef(String rootpath) {
		m_path = rootpath;
		InputStream is = null;
		try {
			is = getInputStream();
			if(is != null)
				is.close();
		} catch(Exception x) {}
		m_exists = is != null;
	}

	@Override
	public InputStream getInputStream() throws Exception {
		return getClass().getResourceAsStream(m_path);
	}

	/**
	 * This one only returns existence: it returns -1 if the resource does
	 * not exist and 1 if it does.
	 * @see to.etc.domui.util.resources.IModifyableResource#getLastModified()
	 */
	@Override
	public long getLastModified() {
		return m_exists ? 1 : -1;
	}
}
