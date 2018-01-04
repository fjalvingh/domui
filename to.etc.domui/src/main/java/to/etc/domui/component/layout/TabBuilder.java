package to.etc.domui.component.layout;

import to.etc.domui.component.event.INotify;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TextNode;
import to.etc.util.StringTool;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TabBuilder used for building tabs.
 *
 * @author <a href="mailto:marc.mol@itris.nl">Marc Mol</a>
 * @since Nov 20, 2014
 */
@DefaultNonNull
final public class TabBuilder {
	private final TabPanelBase m_tabPanel;

	/** The position where the tab should be opened in the row of tabs. **/
	private int m_position = -1;

	@Nullable
	private NodeBase m_label;

	@Nullable
	private String m_image;

	@Nullable
	private NodeBase m_content;

	private boolean m_lazy;

	private boolean m_closable;

	@Nullable
	private INotify<ITabHandle> m_onClose;

	@Nullable
	private INotify<ITabHandle> m_onDisplay;

	@Nullable
	private INotify<ITabHandle> m_onHide;

	TabBuilder(TabPanelBase tabPanelBase) {
		m_tabPanel = tabPanelBase;
	}

	public TabBuilder label(@Nonnull String label) {
		m_label = new TextNode(label);
		return this;
	}

	public TabBuilder label(NodeBase node) {
		m_label = node;
		return this;
	}

	public TabBuilder content(@Nonnull NodeBase content) {
		m_content = content;
		return this;
	}

	public TabBuilder image(@Nonnull String image) {
		if(! StringTool.isBlank(image))
			m_image = image;
		return this;
	}

	public TabBuilder lazy() {
		m_lazy = true;
		return this;
	}
	public TabBuilder lazy(boolean lazy) {
		m_lazy = lazy;
		return this;
	}


	/**
	 * The position where the tab should be opened in the row of tabs.
	 */
	public TabBuilder position(int position) {
		m_position = position;
		return this;
	}

	public TabBuilder onClose(@Nullable INotify<ITabHandle> notify) {
		m_onClose = notify;
		return this;
	}

	public TabBuilder onDisplay(@Nonnull INotify<ITabHandle> notify) {
		m_onDisplay = notify;
		return this;
	}

	public TabBuilder onHide(@Nonnull INotify<ITabHandle> notify) {
		m_onHide = notify;
		return this;
	}

	@Nullable
	public INotify<ITabHandle> getOnClose() {
		return m_onClose;
	}

	@Nullable public INotify<ITabHandle> getOnDisplay() {
		return m_onDisplay;
	}

	@Nullable public INotify<ITabHandle> getOnHide() {
		return m_onHide;
	}

	@Nonnull
	public TabBuilder closable() {
		m_closable = true;
		return this;
	}

	@Nonnull
	public ITabHandle build() {
		return m_tabPanel.add(this);
	}

	public int getPosition() {
		return m_position;
	}

	@Nullable public NodeBase getLabel() {
		return m_label;
	}

	@Nullable public String getImage() {
		return m_image;
	}

	@Nullable public NodeBase getContent() {
		return m_content;
	}

	public boolean isLazy() {
		return m_lazy;
	}

	public boolean isClosable() {
		return m_closable;
	}
}
