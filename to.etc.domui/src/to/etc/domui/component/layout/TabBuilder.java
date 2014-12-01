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

	private NodeBase m_label;

	private NodeBase m_content;

	private String m_imageName;

	private Li m_tab;

	private boolean m_lazy;

	private boolean m_added;

	private boolean m_closable;

	public TabBuilder(final NodeBase content) {
		m_content = content;
	}

	public TabBuilder label(String label) {
		TextNode tn = new TextNode(label);
		m_label = tn;
		return this;
	}

	public TabBuilder imageName(String imageName) {
		m_imageName = imageName;
		return this;
	}

	public TabBuilder tab(Li tab) {
		m_tab = tab;
		return this;
	}

	public TabBuilder lazy() {
		m_lazy = true;
		return this;
	}

	public TabBuilder added() {
		m_added = true;
		return this;
	}

	public TabBuilder closable() {
		m_closable = true;
		return this;
	}

	protected TabInstance build() {

		Img image = TabPanelBase.createIcon(m_imageName);
		TabInstance tabInstance = new TabInstance(m_label, m_content, image);

		if(m_lazy) {
			tabInstance.setLazy(true);
		}
		if(m_added) {
			tabInstance.setAdded(true);
		}
		if(m_closable) {
			tabInstance.setClosable(true);
		}
		return tabInstance;
	}
}
