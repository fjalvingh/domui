package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.state.IPageParameters;
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

	@Nullable
	private Class<? extends SubPage> m_currentPage;

	@Nullable
	private IPageParameters m_currentParameters;

	public SpiContainer(NodeContainer container, ISpiContainerName containerName, Class<? extends SubPage> initialContent) {
		m_container = container;
		m_containerName = containerName;
		m_initialContent = initialContent;
		m_currentPage = initialContent;
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

	@Nullable public synchronized Class<? extends SubPage> getCurrentPage() {
		return m_currentPage;
	}

	public synchronized void setCurrentPage(@Nullable Class<? extends SubPage> currentPage) {
		m_currentPage = currentPage;
	}

	@Nullable public synchronized IPageParameters getCurrentParameters() {
		return m_currentParameters;
	}

	public synchronized void setCurrentParameters(@Nullable IPageParameters currentParameters) {
		m_currentParameters = currentParameters;
	}
}
