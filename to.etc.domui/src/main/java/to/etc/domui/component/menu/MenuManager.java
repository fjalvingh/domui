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

import to.etc.domui.annotations.UIMenu;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.PageParameters;
import to.etc.domui.util.DomUtil;
import to.etc.webapp.nls.BundleRef;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The singleton which maintains the full system menu and all personal copies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
public class MenuManager {
	static private MenuManager m_instance = new MenuManager();

	final private List<MenuItem> m_newItemList = new ArrayList<>();

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
	private MenuManager() {}

	public static MenuManager getInstance() {
		return m_instance;
	}

	private synchronized void add(MenuItem m) {
		m_newItemList.add(m);
	}

	/**
	 * Plugin-based registration of menu items.
	 */
	public MenuItem registerMenuItem(BundleRef bundle, String labelkey, String titlekey, String desckey, String searchKey) {
		MenuItem m = new MenuItem(this);
		m.setMsgBundle(bundle);
		m.setLabelKey(labelkey);
		m.setDescKey(desckey);
		m.setSearchKey(searchKey);
		m.setTitleKey(titlekey);
		add(m);
		return m;
	}

	/**
	 * Registers a menu item with your own texts overriding the page's texts. The title key, label key, description key and search key all start with the specified name; the actual key for each item is
	 * formed by adding .label, .desc, .title and .search after this initial key.
	 * @param bundle
	 * @param keyBase
	 * @return
	 */
	public MenuItem registerMenuItem(BundleRef bundle, String keyBase) {
		MenuItem m = registerMenuItem(bundle, keyBase + ".label", keyBase + ".title", keyBase + ".desc", keyBase + ".search");
		m.setId(keyBase);
		return m;
	}

	/**
	 * Registers a menu item.
	 * @param bundle
	 * @param keyBase
	 * @param pageClass
	 * @param parameters
	 * @return
	 */
	public MenuItem registerMenuItem(BundleRef bundle, String keyBase, Class< ? extends UrlPage> pageClass, Object... parameters) {
		MenuItem m = registerMenuItem(bundle, keyBase);
		m.setPageClass(pageClass);
		m.setPageParameters(new PageParameters(parameters));
		return m;
	}

	/**
	 * Try to find an icon to use. This must use the same logic as AppPageTitle to locate an image.
	 */
	private void calculateIcon(MenuItem m, Class< ? extends UrlPage> clz) {
		//-- 1. Is an icon or icon resource specified in any attached UIMenu annotation? If so use that;
		UIMenu ma = clz.getAnnotation(UIMenu.class);
		if(ma != null) {
			if(ma.iconName() != null) {
				if(ma.iconBase() != Object.class)
					m.setIconPath(DomUtil.getJavaResourceRURL(ma.iconBase(), ma.iconName())); // Set class-based URL
				else
					m.setIconPath(ma.iconName());
			}
		}

		//-- Not set using a UIMenu annotation. Is a .png with the same classname available?
		String cn = DomUtil.getClassNameOnly(clz) + ".png";
		if(DomUtil.hasResource(clz, cn)) {
			m.setIconPath(DomUtil.getJavaResourceRURL(clz, cn)); // Set class-based URL
			return;
		}
	}

	/**
	 * Registers a new menu item. All menu data is obtained from the UrlPage's metadata.
	 */
	public MenuItem registerMenuItem(Class< ? extends UrlPage> pageClass, Object... parameters) {
		MenuItem m = new MenuItem(this);
		m.setPageClass(pageClass);
		m.setPageParameters(new PageParameters(parameters));
		add(m);

		/*
		 * Try to calculate a default icon for this item.
		 */
		calculateIcon(m, pageClass);

		/*
		 * We try to prime the source for title, label, search and description from the properties defined
		 * in the Page class. This can be overridden by separate calls into the returned item. The logic
		 * used here should duplicate the logic exposed in AppUIUtil for the items mostly. The exception
		 * is that the code here tries to find a single source for the strings using the same chain of
		 * locations specified in AppUIUtil; it will then use this single source for /all/ strings.
		 * These things all set a bundle and key for all items.
		 */
		UIMenu ma = pageClass.getAnnotation(UIMenu.class); // Is annotated with UIMenu?
		if(ma != null) {
			BundleRef ref = DomUtil.findBundle(ma, pageClass);
			if(ref != null) {
				boolean ok = false;
				if(ma.baseKey().length() != 0) {
					m.setLabelKey(ma.baseKey() + ".label");
					m.setTitleKey(ma.baseKey() + ".title");
					m.setSearchKey(ma.baseKey() + ".search");
					m.setDescKey(ma.baseKey() + ".desc");
					ok = true;
				}
				if(ma.labelKey().length() != 0) {
					m.setLabelKey(ma.labelKey());
					ok = true;
				}
				if(ma.titleKey().length() != 0) {
					m.setTitleKey(ma.titleKey());
					ok = true;
				}
				if(ma.descKey().length() != 0) {
					m.setDescKey(ma.descKey());
					ok = true;
				}
				if(ma.searchKey().length() != 0) {
					m.setSearchKey(ma.searchKey());
					ok = true;
				}
				m.setMsgBundle(ref);
				if(ok)
					return m;
			}
		}

		//-- Not using UIMenu; use page/package based structures. This depends on whether a Page resource exists.
		BundleRef br = DomUtil.getClassBundle(pageClass); // PageClass bundle
		if(br.exists()) {
			//-- Use page-based resources.
			m.setMsgBundle(br);
			m.setLabelKey("label");
			m.setTitleKey("title");
			m.setSearchKey("search");
			m.setDescKey("desc");
			return m;
		}

		//-- Try package-based keys
		br = DomUtil.getPackageBundle(pageClass); // Package bundle.
		if(br.exists()) {
			//-- Use the package-based bundle for $ provided some exist...
			String bn = pageClass.getName();
			bn = bn.substring(bn.lastIndexOf('.') + 1); // Class name only,
			String kl = bn + ".label";
			String kt = bn + ".title";
			if(br.findMessage(Locale.US, kl) != null || br.findMessage(Locale.US, kt) != null) {
				m.setMsgBundle(br);
				m.setLabelKey(kl);
				m.setTitleKey(kt);
				m.setSearchKey(bn + ".search");
				m.setDescKey(bn + ".desc");
				return m;
			}
		}

		//--Nothing found..
		return m;
	}

	/**
	 * Registers a ROOT submenu (can be other level if one of the setLocation() calls gets called after this).
	 */
	public MenuItem registerSubMenu(BundleRef bundle, String keyBase) {
		MenuItem m = registerMenuItem(bundle, keyBase);
		m.setSubMenu(true);

		return m;
	}

	public MenuItem registerSubMenu(BundleRef bundle, String keyBase, MenuItem parent, int order) {
		MenuItem m = registerSubMenu(bundle, keyBase);
		m.setLocation(parent, order);
		return m;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Return the global menu. 							*/
	/*--------------------------------------------------------------*/
	private Map<String, MenuItem> m_idMap = new HashMap<String, MenuItem>();

	private List<MenuItem> m_rootMenu = new ArrayList<MenuItem>();

	/**
	 * Return the global central menu. This is the menu in the structure as specified by the system, and without
	 * any per-person modification. It always consists entirely of MenuItemImpl nodes. This checks to see if new
	 * menu registrations are available and if so creates a new copy of the menu containing those new items.
	 */
	public synchronized List<MenuItem> getRootMenu() {
		if(m_newItemList.size() > 0) {
			//-- Register all new items! First add them by ID to the idMAP so we can locate them when looking for parents,
			for(MenuItem m : m_newItemList) {
				//-- Assign ID;
				String id = m.getId();
				if(id == null) {
					id = m.getPageClass().getName();
					if(m.getPageParameters() != null)
						id += "?" + m.getPageParameters().toString();
					m.setId(id);
				}
				if(id != null) {
					if(null != m_idMap.put(id, m))
						System.err.println("MENU: Duplicate menu ID=" + id);
				}
			}

			//-- Now construct the tree inside all nodes.
			for(MenuItem m : m_newItemList) {
				//-- Locate the specified parent,
				MenuItem p = locateParent(m); // Can we find a parent?
				if(p == null)
					m_rootMenu.add(m); // Not found: add to root,
				else {
					if(p.getChildren().contains(m))
						throw new IllegalStateException("Re-adding a node already present in the menu!?");
					p.getChildren().add(m); // Reorder is done in proxied menu.
					m.setParent(p);
				}
			}
			m_newItemList.clear(); // Discard: all items registered ok.
		}
		return m_rootMenu;
	}

	private MenuItem locateParent(MenuItem m) {
		if(m.getParent() != null)
			return m.getParent();
		if(m.getParentID() != null) {
			MenuItem p = m_idMap.get(m.getParentID());
			if(p != null)
				return p;
		}

		//-- Parent not found: add to ROOT menu.
		return null;
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
