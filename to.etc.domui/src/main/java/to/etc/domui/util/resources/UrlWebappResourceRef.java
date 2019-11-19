package to.etc.domui.util.resources;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.util.FileTool;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-10-19.
 */
final public class UrlWebappResourceRef implements IResourceRef, IModifyableResource {
	@NonNull
	private URL m_resource;

	public UrlWebappResourceRef(@NonNull URL resource) {
		m_resource = resource;
	}

	@Override
	public boolean exists() {
		try {
			try(InputStream is = m_resource.openStream()) {
				return true;
			}
		} catch(Exception x) {
			return false;
		}
	}

	@Override
	public long getLastModified() {
		try {
			URLConnection uc = m_resource.openConnection();
			try {
				return uc.getLastModified();
			} finally {
				FileTool.closeAll(uc.getInputStream(), uc.getOutputStream());
			}
		} catch(Exception x) {
			return -1;
		}
	}

	@Override
	@NonNull
	public InputStream getInputStream() throws Exception {
		return m_resource.openStream();
	}

	@Override
	public String toString() {
		return "UrlWebAppResourceRef[" + m_resource + "]";
	}
}
