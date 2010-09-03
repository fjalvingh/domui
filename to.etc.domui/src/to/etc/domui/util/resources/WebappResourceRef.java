package to.etc.domui.util.resources;

import java.io.*;

/**
 * A full reference to a web app file (a file somewhere in the webapp's web files or WEB-INF directorty).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 19, 2009
 */
public class WebappResourceRef implements IResourceRef {
	private File m_resource;

	public WebappResourceRef(File resource) {
		m_resource = resource;
	}

	@Override
	public long getLastModified() {
		if(!m_resource.exists())
			return -1;
		return m_resource.lastModified();
	}

	@Override
	public InputStream getInputStream() throws Exception {
		return new FileInputStream(m_resource);
	}
}
