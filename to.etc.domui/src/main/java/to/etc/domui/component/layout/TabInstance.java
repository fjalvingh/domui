package to.etc.domui.component.layout;

import to.etc.domui.component.event.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;

import javax.annotation.*;
import java.util.*;

/**
 * @author <a href="mailto:yoeri.nijs@itris.nl">Yoeri Nijs, Vladimir Mijic</a>
 * vmijic 20090923 TabInstance can be registered as ErrorMessageListener in case when TabPanel has m_markErrorTabs set.
 * Created on 18-8-17.
 */
@DefaultNonNull
public class TabInstance implements IErrorMessageListener, ITabHandle {

	@Nullable
	private NodeBase m_label;

	@Nullable
	private NodeBase m_content;

	@Nullable
	private Img m_img;

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

	public TabInstance() {
		this(null, null, null);
	}

	public TabInstance(@Nullable NodeBase label, @Nullable NodeBase content, @Nullable String image) {
		m_label = label;
		m_content = content;
		if(null != image) {
			setImage(image);
		}
	}

	@Nullable
	public NodeBase getContent() {
		return m_content;
	}

	public void setContent(NodeBase content) {
		m_content = content;
	}

	@Nullable
	public NodeBase getLabel() {
		return m_label;
	}

	public void setLabel(NodeBase label) {
		m_label = label;
	}

	@Nullable
	public Li getTab() {
		return m_tab;
	}

	public void setTab(Li tab) {
		m_tab = tab;
	}

	@Nullable
	public Li getSeparator() {
		return m_separator;
	}

	public void setSeparator(Li separator) {
		m_separator = separator;
	}

	@Nullable
	public Img getImg() {
		return m_img;
	}

	public void setImage(Img image) {
		m_img = image;
	}

	public void setImage(String image) {
		if(image.isEmpty()) {
			return; // If string is empty, we do not have to create an image.
		}
		Img img = createIcon(image);
		m_img = img;
	}

	public boolean isLazy() {
		return m_lazy;
	}

	public void setLazy(boolean lazy) {
		m_lazy = lazy;
	}

	public boolean isAdded() {
		return m_added;
	}

	protected void setAdded(boolean added) {
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

	public void closable(boolean closeable) {
		m_closable = closeable;
	}

	@Override
	public void setOnClose(@Nullable INotify<ITabHandle> notify) {
		m_onClose = notify;
	}

	@Nullable
	public INotify<ITabHandle> getOnClose() {
		return m_onClose;
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

	private Img createIcon(String icon) {
		Img i = new Img();
		i.setSrc(icon);
		i.setCssClass("ui-tab-icon");
		i.setBorder(0);
		return i;
	}
}
