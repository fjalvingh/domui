package to.etc.domui.components.menu;

import java.util.*;
import java.util.List;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.nls.*;

/**
 * The singleton which maintains the full system menu and all personal copies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
final public class MenuManager {
	static private MenuManager m_instance = new MenuManager();

	private final List<MenuItemImpl> m_regList = new ArrayList<MenuItemImpl>();

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

	/**
	 * Plugin-based registration of menu items.
	 * @param msgbase
	 * @param labelkey
	 * @param desckey
	 * @return
	 */
	public MenuItemImpl registerMenuItem(final BundleRef bundle, final String labelkey, final String titlekey, final String desckey, final String searchKey) {
		MenuItemImpl m = new MenuItemImpl();
		m.setMsgBundle(bundle);
		m.setLabelKey(labelkey);
		m.setDescKey(desckey);
		m.setSearchKey(searchKey);
		m.setTitleKey(titlekey);
		m_regList.add(m);
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
		return registerMenuItem(bundle, keyBase + ".label", keyBase + ".title", keyBase + ".desc", keyBase + ".search");
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
	 * Registers a new menu item. All menu data is obtained from the UrlPage's metadata.
	 * @param pageClass
	 * @param parameters
	 * @return
	 */
	public MenuItemImpl registerMenuItem(final Class<? extends UrlPage> pageClass, final Object... parameters) {
		MenuItemImpl	m	= new MenuItemImpl();
		m.setPageClass(pageClass);
		m.setPageParameters(new PageParameters(parameters));
		return m;
	}

//	/**
//	 * @return
//	 */
//	public MenuItemImpl	registerSubMenu() {
//
//	}
}
