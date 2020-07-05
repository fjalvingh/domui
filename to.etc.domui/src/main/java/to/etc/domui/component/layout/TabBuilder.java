package to.etc.domui.component.layout;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import to.etc.domui.component.event.INotify;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TextNode;

/**
 * TabBuilder used for building tabs.
 *
 * @author <a href="mailto:marc.mol@itris.nl">Marc Mol</a>
 * @since Nov 20, 2014
 */
@NonNullByDefault
final public class TabBuilder {
	private final TabPanelBase m_tabPanel;

	/** The position where the tab should be opened in the row of tabs. **/
	private int m_position = -1;

	@Nullable
	private NodeBase m_label;

	@Nullable
	private IIconRef m_image;

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

	@Nullable
	private String m_testId;

	TabBuilder(TabPanelBase tabPanelBase) {
		m_tabPanel = tabPanelBase;
	}

	public TabBuilder label(@NonNull String label) {
		m_label = new TextNode(label);
		return this;
	}

	public TabBuilder label(NodeBase node) {
		m_label = node;
		return this;
	}

	public TabBuilder content(@NonNull NodeBase content) {
		m_content = content;
		return this;
	}

	public TabBuilder image(@NonNull IIconRef image) {
		m_image = image;
		return this;
	}

	public TabBuilder testId(@NotNull String testId) {
		m_testId = testId;
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

	public TabBuilder onDisplay(@NonNull INotify<ITabHandle> notify) {
		m_onDisplay = notify;
		return this;
	}

	public TabBuilder onHide(@NonNull INotify<ITabHandle> notify) {
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

	@NonNull
	public TabBuilder closable() {
		m_closable = true;
		return this;
	}

	@NonNull
	public ITabHandle build() {
		if(m_testId != null) {
			if(m_content != null) {
				m_content.setTestID(m_testId + "_tc");
			}
		}
		return m_tabPanel.add(this);
	}

	public int getPosition() {
		return m_position;
	}

	@Nullable public NodeBase getLabel() {
		return m_label;
	}

	@Nullable public IIconRef getImage() {
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

	@Nullable
	public String getTestId() {
		return m_testId;
	}
}
