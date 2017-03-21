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

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * A single item in the menu, as defined by the *code*.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
public class MenuItemImpl implements IMenuItem {
	/** Required for locking purposes. */
	private final MenuManager m_manager;

	private String m_id;

	private BundleRef m_msgBundle;

	private String m_labelKey;

	private String m_titleKey;

	private String m_descKey;

	private String m_searchKey;

	/** Parent location indicator by ID. */
	private String m_parentID;

	/** When adding this can be set to define the child's parent. If unset other items are checked to find a parent menu. */
	private MenuItemImpl m_parent;

	private Class< ? extends UrlPage> m_pageClass;

	private PageParameters m_pageParameters;

	private String m_iconPath;

	private boolean m_disabled;

	private int m_order;

	private boolean m_subMenu;

	/** The list of rights the user MUST have to access this menu item. This can get delegated to the UrlPage's annotation. */
	private String[] m_requiredRights;

	private List<IMenuItem> m_children = new ArrayList<IMenuItem>();

	/**
	 * Once this item has been integrated in the main menu it cannot be changed anymore (except it's children)
	 */
	//	private boolean		m_complete;

	private String m_target;

	private String m_rurl;

	public MenuItemImpl(final MenuManager m) {
		m_manager = m;
	}

	/**
	 * Defines the rights that the user MUST have to see this menu item. If more than one Right is passed the user needs to possess *all* rights. When
	 * defined this overrides the rights in
	 * @param name
	 * @return
	 */
	public MenuItemImpl setRequiredRights(final String... rights) {
		m_requiredRights = rights;
		return this;
	}

	public MenuItemImpl setImage(final String name) {
		m_iconPath = name;
		return this;
	}

	public MenuItemImpl setImage(final Class< ? > res, final String name) {
		m_iconPath = DomUtil.getJavaResourceRURL(res, name);
		return this;
	}

	public MenuItemImpl setLocation(final MenuItemImpl parent, final int order) {
		m_parent = parent;
		m_order = order;
		return this;
	}

	private String byKey(final String k) {
		Locale loc = NlsContext.getLocale();
		return m_msgBundle.getString(loc, k);
	}

	/**
	 * @see to.etc.domui.component.menu.IMenuItem#getId()
	 */
	@Override
	public String getId() {
		return m_id;
	}

	public void setId(final String id) {
		m_id = id;
	}

	public BundleRef getMsgBundle() {
		return m_msgBundle;
	}

	public void setMsgBundle(final BundleRef msgBundle) {
		m_msgBundle = msgBundle;
	}

	public String getLabelKey() {
		return m_labelKey;
	}

	public void setLabelKey(final String labelKey) {
		m_labelKey = labelKey;
	}

	public String getDescKey() {
		return m_descKey;
	}

	public void setDescKey(final String descKey) {
		m_descKey = descKey;
	}

	public String getSearchKey() {
		return m_searchKey;
	}

	public void setSearchKey(final String searchKey) {
		m_searchKey = searchKey;
	}

	/**
	 * @see to.etc.domui.component.menu.IMenuItem#getParentID()
	 */
	@Override
	public String getParentID() {
		return m_parentID;
	}

	public void setParentID(final String parentID) {
		m_parentID = parentID;
	}

	/**
	 * @see to.etc.domui.component.menu.IMenuItem#getPageClass()
	 */
	@Override
	public Class< ? extends UrlPage> getPageClass() {
		return m_pageClass;
	}

	public MenuItemImpl setPageClass(final Class< ? extends UrlPage> pageClass) {
		m_pageClass = pageClass;
		return this;
	}

	/**
	 * @see to.etc.domui.component.menu.IMenuItem#getPageParameters()
	 */
	@Override
	public PageParameters getPageParameters() {
		return m_pageParameters;
	}

	public MenuItemImpl setPageParameters(final PageParameters pageParameters) {
		m_pageParameters = pageParameters;
		return this;
	}

	/**
	 * @see to.etc.domui.component.menu.IMenuItem#getIconPath()
	 */
	@Override
	public String getIconPath() {
		return m_iconPath;
	}

	public void setIconPath(final String iconPath) {
		m_iconPath = iconPath;
	}

	/**
	 * @see to.etc.domui.component.menu.IMenuItem#isDisabled()
	 */
	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(final boolean disabled) {
		m_disabled = disabled;
	}

	/**
	 * @see to.etc.domui.component.menu.IMenuItem#getChildren()
	 */
	@Override
	public List<IMenuItem> getChildren() {
		synchronized(m_manager) {
			return m_children;
		}
	}

	public void setChildren(final List<IMenuItem> children) {
		if(!m_subMenu)
			throw new IllegalStateException("Cannot add children to a LEAF item.");
		synchronized(m_manager) {
			m_children = children;
		}
	}

	/**
	 * @see to.etc.domui.component.menu.IMenuItem#getSearchString()
	 */
	@Override
	public String getSearchString() {
		return byKey(m_searchKey);
	}

	/**
	 * @see to.etc.domui.component.menu.IMenuItem#getRequiredRights()
	 */
	@Override
	public String[] getRequiredRights() {
		return m_requiredRights;
	}

	/**
	 * @see to.etc.domui.component.menu.IMenuItem#getLabel()
	 */
	@Override
	public String getLabel() {
		return byKey(m_labelKey);
	}

	/**
	 * @see to.etc.domui.component.menu.IMenuItem#getDescription()
	 */
	@Override
	public String getDescription() {
		return byKey(m_descKey);
	}

	@Override
	public boolean isSubMenu() {
		return m_subMenu;
	}

	public void setSubMenu(final boolean subMenu) {
		m_subMenu = subMenu;
	}

	@Override
	public int getOrder() {
		return m_order;
	}

	public void setOrder(final int order) {
		m_order = order;
	}

	public String getTitleKey() {
		return m_titleKey;
	}

	public void setTitleKey(final String titleKey) {
		m_titleKey = titleKey;
	}

	public MenuItemImpl getParent() {
		return m_parent;
	}

	void setParent(final MenuItemImpl parent) {
		m_parent = parent;
	}

	@Override
	public String getTarget() {
		return m_target;
	}

	public MenuItemImpl setTarget(final String target) {
		m_target = target;
		return this;
	}

	@Override
	public String getRURL() {
		return m_rurl;
	}

	public MenuItemImpl setRURL(final String rurl) {
		m_rurl = rurl;
		return this;
	}
}
