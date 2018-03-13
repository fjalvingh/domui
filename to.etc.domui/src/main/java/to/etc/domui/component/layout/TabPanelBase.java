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

import to.etc.domui.component.event.INotify;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.dom.css.ClearType;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.Li;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.util.DomUtil;
import to.etc.webapp.ProgrammerErrorException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TabPanelBase extends Div {
	@Nullable
	private NodeContainer m_labelContainer;

	/**
	 * Represents on tab selected event listener.
	 *
	 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
	 * Created on 24 Sep 2009
	 */
	public interface ITabSelected {
		void onTabSelected(TabPanelBase tabPanel, int oldTabIndex, int newTabIndex) throws Exception;
	}

	private List<TabInstance> m_tablist = new ArrayList<TabInstance>();

	/**
	 * The index for the currently visible tab.
	 */
	private int m_currentTab;

	/**
	 * In case that it is set through constructor TabPanel would mark tabs that contain errors in content
	 */
	private boolean m_markErrorTabs;

	private ITabSelected m_onTabSelected;

	@Nullable
	private TabBuilder m_tabBuilder;

	@Nullable
	private NodeContainer m_contentContainer;

	protected TabPanelBase(boolean markErrorTabs) {
		m_markErrorTabs = markErrorTabs;
		if(markErrorTabs)
			setErrorFence();
	}

	protected void renderTabPanels(NodeContainer labelcontainer, NodeContainer contentcontainer) throws Exception {
		if(m_tabBuilder != null)
			throw new IllegalStateException("A tab builder was created but build() was not called on it.");

		m_contentContainer = contentcontainer;
		m_labelContainer = labelcontainer;
		int index = 0;

		for(TabInstance ti : m_tablist) {
			renderLabel(index, ti);
			boolean isselected = getCurrentTab() == index;

			//-- Add the body to the tab's main div, except if it is lazy.
			NodeBase content = ti.getContent();
			content.addCssClass("ui-tab-pg");
			content.setClear(ClearType.BOTH);

			if(!ti.isLazy() || isselected) {
				ti.setAdded(true);
				contentcontainer.add(content);
				if(isselected) {
					content.setDisplay(null);
					//content.setDisplay(DisplayType.BLOCK);
					INotify<ITabHandle> onDisplay = ti.getOnDisplay();
					if(onDisplay != null)
						onDisplay.onNotify(ti);
				} else {
					content.setDisplay(DisplayType.NONE);
				}
			}
			index++;
		}
	}

	protected void renderLabel(int index, TabInstance ti) {
		NodeContainer into = Objects.requireNonNull(m_labelContainer);
		Li li = ti.getTab();
//		Li separator = new Li();
//		separator.setCssClass("ui-tab-ibt");
//		if(index == 0)
//			separator.addCssClass("ui-tab-ibt-first");
		if(li == null || !li.isAttached()) {
			li = new Li();
			if(ti.isCloseable()) {
				li.setCssClass("ui-tab-close-li");
			} else {
				li.setCssClass("ui-tab-li");
			}
//			into.add(separator);
			into.add(li);
			ti.setTab(li);                    	// Save for later use,
//			ti.setSeparator(separator);			// Save for later use,
			if(index == getCurrentTab()) {
				li.addCssClass("ui-tab-sel");
			} else {
				li.removeCssClass("ui-tab-sel");
			}
		}

		List<Div> divs = li.getChildren(Div.class);
		for(Div div : divs) {
			div.remove();
		}

		Div d = new Div();
		d.setCssClass("ui-tab-div");

		Span dt = new Span();
		d.add(dt);

		String iconUrl = ti.getImage();
		if(null != iconUrl) {
			//-- Add any icon
			if(DomUtil.isIconName(iconUrl)) {
				FaIcon icon = new FaIcon(iconUrl);
				dt.add(icon);
			} else {
				String icon = getThemedResourceRURL(iconUrl);
				Img img = new Img(icon);
				dt.add(img);
				img.setImgBorder(0);
			}
		}

		NodeBase label = ti.getLabel();
		dt.add(label);
		li.setClicked(b -> setCurrentTab(ti));

		if(ti.isCloseable()) {
			ATag x = new ATag();
			d.add(x);
			x.setCssClass("ui-tab-close");
			x.setClicked((IClicked<ATag>) b -> closeTab(ti));
		}
		li.add(d);
	}

	/**
	 * Close the given tab instance. This will call the onClose listener if present.
	 */
	public void closeTab(@Nonnull ITabHandle th) throws Exception {
		if(!(th instanceof TabInstance)) {
			throw new IllegalArgumentException("Only instance of TabInstance can be used for closing a tab.");
		}
		TabInstance ti = (TabInstance) th;
		if(ti.getTabPanel() != this)
			throw new IllegalStateException("The tab handle does not belong to this tab panel");

		// If the thing has already been closed ignore that.
		int index = m_tablist.indexOf(ti);
		if(index < 0) {
			return;
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

		rebuildIfNeeded(ti);

		//-- We do not call onHide by design.
		callOnClose(ti);
	}

	private void callOnClose(TabInstance ti) throws Exception {
		INotify<ITabHandle> getOnClose = ti.getOnClose();
		if(getOnClose != null) {
			getOnClose.onNotify(ti);
		}
	}

	private void rebuildIfNeeded(TabInstance ti) {
		if(isBuilt()) {
			Li tab = ti.getTab();
			if(tab != null)
				tab.remove();

			Li separator = ti.getSeparator();
			if(separator != null)
				separator.remove();

			NodeBase nb = ti.getContent();
			if(nb != null) {
				nb.remove();
			}
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
	 */
	@Nonnull
	public TabBuilder tab() {
		if(null != m_tabBuilder) {
			throw new ProgrammerErrorException("A new tab is already created without adding it to the panel (call the build() method on the TabBuilder)");
		}
		return m_tabBuilder = new TabBuilder(this);
	}

	@Nonnull
	TabInstance add(TabBuilder b) {
		TabInstance ti = new TabInstance(this, b);
		return addTabInstance(ti, b.getPosition());
	}

	private TabInstance addTabInstance(TabInstance ti, int pos) {
		if(pos < 0 || pos >= m_tablist.size())
			m_tablist.add(ti);
		else {
			m_tablist.add(pos, ti);
		}
		m_tabBuilder = null;
		if(isBuilt())
			forceRebuild();
		return ti;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Legacy quick add methods - prefer the builder instead		*/
	/*----------------------------------------------------------------------*/
	public ITabHandle add(NodeBase content, String label) {
		return tab().content(content).label(label).build();
	}

	public ITabHandle add(NodeBase content, String label, boolean lazy) {
		return tab().content(content).label(label).lazy(lazy).build();
	}

	public ITabHandle add(NodeBase content, String label, String icon) {
		return tab().content(content).label(label).image(icon).build();
	}

	public ITabHandle add(NodeBase content, String label, String icon, boolean lazy) {
		return tab().content(content).label(label).image(icon).lazy(lazy).build();
	}

	/**
	 * Add a tab page with a complex label part.
	 */
	public ITabHandle add(NodeBase content, NodeBase tablabel) {
		return tab().content(content).label(tablabel).build();
	}

	/**
	 * Add a tab page with a complex label part.
	 */
	public ITabHandle add(NodeBase content, NodeBase tablabel, boolean lazy) {
		return tab().content(content).label(tablabel).lazy(lazy).build();
	}

	public ITabHandle add(NodeBase content, NodeBase tablabel, String icon) {
		return tab().content(content).label(tablabel).image(icon).build();
	}

	public ITabHandle add(NodeBase content, NodeBase tablabel, String icon, boolean lazy) {
		return tab().content(content).label(tablabel).image(icon).lazy(lazy).build();
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
			if(index == getCurrentTab() || index < 0 || index >= m_tablist.size())            // Silly index
				return;
			//-- We must switch the styles on the current "active" panel and the current "old" panel
			int oldIndex = getCurrentTab();
			TabInstance oldti = m_tablist.get(getCurrentTab());        // Get the currently active instance,
			TabInstance newti = m_tablist.get(index);
			NodeBase oldc = oldti.getContent();
			oldc.setDisplay(DisplayType.NONE);					// Switch displays on content

			NodeBase newc = newti.getContent();
			if(newti.isLazy() && !newti.isAdded()) {			// Add the new thing if it was lazy.
				Objects.requireNonNull(m_contentContainer).add(newc);
				newti.setAdded(true);
			}
			//newc.setDisplay(DisplayType.BLOCK);
			newc.setDisplay(null);

			Li oldtab = oldti.getTab();
			if(null != oldtab)
				oldtab.removeCssClass("ui-tab-sel");            // Remove selected indicator

			Li newtab = newti.getTab();
			if(null != newtab)
				newtab.addCssClass("ui-tab-sel");

			if(m_onTabSelected != null) {
				m_onTabSelected.onTabSelected(this, oldIndex, index);
			}

			INotify<ITabHandle> onHide = oldti.getOnHide();
			if(null != onHide)
				onHide.onNotify(oldti);

			INotify<ITabHandle> onDisplay = newti.getOnDisplay();
			if(null != onDisplay)
				onDisplay.onNotify(newti);
//			appendJavascript("$(window).trigger('resize');");
		}
		m_currentTab = index;                                        // ORDERED!!! Must be below the above!!!
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

	public void updateLabel(TabInstance tabInstance) {
		int index = m_tablist.indexOf(tabInstance);
		if(index == -1) {
			throw new IllegalStateException("The tab instance is no longer part of the panel");
		}
		if(isBuilt())
			renderLabel(index, tabInstance);
	}

	public TabInstance getCurrentTabInstance() {
		int currentTabIndex = getCurrentTab();
		if(currentTabIndex == -1) {
			throw new IllegalStateException("There is no tab created!");
		}
		return m_tablist.get(currentTabIndex);
	}

	public void updateContent(TabInstance tabInstance, @Nonnull NodeBase old) {
		int index = m_tablist.indexOf(tabInstance);
		if(index == -1) {
			throw new IllegalStateException("The tab instance is no longer part of the panel");
		}
		if(! isBuilt())
			return;

		//-- If this is not the current tab we're done
		if(m_currentTab != index)
			return;

		//-- We need to replace the currently visible content.
		old.replaceWith(tabInstance.getContent());
		//tabInstance.getContent().setDisplay(DisplayType.BLOCK);
		tabInstance.getContent().setDisplay(null);
	}
}
