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

/**
 * Proxy to an actual MenuItem which can be used to override a menu with a different order, tree structure.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
public class MenuItemProxy implements IMenuItem {
	/** The actual item being proxied. */
	private final IMenuItem m_actual;

	private List<IMenuItem> m_children;

	private int m_order;

	private IMenuItem m_parent;

	public MenuItemProxy(final IMenuItem actual) {
		if(null == actual)
			throw new NullPointerException("?? Null MenuItem here?");
		m_actual = actual;
		if(m_actual.isSubMenu())
			m_children = new ArrayList<IMenuItem>();
	}

	@Override
	public String getDescription() {
		return m_actual.getDescription();
	}

	@Override
	public String getIconPath() {
		return m_actual.getIconPath();
	}

	@Override
	public String getId() {
		return m_actual.getId();
	}

	@Override
	public String getLabel() {
		return m_actual.getLabel();
	}

	@Override
	public Class< ? extends UrlPage> getPageClass() {
		return m_actual.getPageClass();
	}

	@Override
	public IPageParameters getPageParameters() {
		return m_actual.getPageParameters();
	}

	@Override
	public String getParentID() {
		return m_actual.getParentID();
	}

	@Override
	public String[] getRequiredRights() {
		return m_actual.getRequiredRights();
	}

	@Override
	public String getSearchString() {
		return m_actual.getSearchString();
	}

	@Override
	public boolean isDisabled() {
		return m_actual.isDisabled();
	}

	@Override
	public boolean isSubMenu() {
		return m_actual.isSubMenu();
	}

	@Override
	public List<IMenuItem> getChildren() {
		return m_children;
	}

	public void setChildren(final List<IMenuItem> children) {
		m_children = children;
	}

	@Override
	public int getOrder() {
		return m_order;
	}

	public IMenuItem getParent() {
		return m_parent;
	}

	public void setParent(final IMenuItem parent) {
		m_parent = parent;
	}

	@Override
	public String getRURL() {
		return m_actual.getRURL();
	}

	@Override
	public String getTarget() {
		return m_actual.getTarget();
	}
}
