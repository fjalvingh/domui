package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.SpiPageHelper;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.util.Constants;
import to.etc.domui.util.ISpiContainerName;
import to.etc.util.StringTool;
import to.etc.webapp.query.QDataContextFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static to.etc.domui.util.DomUtil.nullChecked;

/**
 * This is the base class to extend for SPI applications. This base class
 * handles the bookmark-based navigation chores for a SPI application, and
 * it forms the basis for defining the "content area's" inside an SPI app.
 *
 * Basically, a DomUI SPI page is a single UrlPage which has one or more
 * "content area's" defined inside it. The actual pages to show are instances
 * of SubPage, and are put inside those content area's by swapping out
 * earlier content with new content.
 *
 * <h2>Relevant documentation</h2>
 * <ul>
 *     <li>https://stackoverflow.com/questions/26088849/url-fragment-allowed-characters</li>
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-10-20.
 */
abstract public class SpiPage extends UrlPage {
	private final Map<String, SpiContainer> m_containerMap = new HashMap<>();

	@Override abstract public void createContent() throws Exception;

	public void registerContainer(@NonNull ISpiContainerName containerName, @NonNull NodeContainer container, @NonNull Class<? extends SubPage> initialContent) {
		registerContainer(containerName, container, initialContent, null);
	}

	public void registerContainer(@NonNull ISpiContainerName containerName, @NonNull NodeContainer container, @NonNull Class<? extends SubPage> initialContent, @Nullable IPageParameters initialContentParameters) {
		if(!StringTool.isValidJavaIdentifier(containerName.name()))
			throw new IllegalStateException("Invalid container name: must follow the rules for a Java identifier");
		if(null != m_containerMap.put(containerName.name().toLowerCase(), new SpiContainer(container, containerName, initialContent, initialContentParameters)))
			throw new IllegalStateException("Duplicate container name: " + containerName);
	}

	@Override protected void afterCreateContent() throws Exception {
		if(m_containerMap.size() == 0)
			throw new IllegalStateException("You need to register content containers using registerContainer inside your createContent method");

		//-- Do we have a hash from the session (login)
		String hashes = (String) getPage().getConversation().getWindowSession().getAttribute(Constants.APPSESSION_FRAGMENT);
		if(hashes != null && hashes.length() != 0) {
			getPage().getConversation().getWindowSession().setAttribute(Constants.APPSESSION_FRAGMENT, null);
			new SpiPageHelper(DomApplication.get()).loadSpiFragmentFromHashes(this, hashes);
		} else {
			for(SpiContainer container : m_containerMap.values()) {
				SubPage subPage = container.getInitialContent().newInstance();
				IPageParameters pp = container.getInitialContentParameters();
				if(null != pp) {
					DomApplication.get().getInjector().injectPageValues(subPage, nullChecked(pp));
				}
				container.setPage(subPage, pp);
			}
		}

		SpiPageHelper helper = new SpiPageHelper(DomApplication.get());
		String newHashes = helper.getContainerHashes(this);
		appendJavascript("WebUI.spiUpdateHashes(" + StringTool.strToJavascriptString(newHashes, true) + ");");

		super.afterCreateContent();
	}

	@NotNull @Override final public QDataContextFactory getSharedContextFactory(@NotNull String key) {
		throw new IllegalStateException("You are not allowed to use a shared context for an SPI root page; that would cause the connection to last for the life time of the session!");
	}

	@NonNull
	public List<SpiContainer> getContainers() {
		return new ArrayList<>(m_containerMap.values());
	}

	@Nullable
	public SpiContainer findSpiContainerByName(String name) {
		name = name.toLowerCase();
		return m_containerMap.get(name);
	}
}
