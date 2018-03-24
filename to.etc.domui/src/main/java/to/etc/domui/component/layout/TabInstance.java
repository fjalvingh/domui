package to.etc.domui.component.layout;

import to.etc.domui.component.event.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.ProgrammerErrorException;

import javax.annotation.*;
import java.util.*;

/**
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs, Vladimir Mijic</a>
 * vmijic 20090923 TabInstance can be registered as ErrorMessageListener in case when TabPanel has m_markErrorTabs set.
 * Created on 18-8-17.
 */
@DefaultNonNull
final public class TabInstance implements IErrorMessageListener, ITabHandle {
	private final TabPanelBase m_tabPanel;

	@Nonnull
	private NodeBase m_label;

	@Nonnull
	private NodeBase m_content;

	@Nullable
	private String m_image;

	@Nullable
	private Li m_tab;

	@Nullable
	private Li m_separator;

	private boolean m_lazy;

	private boolean m_added;

	private boolean m_closable;

	private final List<UIMessage> m_msgList = new ArrayList<UIMessage>();

	@Nullable
	private INotify<ITabHandle> m_onClose;

	@Nullable
	private INotify<ITabHandle> m_onDisplay;

	@Nullable
	private INotify<ITabHandle> m_onHide;

	public TabInstance(TabPanelBase tabPanel, TabBuilder b) {
		m_tabPanel = tabPanel;
		NodeBase content = b.getContent();
		if(null == content)
			throw new ProgrammerErrorException("Tab panel must have content");
		m_content = content;
		NodeBase label = b.getLabel();
		if(null == label)
			throw new ProgrammerErrorException("Tab panel must have a label");
		m_label = label;
		m_image = b.getImage();
		m_lazy = b.isLazy();
		m_closable = b.isClosable();
		m_onClose = b.getOnClose();
		m_onDisplay = b.getOnDisplay();
		m_onHide = b.getOnHide();
	}

	public NodeBase getContent() {
		return m_content;
	}

	public NodeBase getLabel() {
		return m_label;
	}

	@Nullable
	Li getTab() {
		return m_tab;
	}

	void setTab(Li tab) {
		m_tab = tab;
	}

	@Nullable
	Li getSeparator() {
		return m_separator;
	}

	void setSeparator(Li separator) {
		m_separator = separator;
	}

	@Nullable public String getImage() {
		return m_image;
	}

	//@Nullable
	//public Img getImg() {
	//	return m_img;
	//}
	//
	//public void setImage(Img image) {
	//	m_img = image;
	//}
	//
	//public void setImage(String image) {
	//	if(image.isEmpty()) {
	//		return; // If string is empty, we do not have to create an image.
	//	}
	//	Img img = createIcon(image);
	//	m_img = img;
	//}

	public boolean isLazy() {
		return m_lazy;
	}

	//public void setLazy(boolean lazy) {
	//	m_lazy = lazy;
	//}

	boolean isAdded() {
		return m_added;
	}

	void setAdded(boolean added) {
		m_added = added;
	}

	/**
	 * If true this tab can be closed. A cross is added.
	 *
	 * @return
	 */
	public boolean isCloseable() {
		return m_closable;
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

	@Override
	public void errorMessageAdded(UIMessage m) {
		if(isPartOfContent(m.getErrorNode())) {
			if(m_msgList.contains(m))
				return;
			m_msgList.add(m);
			adjustUI();
		}
	}

	@Override
	public void errorMessageRemoved(UIMessage m) {
		if(isPartOfContent(m.getErrorNode())) {
			if(!m_msgList.remove(m))
				return;
			adjustUI();
		}
	}

	/**
	 * Returns T if the node passed - or any of it's parents - is part of this content area.
	 *
	 * @param errorNode
	 * @return
	 */
	final private boolean isPartOfContent(@Nullable NodeBase errorNode) {
		while(errorNode != null) {
			if(errorNode == m_content) {
				return true;
			}
			if(!errorNode.hasParent())
				return false;
			errorNode = errorNode.getParent();
		}
		return false;
	}

	private void adjustUI() {
		Li tab = getTab();
		if(tab != null) {
			final String errorCssClass = "ui-tab-err";
			if(hasErrors()) {
				tab.addCssClass(errorCssClass);
			} else {
				tab.removeCssClass(errorCssClass);
			}
		}
	}

	public boolean hasErrors() {
		return m_msgList.size() > 0;
	}

	public TabPanelBase getTabPanel() {
		return m_tabPanel;
	}

	@Override public void close() throws Exception {
		m_tabPanel.closeTab(this);
	}

	@Override public void select() throws Exception {
		m_tabPanel.setCurrentTab(this);
	}

	@Override
	public void updateLabel(@Nonnull String name, @Nullable String image) {
		m_label = new TextNode(name);
		m_image = image;
		m_tabPanel.updateLabel(this);
	}

	@Override public void updateLabel(@Nonnull NodeBase label, @Nullable String image) {
		m_label = label;
		m_image = image;
		m_tabPanel.updateLabel(this);
	}

	@Override public void updateContent(@Nonnull NodeContainer content) {
		NodeBase old = m_content;
		if(content == old)
			return;
		m_content = content;
		m_added = false;
		m_tabPanel.updateContent(this, old);
	}
}
