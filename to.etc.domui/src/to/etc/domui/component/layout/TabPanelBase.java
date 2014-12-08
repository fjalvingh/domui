/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.layout;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class TabPanelBase extends Div {

	//vmijic 20090923 TabInstance can be registered as ErrorMessageListener in case when TabPanel has m_markErrorTabs set.
	protected class TabInstance implements IErrorMessageListener, ITabHandle {

		private NodeBase m_label;

		private NodeBase m_content;

		private Img m_img;

		private Li m_tab;

		private Li m_separator;

		private boolean m_lazy;

		private boolean m_added;

		private boolean m_closable;

		private List<UIMessage> m_msgList = new ArrayList<UIMessage>();

		public TabInstance() {}

		public TabInstance(NodeBase label, NodeBase content, Img img) {
			m_label = label;
			m_content = content;
			m_img = img;
		}

		public NodeBase getContent() {
			return m_content;
		}

		public void setContent(@Nonnull NodeBase content) {
			m_content = content;
		}

		public NodeBase getLabel() {
			return m_label;
		}

		public void setLabel(@Nonnull NodeBase label) {
			m_label = label;
		}

		public Li getTab() {
			return m_tab;
		}

		public void setTab(@Nonnull Li tab) {
			m_tab = tab;
		}

		public Li getSeparator() {
			return m_separator;
		}

		public void setSeparator(@Nonnull Li separator) {
			m_separator = separator;
		}

		public Img getImg() {
			return m_img;
		}

		public void setImage(@Nonnull Img image) {
			m_img = image;
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
		public void errorMessageAdded(@Nonnull UIMessage m) {
			if(isPartOfContent(m.getErrorNode())) {
				if(m_msgList.contains(m))
					return;
				m_msgList.add(m);
				adjustUI();
			}
		}

		@Override
		public void errorMessageRemoved(@Nonnull UIMessage m) {
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
		public void onTabSelected(TabPanelBase tabPanel, int oldTabIndex, int newTabIndex) throws Exception;
	}

	private List<TabInstance> m_tablist = new ArrayList<TabInstance>();

	/** The index for the currently visible tab. */
	private int m_currentTab;

	/** In case that it is set through constructor TabPanel would mark tabs that contain errors in content */
	private boolean m_markErrorTabs = false;

	private ITabSelected m_onTabSelected;

	private NodeContainer m_contentContainer;

	protected TabPanelBase(boolean markErrorTabs) {
		m_markErrorTabs = markErrorTabs;
		if(markErrorTabs)
			setErrorFence();
	}

	protected void renderTabPanels(NodeContainer labelcontainer, NodeContainer contentcontainer) {
		m_contentContainer = contentcontainer;
		int index = 0;

		if(m_tablist.isEmpty()) {
			contentcontainer.remove();
		}

		for(TabInstance ti : m_tablist) {
			renderLabel(labelcontainer, index, ti);
			boolean isselected = getCurrentTab() == index;
			//-- Add the body to the tab's main div, except if it is lazy.
			NodeBase content = ti.getContent();
			content.addCssClass("ui-tab-pg");
			content.setClear(ClearType.BOTH);

			if(!ti.isLazy() || isselected) {
				ti.setAdded(true);
				contentcontainer.add(content);
				if(isselected) {
					content.setDisplay(DisplayType.BLOCK);
					if(content instanceof IDisplayedListener) {
						((IDisplayedListener) content).onDisplayStateChanged(false);
					}
				} else {
					content.setDisplay(DisplayType.NONE);
				}
			}
			index++;
		}
	}

	protected void renderLabel(final NodeContainer into, final int index, TabInstance ti) {
		Li li = ti.getTab();
		Li separator = new Li();
		separator.setCssClass("ui-tab-ibt");
		if(li == null || !li.isAttached()) {
			li = new Li();
			li.setCssClass("ui-tab-li");
			into.add(separator);
			into.add(li);
			ti.setTab(li); 					// Save for later use,
			ti.setSeparator(separator);		// Save for later use,
			if(index == getCurrentTab()) {
				li.addCssClass("ui-tab-sel");
			} else {
				li.removeCssClass("ui-tab-sel");
			}
			//li.setCssClass(index == m_currentTab ? "ui-tab-lbl ui-tab-sel" : "ui-tab-lbl");
		}

		List<Div> divs = li.getChildren(Div.class);
		for(Div div : divs) {
			li.removeChild(div);
		}

		Div d = new Div();
		d.setCssClass("ui-tab-div");
		li.add(d);

		ATag a = new ATag();
		a.setCssClass("ui-tab-link");
		d.add(a);
		Div dt = new Div();
		a.add(dt);
		if(ti.getImg() != null)
			dt.add(ti.getImg());
		dt.add(ti.getLabel()); // Append the label.
		a.setClicked(new IClicked<ATag>() {
			@Override
			public void clicked(@Nonnull ATag b) throws Exception {
				setCurrentTab(index);
			}
		});

		if(ti.isCloseable()) {
			li.removeCssClass("ui-tab-li");
			li.addCssClass("ui-tab-close-li");
			ATag x = new ATag();
			dt.add(x);
			Div ds = new Div();
			x.add(ds);
			ds.setCssClass("ui-tab-close");
			x.setClicked(new IClicked<ATag>() {
				@Override
				public void clicked(@Nonnull ATag b) throws Exception {
					closeCurrentTab(into, index);
				}

			});
		}
	}

	/**
	 * Close the current tab
	 *
	 * @param index
	 * @throws Exception
	 */
	private void closeCurrentTab(@Nonnull final NodeContainer into, int index) throws Exception {

		// Check for a silly index
		if(index < 0 || index >= m_tablist.size()) {
			throw new IllegalArgumentException("Invalid index for closing a tab.");
		}

		TabInstance ti = m_tablist.get(index);
		NodeBase nbTab = ti.getTab();
		nbTab.remove();
		NodeBase nbSep = ti.getSeparator();
		nbSep.remove();
		NodeBase nbCon = ti.getContent();
		nbCon.remove();

		m_tablist.remove(index);

		if (!m_tablist.isEmpty()) {
			if(index == getCurrentTab()) {
				// Current tab is removed, select another one
				int newCurrentTab = selectNewCurrentTab(index);
				internalSetCurrentTab(newCurrentTab);
				TabInstance newti = m_tablist.get(newCurrentTab);
				NodeBase newc = newti.getContent();
				newc.setDisplay(DisplayType.BLOCK);
				newti.getTab().addCssClass("ui-tab-sel");
			} else if(index < getCurrentTab()) {
				// Current tab remains active. One tabinstance removed before the current one
				internalSetCurrentTab(getCurrentTab() - 1);
			}
		}

		if(isBuilt()) {
			renderTabPanels(into, m_contentContainer);
		}
	}

	/**
	 * Select the new current tab
	 */
	private int selectNewCurrentTab(int index) {

		if(index == m_tablist.size()) {
			return --index;
		}
		return index;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Adding tabs.										*/
	/*--------------------------------------------------------------*/

	/**
	 * Adding a tabInstance by use of the {@link TabBuilder}
	 *
	 * @return
	 */
	@Nonnull
	public TabBuilder add() {
		TabInstance tabInstance = new TabInstance();
		TabBuilder tabBuilder = new TabBuilder(tabInstance);
		addTabToPanel(tabInstance);
		return tabBuilder;
	}

	private void addTabToPanel(@Nonnull final TabInstance tabInstance) {
		if(m_markErrorTabs) {
			DomUtil.getMessageFence(this).addErrorListener(tabInstance);
		}
		m_tablist.add(tabInstance);
		if(!isBuilt())
			return;

		forceRebuild();
	}

	public void add(NodeBase content, String label) {
		add(content, label, false);
	}

	public void add(NodeBase content, String label, boolean lazy) {
		TextNode tn = new TextNode(label);
		add(content, tn, lazy);
	}

	public void add(NodeBase content, String label, String icon) {
		add(content, label, icon, false);
	}

	public void add(NodeBase content, String label, String icon, boolean lazy) {
		TextNode tn = new TextNode(label);
		add(content, tn, icon);
	}
	/**
	 * Add a tab page with a complex label part.
	 * @param content
	 * @param tablabel
	 */
	public void add(NodeBase content, NodeBase tablabel) {
		add(content, tablabel, false);
	}

	/**
	 * Add a tab page with a complex label part.
	 * @param content
	 * @param tablabel
	 */
	public void add(NodeBase content, NodeBase tablabel, boolean lazy) {
		TabInstance tabInstance = new TabInstance(tablabel, content, null);
		tabInstance.setLazy(lazy);
		addTabToPanel(tabInstance);
	}

	public void add(NodeBase content, NodeBase tablabel, String icon) {
		add(content, tablabel, icon, false);
	}

	public void add(NodeBase content, NodeBase tablabel, String icon, boolean lazy) {
		TabInstance tabInstance = new TabInstance(tablabel, content, createIcon(icon));
		tabInstance.setLazy(lazy);
		addTabToPanel(tabInstance);
	}

	@Override
	protected void onForceRebuild() {
		super.onForceRebuild();

		for(TabInstance ti : m_tablist) {
			if(ti.isLazy()) {
				ti.setAdded(false);
			}
		}
	}

	protected static Img createIcon(String icon) {
		Img i = new Img();
		i.setSrc(icon);
		i.setCssClass("ui-tab-icon");
		i.setBorder(0);
		return i;
	}

	public int getCurrentTab() {
		return m_currentTab;
	}

	protected void internalSetCurrentTab(int index) {
		m_currentTab = index;
	}

	public boolean setCurrentTab(ITabHandle tabHandle) throws Exception {

		if(!(tabHandle instanceof TabInstance))
			throw new IllegalArgumentException("Only instance of TabInstance can be used for setting the current tab.");

		TabInstance ti = (TabInstance) tabHandle;

		int index = m_tablist.indexOf(ti);
		if(index == -1) {
			return false;
		}
		setCurrentTab(index);
		return true;

	}
	public void setCurrentTab(int index) throws Exception {
		//		System.out.println("Switching to tab " + index);
		if(index == getCurrentTab() || index < 0 || index >= m_tablist.size())			// Silly index
			return;
		if(isBuilt()) {
			//-- We must switch the styles on the current "active" panel and the current "old" panel
			int oldIndex = getCurrentTab();
			TabInstance oldti = m_tablist.get(getCurrentTab());		// Get the currently active instance,
			TabInstance newti = m_tablist.get(index);
			NodeBase oldc = oldti.getContent();
			oldc.setDisplay(DisplayType.NONE);		// Switch displays on content
			NodeBase newc = newti.getContent();

			//-- Add the new thing if it was lazy.
			if(newti.isLazy() && !newti.isAdded()) {
				m_contentContainer.add(newc);
				newti.setAdded(true);
			}

			newc.setDisplay(DisplayType.BLOCK);
			oldti.getTab().removeCssClass("ui-tab-sel"); 			// Remove selected indicator
			newti.getTab().addCssClass("ui-tab-sel");
			if(m_onTabSelected != null) {
				m_onTabSelected.onTabSelected(this, oldIndex, index);
			}

			if(oldti instanceof IDisplayedListener) {
				((IDisplayedListener) oldti).onDisplayStateChanged(false);
			}
			if(newti instanceof IDisplayedListener) {
				((IDisplayedListener) oldti).onDisplayStateChanged(true);
			}

//			appendJavascript("$(window).trigger('resize');");
		}
		m_currentTab = index;										// ORDERED!!! Must be below the above!!!
	}

	public int getTabCount() {
		return m_tablist.size();
	}


	public void setOnTabSelected(ITabSelected onTabSelected) {
		m_onTabSelected = onTabSelected;
	}

	public ITabSelected getOnTabSelected() {
		return m_onTabSelected;
	}

	public int getTabIndex(NodeBase tabContent) {
		for(TabInstance tab : m_tablist) {
			if(tab.getContent() == tabContent) {
				return m_tablist.indexOf(tab);
			}
		}
		return -1;
	}

	protected void replaceLabel(NodeContainer into, NodeBase tabContent, String tabLabel, String tabIcon) {
		int index = getTabIndex(tabContent);
		if(index == -1) {
			return;
		}
		TabInstance tab = m_tablist.get(index);
		tab.setLabel(new TextNode(tabLabel));
		tab.setImage(createIcon(tabIcon));
		renderLabel(into, index, tab);
	}
}
