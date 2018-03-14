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
package to.etc.domui.component.menu;

import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.PageParameters;
import to.etc.domui.util.DomUtil;
import to.etc.webapp.nls.BundleRef;
import to.etc.webapp.nls.NlsContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A single item in the menu, as defined by the *code*.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
public class MenuItem {
	/** Required for locking purposes. */
	private MenuManager m_manager;

	private String m_id;

	private BundleRef m_msgBundle;

	private String m_labelKey;

	private String m_titleKey;

	private String m_descKey;

	private String m_searchKey;

	/** Parent location indicator by ID. */
	private String m_parentID;

	/** When adding this can be set to define the child's parent. If unset other items are checked to find a parent menu. */
	private MenuItem m_parent;

	private Class< ? extends UrlPage> m_pageClass;

	private PageParameters m_pageParameters;

	private String m_iconPath;

	private boolean m_disabled;

	private int m_order;

	private boolean m_subMenu;

	/** The list of rights the user MUST have to access this menu item. This can get delegated to the UrlPage's annotation. */
	private String[] m_requiredRights;

	private List<MenuItem> m_children = new ArrayList<MenuItem>();

	/**
	 * Once this item has been integrated in the main menu it cannot be changed anymore (except it's children)
	 */
	//	private boolean		m_complete;

	private String m_target;

	private String m_rurl;

	public MenuItem(MenuManager m) {
		m_manager = m;
	}

	/**
	 * Defines the rights that the user MUST have to see this menu item. If more than one Right is passed the user needs to possess *all* rights. When
	 * defined this overrides the rights in
	 */
	public MenuItem setRequiredRights(String... rights) {
		m_requiredRights = rights;
		return this;
	}

	public MenuItem setImage(String name) {
		m_iconPath = name;
		return this;
	}

	public MenuItem setImage(Class< ? > res, String name) {
		m_iconPath = DomUtil.getJavaResourceRURL(res, name);
		return this;
	}

	public MenuItem setLocation(MenuItem parent, int order) {
		m_parent = parent;
		m_order = order;
		return this;
	}

	private String byKey(String k) {
		Locale loc = NlsContext.getLocale();
		return m_msgBundle.getString(loc, k);
	}

	public String getId() {
		return m_id;
	}

	public void setId(String id) {
		m_id = id;
	}

	public BundleRef getMsgBundle() {
		return m_msgBundle;
	}

	public void setMsgBundle(BundleRef msgBundle) {
		m_msgBundle = msgBundle;
	}

	public String getLabelKey() {
		return m_labelKey;
	}

	public void setLabelKey(String labelKey) {
		m_labelKey = labelKey;
	}

	public String getDescKey() {
		return m_descKey;
	}

	public void setDescKey(String descKey) {
		m_descKey = descKey;
	}

	public String getSearchKey() {
		return m_searchKey;
	}

	public void setSearchKey(String searchKey) {
		m_searchKey = searchKey;
	}

	public String getParentID() {
		return m_parentID;
	}

	public void setParentID(String parentID) {
		m_parentID = parentID;
	}

	public Class< ? extends UrlPage> getPageClass() {
		return m_pageClass;
	}

	public MenuItem setPageClass(Class< ? extends UrlPage> pageClass) {
		m_pageClass = pageClass;
		return this;
	}

	public PageParameters getPageParameters() {
		return m_pageParameters;
	}

	public MenuItem setPageParameters(PageParameters pageParameters) {
		m_pageParameters = pageParameters;
		return this;
	}

	public String getIconPath() {
		return m_iconPath;
	}

	public void setIconPath(String iconPath) {
		m_iconPath = iconPath;
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		m_disabled = disabled;
	}

	public List<MenuItem> getChildren() {
		synchronized(m_manager) {
			return m_children;
		}
	}

	public void setChildren(List<MenuItem> children) {
		if(!m_subMenu)
			throw new IllegalStateException("Cannot add children to a LEAF item.");
		synchronized(m_manager) {
			m_children = children;
		}
	}

	public String getSearchString() {
		return byKey(m_searchKey);
	}

	public String[] getRequiredRights() {
		return m_requiredRights;
	}

	public String getLabel() {
		return byKey(m_labelKey);
	}

	public String getDescription() {
		return byKey(m_descKey);
	}

	public boolean isSubMenu() {
		return m_subMenu;
	}

	public void setSubMenu(boolean subMenu) {
		m_subMenu = subMenu;
	}

	public int getOrder() {
		return m_order;
	}

	public void setOrder(int order) {
		m_order = order;
	}

	public String getTitleKey() {
		return m_titleKey;
	}

	public void setTitleKey(String titleKey) {
		m_titleKey = titleKey;
	}

	public MenuItem getParent() {
		return m_parent;
	}

	void setParent(MenuItem parent) {
		m_parent = parent;
	}

	public String getTarget() {
		return m_target;
	}

	public MenuItem setTarget(String target) {
		m_target = target;
		return this;
	}

	public String getRURL() {
		return m_rurl;
	}

	public MenuItem setRURL(String rurl) {
		m_rurl = rurl;
		return this;
	}
}
