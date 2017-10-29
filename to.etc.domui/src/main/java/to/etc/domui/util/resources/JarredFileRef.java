package to.etc.domui.util.resources;

import to.etc.util.ByteBufferInputStream;

import javax.annotation.Nullable;
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

	private volatile long m_time;

	private volatile long m_size;

	@Nullable
	private byte[][] m_resourceData;

	public JarredFileRef(JarFileContainer container, String name, long time, long size) {
		m_container = container;
		m_name = name;
		m_time = time;
		m_size = size;
	}

	@Override public long getLastModified() {
		m_container.reloadIfChanged();
		return m_time;
	}

	@Override public String toString() {
		return "JarredFile " + m_name + " ts=" + m_time;
	}

	/**
	 * In debug mode, this tries to read the specified resource from the .jar file and
	 * caches it. This does an explicit test for the jar being changed and clears the
	 * cache if it has.
	 */
	public InputStream getResource() throws IOException {
		byte[][] rd = m_resourceData;
		if(rd == null) {
			rd = m_resourceData = m_container.loadResource(m_name);
		} else {
			m_container.reloadIfChanged();
		}
		return new ByteBufferInputStream(rd);
	}

	public void update(long lastModified, long size) {
		if(lastModified != m_time || size != m_size) {
			m_resourceData = null;
		}
		m_time = lastModified;
		m_size = size;
	}

	@Nullable byte[][] getResourceData() {
		return m_resourceData;
	}

	void setResourceData(@Nullable byte[][] resourceData) {
		m_resourceData = resourceData;
	}
}
