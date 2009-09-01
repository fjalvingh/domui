package to.etc.domui.util.resources;

import java.io.*;

import to.etc.domui.util.*;

public class WebappResourceRef implements IResourceRef {
	private File m_resource;

	public WebappResourceRef(File resource) {
		m_resource = resource;
	}

	public long getLastModified() {
		if(!m_resource.exists())
			return -1;
		return m_resource.lastModified();
	}

	public InputStream getInputStream() throws Exception {
		return new FileInputStream(m_resource);
	}
}
