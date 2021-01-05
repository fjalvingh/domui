package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;
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

	@Nullable private IPageParameters m_initialContentParameters;

	@Nullable
	private Class<? extends SubPage> m_currentPage;

	@Nullable
	private IPageParameters m_currentParameters;

	public SpiContainer(NodeContainer container, ISpiContainerName containerName, Class<? extends SubPage> initialContent, @Nullable IPageParameters initialContentParameters) {
		m_container = container;
		m_containerName = containerName;
		m_initialContent = initialContent;
		m_currentPage = initialContent;
		m_initialContentParameters = initialContentParameters;
	}

	private NodeContainer getContainer() {
		return m_container;
	}

	public ISpiContainerName getContainerName() {
		return m_containerName;
	}

	public Class<? extends SubPage> getInitialContent() {
		return m_initialContent;
	}

	@Nullable public IPageParameters getInitialContentParameters() {
		return m_initialContentParameters;
	}

	@Nullable public synchronized Class<? extends SubPage> getCurrentPage() {
		return m_currentPage;
	}

	private synchronized void setCurrentPage(@Nullable Class<? extends SubPage> currentPage) {
		m_currentPage = currentPage;
	}

	public void setPage(@NonNull SubPage content, @Nullable IPageParameters pp) {
		getContainer().removeAllChildren();
		getContainer().add(content);
		setCurrentPage(content.getClass());
		setCurrentParameters(pp);
	}

	@Nullable public synchronized IPageParameters getCurrentParameters() {
		return m_currentParameters;
	}

	public synchronized void setCurrentParameters(@Nullable IPageParameters currentParameters) {
		m_currentParameters = currentParameters;
	}
}
