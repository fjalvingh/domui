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

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.state.PageParameters;
import to.etc.domui.util.DomUtil;
import to.etc.webapp.nls.IBundleCode;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A single item in the menu, as defined by the *code*.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
final public class MenuItem {
	/** Required for locking purposes. */
	final private MenuManager m_manager;

	/** When adding this can be set to define the child's parent. If unset other items are checked to find a parent menu. */
	final private MenuItem m_parent;

	private String m_id;

	private BundleMessage m_label;

	private BundleMessage m_title;

	private BundleMessage m_desc;

	private IIconRef m_image;

	private Class<? extends UrlPage> m_pageClass;

	private IPageParameters m_pageParameters;

	private boolean m_disabled;

	private int m_order;

	/** The list of rights the user MUST have to access this menu item. This can get delegated to the UrlPage's annotation. */
	private String[] m_requiredRights;

	private List<MenuItem> m_children = new ArrayList<>();

	private boolean m_calculated;

	private String m_target;

	private String m_rurl;

	/**
	 * Root node constructor.
	 */
	MenuItem(MenuManager m) {
		m_manager = m;
		m_parent = null;
	}

	MenuItem(MenuItem parent) {
		m_manager = parent.getManager();
		m_parent = parent;
	}

	MenuItem(MenuItem parent, Class<? extends UrlPage> pageClass) {
		m_manager = parent.getManager();
		m_parent = parent;
		m_pageClass = pageClass;
	}

	MenuItem(MenuItem parent, String url) {
		m_manager = parent.getManager();
		m_parent = parent;
		m_rurl = url;
	}

	MenuManager getManager() {
		return m_manager;
	}

	/**
	 * Add a menu item (leaf) linking to the specified page.
	 */
	public MenuItem add(Class<? extends UrlPage> pageClass) {
		MenuItem item = new MenuItem(this, pageClass);
		m_children.add(item);
		return item;
	}

	/**
	 * Duplicate an item and add it here.
	 */
	MenuItem addClone(MenuItem from) {
		MenuItem to = new MenuItem(this);

		to.m_label = from.m_label;
		to.m_title = from.m_title;
		to.m_desc = from.m_desc;
		to.m_image = from.m_image;
		to.m_pageClass = from.m_pageClass;
		to.m_pageParameters = from.m_pageParameters;
		to.m_rurl = from.m_rurl;
		to.m_calculated = from.m_calculated;
		to.m_disabled = from.m_disabled;
		to.m_order = from.m_order;
		to.m_requiredRights = from.m_requiredRights;
		to.m_target = from.m_target;
		m_children.add(to);
		return to;
	}

	/**
	 * Add a menu node linking to the specified relative url.
	 */
	public MenuItem add(String rurl) {
		MenuItem item = new MenuItem(this, rurl);
		m_children.add(item);
		return item;
	}

	public MenuItem addSub(IBundleCode code, Object... parameters) {
		MenuItem item = new MenuItem(this);
		item.label(code, parameters);
		m_children.add(item);
		return item;
	}

	public MenuItem up() {
		return requireNonNull(m_parent);
	}

	/**
	 * Defines the rights that the user MUST have to see this menu item. These rights are in addition to the page rights.
	 */
	public MenuItem rights(String... rights) {
		m_requiredRights = rights;
		return this;
	}

	public MenuItem image(IIconRef name) {
		m_image = name;
		return this;
	}

	public MenuItem image(Class<?> res, String name) {
		m_image = Icon.of(DomUtil.getJavaResourceRURL(res, name));
		return this;
	}

	public MenuItem order(int order) {
		m_order = order;
		return this;
	}

	public MenuItem label(IBundleCode code, Object... parameters) {
		m_label = new BundleMessage(code, parameters);
		return this;
	}

	public MenuItem desc(IBundleCode code, Object... parameters) {
		m_desc = new BundleMessage(code, parameters);
		return this;
	}

	public MenuItem title(IBundleCode code, Object... parameters) {
		m_title = new BundleMessage(code, parameters);
		return this;
	}

	public MenuItem id(String id) {
		m_id = id;
		return this;
	}

	public MenuItem target(String target) {
		m_target = target;
		return this;
	}

	public MenuItem parameter(String name, String value) {
		IPageParameters pp = m_pageParameters;
		if(pp instanceof PageParameters || pp == null) {
			PageParameters ppp = (PageParameters) pp;
			if(ppp == null) {
				m_pageParameters = ppp = new PageParameters();
			}
			ppp.addParameter(name, value);
		} else
			throw new IllegalStateException("Invalid page parameters");
		return this;
	}

	/**
	 * Sets all parameters to use.
	 */
	public MenuItem parameters(IPageParameters pageParameters) {
		m_pageParameters = pageParameters;
		return this;
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

	public String getSearchString() {
		String label = getLabel();
		return label;
	}

	public String[] getRequiredRights() {
		return m_requiredRights;
	}

	public String getLabel() {
		BundleMessage label = m_label;
		if(null != label)
			return label.getString();
		Class<? extends UrlPage> pageClass = m_pageClass;
		if(null != pageClass) {
			return pageClass.getSimpleName();
		}
		return "Unknown label";
	}

	public String getDescription() {
		BundleMessage desc = m_desc;
		if(desc == null)
			return null;
		return desc.getString();
	}

	public boolean isSubMenu() {
		return m_rurl == null && m_pageClass == null;
	}

	public int getOrder() {
		return m_order;
	}

	public MenuItem getParent() {
		return m_parent;
	}

	public String getTarget() {
		return m_target;
	}

	public String getRURL() {
		return m_rurl;
	}

	@Nullable
	public String getId() {
		return m_id;
	}

	public Class<? extends UrlPage> getPageClass() {
		return m_pageClass;
	}

	public IPageParameters getPageParameters() {
		return m_pageParameters;
	}

	public IIconRef getImage() {
		return m_image;
	}

	//private void calculateBundleData() {
	//	if(m_calculated)
	//		return;
	//
	//	Class<? extends UrlPage> clz = getPageClass();
	//	if(null != clz) {
	//		if(m_image == null) {
	//			//-- 1. Is an icon or icon resource specified in any attached UIMenu annotation? If so use that;
	//			UIMenu ma = clz.getAnnotation(UIMenu.class);
	//			if(ma != null) {
	//				if(!ma.iconName().isEmpty()) {
	//					if(ma.iconBase() != Object.class)
	//						image(ma.iconBase(), ma.iconName());
	//					else
	//						image(Icon.of(ma.iconName()));
	//				}
	//			}
	//
	//			//-- Not set using a UIMenu annotation. Is a .png with the same classname available?
	//			String cn = DomUtil.getClassNameOnly(clz) + ".png";
	//			if(DomUtil.hasResource(clz, cn)) {
	//				image(clz, cn);                                    // Set class-based URL
	//			}
	//		}
	//
	//		/*
	//		 * We try to prime the source for title, label, search and description from the properties defined
	//		 * in the Page class. This can be overridden by separate calls into the returned item. The logic
	//		 * used here should duplicate the logic exposed in AppUIUtil for the items mostly. The exception
	//		 * is that the code here tries to find a single source for the strings using the same chain of
	//		 * locations specified in AppUIUtil; it will then use this single source for /all/ strings.
	//		 * These things all set a bundle and key for all items.
	//		 */
	//		UIMenu ma = clz.getAnnotation(UIMenu.class); // Is annotated with UIMenu?
	//		String labelKey = null;
	//		String titleKey = null;
	//		String searchKey = null;
	//		String descKey = null;
	//		BundleRef labelRef = null;
	//		BundleRef titleRef = null;
	//		BundleRef searchRef = null;
	//		BundleRef descRef = null;
	//
	//		if(ma != null) {
	//			BundleRef ref = DomUtil.findBundle(ma, clz);
	//			if(ref != null) {
	//				if(ma.baseKey().length() != 0) {
	//					labelKey = ma.baseKey() + ".label";
	//					titleKey = ma.baseKey() + ".title";
	//					searchKey = ma.baseKey() + ".search";
	//					descKey = ma.baseKey() + ".desc";
	//					labelRef = titleRef = searchRef = descRef = ref;
	//				}
	//				if(ma.labelKey().length() != 0) {
	//					labelKey = ma.labelKey();
	//					labelRef = ref;
	//				}
	//				if(ma.titleKey().length() != 0) {
	//					titleKey = ma.titleKey();
	//					titleRef = ref;
	//				}
	//				if(ma.descKey().length() != 0) {
	//					descKey = ma.descKey();
	//					descRef = ref;
	//				}
	//				if(ma.searchKey().length() != 0) {
	//					searchKey = ma.searchKey();
	//					searchRef = ref;
	//				}
	//			}
	//		}
	//
	//		//-- Not using UIMenu; use page/package based structures. This depends on whether a Page resource exists.
	//		BundleRef br = DomUtil.getClassBundle(clz);    // PageClass bundle
	//		if(br.exists()) {
	//			//-- Use page-based resources.
	//			if(br.getString("label") != null) {
	//				labelKey = "label";
	//				labelRef = br;
	//			}
	//			if(br.getString("title") != null) {
	//				titleKey = "title";
	//				titleRef = br;
	//			}
	//			if(br.getString("search") != null) {
	//				searchKey = "search";
	//				searchRef = br;
	//			}
	//			if(br.getString("desc") != null) {
	//				descKey = "desc";
	//				descRef = br;
	//			}
	//		}
	//
	//		//-- Try package-based keys
	//		br = DomUtil.getPackageBundle(clz);                // Package bundle.
	//		if(br.exists()) {
	//			//-- Use the package-based bundle for $ provided some exist...
	//			String bn = clz.getSimpleName();
	//			String kl = bn + ".label";
	//			String kt = bn + ".title";
	//			if(br.findMessage(Locale.US, kl) != null || br.findMessage(Locale.US, kt) != null) {
	//				titleRef = labelRef = descRef = searchRef = br;
	//				labelKey = kl;
	//				titleKey = kt;
	//				searchKey = bn + ".search";
	//				descKey = bn + ".desc";
	//			}
	//		}
	//
	//		if(m_labelKey == null && m_label == null)
	//			labelKey(labelRef, labelKey);
	//		if(m_titleKey == null && m_title == null)
	//			titleKey(titleRef, titleKey);
	//		if(m_searchKey == null)
	//			searchKey(searchRef, searchKey);
	//		if(m_desc == null && m_descKey == null)
	//			descKey(descRef, descKey);
	//	}
	//	m_calculated = true;
	//}

	public boolean siblingHasIcons() {
		MenuItem parent = m_parent;
		if(null == parent)
			return false;
		for(MenuItem menuNode : parent.getChildren()) {
			if(menuNode.getImage() != null)
				return true;
		}
		return false;
	}
}
