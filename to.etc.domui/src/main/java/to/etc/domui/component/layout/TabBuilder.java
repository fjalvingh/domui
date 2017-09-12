package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * TabBuilder used for building tabs.
 *
 * @author <a href="mailto:marc.mol@itris.nl">Marc Mol</a>
 * @since Nov 20, 2014
 */
public class TabBuilder {

	@Nonnull
	private TabPanelBase m_tabPanelBase;

	@Nonnull
	private TabInstance m_tabInstance;

	/** The position where the tab should be opened in the row of tabs. **/
	private int m_position;

	protected TabBuilder(@Nonnull TabPanelBase tabPanelBase, @Nonnull final TabInstance tabInstance) {
		m_tabInstance = tabInstance;
		m_tabPanelBase = tabPanelBase;
	}

	@Nonnull
	public TabBuilder label(@Nonnull final String label) {
		TextNode tn = new TextNode(label);
		m_tabInstance.setLabel(tn);
		return this;
	}

	@Nonnull
	public TabBuilder content(@Nonnull final NodeBase content) {
		m_tabInstance.setContent(content);
		return this;
	}

	@Nonnull
	public TabBuilder image(@Nonnull final String image) {
		m_tabInstance.setImage(image);
		return this;
	}

	@Nonnull
	public TabBuilder tab(@Nonnull final Li tab) {
		m_tabInstance.setTab(tab);
		return this;
	}

	@Nonnull
	public TabBuilder lazy() {
		m_tabInstance.setLazy(true);
		return this;
	}

	/**
	 * The position where the tab should be opened in the row of tabs.
	 *
	 * @return
	 */
	@Nonnull
	public TabBuilder positionTab(int position) {
		m_position = position;
		return this;
	}

	@Nonnull
	public TabBuilder closable() {
		m_tabInstance.closable(true);
		return this;
	}

	@Nonnull
	public ITabHandle build() {

		m_tabPanelBase.addTabToPanel(m_tabInstance, m_position);
		return m_tabInstance;
	}
}
