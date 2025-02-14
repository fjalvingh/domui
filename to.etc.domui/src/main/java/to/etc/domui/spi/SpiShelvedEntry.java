package to.etc.domui.spi;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.layout.IBreadCrumbTitler;
import to.etc.domui.dom.html.SubPage;
import to.etc.domui.state.IPageParameters;

import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-02-21.
 */
@NonNullByDefault
final public class SpiShelvedEntry implements ISpiShelvedEntry {
	final private SpiContainer m_container;

	final private SubPage m_page;

	final private IPageParameters m_parameters;

	public SpiShelvedEntry(SpiContainer container, SubPage page, IPageParameters parameters) {
		m_container = container;
		m_page = page;
		m_parameters = parameters;
	}

	public SubPage getPage() {
		return m_page;
	}

	public IPageParameters getParameters() {
		return m_parameters;
	}

	@Override
	@NonNull
	public String getName() {
		SubPage body = getPage();
		if(body instanceof IBreadCrumbTitler) {
			return ((IBreadCrumbTitler) body).getBreadcrumbName();
		} else {
			String name = body.getClass().getName();
			return name.substring(name.lastIndexOf('.') + 1);
		}
	}

	@Nullable
	@Override
	public String getTitle() {
		if(getPage() instanceof IBreadCrumbTitler) {
			IBreadCrumbTitler body = (IBreadCrumbTitler) getPage();
			return body.getBreadcrumbTitle();
		}
		return null;
	}

	public boolean isForPage(Class<? extends SubPage> spiClass, @Nullable IPageParameters parameters) {
		if(spiClass != m_page.getClass())
			return false;
		if(parameters == null) {
			return m_parameters == null || m_parameters.size() == 0;
		}
		return Objects.equals(parameters, m_parameters);
	}

	@Override public void discard() {
		m_container.destroyShelvedEntry(this);
	}

	@Override public void activate(@NonNull SpiContainer container) throws Exception {
		m_container.activateSpiSubpage(this);
	}

	@Override public void select() throws Exception {
		m_container.selectShelvedEntry(this);
	}
}
