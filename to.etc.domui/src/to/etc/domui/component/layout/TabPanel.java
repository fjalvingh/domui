package to.etc.domui.component.layout;

import java.util.*;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * A panel containing multiple tabs. Each tab consists of two components: the
 * tab label component and the tab page body.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class TabPanel extends Div {
	//vmijic 20090923 TabInstance can be registered as ErrorMessageListener in case when TabPanel has m_markErrorTabs set.
	private static class TabInstance implements IErrorMessageListener {
		private NodeBase m_label;

		private NodeBase m_content;

		private Img m_img;

		//		private Img m_errorInfo;

		private Li m_tab;

		private List<UIMessage> m_msgList = new ArrayList<UIMessage>();

		public TabInstance(NodeBase label, NodeBase content, Img img) {
			m_label = label;
			m_content = content;
			m_img = img;
		}

		public NodeBase getContent() {
			return m_content;
		}

		public NodeBase getLabel() {
			return m_label;
		}

		public Li getTab() {
			return m_tab;
		}

		public void setTab(Li tab) {
			m_tab = tab;
		}

		public Img getImg() {
			return m_img;
		}

		public void errorMessageAdded(Page pg, UIMessage m) {
			if(isPartOfContent(m.getErrorNode())) {
				if(m_msgList.contains(m))
					return;
				m_msgList.add(m);
				adjustUI();
			}
		}

		public void errorMessageRemoved(Page pg, UIMessage m) {
			if(isPartOfContent(m.getErrorNode())) {
				if(!m_msgList.remove(m))
					return;
				adjustUI();
			}
		}

		private boolean isPartOfContent(NodeBase errorNode) {
			if(errorNode == null) {
				return false;
			}
			if(errorNode == m_content) {
				return true;
			}
			return isPartOfContent(errorNode.getParent());
		}

		private void adjustUI() {
			if(hasErrors()) {
				m_tab.addCssClass("ui-tab-err");
				//FIXME: this code can not work since there is refresh problem (error image is added only after refresh in browser is pressed)
				//is this same 'HTML rendering already done for visited node' bug in framework?
				//for now error image is set through css
				/*
				if(m_errorInfo == null) {
					m_errorInfo = new Img("THEME/mini-error.png");
					m_errorInfo.setTitle("Tab contain errors.");
					if(m_tab.getChildCount() > 0 && m_tab.getChild(0) instanceof ATag) {
						((ATag) m_tab.getChild(0)).add(m_errorInfo);
					}
				}
				*/
			} else {
				m_tab.removeCssClass("ui-tab-err");
				//FIXME: this code can not work since there is refresh problem (error image is added only after refresh in browser is pressed)
				//is this same 'HTML rendering already done for visited node' bug in framework?
				/*
				if(m_errorInfo != null) {
					if(m_tab.getChildCount() > 0 && m_tab.getChild(0) instanceof ATag) {
						((ATag) m_tab.getChild(0)).removeChild(m_errorInfo);
					}
					m_errorInfo = null;
				}
				*/
			}
		}

		public boolean hasErrors() {
			return m_msgList.size() > 0;
		}
	}

	/**
	 * Represents on tab selected event listener.
	 *
	 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
	 * Created on 24 Sep 2009
	 */
	public interface ITabSelected {
		public void onTabSelected(TabPanel tabPanel, int oldTabIndex, int newTabIndex) throws Exception;
	}

	private List<TabInstance> m_tablist = new ArrayList<TabInstance>();

	/** The index for the currently visible tab. */
	private int m_currentTab;

	private Ul m_tabul;

	/** In case that it is set through constructor TabPanel would mark tabs that contain errors in content */
	private boolean m_markErrorTabs = false;

	private ITabSelected m_onTabSelected;

	public TabPanel() {}

	public TabPanel(final boolean markErrorTabs) {
		m_markErrorTabs = markErrorTabs;
		if(m_markErrorTabs) {
			setErrorFence();
		}
	}

	/**
	 * Simple form for adding a tab which contains a text tabel.
	 *
	 * @param content
	 * @param label
	 */
	public void add(NodeBase content, String label) {
		TextNode tn = new TextNode(label);
		add(content, tn);
	}

	public void add(NodeBase content, String label, String icon) {
		TextNode tn = new TextNode(label);
		add(content, tn, icon);
	}

	/**
	 * Add a tab page with a complex label part.
	 * @param content
	 * @param tablabel
	 */
	public void add(NodeBase content, NodeBase tablabel) {
		TabInstance tabInstance = new TabInstance(tablabel, content, null);
		if(m_markErrorTabs) {
			DomUtil.getMessageFence(this).addErrorListener(tabInstance);
		}
		m_tablist.add(tabInstance);
		if(!isBuilt())
			return;

		//-- Render the new thingies.
	}

	public void add(NodeBase content, NodeBase tablabel, String icon) {
		Img i = new Img();
		i.setSrc(icon);
		i.setCssClass("ui-tab-icon");
		i.setBorder(0);
		TabInstance tabInstance = new TabInstance(tablabel, content, i);
		if(m_markErrorTabs) {
			DomUtil.getMessageFence(this).addErrorListener(tabInstance);
		}

		m_tablist.add(tabInstance);
		if(!isBuilt())
			return;

		//-- Render the new thingies.
	}

	/**
	 * Build the tab. We generate a "sliding window" variant where the tabs are part of an
	 * "ul"; each content pane is a div.
	 * The complete generated structure looks like:
	 * <pre><![CDATA[
	 * 	<div class="ui-tab-c">
	 *
	 *
	 *  </div>
	 * ]]></pre>
	 *
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		setCssClass("ui-tab-c");

		//-- Adjust selected tab index
		if(getCurrentTab() >= m_tablist.size())
			m_currentTab = 0;

		//-- Create the TAB structure..
		Div hdr = new Div();
		add(hdr); // The div containing the tab buttons
		hdr.setCssClass("ui-tab-hdr");
		Ul u = new Ul();
		m_tabul = u;
		hdr.add(u);
		int index = 0;
		for(TabInstance ti : m_tablist) {
			renderLabel(index, ti);

			//-- Add the body to the tab's main div.
			add(ti.getContent());
			ti.getContent().setClear(ClearType.BOTH);
			ti.getContent().setDisplay(m_currentTab == index ? DisplayType.BLOCK : DisplayType.NONE);
			ti.getContent().addCssClass("ui-tab-pg");
			index++;
		}
	}

	private void renderLabel(int index, TabInstance ti) {
		Li li = new Li();
		m_tabul.add(index, li);
		ti.setTab(li); // Save for later use,
		if(index == m_currentTab) {
			li.addCssClass("ui-tab-sel");
		} else {
			li.removeCssClass("ui-tab-sel");
		}
		//li.setCssClass(index == m_currentTab ? "ui-tab-lbl ui-tab-sel" : "ui-tab-lbl");
		ATag a = new ATag();
		li.add(a);
		if(ti.getImg() != null)
			a.add(ti.getImg());
		a.add(ti.getLabel()); // Append the label.
		final int index_ = index;
		a.setClicked(new IClicked<ATag>() {
			public void clicked(ATag b) throws Exception {
				setCurrentTab(index_);
			}
		});
	}

	public int getCurrentTab() {
		return m_currentTab;
	}

	public void setCurrentTab(int index) throws Exception {
		//		System.out.println("Switching to tab " + index);
		if(index == getCurrentTab() || index < 0 || index >= m_tablist.size()) // Silly index
			return;
		if(isBuilt()) {
			//-- We must switch the styles on the current "active" panel and the current "old" panel
			int oldIndex = getCurrentTab();
			TabInstance oldti = m_tablist.get(getCurrentTab()); // Get the currently active instance,
			TabInstance newti = m_tablist.get(index);
			oldti.getContent().setDisplay(DisplayType.NONE); // Switch displays on content
			newti.getContent().setDisplay(DisplayType.BLOCK);
			oldti.getTab().removeCssClass("ui-tab-sel"); // Remove selected indicator
			newti.getTab().addCssClass("ui-tab-sel");
			if(m_onTabSelected != null) {
				m_onTabSelected.onTabSelected(this, oldIndex, index);
			}
		}
		m_currentTab = index; // ORDERED!!! Must be below the above!!!
	}

	public void setOnTabSelected(ITabSelected onTabSelected) {
		m_onTabSelected = onTabSelected;
	}

	public ITabSelected getOnTabSelected() {
		return m_onTabSelected;
	}
}
