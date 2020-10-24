package to.etc.domui.dom.html;

import org.jetbrains.annotations.NotNull;
import to.etc.util.StringTool;
import to.etc.webapp.query.QDataContextFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-10-20.
 */
abstract public class SpiPage extends UrlPage {
	private final Map<String, SpiContainer> m_containerMap = new HashMap<>();

	@Override abstract public void createContent() throws Exception;

	public void registerContainer(String containerName, NodeContainer container, Class<? extends SubPage> initialContent) {
		if(!StringTool.isValidJavaIdentifier(containerName))
			throw new IllegalStateException("Invalid container name: must follow the rules for a Java identifier");
		if(null != m_containerMap.put(containerName.toLowerCase(), new SpiContainer(container, containerName, initialContent)))
			throw new IllegalStateException("Duplicate container name: " + containerName);
	}

	@Override protected void afterCreateContent() throws Exception {
		if(m_containerMap.size() == 0)
			throw new IllegalStateException("You need to register content containers using registerContainer inside your createContent method");
		for(Entry<String, SpiContainer> entry : m_containerMap.entrySet()) {
			String name = entry.getKey();
			SpiContainer container = entry.getValue();
			SubPage subPage = container.getInitialContent().newInstance();
			container.getContainer().add(subPage);
		}
		super.afterCreateContent();
	}

	@NotNull @Override final public QDataContextFactory getSharedContextFactory(@NotNull String key) {
		throw new IllegalStateException("You are not allowed to use a shared context for an SPI root page; that would cause the connection to last for the life time of the session!");
	}
}
