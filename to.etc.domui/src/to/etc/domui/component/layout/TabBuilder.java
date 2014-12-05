package to.etc.domui.component.layout;

import to.etc.domui.component.layout.TabPanelBase.TabInstance;
import to.etc.domui.dom.html.*;

/**
 * TabBuilder used for building tabs.
 *
 * @author <a href="mailto:marc.mol@itris.nl">Marc Mol</a>
 * @since Nov 20, 2014
 */
public class TabBuilder {

	private TabInstance m_tabInstance;

	private NodeBase m_label;

	private NodeBase m_content;

	private String m_imageLocation;

	private Li m_tab;

	private boolean m_lazy;

	private boolean m_added;

	private boolean m_closable;

	protected TabBuilder(final TabInstance tabInstance) {
		m_tabInstance = tabInstance;
	}

	public TabBuilder label(String label) {
		TextNode tn = new TextNode(label);
		m_tabInstance.label(tn);
		return this;
	}

	public TabBuilder content(NodeBase content) {
		m_tabInstance.content(content);
		return this;
	}

	public TabBuilder imageLocation(String imageLocation) {
		m_tabInstance.imageLocation(imageLocation);
		return this;
	}

	public TabBuilder tab(Li tab) {
		m_tabInstance.tab(tab);
		return this;
	}

	public TabBuilder lazy() {
		m_tabInstance.lazy(true);
		return this;
	}

	public TabBuilder closable() {
		m_tabInstance.closable(true);
		return this;
	}

	public ITabHandle build() {

		Img image = TabPanelBase.createIcon(m_imageLocation);
		m_tabInstance.image(image);

		return m_tabInstance;
	}
}
