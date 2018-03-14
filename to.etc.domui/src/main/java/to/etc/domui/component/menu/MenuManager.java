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

import to.etc.domui.annotations.UIRights;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.login.IUser;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The singleton which maintains the full system menu and all personal copies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
public class MenuManager {
	static private MenuManager m_instance = new MenuManager();

	private final MenuItem m_root = new MenuItem(this);

	private Map<String, MenuItem> m_idMap;

	static public Comparator<MenuItem> C_BY_ORDER_AND_CHILDREN = new Comparator<MenuItem>() {
		@Override
		public int compare(MenuItem o1, MenuItem o2) {
			boolean c1 = o1.getChildren().size() > 0;
			boolean c2 = o2.getChildren().size() > 0;
			if(c1 != c2)
				return c1 ? 1 : -1;
			return o1.getOrder() - o2.getOrder();
		}
	};

	/**
	 * Forbidden constructor.
	 */
	private MenuManager() {
	}

	public static MenuManager getInstance() {
		return m_instance;
	}

	public MenuItem getRoot() {
		return m_root;
	}

	public synchronized Map<String, MenuItem> getIdMap() {
		Map<String, MenuItem> idMap = m_idMap;
		if(null == idMap) {
			m_idMap = idMap = new HashMap<>();
			calculateIds(getRoot(), idMap);
		}
		return idMap;
	}

	private void calculateIds(MenuItem root, Map<String, MenuItem> idMap) {
		String id = root.getId();
		if(null != id)
			idMap.put(id, root);
		for(MenuItem menuItem : root.getChildren()) {
			calculateIds(menuItem, idMap);
		}
	}

	/**
	 * Calculate the user-specific menu by applying the users' rights on all menu items.
	 */
	public MenuItem createUserMenu(IUser user) {
		MenuItem root = getRoot();

		MenuItem userRoot = new MenuItem(this);

		//-- First: build a tree of all nodes the user is authorized to see
		buildAuthorization(userRoot, root, user);
		pruneEmpties(userRoot);
		return userRoot;
	}

	/**
	 * Do a depth-first traversal of the user menu and remove all empty submenus.
	 */
	private void pruneEmpties(MenuItem item) {
		for(MenuItem menuItem : item.getChildren()) {
			pruneEmpties(menuItem);
		}

		//-- Now check: which of my children are empty?
		List<MenuItem> children = item.getChildren();
		for(int i = children.size() - 1; i >= 0; i--) {
			MenuItem menuItem = children.get(i);
			if(menuItem.isSubMenu() && menuItem.getChildren().size() == 0) {
				children.remove(i);
			}
		}
	}

	private void buildAuthorization(MenuItem userMenu, MenuItem systemMenu, IUser user) {
		for(MenuItem sysItem : systemMenu.getChildren()) {
			if(isNodeAuthorized(systemMenu, user)) {
				//-- We're allowed to use this, so copy it
				MenuItem userItem = userMenu.addClone(sysItem);

				buildAuthorization(userItem, sysItem, user);
			}
		}
	}

	private boolean isNodeAuthorized(MenuItem item, IUser user) {
		//-- Collect all applicable rights.
		String[] requiredRights = item.getRequiredRights();
		if(null != requiredRights) {
			boolean menuOk = false;

			//-- If the user has none of these then do not show
			for(String rr : requiredRights) {
				if(user.hasRight(rr)) {
					menuOk = true;
					break;
				}
			}
			if(! menuOk)
				return false;
		}

		Class<? extends UrlPage> pageClass = item.getPageClass();
		if(null != pageClass) {
			UIRights uir = pageClass.getAnnotation(UIRights.class);
			if(null != uir && uir.value().length > 0) {
				for(String rr : uir.value()) {
					if(user.hasRight(rr))
						return true;
				}
				return false;
			}
		}

		return true;
	}


	///**
	// * Creates a filtered and possibly reordered user menu.
	// */
	//public List<MenuItem> createUserMenu(IMenuItemFilter filter) throws Exception {
	//	List<MenuItem> root = getRootMenu();
	//	List<MenuItem> res = createSubMenu(root, filter, null);
	//	return res;
	//}
	//
	//private List<MenuItem> createSubMenu(List<MenuItem> root, IMenuItemFilter filter, MenuItem parent) throws Exception {
	//	List<MenuItem> res = new ArrayList<MenuItem>();
	//	for(MenuItem mi : root) {
	//		MenuItem m = (MenuItem) mi;
	//		if(filter != null) {
	//			filter.setNode(m);
	//			if(!filter.isAllowed())
	//				continue;
	//		}
	//
	//		MenuItemProxy p = new MenuItemProxy(m);
	//		if(m.isSubMenu()) {
	//			p.setChildren(createSubMenu(m.getChildren(), filter, p));
	//		}
	//		p.setParent(parent);
	//		if(!m.isSubMenu() || p.getChildren().size() > 0)
	//			res.add(p);
	//	}
	//	return res;
	//}
}
