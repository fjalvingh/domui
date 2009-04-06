package to.etc.domui.components.menu;

import java.util.*;

import to.etc.domui.annotations.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.domui.util.nls.*;
import to.etc.domui.utils.*;

/**
 * The singleton which maintains the full system menu and all personal copies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
final public class MenuManager {
	static private MenuManager m_instance = new MenuManager();

	private final List<MenuItemImpl> m_newItemList = new ArrayList<MenuItemImpl>();

	static public final Comparator<IMenuItem> C_BY_ORDER_AND_CHILDREN = new Comparator<IMenuItem>() {
		public int compare(final IMenuItem o1, final IMenuItem o2) {
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

	private synchronized void	add(final MenuItemImpl m) {
		m_newItemList.add(m);
	}

	/**
	 * Plugin-based registration of menu items.
	 * @param msgbase
	 * @param labelkey
	 * @param desckey
	 * @return
	 */
	public MenuItemImpl registerMenuItem(final BundleRef bundle, final String labelkey, final String titlekey, final String desckey, final String searchKey) {
		MenuItemImpl m = new MenuItemImpl(this);
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
	public MenuItemImpl registerMenuItem(final BundleRef bundle, final String keyBase) {
		MenuItemImpl m = registerMenuItem(bundle, keyBase + ".label", keyBase + ".title", keyBase + ".desc", keyBase + ".search");
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
	public MenuItemImpl registerMenuItem(final BundleRef bundle, final String keyBase, final Class<? extends UrlPage> pageClass, final Object... parameters) {
		MenuItemImpl	m = registerMenuItem(bundle, keyBase);
		m.setPageClass(pageClass);
		m.setPageParameters(new PageParameters(parameters));
		return m;
	}

	/**
	 * Try to find an icon to use. This must use the same logic as AppPageTitle to locate an image.
	 * @param m
	 * @param pageClass
	 */
	private void	calculateIcon(final MenuItemImpl m, final Class<? extends UrlPage> clz) {
		//-- 1. Is an icon or icon resource specified in any attached UIMenu annotation? If so use that;
		UIMenu	ma	= clz.getAnnotation(UIMenu.class);
		if(ma != null) {
			if(ma.iconName() != null) {
				if(ma.iconBase() != Object.class)
					m.setIconPath(DomUtil.getJavaResourceRURL(ma.iconBase(), ma.iconName()));			// Set class-based URL
				else
					m.setIconPath(ma.iconName());
			}
		}

		//-- Not set using a UIMenu annotation. Is a .png with the same classname available?
		String	cn	= AppUIUtil.getClassNameOnly(clz)+".png";
		if(AppUIUtil.hasResource(clz, cn)) {
			m.setIconPath(DomUtil.getJavaResourceRURL(clz, cn));	// Set class-based URL
			return;
		}
	}

	/**
	 * Registers a new menu item. All menu data is obtained from the UrlPage's metadata.
	 * @param pageClass
	 * @param parameters
	 * @return
	 */
	public MenuItemImpl registerMenuItem(final Class<? extends UrlPage> pageClass, final Object... parameters) {
		MenuItemImpl	m	= new MenuItemImpl(this);
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
		UIMenu	ma = pageClass.getAnnotation(UIMenu.class);		// Is annotated with UIMenu?
		if(ma != null) {
			BundleRef	ref	= AppUIUtil.findBundle(ma, pageClass);
			if(ref != null) {
				boolean ok = false;
				if(ma.baseKey().length() != 0) {
					m.setLabelKey(ma.baseKey()+".label");
					m.setTitleKey(ma.baseKey()+".title");
					m.setSearchKey(ma.baseKey()+".search");
					m.setDescKey(ma.baseKey()+".desc");
					ok	= true;
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
		BundleRef	br	= AppUIUtil.getClassBundle(pageClass);		// PageClass bundle
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
		br	= AppUIUtil.getPackageBundle(pageClass);	// Package bundle.
		if(br.exists()) {
			//-- Use the package-based bundle for $ provided some exist...
			String	bn = pageClass.getName();
			bn = bn.substring(bn.lastIndexOf('.')+1);	// Class name only,
			String	kl = bn+".label";
			String	kt = bn+".title";
			if(br.findMessage(Locale.US, kl) != null || br.findMessage(Locale.US, kt) != null) {
				m.setMsgBundle(br);
				m.setLabelKey(kl);
				m.setTitleKey(kt);
				m.setSearchKey(bn+".search");
				m.setDescKey(bn+".desc");
				return m;
			}
		}

		//--Nothing found..
		return m;
	}

	/**
	 * Registers a ROOT submenu (can be other level if one of the setLocation() calls gets called after this).
	 * @return
	 */
	public MenuItemImpl	registerSubMenu(final BundleRef bundle, final String keyBase) {
		MenuItemImpl	m	= registerMenuItem(bundle, keyBase);
		m.setSubMenu(true);

		return m;
	}

	/**
	 *
	 * @param bundle
	 * @param keyBase
	 * @param parent
	 * @param order
	 * @return
	 */
	public MenuItemImpl	registerSubMenu(final BundleRef bundle, final String keyBase, final MenuItemImpl parent, final int order) {
		MenuItemImpl	m = registerSubMenu(bundle, keyBase);
		m.setLocation(parent, order);
		return m;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Return the global menu. 							*/
	/*--------------------------------------------------------------*/
	private final Map<String, MenuItemImpl>		m_idMap = new HashMap<String, MenuItemImpl>();
	private final List<IMenuItem>			m_rootMenu = new ArrayList<IMenuItem>();

	/**
	 * Return the global central menu. This is the menu in the structure as specified by the system, and without
	 * any per-person modification. It always consists entirely of MenuItemImpl nodes. This checks to see if new
	 * menu registrations are available and if so creates a new copy of the menu containing those new items.
	 *
	 * @return
	 */
	public synchronized List<IMenuItem>		getRootMenu() {
		if(m_newItemList.size() > 0) {
			//-- Register all new items! First add them by ID to the idMAP so we can locate them when looking for parents,
			for(MenuItemImpl m: m_newItemList) {
				//-- Assign ID;
				String	id	= m.getId();
				if(id == null) {
					id = m.getPageClass().getName();
					if(m.getPageParameters() != null)
						id	+= "?"+m.getPageParameters().toString();
					m.setId(id);
				}
				if(id != null) {
					if(null != m_idMap.put(id, m))
						System.err.println("MENU: Duplicate menu ID="+id);
				}
			}

			//-- Now construct the tree inside all nodes.
			for(MenuItemImpl m: m_newItemList) {
				//-- Locate the specified parent,
				MenuItemImpl	p = locateParent(m);		// Can we find a parent?
				if(p == null)
					m_rootMenu.add(m);						// Not found: add to root,
				else {
					if(p.getChildren().contains(m))
						throw new IllegalStateException("Re-adding a node already present in the menu!?");
					p.getChildren().add(m);					// Reorder is done in proxied menu.
					m.setParent(p);
				}
			}
			m_newItemList.clear();									// Discard: all items registered ok.
		}
		return m_rootMenu;
	}

	private MenuItemImpl	locateParent(final MenuItemImpl m) {
		if(m.getParent() != null)
			return m.getParent();
		if(m.getParentID() != null) {
			MenuItemImpl p = m_idMap.get(m.getParentID());
			if(p != null)
				return p;
		}

		//-- Parent not found: add to ROOT menu.
		return null;
	}

	/**
	 * Creates a filtered and possibly reordered user menu.
	 * @param filter
	 * @return
	 * @throws Exception
	 */
	public List<IMenuItem>		createUserMenu(final IMenuItemFilter filter) throws Exception {
		List<IMenuItem>		root = getRootMenu();
		List<IMenuItem>		res	= createSubMenu(root, filter, null);
		return res;
	}

	private List<IMenuItem>	createSubMenu(final List<IMenuItem> root, final IMenuItemFilter filter, final IMenuItem parent) throws Exception {
		List<IMenuItem>	res = new ArrayList<IMenuItem>();
		for(IMenuItem mi: root) {
			MenuItemImpl	m = (MenuItemImpl)mi;
			if(filter != null) {
				filter.setNode(m);
				if(! filter.isAllowed())
					continue;
			}

			MenuItemProxy	p	= new MenuItemProxy(m);
			if(m.isSubMenu()) {
				p.setChildren( createSubMenu(m.getChildren(), filter, p));
			}
			p.setParent(parent);
			if(! m.isSubMenu() || p.getChildren().size() > 0)
				res.add(p);
		}
		return res;
	}
}
