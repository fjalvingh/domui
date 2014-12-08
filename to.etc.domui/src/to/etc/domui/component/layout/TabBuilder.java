package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.component.layout.TabPanelBase.TabInstance;
import to.etc.domui.dom.html.*;

/**
 * TabBuilder used for building tabs.
 *
 * @author <a href="mailto:marc.mol@itris.nl">Marc Mol</a>
 * @since Nov 20, 2014
 */
public class TabBuilder {

	@Nonnull
	private TabInstance m_tabInstance;

	@Nullable
	private NodeBase m_label;

	@Nullable
	private NodeBase m_content;

	@Nullable
	private String m_imageLocation;

	@Nullable
	private Li m_tab;

	private boolean m_lazy;

	private boolean m_added;

	private boolean m_closable;

	protected TabBuilder(@Nonnull final TabInstance tabInstance) {
		m_tabInstance = tabInstance;
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
	public TabBuilder imageLocation(@Nonnull final String imageLocation) {
		Img image = TabPanelBase.createIcon(imageLocation);
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

	@Nonnull
	public TabBuilder closable() {
		m_tabInstance.closable(true);
		return this;
	}

	@Nonnull
	public ITabHandle build() {

		return m_tabInstance;
	}
}
