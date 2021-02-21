package to.etc.domui.spi;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.SpiPage;
import to.etc.domui.dom.html.SubPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.SpiPageHelper;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.SubConversationContext;
import to.etc.domui.util.ISpiContainerName;
import to.etc.function.IExecute;
import to.etc.util.StringTool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a SPI "Content Area" inside a SpiPage. It handles the per-content area
 * page history.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-10-20.
 */
@NonNullByDefault
final public class SpiContainer {
	private SpiPage m_spiPage;

	private final NodeContainer m_container;

	private final ISpiContainerName m_containerName;

	private final Class<? extends SubPage> m_initialContent;

	@Nullable private IPageParameters m_initialContentParameters;

	@Nullable
	private Class<? extends SubPage> m_currentPage;

	@Nullable
	private IPageParameters m_currentParameters;

	private List<ISpiShelvedEntry> m_shelf = new ArrayList<>();

	private List<WeakReference<IExecute>> m_shelfChangedListeners = new ArrayList<>();

	public SpiContainer(SpiPage spiPage, NodeContainer container, ISpiContainerName containerName, Class<? extends SubPage> initialContent, @Nullable IPageParameters initialContentParameters) {
		m_spiPage = spiPage;
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

	private synchronized void setCurrentParameters(@Nullable IPageParameters currentParameters) {
		m_currentParameters = currentParameters;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	History stack operations.									*/
	/*----------------------------------------------------------------------*/

	/**
	 * Find the index of the page specified on the shelf if it is there. Return -1 if not found.
	 */
	private int findShelfEntry(Class<? extends SubPage> spiClass, IPageParameters parameters) {
		for(int i = m_shelf.size() - 1; i >= 0; i--) {
			ISpiShelvedEntry entry = m_shelf.get(i);
			if(entry instanceof SpiShelvedEntry) {
				if(((SpiShelvedEntry) entry).isForPage(spiClass, parameters)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Clear all shelf entries that are at the specified index AND above it. If the index
	 * is zero the net result is an empty history stack.
	 */
	private void clearShelf(int index) {
		int i = m_shelf.size();
		for(;;) {
			i--;
			if(i >= index) {
				ISpiShelvedEntry entry = m_shelf.remove(i);
				entry.discard();
			}
		}
	}

	void destroyShelvedEntry(SpiShelvedEntry entry) {
		SubPage page = entry.getPage();
		if(page.isAttached()) {
			page.remove();
		}

		SubConversationContext conversation = page.getConversation();
		conversation.setShelvedIn(null);								// No longer managed by this container
		try {
			getContainer().getPage().getConversation().removeAndDestroySubConversation(conversation);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	public void handleMoveSub(Class<? extends SubPage> spiClass, @Nullable IPageParameters pp) throws Exception {
		if(null == pp)
			pp = new PageParameters();
		DomApplication app = DomApplication.get();
		SpiPageHelper helper = new SpiPageHelper(app);

		//-- Do we have this page on the shelf already? If so just go "back" to it
		SubPage subPage;
		int index = findShelfEntry(spiClass, pp);
		if(index == -1) {
			//-- No (longer) there: create the page
			subPage = helper.createSpiPage(spiClass);
			app.getInjector().injectPageValues(subPage, pp);
			m_shelf.add(new SpiShelvedEntry(this, subPage, pp));
		} else {
			//-- Page already existed; restore it.
			clearShelf(index + 1);							// Destroy all pages "below" the new one

			ISpiShelvedEntry entry = m_shelf.get(index);
			subPage = ((SpiShelvedEntry) entry).getPage();
		}

		subPage.getConversation().setShelvedIn(this);				// Mark as managed by this container
		setPage(subPage, pp);
		String hashes = helper.getContainerHashes(m_spiPage);
		m_spiPage.appendJavascript("WebUI.spiUpdateHashes(" + StringTool.strToJavascriptString(hashes, true) + ");");
		callListeners();
	}

	/**
	 * Move one shelf item back.
	 */
	public void back() throws Exception {
		int size = m_shelf.size();
		if(size <= 1) {
			//-- Empty: do a "sub" to the initial content
			handleMoveSub(getInitialContent(), getInitialContentParameters());
			return;
		}

		size--;
		clearShelf(size);											// Remove the bottommost entry
		ISpiShelvedEntry entry = m_shelf.get(size - 1);
		entry.activate(this);
		callListeners();
	}

	void activateSpiSubpage(SpiShelvedEntry entry) {
		setPage(entry.getPage(), entry.getParameters());
		DomApplication app = DomApplication.get();
		SpiPageHelper helper = new SpiPageHelper(app);
		String hashes = helper.getContainerHashes(m_spiPage);
		m_spiPage.appendJavascript("WebUI.spiUpdateHashes(" + StringTool.strToJavascriptString(hashes, true) + ");");
	}


	/**
	 * Replace the current topmost page in the container with a new page. The old page gets destroyed, so
	 * the stack size remains the same. If the page to replace is already on the stack then the stack
	 * is unwound till that page before the replace takes place; in that case the "old" entry on the stack
	 * will also be destroyed and be replaced with a new fresh page.
	 */
	public void replace(Class<? extends SubPage> spiClass, PageParameters pp) throws Exception {
		int index = findShelfEntry(spiClass, pp);
		if(index != -1) {
			//-- This page is already on the stack... Roll back to that entry 1st
			clearShelf(index);
		}
		if(m_shelf.size() == 0) {
			//-- If there's nothing then just add this.
			handleMoveSub(spiClass, pp);
			return;
		}

		clearShelf(m_shelf.size() - 1);						// Remove the topmost entry
		handleMoveSub(spiClass, pp);
	}

	/**
	 * Clear the entire shelf, and start completely anew with a fresh page on top.
	 */
	public void moveNew(Class<? extends SubPage> spiClass, PageParameters pp) throws Exception {
		clearShelf(0);										// Kill everything
		handleMoveSub(spiClass, pp);
	}

	private void callListeners() {
		for(int i = m_shelfChangedListeners.size() - 1; i >= 0; i--) {
			WeakReference<IExecute> l = m_shelfChangedListeners.get(i);
			IExecute old = l.get();
			if(old == null) {
				m_shelfChangedListeners.remove(i);
			} else {
				try {
					old.execute();
				} catch(Exception x) {
					System.err.println("Exception on shelf changed listener: " + x);
					x.printStackTrace();
				}
			}
		}
	}

	/**
	 * Add a listener, and return a method that can be called to remove it again.
	 */
	public Runnable addShelfListener(IExecute onChange) {
		WeakReference<IExecute> wr = new WeakReference<>(onChange);
		m_shelfChangedListeners.add(wr);

		return () -> {
			m_shelfChangedListeners.remove(wr);
		};
	}

	public void removeShelfListener(IExecute onChange) {
		for(int i = m_shelfChangedListeners.size() - 1; i >= 0; i--) {
			WeakReference<IExecute> l = m_shelfChangedListeners.get(i);
			IExecute old = l.get();
			if(old == null) {
				m_shelfChangedListeners.remove(i);
			} else if(old == onChange) {
				m_shelfChangedListeners.remove(i);
				return;
			}
		}
	}

	/**
	 * The current shelf.
	 */
	public List<ISpiShelvedEntry> getShelf() {
		return m_shelf;
	}

	/**
	 * Called from shelf items when a breadcrumb wants the thing to become selected.
	 */
	public void selectShelvedEntry(ISpiShelvedEntry shelvedEntry) throws Exception {
		int index = m_shelf.indexOf(shelvedEntry);				// Can we find the thing?
		if(index == -1) {
			//-- Not found. We cannot do anything, really.
			MsgBox.info(m_spiPage, "I can't locate that page");
			return;
		}

		clearShelf(index + 1);							// Clear all that is above
		shelvedEntry.activate(this);
	}
}
