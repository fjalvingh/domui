package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.util.ISpiContainerName;

/**
 * Describes a SPI "Content Area" inside a SpiPage.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-10-20.
 */
@NonNullByDefault
final public class SpiContainer {
	private final NodeContainer m_container;

	private final ISpiContainerName m_containerName;

	private final Class<? extends SubPage> m_initialContent;

	public SpiContainer(NodeContainer container, ISpiContainerName containerName, Class<? extends SubPage> initialContent) {
		m_container = container;
		m_containerName = containerName;
		m_initialContent = initialContent;
	}

	public NodeContainer getContainer() {
		return m_container;
	}

	public ISpiContainerName getContainerName() {
		return m_containerName;
	}

	public Class<? extends SubPage> getInitialContent() {
		return m_initialContent;
	}
}
