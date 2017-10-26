package to.etc.domui.util.resources;

import java.io.IOException;
import java.io.InputStream;

/**
 * A reference to a file in a {@link JarFileContainer}. It checks for
 * changes of itself by first checking the container jar and if
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
final public class JarredFileRef implements IModifyableResource {
	private final JarFileContainer m_container;

	private final String m_name;

	private final long m_time;

	private final long m_size;

	public JarredFileRef(JarFileContainer container, String name, long time, long size) {
		m_container = container;
		m_name = name;
		m_time = time;
		m_size = size;
	}

	@Override public long getLastModified() {
		return m_time;
	}

	@Override public String toString() {
		return "JarredFile " + m_name + " ts=" + m_time;
	}

	public InputStream getResource() throws IOException {
		return m_container.getResource(m_name);
	}
}
