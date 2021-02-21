package to.etc.domui.component2.navigation;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Li;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.SpiPage;
import to.etc.domui.dom.html.Ul;
import to.etc.domui.spi.ISpiShelvedEntry;
import to.etc.domui.spi.SpiContainer;
import to.etc.domui.state.UIGoto;
import to.etc.domui.util.ISpiContainerName;
import to.etc.function.IExecute;

import java.util.List;

/**
 * A breadcrumb for a specific SPI container on a SPI page.
 * Modeled after <a href="http://cssmenumaker.com/blog/fancy-breadcrumb-navigation-tutorial-example/">this article</a>.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2021-02-21.
 */
@NonNullByDefault
public class SpiBreadCrumb extends Div {
	final private ISpiContainerName m_containerName;

	private SpiBreadcrumbMode m_mode;

	/** When set the listener has been allocated */
	@Nullable
	private Runnable m_deleteListener;

	@Nullable
	private String m_homeItemName;

	@Nullable
	private NodeBase m_homeIcon;

	public enum SpiBreadcrumbMode {
		/** Make the "initial" page of the container always visible as the 1st element in the breadcrumb */
		INITIAL,
		NONE
	}

	public SpiBreadCrumb(ISpiContainerName containerName) {
		this(containerName, SpiBreadcrumbMode.INITIAL, "Home", null);
	}

	public SpiBreadCrumb(ISpiContainerName containerName, SpiBreadcrumbMode mode, @Nullable String homeItemName, @Nullable NodeBase homeIcon) {
		m_containerName = containerName;
		m_mode = mode;
		m_homeItemName = homeItemName;
		m_homeIcon = homeIcon;
	}

	@Override
	public void createContent() throws Exception {
		SpiPage spiPage = getParent(SpiPage.class);                    // We MUST be a part of a SPI page of course
		SpiContainer container = spiPage.getSpiContainer(m_containerName);

		//-- Make sure a listener is registered so that we can redraw when history changes. */
		if(m_deleteListener == null) {
			IExecute shelfListener = () -> forceRebuild();
			m_deleteListener = container.addShelfListener(shelfListener);
		}

		addCssClass("ui-brcr2");

		Ul cont = new Ul();
		add(cont);

		List<ISpiShelvedEntry> shelf = container.getShelf();
		if(m_mode == SpiBreadcrumbMode.INITIAL) {
			renderHomeItem(cont, shelf.size() == 0);
		}

		for(int i = 0; i < shelf.size(); i++) {
			ISpiShelvedEntry item = shelf.get(i);
			renderItem(cont, item, i == shelf.size() - 1);
		}
	}

	/**
	 * Make sure to delete the listener if we're discarded!
	 */
	@Override public void onRemoveFromPage(Page p) {
		Runnable listener = m_deleteListener;
		m_deleteListener = null;
		if(null != listener) {
			listener.run();
		}
	}

	private void renderItem(Ul cont, ISpiShelvedEntry item, boolean active) {
		Li li = new Li();
		cont.add(li);
		if(active)
			li.addCssClass("ui-brcr2-a");
		ATag a = new ATag();
		li.add(a);
		a.setClicked(v -> {
			item.select();
		});
		NodeBase icon = item.getIcon();
		if(null != icon) {
			Span sp = new Span();
			a.add(sp);
			sp.addCssClass("ui-brcr2-i");
			sp.add(icon);
		}
		a.add(item.getName());
		a.setTitle(item.getTitle());
	}

	private void renderHomeItem(Ul cont, boolean active) {
		Li li = new Li();
		cont.add(li);
		if(active)
			li.addCssClass("ui-brcr2-a");
		ATag a = new ATag();
		li.add(a);
		a.setClicked(v -> {
			SpiPage spiPage = getParent(SpiPage.class);                    // We MUST be a part of a SPI page of course
			SpiContainer container = spiPage.getSpiContainer(m_containerName);
			UIGoto.moveNew(m_containerName, container.getInitialContent(), container.getInitialContentParameters());
		});
		NodeBase icon = m_homeIcon;
		if(null != icon) {
			Span sp = new Span();
			a.add(sp);
			sp.addCssClass("ui-brcr2-i");
			sp.add(icon);
		}
		a.add(m_homeItemName);
		//a.setTitle(item.getTitle());
	}

}
