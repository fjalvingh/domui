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

import to.etc.domui.component.event.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class TabPanelBase extends Div {

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

	@Nullable
	private TabBuilder m_tabBuilder;

	private NodeContainer m_contentContainer;

	protected TabPanelBase(boolean markErrorTabs) {
		m_markErrorTabs = markErrorTabs;
		if(markErrorTabs)
			setErrorFence();
	}

	protected void renderTabPanels(NodeContainer labelcontainer, NodeContainer contentcontainer) {
		m_contentContainer = contentcontainer;
		int index = 0;

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

	protected void renderLabel(final NodeContainer into, final int index, final TabInstance ti) {
		Li li = ti.getTab();
		Li separator = new Li();
		separator.setCssClass("ui-tab-ibt");
		if(index == 0)
			separator.addCssClass("ui-tab-ibt-first");
		if(li == null || !li.isAttached()) {
			li = new Li();
			if(ti.isCloseable()) {
				li.setCssClass("ui-tab-close-li");
			} else {
				li.setCssClass("ui-tab-li");
			}
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
			div.remove();
		}

		Div d = new Div();
		d.setCssClass("ui-tab-div");

		Span dt = new Span();
		d.add(dt);
		if(ti.getImg() != null)
			dt.add(ti.getImg());
		dt.add(ti.getLabel()); // Append the label.
		d.setClicked(new IClicked<Div>() {
			@Override
			public void clicked(@Nonnull Div b) throws Exception {
				setCurrentTab(ti);
			}
		});

		if(ti.isCloseable()) {
			ATag x = new ATag();
			d.add(x);
			x.setCssClass("ui-tab-close");
			x.setClicked(new IClicked<ATag>() {
				@Override
				public void clicked(@Nonnull ATag b) throws Exception {
					closeTab(ti);
				}
			});
		}
		li.add(d);
	}

	/**
	 * Close the given tab instance.
	 *
	 * @param into
	 * @param index
	 * @throws Exception
	 */
	public void closeTab(@Nonnull final ITabHandle th) throws Exception {
		if(!(th instanceof TabInstance)) {
			throw new IllegalArgumentException("Only instance of TabInstance can be used for closing a tab.");
		}

		TabInstance ti = (TabInstance) th;

		// Check for a silly index
		int index = m_tablist.indexOf(ti);
		if (index < 0) {
			throw new IllegalArgumentException("Invalid index for closing a tab.");
		}

		if(index == getCurrentTab()) {
			int newIndex = selectNewCurrentTab(index);
			setCurrentTab(newIndex);
		}
		if(index < getCurrentTab()) {
			m_currentTab--;
		}
		ti.setAdded(false);
		m_tablist.remove(index);

		if(isBuilt()) {
			ti.getTab().remove();
			ti.getSeparator().remove();
			ti.getContent().remove();
		}

		if (ti.getOnClose() != null) {
			ti.getOnClose().onNotify(ti);
		}
	}

	/**
	 * Select the new current tab
	 */
	private int selectNewCurrentTab(int index) {

		if(m_tablist.size() == 1) {
			return 0;
		}

		if(index > 0) {
			return --index;
		}

		return ++index;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Adding tabs.										*/
	/*--------------------------------------------------------------*/

	/**
	 * Adding a tabInstance by use of the {@link TabBuilder}
	 *
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public TabBuilder tab() throws Exception {

		if(null != m_tabBuilder) {
			throw new Exception("A new tab is already created without adding it to the panel (call the build() method on the TabBuilder)");
		}
		m_tabBuilder = new TabBuilder(this, new TabInstance());
		return m_tabBuilder;
	}

	void addTabToPanel(@Nonnull final TabInstance tabInstance, int position) {

		m_tabBuilder = null;
		if(m_markErrorTabs) {
			DomUtil.getMessageFence(this).addErrorListener(tabInstance);
		}
		if(position == 0)
			m_tablist.add(tabInstance);
		else
			m_tablist.add(position, tabInstance);

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
		add(content, tn, icon, lazy);
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
		addTabToPanel(tabInstance, 0);
	}

	public void add(NodeBase content, NodeBase tablabel, String icon) {
		add(content, tablabel, icon, false);
	}

	public void add(NodeBase content, NodeBase tablabel, String icon, boolean lazy) {
		TabInstance tabInstance = new TabInstance(tablabel, content, icon);
		tabInstance.setLazy(lazy);
		addTabToPanel(tabInstance, 0);
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

	public int getCurrentTab() {
		return m_currentTab;
	}

	protected void internalSetCurrentTab(int index) {
		m_currentTab = index;
	}

	public boolean setCurrentTab(@Nonnull final ITabHandle tabHandle) throws Exception {

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

	private void setCurrentTab(@Nonnull final TabInstance ti) throws Exception {

		int index = m_tablist.indexOf(ti);
		setCurrentTab(index);
	}

	public void setCurrentTab(int index) throws Exception {

		//		System.out.println("Switching to tab " + index);
		if(isBuilt()) {
			if(index == getCurrentTab() || index < 0 || index >= m_tablist.size())			// Silly index
				return;
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
		tab.setImage(tabIcon);
		renderLabel(into, index, tab);
	}

	public TabInstance getCurrentTabInstance() {
		int currentTabIndex = getCurrentTab();
		if(currentTabIndex == -1) {
			throw new IllegalStateException("There is no tab created!");
		}
		return m_tablist.get(currentTabIndex);
	}
}
